package com.nbt.blytics.modules.changepassword

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.changepassword.model.ChangePasswordRequest
import com.nbt.blytics.modules.changepassword.model.ChangePasswordResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class ChangePasswordViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun changePassword(email: String, password: String) {
        val changePasswordRequest = ChangePasswordRequest(email, password)

        val request = service.changePassword(changePasswordRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                observerResponse.value = FailResponse("", t.message.toString())
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                try {

                        getChangePasswordResponse(response.body()!!.string())
                }catch (ex:Exception){
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            private fun getChangePasswordResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = ChangePasswordResponse(status, message)
                } else {
                   // val errorCode = json.getString("error_code")
                    observerResponse.value = ChangePasswordResponse(status, message, "")
                }

            }
        })
    }
}