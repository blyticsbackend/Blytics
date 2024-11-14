package com.nbt.blytics.modules.setpassword

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.setpassword.models.AddAvatarResponse
import com.nbt.blytics.modules.setpassword.models.SignUpRequest
import com.nbt.blytics.modules.setpassword.models.SignUpResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SetPasswordViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()


    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        dob: String,
        gender: Int,
        pinCode: String,
        address: String,
        country: String,
        state: String,
        password: String,
        mobNo: String,
        mobVerified: Boolean,
        deviceToken: String, countryCode: String, context: Context
    ) {
        (context as UserActivity).apply {
            val deviceData =
                SignUpRequest.DeviceData(deviceModel, version, apiLevel, fingerPrint, deviceId)

            val userPhone =
                SignUpRequest.UserPhone(mobNo, mobVerified, deviceToken, deviceData, countryCode)

            val signUpRequest = SignUpRequest(
                firstName,
                lastName,
                email,
                dob,
                gender,
                pinCode,
                address,
                country,
                state,
                password,
                userPhone
            )
            val request = service.signUp(signUpRequest)
            request.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    try {
                            getSignUpResponse(response.body()!!.string())

                    } catch (ex: Exception) {
                        observerResponse.value = FailResponse("", ex.message.toString())
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    observerResponse.value = FailResponse("", t.message.toString())
                }

                private fun getSignUpResponse(res: String) {
                    val json = JSONObject(res)
                    val status = json.getString("status")
                    if (status.equals(Constants.Status.SUCCESS.name, true)) {
                        val message = json.getString("message")
                        val dataObj = json.getJSONObject("data")
                        val userId = dataObj.getString("user_id")
                        val userToken = dataObj.getString("user_token")
                        val walletUuid = dataObj.getString("wallet_uuid")
                        val data = SignUpResponse.Data(userId, userToken,walletUuid)
                        observerResponse.value = SignUpResponse(data, message, status)
                    } else {
                        val errorCode = json.getString("error_code")
                        observerResponse.value = SignUpResponse(null, status, errorCode)
                    }
                }
            })

        }
    }


    fun addAvatar(user_id: String, avatar_id: String, avatar_image: File?, user_token: String) {
        val userID = user_id.toRequestBody("text/plain".toMediaType())
        val avatarId = avatar_id.toRequestBody("text/plain".toMediaType())
        val userToken = user_token.toRequestBody("text/plain".toMediaType())


        val request: Call<ResponseBody> = if (avatar_image != null) {
            val requestFile: RequestBody = avatar_image.asRequestBody("image/*".toMediaTypeOrNull())
            val avatarImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                "avatar_image",
                "avatar.${avatar_image.extension}",
                requestFile
            )
            service.addUserAvatarByFile(userID, avatarImage, userToken)
        } else {
            service.addUserAvatarByID(userID, avatarId, userToken)
        }

        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                        getAddAvatarResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getAddAvatarResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataObj = json.getJSONObject("data")
                    val imageUrl = dataObj.getString("image_url")
                    val avatarDefault = dataObj.getString("avatar_default")
                    val updated = dataObj.getString("updated")
                    val userId = dataObj.getString("user_id")
                    Log.d("uploadedIMage===", res)
                    val data = AddAvatarResponse.Data(avatarDefault,updated, imageUrl, userId)
                    observerResponse.value = AddAvatarResponse(status, message, data)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = AddAvatarResponse(status, message, null, errorCode)
                }
            }
        })


    }


}