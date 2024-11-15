package com.nbt.blytics.modules.newprofile

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.otp.model.EmailOtpRequest
import com.nbt.blytics.modules.otp.model.EmailOtpResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest
import com.nbt.blytics.modules.profile.UpdateMobileRequest
import com.nbt.blytics.modules.profile.UpdateMobileResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.UpdateAvatarResponse
import com.nbt.blytics.modules.userprofile.models.UpdateDocumentResponse
import com.nbt.blytics.modules.userprofile.models.UpdateProfileResponse
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

class NewProfileViewModel (application: Application): BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun registerBVN(
        user_id: String,
        bvn_number: String,
        user_token: String,
        document1: File,
        document2: File,
        forBvnRegistration:Boolean
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
           if(forBvnRegistration) {
               service.registerBVN(
                   userID,
                   bvnNumber,
                   userToken,
                   idProof,
                   addressProof,
                   doc1,
                   doc2
               )
           }else{
               service.updateBVN(
                   userID,
                   bvnNumber,
                   userToken,
                   idProof,
                   addressProof,
                   doc1,
                   doc2
               )
           }

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

    fun getAvatars(){
        val request = service.avatarImage()
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getAvatarResponse(response.body()!!.string())

                }catch (ex:Exception){
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getAvatarResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataArray = json.getJSONArray("data")
                    val dataList = mutableListOf<AvatarModel.Data>()
                    for(i in 0 until dataArray.length()){
                        dataArray.getJSONObject(i).apply {
                            val id = getInt("id")
                            val image = getString("image")
                            val data = AvatarModel.Data(id, image)
                            dataList.add(data)
                        }

                    }
                    observerResponse.value = AvatarModel(status ,dataList)

                }else{
                    observerResponse.value = AvatarModel(status ,null)
                }

            }
        })
    }
    fun updateAvatar(user_id: String, avatar_id: String, avatar_image: File?, user_token: String) {
        val userID = user_id.toRequestBody("text/plain".toMediaType())
        val avatarId = avatar_id.toRequestBody("text/plain".toMediaType())
        val userToken = user_token.toRequestBody("text/plain".toMediaType())


        val request: Call<ResponseBody> = if (avatar_image != null) {
            val requestFile: RequestBody = avatar_image.asRequestBody("image/*".toMediaTypeOrNull())
            val avatarImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                "avatar_image",
                "avatar.${avatar_image.extension}",
                requestFile
            )
            service.updateUserAvatarByFile(userID, avatarImage, userToken)
        } else {
            service.updateUserAvatarByID(userID, avatarId, userToken)

        }

        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getAddAvatarResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getAddAvatarResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataObj = json.getJSONObject("data")
                    val avatarDefault = dataObj.getString("avatar_default")
                    val imageUrl = dataObj.getString("image_url")
                    val uploaded = dataObj.getString("updated")
                    Log.d("uploadedIMage===", res)
                    val data = UpdateAvatarResponse.Data(avatarDefault, imageUrl,uploaded)
                    observerResponse.value = UpdateAvatarResponse(status, message, data)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = UpdateAvatarResponse(status, message, null, errorCode)
                }
            }
        })


    }


    fun checkExist(str: String) {
        val checkExistRequestModel = CheckExistRequest("",str, CheckFor.FULL_DETAILS.ordinal)
        val request = service.userCheckExist(checkExistRequestModel)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {

                    getCheckExistResponse(response.body()!!.string())

                }catch (ex:Exception){
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            private fun getCheckExistResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val errorCode = json.getInt("error_code")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userId = data.getString("user_id")
                    val email = data.getString("email")
                    val userName = data.getString("user_name")
                    val mobNo = data.getString("mob_no")
                    val walletUUID =data.getString("wallet_uuid")
                    val tpin =data.getInt("tpin")
                    val securityQuestion =data.getInt("security_question")
                    observerResponse.value = CheckExistPhoneResponse(CheckExistPhoneResponse.Data(email, mobNo, userId,walletUUID,tpin, securityQuestion,userName), errorCode, status)

                } else {
                    observerResponse.value = CheckExistPhoneResponse(null, errorCode, status, Constants.errorMessage(errorCode))
                }
            }
        })
    }

    fun updateDocument(userId: String, identityProof: MultipartBody.Part) {
        val userIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)
        val request = service.updateDocument(userIdBody, identityProof)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.let {
                    getDocumentUpdateResponse(it.string())
                } ?: run {
                    observerResponse.value = FailResponse("", "Empty response")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            private fun getDocumentUpdateResponse(res: String) {
                val json = JSONObject(res)
                val status = json.optString("doc_verified", "false")
                observerResponse.value = UpdateDocumentResponse(status)
            }
        })
    }

    fun updateAddress(userId: String, identityProofDocument: MultipartBody.Part) {
        val userIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)
        val request = service.updateAddress(userIdBody, identityProofDocument)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                response.body()?.let {
                    getDocumentUpdateResponse(it.string())
                } ?: run {
                    observerResponse.value = FailResponse("", "Empty response")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            private fun getDocumentUpdateResponse(res: String) {
                val json = JSONObject(res)
                val status = json.optString("doc_verified", "false")
                observerResponse.value = UpdateDocumentResponse(status)
            }
        })
    }

    fun updateProfileInfo(model:Any){
        val request = service.updateProfileInfo(model)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try{
                    getProfileUpdateResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getProfileUpdateResponse(res:String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value= UpdateProfileResponse(status,message)
                }else{
                    observerResponse.value= UpdateProfileResponse(status,message)
                }
            }
        })
    }

    fun otpEmail(email:String) {
        val emailOtpRequest = EmailOtpRequest(email)
        val request = service.sendEmailOTP(emailOtpRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getEmailOtpResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getEmailOtpResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = EmailOtpResponse(status, message)
                }else{
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }

    fun updateMobile(userId:String, userToke:String, mobileNumber:String, deviceToken:String,countryCode:String){
        val mobileRequest = UpdateMobileRequest(userId, userToke, mobileNumber,true, deviceToken,countryCode)
        val request = service.changeMobileNumber(mobileRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getMobileResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getMobileResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = UpdateMobileResponse(status, message)
                }else{
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }
}