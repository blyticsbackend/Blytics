package com.nbt.blytics.modules.userprofile

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.UpdateAvatarResponse
import com.nbt.blytics.modules.userprofile.models.UpdateProfileResponse
import com.nbt.blytics.utils.Constants
import okhttp3.MediaType
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

class UserProfileViewModel(application: Application) : BaseViewModel(application) {

    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    fun getAvatars(){
        val request = service.avatarImage()
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getAvatarResponse(response.body()!!.string())

                }catch (ex:Exception){
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }
            fun getAvatarResponse(res:String){
                val json = JSONObject(res)
                val status = json.getString("status")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataArray = json.getJSONArray("data")
                    val dataList = mutableListOf<AvatarModel.Data>()
                    for(i in 0 until dataArray.length()){
                        dataArray.getJSONObject(i).apply {
                            val id = getInt("id")
                            val image = getString("image")
                            val data = AvatarModel.Data(id, image)
                            dataList.add(data)
                        }

                    }
                    observerResponse.value = AvatarModel(status ,dataList)

                }else{
                    observerResponse.value = AvatarModel(status ,null)
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
            val avatarImage: MultipartBody.Part = MultipartBody.Part.createFormData(
                "avatar_image",
                "avatar.${avatar_image.extension}",
                requestFile
            )
            service.updateUserAvatarByFile(userID, avatarImage, userToken)
        } else {
            service.updateUserAvatarByID(userID, avatarId, userToken)

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
                    val avatarDefault = dataObj.getString("avatar_default")
                    val imageUrl = dataObj.getString("image_url")
                    val uploaded = dataObj.getString("updated")
                    Log.d("uploadedIMage===", res)
                    val data = UpdateAvatarResponse.Data(avatarDefault, imageUrl,uploaded)
                    observerResponse.value = UpdateAvatarResponse(status, message, data)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = UpdateAvatarResponse(status, message, null, errorCode)
                }
            }
        })
    }

    fun updateProfileInfo(model:Any){
        val request = service.updateProfileInfo(model)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getProfileUpdateResponse(response.body()!!.string())
                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

           fun getProfileUpdateResponse(res:String) {
               val json = JSONObject(res)
               val status = json.getString("status")
               val message = json.getString("message")
               if (status.equals(Constants.Status.SUCCESS.name, true)) {
                  observerResponse.value= UpdateProfileResponse(status,message)
               }else{
                   observerResponse.value= UpdateProfileResponse(status,message)
               }
           }
        }
        )
    }
}