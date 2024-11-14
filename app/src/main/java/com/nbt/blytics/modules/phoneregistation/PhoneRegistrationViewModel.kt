package com.nbt.blytics.modules.phoneregistation

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest2
import com.nbt.blytics.modules.phoneregistation.models.CheckUserFullResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.Constants.errorMessage
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PhoneRegistrationViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun checkExist(phoneNumber: String, checkFor: CheckFor) {
        val checkExistRequestModel = CheckExistRequest("",phoneNumber, checkFor.ordinal)
        val request = service.userCheckExist(checkExistRequestModel)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (checkFor.ordinal != 2) {
                        getCheckExistResponse(response.body()!!.string())
                    } else {
                        getCheckExistFullResponse(response.body()!!.string())
                    }
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            private fun getCheckExistResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val errorCode = json.getInt("error_code")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userId = data.getString("user_id")
                    val email = data.getString("email")
                    val mobNo = data.getString("mob_no")
                    val userName = data.getString("user_name")
                    val walletUUID = data.getString("wallet_uuid")
                    val tpin = data.getInt("tpin")
                    val securityQuestion = data.getInt("security_question")
                    observerResponse.value = CheckExistPhoneResponse(CheckExistPhoneResponse.Data(email, mobNo, userId, walletUUID, tpin, securityQuestion, "", ""), errorCode, status)
                } else {
                    observerResponse.value = CheckExistPhoneResponse(null, errorCode, status, errorMessage(errorCode))
                }
            }

            private fun getCheckExistFullResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val errorCode = json.getInt("error_code")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val user_id = data.getString("user_id")
                    val first_name = data.getString("first_name")
                    val last_name = data.getString("last_name")
                    val email = data.getString("email")
                    val address = data.getString("address")
                    val state = data.getString("state")
                    val country = data.getString("country")
                    val pin_code = data.getString("pin_code")
                    val country_code = data.getString("country_code")
                    val mob_no = data.getString("mob_no")
                    val avatar_url = data.getString("avatar_url")
                    observerResponse.value = CheckUserFullResponse(status, errorCode, "", CheckUserFullResponse.Data(user_id, first_name, last_name, email, address, state, country, pin_code, country_code, mob_no, avatar_url))
                } else {
                    observerResponse.value = CheckUserFullResponse(status, errorCode, errorMessage(errorCode), null)
                }
            }
        })
    }

    fun checkExist2(str: String, checkFor: CheckFor) {
        val checkExistRequestModel = CheckExistRequest2("", checkFor.ordinal, str)
        val request = service.userCheckExist2(checkExistRequestModel)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (checkFor.ordinal != 2) {
                        getCheckExistResponse(response.body()!!.string())
                    } else {
                        getCheckExistFullResponse(response.body()!!.string())
                    }
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            private fun getCheckExistResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val errorCode = json.getInt("error_code")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userId = data.getString("user_id")
                    val email = data.getString("email")
                    val mobNo = data.getString("mob_no")
                    val walletUUID = data.getString("wallet_uuid")
                    val tpin = data.getInt("tpin")
                    val userName = data.getString("user_name")
                    val securityQuestion = data.getInt("security_question")
                    observerResponse.value = CheckExistPhoneResponse(CheckExistPhoneResponse.Data(email, mobNo, userId, walletUUID, tpin, securityQuestion, userName), errorCode, status)
                } else {
                    observerResponse.value = CheckExistPhoneResponse(null, errorCode, status, errorMessage(errorCode))
                }
            }

            private fun getCheckExistFullResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val errorCode = json.getInt("error_code")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val user_id = data.getString("user_id")
                    val first_name = data.getString("first_name")
                    val last_name = data.getString("last_name")
                    val email = data.getString("email")
                    val address = data.getString("address")
                    val state = data.getString("state")
                    val country = data.getString("country")
                    val pin_code = data.getString("pin_code")
                    val country_code = data.getString("country_code")
                    val mob_no = data.getString("mob_no")
                    val avatar_url = data.getString("avatar_url")
                    observerResponse.value = CheckUserFullResponse(status, errorCode, "", CheckUserFullResponse.Data(user_id, first_name, last_name, email, address, state, country, pin_code, country_code, mob_no, avatar_url))
                } else {
                    observerResponse.value = CheckUserFullResponse(status, errorCode, errorMessage(errorCode), null)
                }
            }
        })
    }
}