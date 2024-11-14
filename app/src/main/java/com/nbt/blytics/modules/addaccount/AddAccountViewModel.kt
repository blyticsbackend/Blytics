package com.nbt.blytics.modules.addaccount

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.addaccount.models.CreateAcRequest
import com.nbt.blytics.modules.addaccount.models.CreateAcResponse
import com.nbt.blytics.modules.addaccount.models.CreateCurrentAcRequest
import com.nbt.blytics.modules.addaccount.models.CreateCurrentResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAccountViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun checkForBalance(userId: Int, uuid: String) {
        val request = service.check_balanc_for_Linked_account(userId, uuid)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        val res = response.body()!!.string()
                        val json = JSONObject(res)
                        val status = json.getInt("status")
                        if (status == 200) {
                            // Parse `data` and `amount_required`
                            val data = json.getBoolean("data")
                            val amountRequired = json.optInt("amount_required", 0)
                            observerResponse.value = BalanceResponse(data, amountRequired)
                        } else {
                            observerResponse.value = FailResponse("", "Unexpected response")
                        }
                    } else {
                        observerResponse.value = FailResponse("", "Error: ${response.code()}")
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

    // Create a data class to represent the parsed response
    data class BalanceResponse(val data: Boolean, val amountRequired: Int)

    fun addAccount(addAccountRequest: CreateAcRequest){
            val request = service.createSavingAc(addAccountRequest)
            request.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        getCreateAcResponse(response.body()!!.string())
                    } catch (ex: Exception) {
                        observerResponse.value = FailResponse("", ex.message.toString())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    observerResponse.value = FailResponse("", t.message.toString())
                }

                fun getCreateAcResponse(res:String){
                    val json = JSONObject(res)
                    val status = json.getString("status")
                    val message = json.getString("message")
                    if (status.equals(Constants.Status.SUCCESS.name, true)) {
                        val jsonObj = json.getJSONObject("data")
                        val accNo= jsonObj.getString("acc_no")
                        val accName= jsonObj.getString("acc_name")
                        val uuid= jsonObj.getString("uuid")
                        val isDefault= jsonObj.getBoolean("is_default")
                        val data = CreateAcResponse.Data(accNo,accName, uuid, isDefault)
                        observerResponse.value = CreateAcResponse(status, message, data)
                    }else{
                        observerResponse.value = CreateAcResponse(status, message, null)
                    }
                }
            })
    }

    fun addCurrentAccount(currentAcRequest: CreateCurrentAcRequest){
        val request = service.createCurrentAc(currentAcRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getCreateAcResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getCreateAcResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val jsonObj = json.getJSONObject("data")
                    val accNo= jsonObj.getString("acc_no")
                    val accId= jsonObj.getString("Acc_id")
                    val uuid= jsonObj.getString("uuid")
                    val data = CreateCurrentResponse.Data(accNo,accId, uuid)
                    observerResponse.value = CreateCurrentResponse(status, message, data)
                }else{
                    observerResponse.value = CreateCurrentResponse(status, message, null)
                }
            }
        })
    }
}