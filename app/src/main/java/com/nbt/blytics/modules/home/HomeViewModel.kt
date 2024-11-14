package com.nbt.blytics.modules.home

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.allaccount.SendAccountData
import com.nbt.blytics.modules.signin.model.FailResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    var walletAccountList = mutableListOf<WalletAccountModel.WalletAccountData>()
    fun getAllAccount(userId: String, token: String, acc_type: String) {
        val sendAccountData = SendAccountData(userId, token, acc_type)
        val request = service.getAccount(sendAccountData)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getAccountData(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }
        })


    }

    private fun getAccountData(res: String) {

        val jsonObject = JSONObject(res)
        val status = jsonObject.getString("status")
        walletAccountList.clear()
        if (status.equals("Success")) {
            val jsonArray = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray.length()) {
                val acc_no = jsonArray.getJSONObject(i).getLong("acc_no").toString()
                val uuid = jsonArray.getJSONObject(i).getString("uuid")
                val active = jsonArray.getJSONObject(i).getBoolean("active")

                walletAccountList.add(
                    WalletAccountModel.WalletAccountData(
                        acc_no,
                        uuid,
                        active
                    )
                )
            }
            observerResponse.value = WalletAccountModel(walletAccountList, status)


        } else {
            observerResponse.value = FailResponse("", "")
        }

    }


    fun getBalance(userId: String, token: String, accUUID: String) {
        val balanceRequest = BalanceRequest(

            accUUID,
            userId,
            token
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
                observerResponse.value = t
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


    fun getSpentAmt(userId: String, token: String, accUUID: String){
        val request = service.spentAmount(SpentRequest(
            userId, token, accUUID
        ))
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getSpent(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }


            }


            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }

            fun getSpent(res: String) {

                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                if (status.equals("Success")) {
                    val data = jsonObject.getJSONObject("data")
                    val month =data.getString("month")
                    val amountObj = data.getJSONObject("amount_display")
                    val balance = amountObj.getString("balance")
                    val todaysReceive = amountObj.getString("todays_receive")
                    val monthlyReceived = amountObj.getString("monthly_received")
                    val todaysSpent = amountObj.getString("todays_spent")
                    val monthlySpent = amountObj.getString("monthly_spent")
                    val amtActualObj = data.getJSONObject("amount_actual")
                    val balanceActual = amtActualObj.getString("balance")

                    observerResponse.value =  SpentResponse(
                        status, SpentResponse.Data(
                            SpentResponse.Data.AmountDisplay(
                                balance,
                                todaysReceive,
                                monthlyReceived,
                                todaysSpent,
                                monthlySpent
                            ),SpentResponse.Data.AmountActual(
                                balanceActual
                            ),
                            month
                        )
                    )

                } else {
                    val message = jsonObject.getString("message")
                   observerResponse.value = FailResponse("", message)
                }

            }
        })
    }
}