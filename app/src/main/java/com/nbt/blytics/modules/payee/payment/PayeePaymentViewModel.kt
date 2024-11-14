package com.nbt.blytics.modules.payee.payment

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PayeePaymentViewModel (application: Application): BaseViewModel(application) {

    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun allRequest(requestMoney:RequestMoneyReq){
        val request = service.allRequestMoney(requestMoney)
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
                val data = json.getJSONArray("data")
                val listReq = mutableListOf<RequestMoneyResponse.Data>()
                for(i in 0 until data.length()){
                    val obj = data.getJSONObject(i)
                    val image_url= obj.getString("image_url")
                    val request_id= obj.getInt("request_id")
                    val requested_amount= obj.getString("requested_amount")
                    val requested_by_user_id= obj.getInt("requested_by_user_id")
                    val request_mob_no= obj.getString("request_mob_no")
                    val request_by_user_name= obj.getString("request_by_user_name")
                    listReq.add(
                        RequestMoneyResponse.Data(
                            imageUrl = image_url,
                            requestId = request_id,
                            requestedAmount = requested_amount,
                            requestedByUserId = requested_by_user_id,
                            requestMobNo = request_mob_no,
                            requestByUserName = request_by_user_name
                        )
                    )


                }


                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value =RequestMoneyResponse(
                        status, listReq
                    )
                }else{
                    val message = json.getString("message")
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }

    fun transactionHistory(tnxListRequest: TnxListRequest) {

        val request = service.transactionList(tnxListRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getTransactionResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getTransactionResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val data = json.getJSONObject("data")
                val totalTxn = data.getString("total_txn")
               // val account_no = data.getString("account_no")
                val data_remaining = data.getString("data_remaining")
                val txn_list = data.getJSONArray("txn_list")
                val listTxn = mutableListOf<TransactionResponse.Data.Txn>()
                for(i in 0 until txn_list.length()){
                    val obj = txn_list.getJSONObject(i)
                    val type= obj.getString("type")
                    val uuid= obj.getString("uuid")
                    val status =obj.getString("status")
                    val user_name= obj.getString("user_name")
                    val amount= obj.getString("amount")
                    val currency= obj.getString("currency")
                    val txn_id= obj.getString("txn_id")
                    val date= obj.getString("date")
                    var mob_no =""
                    var user_id=""
                    var user_image=    obj.getString("image_url")
                    val bank_type = obj.getString("bank_type")
                    if(bank_type.equals("Internal",true)){
                        mob_no= obj.getString("mob_no")
                        user_id= obj.getString("user_id")
                        user_image = obj.getString("image_url")
                    }
                    var bank_acc =""
                    var bank_code =""
                    if(bank_type.equals("External",true)){
                        bank_acc= obj.getString("bank_acc")
                        bank_code= obj.getString("bank_code")
                    }
                    listTxn.add(
                        TransactionResponse.Data.Txn(
                        type = type,
                        uuid = uuid,
                        status =status,
                        userImage = user_image,
                        userName = user_name,
                        userId = user_id,
                        amount = amount,
                        currency = currency,
                        txnId = txn_id,
                        date = date,
                            mobNo = mob_no,
                            bankType = bank_type,
                            bank_code = bank_code,
                            bank_acc = bank_acc
                    ))

                }
                    val txnData= TransactionResponse.Data(
                    totalTxn = totalTxn,
                    accountNo = "",
                    dataRemaining = data_remaining,
                    txnList = listTxn
                )

                // val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = TransactionResponse(txnData,status)
                }else{
                    val message = json.getString("message")
                    observerResponse.value = FailResponse("", message)
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
                    val mobNo = data.getString("mob_no")
                    val userName = data.getString("user_name")
                    val avatarUrl = data.getString("avatar_url")
                    val walletUUID =data.getString("wallet_uuid")
                    val tpin =data.getInt("tpin")
                    val securityQuestion =data.getInt("security_question")

                    observerResponse.value = CheckExistPhoneResponse(
                        CheckExistPhoneResponse.Data(email, mobNo, userId,walletUUID,tpin, securityQuestion, userName,avatarUrl),
                        errorCode, status
                    )

                } else {
                    val message = json.getString("message")
                    observerResponse.value = CheckExistPhoneResponse(
                        null,
                        errorCode, status, message)

                }
            }

        })
    }
}