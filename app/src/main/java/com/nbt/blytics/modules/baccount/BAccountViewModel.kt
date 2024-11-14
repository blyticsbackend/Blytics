package com.nbt.blytics.modules.baccount

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.squpdate.model.SqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqlRequest
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BAccountViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun getQuestion(){
        val request = service.getSQ()
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getQuestionResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getQuestionResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                val quesList = mutableListOf< SqResponse.Question>()
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val questionArray = json.getJSONArray("question")
                    for(i in 0 until questionArray.length()){
                        questionArray.getJSONObject(i).apply {
                            val ques = getString("question")
                            val id = getInt("id")
                            quesList.add(SqResponse.Question(id, ques, "",""))
                        }
                    }
                    observerResponse.value = SqResponse(quesList, status)


                }else{
                    observerResponse.value = SqResponse(quesList, status)
                }
            }

        })
    }

    fun postSQ(sqUpdateRequest: UpdateSqlRequest) {


        val request = service.updateSQ(sqUpdateRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getSQResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getSQResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = UpdateSqResponse(status, "")
                }else{
                    val message = json.getString("message")
                    observerResponse.value = UpdateSqResponse(status, message)
                }
            }
        })
    }
}