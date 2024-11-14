package com.nbt.blytics.modules.signin

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signin.model.LoginResponse
import com.nbt.blytics.modules.signin.model.LoginRequest
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun login(email: String, password: String,deviceToken :String,context: Context,changeDevice:Boolean) {
        (context as UserActivity).apply {
            val deviceData =
                LoginRequest.DeviceData(deviceModel, version, apiLevel, fingerPrint, deviceId)
            val loginRequest = LoginRequest(email, password,deviceToken,changeDevice,deviceData)

            val request = service.login(loginRequest)
            request.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    observerResponse.value = FailResponse("", t.message.toString())
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    try {
                        getLoginResponse(response.body()!!.string())

                    } catch (e: Exception) {
                        observerResponse.value = FailResponse("", e.message.toString())
                    }

                }

                private fun getLoginResponse(res: String) {
                    val json = JSONObject(res)
                    val status = json.getString("status")
                    val message = json.getString("message")
                    if (status.equals(Constants.Status.SUCCESS.name, true)) {
                        val dataObj = json.getJSONObject("data")
                        var isBlock =false
                        try {
                           isBlock = dataObj.getBoolean("is_block")
                        }catch (ex:Exception){
                           Log.d("isBlock........Key not found", ex.message.toString())
                        }

                        var userId = ""
                        var userToken = ""
                        var walletUuid = ""
                        var mobileNo =""
                        var countryCode =""
                        var deviceChanged =false
                        if(isBlock.not()) {
                            mobileNo = dataObj.getString("mobile_no")
                             countryCode = dataObj.getString("country_code")
                             deviceChanged = dataObj.getBoolean("device_changed")

                            if (deviceChanged.not()) {
                                userId = dataObj.getString("user_id")
                                userToken = dataObj.getString("user_token")
                                walletUuid = dataObj.getString("wallet_uuid")
                            }
                        }
                        observerResponse.value =
                            LoginResponse(status, message, LoginResponse.Data(userId, userToken,walletUuid,mobileNo, countryCode,deviceChanged,isBlock))
                    } else {
                        val errorCode = json.getString("error_code")
                       // val dataObj = json.getJSONObject("data")

                        observerResponse.value =
                            LoginResponse(status, message, LoginResponse.Data(
                                "","","","","",false
                            ,false), errorCode)
                    }


                }

            })

        }
    }


}