package com.nbt.blytics.modules.chagnetpin

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.chagnetpin.models.ChangeTipRequest
import com.nbt.blytics.modules.chagnetpin.models.ChangeTpinResponse
import com.nbt.blytics.modules.chagnetpin.models.CheckTpin

import com.nbt.blytics.modules.chagnetpin.models.CheckTpinResponse
import com.nbt.blytics.modules.otp.model.CheckValidTpinRequest
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangeTipViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    var checksqTpin = -1
    var checkwTpin = -1

    fun changeTpi(userId: String, accUUID: String, tPin: String) {
        val changeTpinRequest = ChangeTipRequest(accUUID, tPin, userId)
        val request = service.changeTpin(changeTpinRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getChangeTpinResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getChangeTpinResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = ChangeTpinResponse(status, message)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = ChangeTpinResponse(status, message, errorCode)


                }
            }
        })

    }


    fun checkTpin(userId: String, user_token: String) {
        val checkTpin= CheckTpin(userId,user_token)
        val request = service.checkPin(checkTpin)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getCheckPin(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getCheckPin(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val data = json.getJSONObject("data")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    checksqTpin = data.getInt("usq")
                    checkwTpin = data.getInt("wtpin")
                    observerResponse.value =
                        CheckTpinResponse(
                            status,
                            "",
                            "",
                            CheckTpinResponse.Data(checksqTpin, checkwTpin)
                        )
                } else {
                    val errorCode = json.getString("error_code")
                    val message = json.getString("message")
                    observerResponse.value = CheckTpinResponse(status, message, errorCode)


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
                    observerResponse.value = CheckValidTpinResponse(status, message,"",null)

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