package com.nbt.blytics.modules.acinfo

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllAcInfoViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
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
                Log.d("AcInfoResponse", "Full Response: $res")
                val json = JSONObject(res)
                val status = json.getString("status")
                val data = json.getJSONArray("data")
                val list = mutableListOf<AcInfoResponse.Data>()
                // Log the status
                for (i in 0 until data.length()) {
                    val obj = data.getJSONObject(i)
                    val acc_no = obj.getString("acc_no")
                    val acc_type = obj.getString("acc_type")
                    val amount = obj.getString("amount")
                    val bank_code = obj.getString("bank_code")
                    val acc_uuid = obj.getString("acc_uuid")
                    var acc_name = obj.optString("acc_name", "")
                    var create_by = ""
                    var create_for = ""
                    var relation = ""
                    var acc_holder_name = ""
                    val acc_id: String = ""

                    if (acc_type.equals("linked", true)) {
                        create_by = obj.getString("created_by")
                        create_for = obj.getString("created_for")
                        relation = obj.getString("relation")
                       acc_name = obj.optString("created_for","")

                    } else {
                        acc_holder_name = obj.getString("acc_holder_name")
                    }
                    list.add(
                        AcInfoResponse.Data(
                            acc_holder_name = acc_name,
                            acc_no = acc_no,
                            acc_type = acc_type,
                            amount = amount,
                            bank_code = bank_code,
                            create_by = create_by,
                            create_for = create_for,
                            relation = relation,
                            acc_uuid = acc_uuid,
                            accid = acc_id,
                            acc_name = acc_name
                        )
                    )
                }
                observerResponse.value = AcInfoResponse(
                    status, list
                )
            }
        })
    }
}