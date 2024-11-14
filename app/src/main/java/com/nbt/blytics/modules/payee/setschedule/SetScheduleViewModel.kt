package com.nbt.blytics.modules.payee.setschedule

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.payee.schedulecreate.ExternalSchedule
import com.nbt.blytics.modules.payee.schedulecreate.InternalSchedule
import com.nbt.blytics.modules.payee.schedulecreate.ScheduleCrateRes
import com.nbt.blytics.modules.payee.schedulecreate.SelfSchedule
import com.nbt.blytics.modules.signin.model.FailResponse
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetScheduleViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun internalSchedule(internalSchedule: InternalSchedule){
        val request = service.internalSchedule(internalSchedule)
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
                val message = json.getString("message")
                observerResponse.value = ScheduleCrateRes(status,message)
            }
        })
    }

    fun externalSchedule(externalSchedule: ExternalSchedule){
        val request = service.externalSchedule(externalSchedule)
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

    fun editSelfSchedule(selfSchedule: SelfSchedule){
        val request =
            service.editSelfSchedule(selfSchedule)

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
    fun editInternalSchedule(internalSchedule: InternalSchedule){
        val request =
            service.editInternalSchedule(internalSchedule)

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
    fun editExternalPayment(externalSchedule: ExternalSchedule){
        val request = service.editExternalSchedule(externalSchedule)
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