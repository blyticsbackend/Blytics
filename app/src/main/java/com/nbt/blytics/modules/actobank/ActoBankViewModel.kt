package com.nbt.blytics.modules.actobank

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.model.CheckValidTnxTpinRequest
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActoBankViewModel(application: Application) : BaseViewModel(application) {

    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun checkAcc(checkAcRequest: CheckAcRequest) {
        val request = service.checkAcc(checkAcRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val accountname = json.getString("accountname")
                    val accountnumber = json.getString("accountnumber")
                    observerResponse.value = CheckAcResponse(status, accountname, accountnumber)

                } else {
                    observerResponse.value = CheckAcResponse(status, "", "")
                }

            }

        })

    }

    fun sendMoneyBank(sendMoneyBank: SendMoneyBankRequest) {
        val request = service.sendMoneyBank(sendMoneyBank)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getCreateAcResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse(ex.message.toString(), ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getCreateAcResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = SendMoneyBankRespone(message, status, "",null)
                }

                else if(status.equals(Constants.Status.FAILED.name,true))
                {
                    observerResponse.value = FailResponse(status,message)                                          //this condition add on 09-11-2023  for is transaction pending

                }

                else {
                    val error_code = json.getString("error_code")
                    val dataObj = json.getJSONObject("data")
                    val chargeable = dataObj.getBoolean("chargeable")
                    observerResponse.value = SendMoneyBankRespone(
                        message, status, error_code, SendMoneyBankRespone.Data(
                            chargeable
                        )
                    )
                }

            }

        })

    }

    fun checkValidTPIN(userId: String, tpin: String, uuid: String, userToken: String) {
        val checkValidTpinRequest = CheckValidTnxTpinRequest(userId, userToken, uuid, tpin)
        val request = service.checkTransactionTpi(checkValidTpinRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getCheckValidResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("no", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("no", t.message.toString())
            }

            fun getCheckValidResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.contains(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = CheckValidTpinResponse(status, message, "", null)

                } else {
                    val errorCode = json.getString("error_code")
                    val dataObj = json.getJSONObject("data")
                    val verified = dataObj.getBoolean("verified")
                    val data = CheckValidTpinResponse.Data(verified)
                    observerResponse.value =
                        CheckValidTpinResponse(status, message, errorCode, data)
                }
            }
        })

    }
}