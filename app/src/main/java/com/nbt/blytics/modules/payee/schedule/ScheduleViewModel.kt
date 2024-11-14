package com.nbt.blytics.modules.payee.schedule

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

class ScheduleViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun getRecentSchedule(recentScheduleRequest: RecentScheduleRequest){
        val request = service.recentSchedule(recentScheduleRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>,response: Response<ResponseBody>) {
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
                val scheduleList = data.getJSONArray("schedule_list")
                val recentScheduleList = mutableListOf<RecentScheduleRes.Data.Schedule>()
                for(i in 0 until scheduleList.length()){
                    val obj = scheduleList.getJSONObject(i)
                    val name= obj.getString("name")
                    val amount= obj.getString("amount")
                    val next_date= obj.getString("next_date")
                    val reference= obj.getString("reference")
                    val end_date = obj.getString("end_date")
                    val frequency = obj.getString("frequency")
                    val schedule_id = obj.getString("schedule_id")
                    val type = obj.getString("type")
                    var user_id:String =""
                    var accountnumber :String =""
                    var mobNo :String =""
                    if(type.equals("internal Schedule",true)){
                         user_id= obj.getString("user_id")
                         mobNo = obj.getString("mob_no")
                    }else{
                        accountnumber = obj.getString("accountnumber")
                    }

                    recentScheduleList.add(
                       RecentScheduleRes.Data.Schedule(
                           userId = user_id,
                           name= name,
                           nextDate =next_date,
                           reference = reference,
                           amount = amount,
                           accountnumber = accountnumber,
                           endDate = end_date,
                           scheduleId = schedule_id,
                           frequency = frequency,
                           type = type,
                           mobNo = mobNo
                       )
                    )
                }

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = RecentScheduleRes(status, RecentScheduleRes.Data(recentScheduleList))
                }else{
                    val message = json.getString("message")
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }

    fun deleteSchedule(userRequest: DeleteScheduleRequest, isInternal:Boolean){
        val request =   if(isInternal){
           service.deleteInternalSchedule(userRequest)}
        else {
            service.deleteExternalSchedule(userRequest)
        }
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
                observerResponse.value = DeleteScheduleResponse(status, message)
            }
        })
    }
}