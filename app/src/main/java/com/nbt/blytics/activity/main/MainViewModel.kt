package com.nbt.blytics.activity.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.home.BalanceRequest
import com.nbt.blytics.modules.home.BalanceResponse
import com.nbt.blytics.modules.payment.models.UserProfileInfoRequest
import com.nbt.blytics.modules.payment.models.UserProfileInfoResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.UpdateAvatarResponse
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

/**
 * Created by Nbt on 26-07-2021
 */
class MainViewModel(application: Application) : BaseViewModel(application) {
    private var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun logOut(logoutRequest: LogoutRequest) {
        val request = service.logOut(logoutRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getLogoutRes(response.body()!!.string())
                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }

            fun getLogoutRes(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = LogoutResponse(status, "")
                } else {
                    val message = json.getString("message")
                    observerResponse.value = LogoutResponse(status, message)
                }
            }
        })
    }

    fun getUserProfileInfo(userId: String, userToken: String) {
        val userProfileInfoRequest = UserProfileInfoRequest(userId, userToken)
        val request = service.userProfileInfo(userProfileInfoRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getUserProfileResponse(response.body()!!.string())
                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getUserProfileResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.contains(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userid = data.getString("user_id")
                    val mobNo = data.getString("mob_no")
                    val mobVerified = data.getBoolean("mob_verified")
                    val email = data.getString("email")
                    val emailVerified = data.getBoolean("email_verified")
                    val address = data.getString("address")
                    val country = data.getString("country")
                    val walletUuid = data.getString("wallet_uuid")
                    val bvn = data.getString("bvn")
                    val bvnVerified = data.getBoolean("bvn_verified")
                    val docVerified = data.getBoolean("doc_verified")
                    val firstName = data.getString("first_name")
                    val lastName = data.getString("last_name")
                    val state = data.getString("state")
                    val pinCode = data.getString("pincode")
                    val dob = data.getString("dob")
                    val countryCode = data.getString("country_code")
                    val profileStatus = data.getString("profile_status")
                    val avatar = data.getJSONObject("avatar")
                    val defaultAvatar = avatar.getString("default_avatar")
                    val avatarImage = avatar.getString("avatar_image")
                    val updated = avatar.getString("updated")
                    val sq = data.getJSONArray("security_question")
                    val securityQuesList = mutableListOf<UserProfileInfoResponse.Data.SQ>()
                    for (i in 0 until sq.length()) {
                        val obj = sq.getJSONObject(i)
                        val ques_no = obj.getString("ques_no")
                        val ans = obj.getString("ans")
                        val hint = obj.getString("hint")
                        val ques = obj.getString("ques")
                        val securityQuestion = UserProfileInfoResponse.Data.SQ(ques_no, ans, hint, ques)
                        securityQuesList.add(securityQuestion)
                    }
                    observerResponse.value = UserProfileInfoResponse(
                        status,
                        UserProfileInfoResponse.Data(
                            userId = userid,
                            mobNo = mobNo,
                            mobVerified = mobVerified,
                            email = email,
                            emailVerified = emailVerified,
                            address = address,
                            country = country,
                            walletUuid = walletUuid,
                            bvn = bvn,
                            bvnVerified = bvnVerified,
                            docVerified = docVerified,
                            profileStatus = profileStatus,
                            avatar = UserProfileInfoResponse.Data.Avatar(defaultAvatar = defaultAvatar, avatarImage = avatarImage, updated = updated),
                            firstName = firstName,
                            lastName = lastName,
                            dob = dob,
                            state = state,
                            pincode = pinCode,
                            countryCode = countryCode,
                            securityQuestion = securityQuesList
                        ),)
                } else {
                    val errorCode = json.getString("error_code")
                    val message = json.getString("message")
                    observerResponse.value = UserProfileInfoResponse(status, null, message, errorCode)
                }
            }
        })
    }

    fun getUnReadNotification(unreadNotification: UnReadNotification) {
        val request = service.unReadCount(unreadNotification)
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
                    val unread_notifications = json.getInt("unread_notifications")
                    observerResponse.value = UnReadNotiRespnose(status, unread_notifications)
                } else {
                    observerResponse.value = UnReadNotiRespnose(status)
                }
            }
        })
    }

    fun updateAvatar(user_id: String, avatar_id: String, avatar_image: File?, user_token: String) {
        val userID = user_id.toRequestBody("text/plain".toMediaType())
        val avatarId = avatar_id.toRequestBody("text/plain".toMediaType())
        val userToken = user_token.toRequestBody("text/plain".toMediaType())
        val request: Call<ResponseBody> = if (avatar_image != null) {
            val requestFile: RequestBody = avatar_image.asRequestBody("image/*".toMediaTypeOrNull())
            val avatarImage: MultipartBody.Part = MultipartBody.Part.createFormData("avatar_image", "avatar.${avatar_image.extension}", requestFile)
            service.updateUserAvatarByFile(userID, avatarImage, userToken)
        } else {
            service.updateUserAvatarByID(userID, avatarId, userToken)
        }

        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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
                    val avatarDefault = dataObj.getString("avatar_default")
                    val imageUrl = dataObj.getString("image_url")
                    val uploaded = dataObj.getString("updated")
                    Log.d("uploadedIMage===", res)
                    val data = UpdateAvatarResponse.Data(avatarDefault, imageUrl, uploaded)
                    observerResponse.value = UpdateAvatarResponse(status, message, data)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = UpdateAvatarResponse(status, message, null, errorCode)
                }
            }
        })
    }

    fun getAvatars() {
        val request = service.avatarImage()
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getAvatarResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getAvatarResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataArray = json.getJSONArray("data")
                    val dataList = mutableListOf<AvatarModel.Data>()
                    for (i in 0 until dataArray.length()) {
                        dataArray.getJSONObject(i).apply {
                            val id = getInt("id")
                            val image = getString("image")
                            val data = AvatarModel.Data(id, image)
                            dataList.add(data)
                        }
                    }
                    observerResponse.value = AvatarModel(status, dataList)
                } else {
                    observerResponse.value = AvatarModel(status, null)
                }
            }
        })
    }

    fun userBlock(userInfo: UserBlockRequest) {
        val request = service.blockedUser(userInfo)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }

            fun getResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val message = json.getString("message")
                    val data = json.getJSONObject("data")
                    val user_id = data.getString("user_id")
                    val user_token = data.getString("user_token")
                    val wallet_uuid = data.getString("wallet_uuid")
                    val country_code = data.getString("country_code")
                    val mobile_no = data.getString("mobile_no")
                    val is_primary = data.getBoolean("is_primary")
                    val is_local = data.getBoolean("is_local")
                    val is_block = data.getBoolean("is_block")
                    val device_changed = data.getBoolean("device_changed")
                    observerResponse.value = UserBlockResponse(
                        status, message, false, false, UserBlockResponse.Data(
                            user_id = user_id,
                            user_token = user_token,
                            wallet_uuid = wallet_uuid,
                            country_code = country_code,
                            mobile_no = mobile_no,
                            is_primary = is_primary,
                            is_local = is_local,
                            is_block = is_block,
                            device_changed = device_changed
                        )
                    )
                } else {
                    // val is_block = json.getBoolean("is_block")
                    val authenticate_user = json.getBoolean("authenticate_user")
                    val message = json.getString("message")
                    observerResponse.value = UserBlockResponse(status, message, false, authenticate_user, null)
                }
            }
        })
    }

    fun getBalance(userId: String, token: String, accUUID: String) {
        val balanceRequest = BalanceRequest(accUUID, userId, token)
        val request = service.balance(balanceRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getBalace(response.body()!!.string())
                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }

            fun getBalace(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                if (status.equals("Success")) {
                    val data = jsonObject.getJSONObject("data")
                    val balance = data.getString("balance")
                    observerResponse.value = BalanceResponse(BalanceResponse.Data(balance), status)
                } else {
                    val message = jsonObject.getString("message")
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }
}