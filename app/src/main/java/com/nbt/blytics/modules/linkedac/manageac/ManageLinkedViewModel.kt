package com.nbt.blytics.modules.linkedac.manageac

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageLinkedViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun mangeAcc(manageRequest: ManageRequest) {

        val request = service.configAcc(manageRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getCoonfigResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getCoonfigResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val created_by = data.getString("created_by")
                    val created_for = data.getString("created_for")
                    val relation = data.getString("relation")
                    val acc_id = data.getString("acc_id")
                    val acc_no = data.getString("acc_no")
                    val acc_uuid = data.getString("acc_uuid")
                    val active = data.getBoolean("active")
                    val expire = data.getBoolean("expire")
                    observerResponse.value = ManageResponse(
                        status,
                        message,
                        ManageResponse.Data(
                            created_by,
                            created_for,
                            relation, acc_id, acc_no, acc_uuid, active, expire

                        )

                    )
                } else {
                    observerResponse.value = FailResponse("", message)
                }
            }

        })
    }
    fun getMangeAcc(manageRequest: GetManageLinkedRequest) {

        val request = service.getConfigLinkedAcc(manageRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    getLinkedData(response.body()!!.string())


                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            /*   fun getConfigLinkedAcc(res: String) {
                   val json = JSONObject(res)
                   val status = json.getString("status")
                   val data = json.getJSONObject("data")


           }*/

            fun getLinkedData(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                val obj = jsonObject.getJSONObject("data")
                /*val dataList = mutableListOf<MangeLinkedResponse.AccList>()
                for (i in 0 until data.length()) {*/
                    val dayList = mutableListOf<String>()

                    val createdFor = obj.getString("created_for")
                    val relation = obj.getString("relation")
                    val accId = obj.getString("acc_id")
                    val accNo = obj.getString("acc_no")
                    val accUuid = obj.getString("acc_uuid")
                    val active = obj.getBoolean("active")
                    val daysArr = obj.getJSONArray("days")
                    val expire = obj.getBoolean("expire")
                    for (j in 0 until daysArr.length()) {
                        val dayObj = daysArr.getString(j)
                        dayList.add(dayObj)
                    }
                    //val accId= obj.getString("acc_id")
                    val amount1 = obj.getString("amount")

                   val manage =     MangeLinkedResponse.AccList(
                            createdFor = createdFor,
                            relation = relation,
                            accId = accId,
                            accNo = accNo,
                            accUuid = accUuid,
                            active = active,
                            days = dayList,
                            expire = expire,
                            amount = amount1,
                            withdrawlLimit = ""
                        )
                    observerResponse.value = MangeLinkedResponse(
                        status = status, data = manage, message = ""
                    )


            }
        }
        )
    }
    fun deleteLinked(manageRequest: ManageRequestLinked){
        val request = service.configAccLinked(manageRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getCoonfigResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getCoonfigResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val created_by = data.getString("created_by")
                    val created_for = data.getString("created_for")
                    val relation = data.getString("relation")
                    val acc_id = data.getString("acc_id")
                    val acc_no = data.getString("acc_no")
                    val acc_uuid = data.getString("acc_uuid")
                    val active = data.getBoolean("active")
                    val expire = data.getBoolean("expire")
                    observerResponse.value = ManageResponse(
                        status,
                        message,
                        ManageResponse.Data(
                            created_by,
                            created_for,
                            relation, acc_id, acc_no, acc_uuid, active, expire

                        )

                    )
                } else {
                    observerResponse.value = FailResponse("", message)
                }
            }

        })
    }
}