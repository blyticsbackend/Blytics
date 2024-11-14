package com.nbt.blytics.modules.payment.manageac

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.allaccount.GetUpdateDefaultAcc
import com.nbt.blytics.modules.allaccount.UpdateDefaultAcc
import com.nbt.blytics.modules.allaccount.UpdateResponse
import com.nbt.blytics.modules.linkedac.manageac.GetManageRequest
import com.nbt.blytics.modules.linkedac.manageac.ResponseMangeAc
import com.nbt.blytics.modules.linkedac.manageac.ResponseMangeCurrentAc
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun checkDefaultWallet(userId: String, token: String, acc_uuid: String) {
        val request = service.checkDefaultWalletAc(DefaultWalletRequest(
            user_id = userId,
            user_token = token,
            uuid = acc_uuid
        ))


        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    defaultAccResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }
            fun defaultAccResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                observerResponse.value = DefaultWalletResponse(
                    status,message
                )
            }
        })
    }
    fun mangeAcc(manageRequest: UpdateManageConfig,accType :String) {

        val request = service.configAcc(manageRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if(accType.equals("SAVING", true)) {
                        getCoonfigResponse(response.body()!!.string())
                    }else{
                        getConfigResponseCurrent(response.body()!!.string())
                    }

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }


        })
    }
    fun getMangeAcc(manageRequest: GetManageRequest, accType :String ) {

        val request = service.getConfigAcc(manageRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    if(accType.equals("SAVING", true)) {
                        getCoonfigResponse(response.body()!!.string())
                    }else{
                        getConfigResponseCurrent(response.body()!!.string())
                    }

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            /* fun getCoonfigResponse(res:String){
                 val json = JSONObject(res)
                 val status = json.getString("status")
                 val message = json.getString("message")
                 val data = json.getJSONObject("data")
                 val acc_id = data.getInt("acc_id")
                 val acc_no = data.getString("acc_no")
                 val acc_uuid = data.getString("acc_uuid")
                 val default = data.getBoolean("default")
                 val withdraw_date = data.getString("withdraw_date")
                 val active = data.getBoolean("active")
                 val expire = data.getBoolean("expire")
                 val deactivate_at = data.getString("deactivate_at")
                 val trackerObj = data.getJSONObject("tracker_info")
                 val trackerInfo = trackerObj.getBoolean("set")
                 observerResponse.value = ResponseMangeAc(status, message, ResponseMangeAc.Data(
                     accId = acc_id,
                     accNo = acc_no,
                    accUuid = acc_uuid,
                     default = default,
                     withdrawDate = withdraw_date,
                     active = active,
                     expire = expire,
                     deactivateAt = deactivate_at,
                     trackerInfo = ResponseMangeAc.Data.TrackerInfo(trackerInfo)

                 ))


             }*/

        })
    }
    fun startTrackerUpdate(startTrackerRequest: StartTrackerRequest) {
        val request = service.startTracker(startTrackerRequest)
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
                    val data = json.getJSONObject("data")
                    val acc_uuid = data.getString("acc_uuid")
                    val goal_achieve = data.getString("goal_achieve")
                    observerResponse.value = StartTrackerResponse(
                        status, data = StartTrackerResponse.Data(
                            acc_uuid, goal_achieve
                        )
                    )
                } else {
                    val message = json.getString("message")
                    observerResponse.value = StartTrackerResponse(
                        status, message = message, null
                    )
                }

            }
        })
    }
    fun getCoonfigResponse(res: String) {
        val json = JSONObject(res)
        val status = json.getString("status")
        var message = ""
        if (status.equals(Constants.Status.SUCCESS.name, true)) {

            val data = json.getJSONObject("data")
            val acc_id = data.getInt("acc_id")
            val acc_no = data.getString("acc_no")
            val acc_uuid = data.getString("acc_uuid")
            val default = data.getBoolean("default")
            val active = data.getBoolean("active")
            val expire = data.getBoolean("expire")
            val deactivate_at = data.getString("deactivate_at")
            val trackerObj = data.getJSONObject("tracker_info")
            val trackerInfo = trackerObj.getBoolean("set")

            val withdraw_date = data.getString("withdraw_date")
            if (trackerInfo) {

                val trackerInfo = trackerObj.getBoolean("set")
                val start_date = trackerObj.getString("start_date")
                val complete_date = trackerObj.getString("complete_date")
                val targeted_amount = trackerObj.getString("targeted_amount")
                val targeted_achieved = trackerObj.getString("target_achieved")
                val saving_basis = trackerObj.getInt("saving_basis")
                val goal_achieved = trackerObj.getString("goal_achieved")

                observerResponse.value = ResponseMangeAc(
                    status, message, ResponseMangeAc.Data(
                        accId = acc_id,
                        accNo = acc_no,
                        accUuid = acc_uuid,
                        default = default,
                        withdrawDate = withdraw_date,
                        active = active,
                        expire = expire,
                        deactivateAt = deactivate_at,
                        trackerInfo = ResponseMangeAc.Data.TrackerInfo(
                            trackerInfo,
                            start_date, complete_date, targeted_amount, targeted_achieved,
                            saving_basis, goal_achieved
                        )

                    )
                )
            } else {
                observerResponse.value = ResponseMangeAc(
                    status, message, ResponseMangeAc.Data(
                        accId = acc_id,
                        accNo = acc_no,
                        accUuid = acc_uuid,
                        default = default,
                        withdrawDate = withdraw_date,
                        active = active,
                        expire = expire,
                        deactivateAt = deactivate_at,
                        trackerInfo = ResponseMangeAc.Data.TrackerInfo(trackerInfo)

                    )
                )
            }
        } else {
            val error = json.getString("error_code")
            message =json.getString("message")
            observerResponse.value = ResponseMangeAc(
                status, message, null, error
            )
        }
    }
    fun getConfigResponseCurrent(res: String) {
        val json = JSONObject(res)
        val status = json.getString("status")
        var message = ""
        if (status.equals(Constants.Status.SUCCESS.name, true)) {

            val data = json.getJSONObject("data")
            val acc_id = data.getInt("acc_id")
            val acc_no = data.getString("acc_no")
            val acc_uuid = data.getString("acc_uuid")
            val default = data.getBoolean("default")
            val active = data.getBoolean("active")
            val expire = data.getBoolean("expire")
            val deactivate_at = ""//data.getString("deactivate_at")
            var withdraw_date =""
            try {
                 withdraw_date = data.getString("withdraw_date")
            }catch (ex:Exception){
                withdraw_date=""
            }

            observerResponse.value = ResponseMangeCurrentAc(
                    status, message, ResponseMangeCurrentAc.Data(
                        accId = acc_id,
                        accNo = acc_no,
                        accUuid = acc_uuid,
                        default = default,
                        active = active,
                        expire = expire,
                        deactivateAt = deactivate_at,
                    withdrawDate = withdraw_date
                    )
                )

        } else {
            message= json.getString("message")
            val error = json.getString("error_code")
            observerResponse.value = ResponseMangeCurrentAc(
                status, message, null, error
            )
        }
    }
    fun makeAccDefault(userId: String, token: String, acc_uuid: String) {
        val request = service.getDefaultAccount(DefaultAccRequest(
            user_id = userId,
            user_token = token,
            uuid = acc_uuid
        ))


        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    defaultAccResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }
            fun defaultAccResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                observerResponse.value = DefaultAccResponse(
                    status,message
                )
            }
        })
    }
    fun updateAccount(userId: String, token: String, acc_uuid: String, acc_type: String) {
        val updateDefaultAcc = UpdateDefaultAcc(userId, token, acc_uuid)
        val request = if (acc_type.equals("CURRENT", true)) {
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
}

