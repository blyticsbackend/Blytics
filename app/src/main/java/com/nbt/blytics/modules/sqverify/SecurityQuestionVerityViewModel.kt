package com.nbt.blytics.modules.sqverify

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.sqverify.models.SQVerifyRequest
import com.nbt.blytics.modules.sqverify.models.SQVerifyResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SecurityQuestionVerityViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun getQuestion(userId:String){
        val sqVerifyRequest = SQVerifyRequest(userId)

        val request = service.getUserSecurityQuestion(sqVerifyRequest)

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
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataObject = json.getJSONObject("data")
                    val dataArray = dataObject.getJSONArray("ques_ans")
                    val quesList = mutableListOf<SQVerifyResponse.Data.QuesAn>()
                    for(i in 0 until dataArray.length()){
                        dataArray.getJSONObject(i).apply {
                            val ans = getString("ans")
                            val hint = getString("hint")
                            val ques = getString("ques")
                            val quesAns = SQVerifyResponse.Data.QuesAn(ans,hint, ques)
                            quesList.add(quesAns)
                        }

                    }
                    val data = SQVerifyResponse.Data(quesList)
                    observerResponse.value  = SQVerifyResponse(status, data)

                }else{
                    val message = json.getString("message")
                    val errorCode = json.getString("error_code")
                    observerResponse.value = SQVerifyResponse(status ,null,message, errorCode)
                }
            }
        })
    }
}