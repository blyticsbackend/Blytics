package com.nbt.blytics.modules.payee.schedulecreate

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.actobank.CheckAcRequest
import com.nbt.blytics.modules.actobank.CheckAcResponse
import com.nbt.blytics.modules.payee.schedule.RecentScheduleRequest
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleCreateViewModel (application: Application): BaseViewModel(application) {

    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun checkAcc(checkAcRequest: CheckAcRequest) {
        val request = service.checkAcc(checkAcRequest)
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
                    val accountname = json.getString("accountname")
                    val accountnumber = json.getString("accountnumber")
                    observerResponse.value = CheckAcResponse(status, accountname, accountnumber)

                } else {
                    observerResponse.value = CheckAcResponse(status, "", "")
                }

            }

        })

    }

}