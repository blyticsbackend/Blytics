package com.nbt.blytics.modules.notification

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

   fun getNotification(notificationListRequest: NotificationListRequest) {
       val request = service.getAllNotification(notificationListRequest)
       request.enqueue(object : Callback<ResponseBody> {
           override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
               try {
                   getNotificationRes(response.body()!!.string())


               } catch (e: Exception) {
                   observerResponse.value = FailResponse("", e.message.toString())
               }
           }

           override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
               observerResponse.value = t
           }

           fun getNotificationRes(res: String) {
               val json = JSONObject(res)
               val status = json.getString("status")
               if (status.equals(Constants.Status.SUCCESS.name, true)) {

                   val data = json.getJSONObject("data")
                   val notificationObj = data.getJSONObject("notification")
                   val total_data = notificationObj.getInt("total_data")
                   val data_remaining = notificationObj.getInt("data_remaining")
                   val finalList = notificationObj.getJSONArray("final_list")
                   val notificationList = mutableListOf<AllNotificationResponse.Data.Notification.Final>()
                   for (i in 0 until finalList.length()) {
                       val nObj = finalList.getJSONObject(i)
                       val id = nObj.getInt("id")
                       val title = nObj.getString("title")
                       val message = nObj.getString("message")
                       val status = nObj.getString("status")
                       val datetime = nObj.getString("datetime")

                       notificationList.add(
                           AllNotificationResponse.Data.Notification.Final(
                               id, title, message, status,datetime
                           )
                       )
                   }
                   observerResponse.value = AllNotificationResponse(
                       status, AllNotificationResponse.Data(
                           AllNotificationResponse.Data.Notification(
                               total_data, data_remaining, notificationList
                           )
                       )
                   )
               }else{
                   val errorCode= json.getString("error_code")
                   val message= json.getString("message")
                   observerResponse.value = FailResponse(
                       errorCode, message
                   )
               }

           }
       })
   }


}