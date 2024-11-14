package com.nbt.blytics.modules.payment

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nbt.blytics.api.RetrofitFactory
import com.nbt.blytics.base.BaseViewModel
import com.nbt.blytics.modules.acinfo.AcInfoRequest
import com.nbt.blytics.modules.acinfo.AcInfoResponse
import com.nbt.blytics.modules.allaccount.SendAccountData
import com.nbt.blytics.modules.home.BalanceRequest
import com.nbt.blytics.modules.home.BalanceResponse
import com.nbt.blytics.modules.home.WalletAccountModel
import com.nbt.blytics.modules.notification.AllNotificationResponse
import com.nbt.blytics.modules.notification.NotificationListRequest
import com.nbt.blytics.modules.payment.models.UserProfileInfoRequest
import com.nbt.blytics.modules.payment.models.UserProfileInfoResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentViewModel(application: Application) : BaseViewModel(application) {
    var service = RetrofitFactory.getApiService()
    var observerResponse = MutableLiveData<Any>()
    var walletAccountList = mutableListOf<WalletAccountModel.WalletAccountData>()

    fun getAllAc(acInfoRequest: AcInfoRequest) {
        val request: Call<ResponseBody> = service.getAllAc(acInfoRequest)
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
                val data = json.getJSONArray("data")
                val list = mutableListOf<AcInfoResponse.Data>()
                for (i in 0 until data.length()) {
                    val obj= data.getJSONObject(i)
                    val acc_no= obj.getString("acc_no")
                    val acc_type= obj.getString("acc_type")
                    val amount= obj.getString("amount")
                    val bank_code= obj.getString("bank_code")
                    val acc_uuid= obj.getString("acc_uuid")

                    var create_by =""
                    var create_for =""
                    var relation =""
                    var acc_holder_name=""
                    var acc_id:String=""

                    if(acc_type.equals("linked", true)){
                        create_by= obj.getString("created_by")
                        create_for= obj.getString("created_for")
                        relation= obj.getString("relation")
                        acc_id =obj.getString("acc_id")

                    }else{
                        acc_holder_name= obj.getString("acc_holder_name")

                    }
                    list.add(
                        AcInfoResponse.Data(
                            acc_holder_name = acc_holder_name,
                            acc_no = acc_no,
                            acc_type = acc_type,
                            amount = amount,
                            bank_code = bank_code,
                            create_by = create_by,
                            create_for = create_for,
                            relation = relation,
                            acc_uuid = acc_uuid,
                            accid = acc_id
                        ))
                }
                observerResponse.value = AcInfoResponse (
                    status, list
                )

            }
        })
    }


    fun getAllAccount(userId: String, token: String, acc_type: String) {
        val sendAccountData = SendAccountData(userId, token, acc_type)
        val request = service.getAccount(sendAccountData)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    getAccountData(response.body()!!.string())

                } catch (e: Exception) {
                    observerResponse.value = FailResponse("", "")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = t
            }
            private fun getAccountData(res: String) {
                val jsonObject = JSONObject(res)
                val status = jsonObject.getString("status")
                walletAccountList.clear()
                if (status.equals("Success")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val acc_no = jsonArray.getJSONObject(i).getLong("acc_no").toString()
                        val uuid = jsonArray.getJSONObject(i).getString("uuid")
                        val active = jsonArray.getJSONObject(i).getBoolean("active")

                        walletAccountList.add(
                            WalletAccountModel.WalletAccountData(
                                acc_no,
                                uuid,
                                active
                            )
                        )
                    }
                    observerResponse.value = WalletAccountModel(walletAccountList, status)


                } else {
                    observerResponse.value = FailResponse("", "")
                }

            }
        })


    }
    fun getUserProfileInfo(userId: String, userToken: String) {
        val userProfileInfoRequest = UserProfileInfoRequest(userId, userToken)
        val request = service.userProfileInfo(userProfileInfoRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
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
                    val userId = data.getString("user_id")
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
                    val counrtyCode = data.getString("country_code")

                    // val profileStatus = data.getString("profile_status")
                    val avatar = data.getJSONObject("avatar")
                    val defaultAvatar = avatar.getString("default_avatar")
                    val avatarImage = avatar.getString("avatar_image")
                    val updated = avatar.getString("updated")
                    val sq = data.getJSONArray("security_question")
                    val securityQuesList= mutableListOf<UserProfileInfoResponse.Data.SQ>()
                    for(i in 0..sq.length()){
                        val obj= sq.getJSONObject(i)
                        val ques_no =obj.getString("ques_no")
                        val ans = obj.getString("ans")
                        val hint = obj.getString("hint")
                        val ques = obj.getString("ques")
                        val securityQuestion = UserProfileInfoResponse.Data.SQ(
                            ques_no, ans, hint, ques
                        )
                        securityQuesList.add(securityQuestion)
                    }



                    observerResponse.value = UserProfileInfoResponse(
                        status, UserProfileInfoResponse.Data(
                            userId = userId,
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
                            profileStatus = "",
                            avatar = UserProfileInfoResponse.Data.Avatar(
                                defaultAvatar = defaultAvatar,
                                avatarImage = avatarImage,
                                updated=  updated
                            ),
                            firstName = firstName,
                            lastName = lastName,
                            dob = dob,
                            state = state,
                            pincode = pinCode,
                            countryCode = counrtyCode,
                            securityQuestion=  securityQuesList

                        ),


                        )
                } else {
                    val errorCode = json.getString("error_code")
                    val message = json.getString("message")
                    observerResponse.value =
                        UserProfileInfoResponse(status, null, message, errorCode)
                }

            }
        })

    }

    fun transactionHistory(transactionRequest: TransactionRequest) {

        val request = service.transactionHistory(transactionRequest)
        request.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                try {

                    getTransactionResponse(response.body()!!.string())

                } catch (ex: Exception) {
                    observerResponse.value = FailResponse("", ex.message.toString())
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                observerResponse.value = FailResponse("", t.message.toString())
            }

            fun getTransactionResponse(res: String) {
                val json = JSONObject(res)
                val status = json.getString("status")
                val data = json.getJSONObject("data")
                val totalTxn = data.getString("total_txn")
                val account_no = data.getString("account_no")
                val data_remaining = data.getString("data_remaining")
                val txn_list = data.getJSONArray("txn_list")
                val listTxn = mutableListOf<TransactionResponse.Data.Txn>()
                for(i in 0 until txn_list.length()){
                    val obj = txn_list.getJSONObject(i)
                    val type= obj.getString("type")
                    val uuid= obj.getString("uuid")
                    val status =obj.getString("status")
                    val user_name= obj.getString("user_name")
                    val amount= obj.getString("amount")
                    val currency= obj.getString("currency")
                    val txn_id= obj.getString("txn_id")
                    val date= obj.getString("date")
                    val reference= obj.getString("reference")
                    var mob_no =""
                    var user_id=""
                    var user_image=obj.getString("image_url")
                    val bank_type = obj.getString("bank_type")
                    if(bank_type.equals("Internal",true)){
                        mob_no= obj.getString("mob_no")
                        user_id= obj.getString("user_id")

                    }
                    var bank_acc =""
                    var bank_code =""
                    if(bank_type.equals("External",true)){
                        bank_acc= obj.getString("bank_acc")
                        bank_code= obj.getString("bank_code")
                    }
                    listTxn.add(
                        TransactionResponse.Data.Txn(
                            type = type,
                            uuid = uuid,
                            status =status,
                            userImage = user_image,
                            userName = user_name,
                            userId = user_id,
                            amount = amount,
                            currency = currency,
                            txnId = txn_id,
                            date = date,
                            mobNo = mob_no,
                            bankType = bank_type,
                            bank_acc = bank_acc,
                            bank_code = bank_code,
                            reference = reference
                        ))

                }
                val txnData= TransactionResponse.Data(
                    totalTxn = totalTxn,
                    accountNo = account_no,
                    dataRemaining = data_remaining,
                    txnList = listTxn
                )

                // val message = json.getString("message")
                if (status.equals(Constants.Status.SUCCESS.name, true)) {
                    observerResponse.value = TransactionResponse(txnData,status)
                }else{
                    val message = json.getString("message")
                    observerResponse.value = FailResponse("", message)
                }
            }
        })
    }

    fun getBalance(userId: String, token: String, accUUID: String) {
        val balanceRequest = BalanceRequest(
            accUUID,
            userId,
            token
        )
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