package com.nbt.blytics.modules.linkedac.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LinkedAcHomeViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun transactionHistory(transactionRequest: TransactionRequest) {
        val request = service.transactionHistory(transactionRequest)
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
                val account_no = data.getString("account_no")
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
                    val user_image= obj.getString("image_url")
                    val bank_type = obj.getString("bank_type")
                    if(bank_type.equals("Internal",true)){
                        mob_no= obj.getString("mob_no")
                        user_id= obj.getString("user_id")

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
                            bank_acc =  bank_acc
                        ))

                }
                val txnData= TransactionResponse.Data(
                    totalTxn = totalTxn,
                    accountNo = account_no,
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


}