package com.nbt.blytics.modules.bvn

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class BvnVerificationViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun registerBVN(
        user_id: String,
        bvn_number: String,
        user_token: String,
        document1: File,
        document2: File
    ) {
        val userID = user_id.toRequestBody("text/plain".toMediaType())
        val bvnNumber = bvn_number.toRequestBody("text/plain".toMediaType())
        val userToken = user_token.toRequestBody("text/plain".toMediaType())
        val idProof = "dl".toRequestBody("text/plain".toMediaType())
        val addressProof = "pan".toRequestBody("text/plain".toMediaType())

        val requestFile: RequestBody =if(document1.extension.equals("pdf",true)) {
            document1.asRequestBody("application/pdf".toMediaTypeOrNull())

        }else{
            document1.asRequestBody("image/*".toMediaTypeOrNull())

        }
        val doc1: MultipartBody.Part = MultipartBody.Part.createFormData(
            "identity_proof_document",
            "identity_proof.${document1.extension}",
            requestFile
        )

        val requestFile2: RequestBody =if(document2.extension.equals("pdf",true)) {
            document2.asRequestBody("application/pdf".toMediaTypeOrNull())

        }else{
            document2.asRequestBody("image/*".toMediaTypeOrNull())

        }
        val doc2: MultipartBody.Part = MultipartBody.Part.createFormData(
            "address_proof_document",
            "address_proof.${document2.extension}",
            requestFile2
        )
        val request =
            service.registerBVN(
                userID,
                bvnNumber,
                userToken,
                idProof,
                addressProof,
                doc1,
                doc2
            )
        request.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    getBvnResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }

            }
            fun getBvnResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userPermission = data.getString("user_permission")
                    val dataSaved = data.getBoolean("data_saved")
                    val data1 = BvnRegisterResponse.Data(dataSaved, userPermission)
                    observerResponse.value = BvnRegisterResponse(message, status, data1)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = BvnRegisterResponse(message, status, null, errorCode)
                }

            }
        }

        )
    }

}