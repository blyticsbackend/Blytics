package com.nbt.blytics.modules.payamount

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.home.BalanceRequest
import com.nbt.blytics.modules.home.BalanceResponse
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.model.*
import com.nbt.blytics.modules.payee.payment.ApprovalRequest
import com.nbt.blytics.modules.payee.payment.ApprovalResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PayAmountViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun statusChange(approvalRequest: ApprovalRequest) {
        val request = service.approvalRequest(approvalRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>, response: Response<ResponseBody>
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
                    observerResponse.value = ApprovalResponse(
                        status
                    )
                } else {
                    val message = json.getString("message")
                    observerResponse.value = FailResponse("", message)
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
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
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
                    observerResponse.value = CheckValidTpinResponse(status, message, errorCode, data)
                }
            }
        })

    }

    fun sendMoney(sendMoneyRequest: SendMoneyRequest) {
        val request = service.sendSelfMoney(sendMoneyRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getSendMoneyResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getSendMoneyResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")

                if (status.contains(Constants.Status.SUCCESS.name, true)) {
                    val message = json.getString("message")
                    observerResponse.value = SendMoneyResponse(status, message, "", null)
                } else {
                    val error_code = json.getString("error_code")
                    var msg = ""
                    val message = json.getJSONArray("message")
                    for (i in 0 until message.length()) {
                        msg += message.getString(i)
                    }
                    val dataObj = json.getJSONObject("data")
                    val chargeable = dataObj.getBoolean("chargeable")
                    val data = SendMoneyResponse.Data(chargeable)
                    observerResponse.value = SendMoneyResponse(status, msg, error_code, data)
                }
            }
        })
    }

    fun requestMoney(requestMoney: RequestMoney) {
        val request = service.requestMoney(requestMoney)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getRequestMoneyResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getRequestMoneyResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                observerResponse.value = RequestMoneyResponse(status = status, message = message)
            }
        })
    }

    fun getBalance(userId: String, token: String, accUUID: String) {
        val balanceRequest = BalanceRequest(

            accUUID, userId, token
        )
        val request = service.balance(balanceRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getBalace(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }


            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getBalace(res: String) {

                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                if (status.equals("Success")) {
                    val data = jsonObject.getJSONObject("data")
                    val balance = data.getString("balance")
                    observerResponse.value = BalanceResponse(BalanceResponse.Data(balance), status)


                } else {
                    val message = jsonObject.getString("message")
                    observerResponse.value = FailResponse("", message)
                }

            }
        })


    }


}