package com.nbt.blytics.modules.otp

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.otp.model.*
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun loginWithMobile(mobile: String) {
        val loginWithMobileRequest = LoginWithMobileRequest(mobile)
        val request = service.loginWithMobile(loginWithMobileRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getLoginWithMobileResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getLoginWithMobileResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.contains(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userId = data.getString("user_id")
                    val userToken = data.getString("user_token")
                    val uuid = data.getString("uuid")
                    observerResponse.value = LoginWithMobileResponse(
                        status, LoginWithMobileResponse.Data(
                            userId, userToken,uuid
                        )
                    )
                } else {
                    val errorCode = json.getString("error_code")
                    val message = json.getString("message")
                    observerResponse.value = LoginWithMobileResponse(
                        status, null, errorCode, message
                    )
                }

            }
        })
    }

    fun checkValidTPIN( userId:String,tpin:String ){
        val checkValidTpinRequest = CheckValidTpinRequest(userId, tpin)
        val request = service.checkValidTpi(checkValidTpinRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getCheckValidResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getCheckValidResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.contains(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value =CheckValidTpinResponse(status, message, "", null)

                }else{
                    val errorCode = json.getString("error_code")
                    val dataObj = json.getJSONObject("data")
                    val verified = dataObj.getBoolean("verified")
                    val data = CheckValidTpinResponse.Data(verified)
                    observerResponse.value =CheckValidTpinResponse(status, message,errorCode,data)
                }
            }
        })

    }



}