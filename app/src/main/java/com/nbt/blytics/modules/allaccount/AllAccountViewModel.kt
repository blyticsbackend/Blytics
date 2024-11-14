package com.nbt.blytics.modules.allaccount

import android.app.Application
import androidx.lifecycle.MutableLiveData
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

class AllAccountViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    var allAccountList = mutableListOf<AllAccountModel.AllAccountData>()

    fun getAllAccount(userId: String, token: String, acc_type: String) {
        val sendAccountData = SendAccountData(userId, token, acc_type)
        val request = service.getAccount(sendAccountData)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (acc_type.equals("saving", true)) {
                        getSavingData(response.body()!!.string())
                    } else if (acc_type.equals("current", true)) {
                        getCurrentData(response.body()!!.string())
                    } else if (acc_type.equals("linked", true)) {
                        getLinkedData(response.body()!!.string())
                    }

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }
            fun getSavingData(res: String) {

                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                allAccountList.clear()
                if (status.equals("Success")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val acc_no = jsonArray.getJSONObject(i).getLong("acc_no").toString()
                        val acc_uuid = jsonArray.getJSONObject(i).getString("acc_uuid")
                        val purpose = jsonArray.getJSONObject(i).getString("acc_name")
                        val default = jsonArray.getJSONObject(i).getBoolean("default")
                        val withdraw_date = jsonArray.getJSONObject(i).getString("withdraw_date")
                        val active = jsonArray.getJSONObject(i).getBoolean("active")
                        val deactivate_at = jsonArray.getJSONObject(i).getString("deactivate_at")
                        val tracker_info = jsonArray.getJSONObject(i).getString("tracker_info")

                        allAccountList.add(
                            AllAccountModel.AllAccountData(
                                acc_no,
                                acc_uuid,
                                active,
                                deactivate_at,
                                default,
                                purpose,
                                tracker_info,
                                withdraw_date
                            )
                        )
                    }
                    observerResponse.value = AllAccountModel(allAccountList, status)


                } else {
                    observerResponse.value = FailResponse("", "")
                }

            }

            fun getCurrentData(res: String) {

                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                allAccountList.clear()
                if (status.equals("Success")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val acc_no = jsonArray.getJSONObject(i).getLong("acc_no").toString()
                        val default = jsonArray.getJSONObject(i).getBoolean("default")
                        val uuid = jsonArray.getJSONObject(i).getString("acc_uuid")
                        val active = jsonArray.getJSONObject(i).getBoolean("active")

                        allAccountList.add(
                            AllAccountModel.AllAccountData(
                                acc_no,
                                uuid,
                                active,
                                "",
                                default,
                                "",
                                "",
                                ""
                            )
                        )
                    }
                    observerResponse.value =AllAccountModel(allAccountList, status)


                } else {
                    observerResponse.value = FailResponse("", "")
                }

            }

            fun getLinkedData(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                val data = jsonObject.getJSONObject("data")
                val acc_list =  data.getJSONArray("acc_list")
                val addressObj = data.getJSONObject("address")
                val address = addressObj.getString("address")
                val state = addressObj.getString("state")
                val country = addressObj.getString("country")
                val pin_code = addressObj.getString("pin_code")
                val dataList = mutableListOf<LinkedAccResponse.AccList>()
                for (i in 0 until acc_list.length()) {
                    val dayList = mutableListOf<String>()
                    val obj = acc_list.getJSONObject(i)
                    val createdFor = obj.getString("created_for")
                    val relation = obj.getString("relation")
                    val accId = obj.getString("acc_id")
                    val accNo = obj.getString("acc_no")
                    val accUuid = obj.getString("acc_uuid")
                    val active = obj.getBoolean("active")
                    val daysArr = obj.getJSONArray("days")
                    val expire = obj.getBoolean("expire")
                    val withdrawl_limit = obj.getString("withdrawl_limit")
                    for (j in 0 until daysArr.length()) {
                        val dayObj = daysArr.getString(j)
                        dayList.add(dayObj)
                    }
                    //val accId= obj.getString("acc_id")
                    val amount1 = obj.getString("amount")

                    dataList.add(
                        LinkedAccResponse.AccList(
                            createdFor, relation, accId, accNo, accUuid, active, dayList, expire, withdrawl_limit,amount1
                        )
                    )
                }
                observerResponse.value = LinkedAccResponse(status,LinkedAccResponse.Data(
                    dataList, LinkedAccResponse.Address(
                        address, state, country, pin_code
                    )
                ))
            }

        })


    }


    fun updateAccount(userId: String, token: String, acc_uuid: String, acc_type: String) {
        val updateDefaultAcc = UpdateDefaultAcc(userId, token, acc_uuid)

        val request = if (acc_type.equals(Constants.CURRENT_ACC)) {
            service.updateDefaultCurrentAccount(updateDefaultAcc)
        } else {
            service.updateDefaultAccount(updateDefaultAcc)
        }

        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    updateAccountData(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }

            fun updateAccountData(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val jsonObj = json.getJSONObject("data")
                    val accNo = jsonObj.getString("acc_no")
                    val uuid = jsonObj.getString("uuid")
                    val data = UpdateResponse(accNo, uuid)
                    observerResponse.value = GetUpdateDefaultAcc(status, message, data)
                } else {
                    observerResponse.value = FailResponse("", "")

                }


            }

        })
    }

    fun transactionHistory(transactionRequest: TransactionRequest) {
        val request = service.transactionHistory(transactionRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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
                    val user_image=obj.getString("image_url")
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
                            bank_acc = bank_acc,
                            bank_code = bank_code
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