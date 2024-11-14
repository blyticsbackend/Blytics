package com.nbt.blytics.modules.selftransfer

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.acinfo.AcInfoRequest
import com.nbt.blytics.modules.acinfo.AcInfoResponse
import com.nbt.blytics.modules.allaccount.AllAccountModel
import com.nbt.blytics.modules.allaccount.LinkedAccResponse
import com.nbt.blytics.modules.allaccount.SendAccountData
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.model.CheckValidTnxTpinRequest
import com.nbt.blytics.modules.payee.schedulecreate.InternalSchedule
import com.nbt.blytics.modules.payee.schedulecreate.ScheduleCrateRes
import com.nbt.blytics.modules.payee.schedulecreate.SelfSchedule
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelfTransferViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    var allAccountList = mutableListOf<AllAccountModel.AllAccountData>()

    fun getAllAc(acInfoRequest: AcInfoRequest) {
        val request: Call<ResponseBody> = service.getAllAc(acInfoRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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
                val list = mutableListOf<AcInfoResponse.Data>()
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val acc_no = obj.getString("acc_no")
                    val acc_type = obj.getString("acc_type")
                    val amount = obj.getString("amount")
                    val bank_code = obj.getString("bank_code")
                    var create_by = ""
                    var create_for = ""
                    var relation = ""
                    var acc_holder_name = ""
                    if (acc_type.equals("linked", true)) {
                        create_by = obj.getString("created_by")
                        create_for = obj.getString("created_for")
                        relation = obj.getString("relation")
                    } else {
                        acc_holder_name = obj.getString("acc_holder_name")
                    }
                    list.add(AcInfoResponse.Data(acc_holder_name = acc_holder_name, acc_no = acc_no, acc_type = acc_type, amount = amount, bank_code = bank_code, create_by = create_by, create_for = create_for, relation = relation))
                }
                observerResponse.value = AcInfoResponse(status, list)
            }
        })
    }

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
                    } else if (acc_type.equals("wallet", true)) {
                        getWallet(response.body()!!.string())
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
                        val purpose = jsonArray.getJSONObject(i).getString("purpose")
                        val default = jsonArray.getJSONObject(i).getBoolean("default")
                        val withdraw_date = jsonArray.getJSONObject(i).getString("withdraw_date")
                        val active = jsonArray.getJSONObject(i).getBoolean("active")
                        val deactivate_at = jsonArray.getJSONObject(i).getString("deactivate_at")
                        val tracker_info = jsonArray.getJSONObject(i).getString("tracker_info")
                        allAccountList.add(AllAccountModel.AllAccountData(acc_no, acc_uuid, active, deactivate_at, default, purpose, tracker_info, withdraw_date))
                    }
                    observerResponse.value = AllAccountModel(allAccountList, status)

                } else {
                    val message = jsonObject.getString("message")
                    observerResponse.value = FailResponse("", message)
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
                        allAccountList.add(AllAccountModel.AllAccountData(acc_no, uuid, active, "", default, "", "", ""))
                    }
                    observerResponse.value = AllAccountModel(allAccountList, status)
                } else {
                    val message = jsonObject.getString("message")
                    observerResponse.value = FailResponse("", message)
                }
            }

            fun getLinkedData(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                val data = jsonObject.getJSONObject("data")
                val acc_list = data.getJSONArray("acc_list")
                val addressObj = data.getJSONObject("address")
                val address = addressObj.getString("address")
                val state = addressObj.getString("state")
                val country = addressObj.getString("country")
                val pin_code = addressObj.getString("pin_code")
                val dataList = mutableListOf<LinkedAccResponse.AccList>()
                val dayList = mutableListOf<String>()
                for (i in 0 until acc_list.length()) {
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
                    val amount1 = obj.getString("amount")
                    dataList.add(LinkedAccResponse.AccList(createdFor, relation, accId, accNo, accUuid, active, dayList, expire, withdrawl_limit, amount1))
                }
                observerResponse.value = LinkedAccResponse(
                    status, LinkedAccResponse.Data(dataList, LinkedAccResponse.Address(address, state, country, pin_code)))
            }

            fun getWallet(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                val data = jsonObject.getJSONArray("data")
                var ac_no: String = ""
                var uuid: String = ""
                for (i in 0 until data.length()) {
                    ac_no = data.getJSONObject(i).getString("acc_no")
                    uuid = data.getJSONObject(i).getString("uuid")
                }
                observerResponse.value = WalletResponse(ac_no, uuid)
            }
        })
    }

    fun sendMoney(selfTxnRequest: SelfTxnRequest) {
        val request = service.sendSelfMoney(selfTxnRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getSelfResponse(response.body()!!.string())
                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }

            //{"status": "Success", "error_code": "", "message": "Amount Transfer Successfully"}
            fun getSelfResponse(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                val message = jsonObject.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = SelfTxnResponse(status, message, "", null)
                } else {
                    val error_code = jsonObject.getString("error_code")
                    val dataObj = jsonObject.getJSONObject("data")
                    val chargeable = dataObj.getBoolean("chargeable")
                    observerResponse.value = SelfTxnResponse(status, message, error_code, SelfTxnResponse.Data(chargeable))
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

    fun selfSchedule(selfSchedule: SelfSchedule){
        val request = service.selfSchedule(selfSchedule)
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
                val message = json.getString("message")
                observerResponse.value = ScheduleCrateRes(
                    status,message
                )
            }
        })
    }
}