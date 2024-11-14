package com.nbt.blytics.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.activity.main.LogoutRequest
import com.nbt.blytics.activity.main.LogoutResponse
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Nbt on 11-06-2021.
 */

open class BaseViewModel(application: Application) : AndroidViewModel(application){
    private var service = RetrofitFactory.getApiService()
    var baseObserverResponse = MutableLiveData<Any>()

    fun logOutSession(logoutRequest: LogoutRequest) {
        val request = service.logOut(logoutRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getLogoutRes(response.body()!!.string())
                } catch (e: Exception) {
                    baseObserverResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                baseObserverResponse.value = FailResponse("", t.message.toString())
            }

            fun getLogoutRes(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {

                    baseObserverResponse.value = LogoutResponse(
                        status,""
                    )
                } else {
                    val message = json.getString("message")
                    baseObserverResponse.value = LogoutResponse(
                        status,message
                    )
                }

            }
        })
    }
}