package com.nbt.blytics.modules.chargesdetails

import android.app.Application
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

class ChargesDetailsViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun userCharge(userChargeRequest: UserChargeRequest) {
        val request: Call<ResponseBody> = service.userCharge(userChargeRequest)
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
                val data = json.getJSONObject("data")
                val total_data = data.getInt("total_data")
                val data_remaining = data.getInt("data_remaining")
                val final_list = data.getJSONArray("final_list")
                val list = mutableListOf<UserChargerResponse.Data.Final>()
                for (i in 0 until final_list.length()) {
                    val obj= final_list.getJSONObject(i)
                    val charged_for= obj.getString("charged_for")
                    val charged_amount= obj.getString("charged_amount")
                    val charged_date= obj.getString("charged_date")
                    list.add(UserChargerResponse.Data.Final(
                        charged_for, charged_amount, charged_date
                    ))
                }
                observerResponse.value = UserChargerResponse(
                    status, UserChargerResponse.Data(
                        total_data, data_remaining, list
                    )
                )

            }
        })
    }

}