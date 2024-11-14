package com.nbt.blytics.modules.signupprofile

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.otp.model.EmailOtpRequest
import com.nbt.blytics.modules.otp.model.EmailOtpResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest
import com.nbt.blytics.modules.setpassword.models.AddAvatarResponse
import com.nbt.blytics.modules.setpassword.models.SignUpRequest
import com.nbt.blytics.modules.setpassword.models.SignUpResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.models.*
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

class SignupProfileEditViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()

    fun checkBVN(bvn :String) {
        val bvnCheck= BvnCheck(
            bvn
        )
        val request = service.checkBVN(bvnCheck)
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
                val message = json.getString("message")
                observerResponse.value = BvnCheckResult(status, message)

            }
        })
    }

    fun getAvatars() {
        val request = service.avatarImage()
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
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
                        val data = SignUpResponse.Data(userId, userToken, walletUuid)
                        observerResponse.value = SignUpResponse(data, message, status)
                    } else {
                        val errorCode = json.getString("error_code")
                        val message = json.getString("message")
                        observerResponse.value = SignUpResponse(null, message, status)
                    }
                }
            })

        }
    }

    fun linkedAcCreate(linkAcRequest: LInkedAcRequest) {
        val request = service.linkedAcCreate(linkAcRequest)

        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    getLinkResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            private fun getLinkResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")

                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val dataObj = json.getJSONObject("data")
                    val bvn_verified = json.getBoolean("bvn_verified")
                    if (bvn_verified) {
                        val linkedUserId = dataObj.getInt("linked_user_id")

                        val bvn_number = dataObj.getString("bvn_number")
                        val BVN_message = dataObj.getString("BVN_message")
                        val link_charge = dataObj.getBoolean("link_charge")
                        val chargeable = dataObj.getBoolean("chargeable")
                        observerResponse.value = LinkedAccResponse(
                            status, "", message, linkedUserId, bvn_verified, LinkedAccResponse.Data(
                                link_charge,
                                chargeable,
                                bvn_verified,
                                bvn_number,
                                BVN_message
                            )
                        )
                    } else {
                        val linkedUserId = dataObj.getInt("linked_user_id")
                        observerResponse.value =
                            LinkedAccResponse(status, "", message, linkedUserId, bvn_verified, null)
                    }
                } else {
                    val errorCode = ""//json.getString("error_code")
                    val dataObj = json.getJSONObject("data")

                    val data = LinkedAccResponse.Data(
                        dataObj.getBoolean("link_charge"),
                        dataObj.getBoolean("chargeable"),
                        false,
                        "", ""
                    )
                    observerResponse.value =
                        LinkedAccResponse(status, errorCode, message, -1, false, data)
                }


            }

        })
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
                    val data = AddAvatarResponse.Data(avatarDefault, updated, imageUrl, userId)
                    observerResponse.value = AddAvatarResponse(status, message, data)
                } else {
                    val errorCode = json.getString("error_code")
                    observerResponse.value = AddAvatarResponse(status, message, null, errorCode)
                }
            }
        })


    }


    fun registerBVN(
        user_id: String,
        bvn_number: String,
        user_token: String,
        document1: File?,
        document2: File?,
        linkedUserId: Int = -1,
        isLinkedUser: Boolean
    ) {
        val userID = user_id.toRequestBody("text/plain".toMediaType())
        val bvnNumber = bvn_number.toRequestBody("text/plain".toMediaType())
        val userToken = user_token.toRequestBody("text/plain".toMediaType())
        val idProof = "dl".toRequestBody("text/plain".toMediaType())
        val addressProof = "pan".toRequestBody("text/plain".toMediaType())
        var doc1: MultipartBody.Part? = null
        if (document1 != null) {
            val requestFile: RequestBody = if (document1.extension.equals("pdf", true)) {
                document1.asRequestBody("application/pdf".toMediaTypeOrNull())

            } else {
                document1.asRequestBody("image/*".toMediaTypeOrNull())

            }
            doc1 = MultipartBody.Part.createFormData(
                "identity_proof_document",
                "identity_proof.${document1.extension}",
                requestFile
            )
        }
        var doc2: MultipartBody.Part? = null
        if (document2 != null) {
            val requestFile2: RequestBody = if (document2.extension.equals("pdf", true)) {
                document2.asRequestBody("application/pdf".toMediaTypeOrNull())
            } else {
                document2.asRequestBody("image/*".toMediaTypeOrNull())
            }
            doc2 = MultipartBody.Part.createFormData(
                "address_proof_document",
                "address_proof.${document2.extension}",
                requestFile2
            )
        }
        val request =
            if (isLinkedUser.not()) {
                service.registerBVN(
                    userID,
                    bvnNumber,
                    userToken,
                    idProof,
                    addressProof,
                    doc1,
                    doc2
                )
            } else {
                val linkedID = linkedUserId.toString().toRequestBody("text/plain".toMediaType())
                service.registerBVNLinked(
                    userID,
                    bvnNumber,
                    userToken,
                    idProof,
                    addressProof,
                    doc1,
                    doc2,
                    linkedID
                )
            }
        request.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {
                    getBvnResponse(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", e.message.toString())
                }

            }

            fun getBvnResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    val data = json.getJSONObject("data")
                    val userPermission = data.getString("user_permission")
                    val dataSaved = data.getBoolean("data_saved")
                    val data1 = BvnRegisterResponse.Data(dataSaved, userPermission)
                    observerResponse.value = BvnRegisterResponse(message, status, data1)
                } else {
                    val errorCode = ""// json.getString("error_code")
                    observerResponse.value = BvnRegisterResponse(message, status, null, errorCode)
                }

            }
        }

        )
    }


    fun checkExist(str: String, callingPreview:Boolean=false) {
        val checkExistRequestModel = CheckExistRequest("",str, CheckFor.FULL_DETAILS.ordinal)
        val request = service.userCheckExist(checkExistRequestModel)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {

                    getCheckExistResponse(response.body()!!.string())

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
                    val userName = data.getString("user_name")

                    val tpin = data.getInt("tpin")
                    val securityQuestion = data.getInt("security_question")

                    observerResponse.value = CheckExistPhoneResponse(
                        CheckExistPhoneResponse.Data(
                            email,
                            mobNo,
                            userId,
                            walletUUID,
                            tpin,
                            securityQuestion,
                            userName),
                        errorCode, status, forPreview = callingPreview
                    )

                } else {

                    observerResponse.value = CheckExistPhoneResponse(
                        null,
                        errorCode, status, Constants.errorMessage(errorCode)
                    )
                }
            }

        })
    }

    fun otpEmail(email: String, callingPreview:Boolean =false) {
        val emailOtpRequest = EmailOtpRequest(email)
        val request = service.sendEmailOTP(emailOtpRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getEmailOtpResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getEmailOtpResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = EmailOtpResponse(status, message, callingPreview)
                } else {
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }
}