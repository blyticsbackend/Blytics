package com.nbt.blytics.api


import com.nbt.blytics.activity.main.LogoutRequest
import com.nbt.blytics.activity.main.UnReadNotification
import com.nbt.blytics.activity.main.UserBlockRequest
import com.nbt.blytics.activity.qrcode.QrRequest
import com.nbt.blytics.modules.acinfo.AcInfoRequest
import com.nbt.blytics.modules.actobank.CheckAcRequest
import com.nbt.blytics.modules.actobank.SendMoneyBankRequest
import com.nbt.blytics.modules.addaccount.models.CreateAcRequest
import com.nbt.blytics.modules.addaccount.models.CreateCurrentAcRequest
import com.nbt.blytics.modules.allaccount.SendAccountData
import com.nbt.blytics.modules.allaccount.UpdateDefaultAcc
import com.nbt.blytics.modules.chagnetpin.models.ChangeTipRequest
import com.nbt.blytics.modules.chagnetpin.models.CheckTpin
import com.nbt.blytics.modules.changepassword.model.ChangePasswordRequest
import com.nbt.blytics.modules.chargesdetails.UserChargeRequest
import com.nbt.blytics.modules.home.BalanceRequest
import com.nbt.blytics.modules.home.SpentRequest
import com.nbt.blytics.modules.linkedac.manageac.GetManageLinkedRequest
import com.nbt.blytics.modules.linkedac.manageac.GetManageRequest
import com.nbt.blytics.modules.linkedac.manageac.ManageRequest
import com.nbt.blytics.modules.linkedac.manageac.ManageRequestLinked
import com.nbt.blytics.modules.notification.NotificationListRequest
import com.nbt.blytics.modules.otp.model.CheckValidTpinRequest
import com.nbt.blytics.modules.otp.model.EmailOtpRequest
import com.nbt.blytics.modules.otp.model.LoginWithMobileRequest
import com.nbt.blytics.modules.payamount.model.CheckValidTnxTpinRequest
import com.nbt.blytics.modules.payamount.model.RequestMoney
import com.nbt.blytics.modules.payamount.model.SendMoneyRequest
import com.nbt.blytics.modules.payee.payment.ApprovalRequest
import com.nbt.blytics.modules.payee.payment.RequestMoneyReq
import com.nbt.blytics.modules.payee.payment.TnxListRequest
import com.nbt.blytics.modules.payee.schedule.DeleteScheduleRequest
import com.nbt.blytics.modules.payee.schedule.RecentScheduleRequest
import com.nbt.blytics.modules.payee.schedulecreate.ExternalSchedule
import com.nbt.blytics.modules.payee.schedulecreate.InternalSchedule
import com.nbt.blytics.modules.payee.schedulecreate.SelfSchedule
import com.nbt.blytics.modules.payment.manageac.DefaultAccRequest
import com.nbt.blytics.modules.payment.manageac.DefaultWalletRequest
import com.nbt.blytics.modules.payment.manageac.StartTrackerRequest
import com.nbt.blytics.modules.payment.manageac.UpdateManageConfig
import com.nbt.blytics.modules.payment.models.UserProfileInfoRequest
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest
import com.nbt.blytics.modules.phoneregistation.models.CheckExistRequest2
import com.nbt.blytics.modules.profile.UpdateMobileRequest
import com.nbt.blytics.modules.securityQes.ChangeTpiRequest
import com.nbt.blytics.modules.securityQes.PasswordUpdateChangeRequest
import com.nbt.blytics.modules.selftransfer.SelfTxnRequest
import com.nbt.blytics.modules.setpassword.models.SignUpRequest
import com.nbt.blytics.modules.signin.model.LoginRequest
import com.nbt.blytics.modules.signupprofile.models.BvnCheck
import com.nbt.blytics.modules.signupprofile.models.LInkedAcRequest
import com.nbt.blytics.modules.squpdate.model.UpdateSqlRequest
import com.nbt.blytics.modules.sqverify.models.SQVerifyRequest
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.io.File

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/user/log-in/")
    fun login(
        @Body body: LoginRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/sign_up/")
    fun signUp(
        @Body body: SignUpRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/linked-user")
    fun linkedAcCreate(
        @Body body: LInkedAcRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/send-otp")
    fun sendEmailOTP(
        @Body body: EmailOtpRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/change_password/")
    fun changePassword(
        @Body body: ChangePasswordRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/check-update-password/")
    fun changeUpdatePassword(
        @Body body: PasswordUpdateChangeRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/loginwithotp")
    fun loginWithMobile(
        @Body body: LoginWithMobileRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/txn-filter")
    fun transactionHistory(
        @Body body: TransactionRequest
    ): Call<ResponseBody>

   /* @GET("transaction/home_items/")
    fun getHomeTransactionHistory(
        @Query("user_id") userId: Int
    ): Call<ResponseBody>*/

    @Headers("Content-Type: application/json")
    @PUT("/transaction/user-charges")
    fun userCharge(
        @Body body: UserChargeRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/txn-list")
    fun transactionList(
        @Body body: TnxListRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/user/notification")
    fun getAllNotification(
        @Body body: NotificationListRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/get-all-accounts")
    fun getAllAc(
        @Body body: AcInfoRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/balance")
    fun balance(
        @Body body: BalanceRequest
    ): Call<ResponseBody>
    @Headers("Content-Type: application/json")
    @POST("/transaction/received-spent-amount")
    fun spentAmount(
        @Body body: SpentRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/user/change-mob-no")
    fun changeMobileNumber(
        @Body body: UpdateMobileRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/account/config-account")
    fun configAcc(
        @Body body: ManageRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/account/config-account")
    fun configAccLinked(
        @Body body: ManageRequestLinked
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/account/config-account")
    fun configAcc(
        @Body body: UpdateManageConfig
    ): Call<ResponseBody>

/*    @Headers("Content-Type: application/json")
    @PUT("/account/config-account")
    fun getConfigAcc(
        @Body body: GetManageRequest
    ): Call<ResponseBody>*/


    @Headers("Content-Type: application/json")
    @POST("/account/get_config_details")
    fun getConfigAcc(
        @Body body: GetManageRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/account/get_config_details")
    fun getConfigLinkedAcc(
        @Body body: GetManageLinkedRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/start-tracker")
    fun startTracker(
        @Body body: StartTrackerRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/get-user-security-question")
    fun getUserSecurityQuestion(
        @Body body: SQVerifyRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/change-wallet-tpin")
    fun changeTpin(
        @Body body: ChangeTipRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/wallet-tpin")
    fun checkValidTpi(
        @Body body: CheckValidTpinRequest
    ): Call<ResponseBody>



    @Headers("Content-Type: application/json")
    @POST("/account/tpin-change")
    fun changeSavingCurrentTpin(
        @Body body: ChangeTpiRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/check_t_pin")
    fun checkTransactionTpi(
        @Body body: CheckValidTnxTpinRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/request-money")
    fun requestMoney(
        @Body body: RequestMoney
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/internal_schedule_payment")
    fun internalSchedule(
        @Body body: InternalSchedule
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/self-schedule")
    fun selfSchedule(
        @Body body: SelfSchedule
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/external_schedule_payment")
    fun externalSchedule(
        @Body body: ExternalSchedule
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT ("/transaction/internal_schedule_payment")
    fun editInternalSchedule(
        @Body body: InternalSchedule
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT ("/transaction/internal_schedule_payment")
    fun editSelfSchedule(
        @Body body: SelfSchedule
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/transaction/external_schedule_payment")
    fun editExternalSchedule(
        @Body body: ExternalSchedule
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/recent-schedule")
    fun recentSchedule(
        @Body body: RecentScheduleRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PATCH ("/transaction/internal_schedule_payment")
    fun deleteInternalSchedule(
        @Body body: DeleteScheduleRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PATCH ("/transaction/external_schedule_payment")
    fun deleteExternalSchedule(
        @Body body: DeleteScheduleRequest
    ): Call<ResponseBody>







    @Headers("Content-Type: application/json")
    @PATCH("/transaction/request-money")
    fun allRequestMoney(
        @Body body: RequestMoneyReq
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @PUT("/transaction/request-money")
    fun approvalRequest(
        @Body body: ApprovalRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/internal-transaction")
    fun sendSelfMoney(
        @Body body: SendMoneyRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/user/check_exist/")
    fun userCheckExist(
        @Body body: CheckExistRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/user/check_exist/")
    fun userCheckExist2(
        @Body body: CheckExistRequest2
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/get-user-profile")
    fun userProfileInfo(
        @Body body: UserProfileInfoRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/blocked-user")
    fun blockedUser(
        @Body body: UserBlockRequest
    ): Call<ResponseBody>



    @Headers("Content-Type: application/json")
    @POST("/user/log-out/")
    fun logOut(
        @Body body: LogoutRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/user/user-security-question")
    fun updateSQ(
        @Body body: UpdateSqlRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("/user/update_user/")
    fun updateProfileInfo(
        @Body body: Any
    ): Call<ResponseBody>

    @Multipart
    @PUT("/user/user_verification/")
    fun updateDocument(
        @Part("user_id") userId: RequestBody,
        @Part identityProofDocument: MultipartBody.Part,
    ): Call<ResponseBody>

    @Multipart
    @PUT("/user/user_verification/")
    fun updateAddress(
        @Part("user_id") userId: RequestBody,
        @Part addressProofDocument: MultipartBody.Part
    ): Call<ResponseBody>

    @Multipart
    @POST("/user/register-bvn")
    fun registerBVN(
        @Part("user_id") user_id: RequestBody,
        @Part("bvn_number") bvn_number: RequestBody,
        @Part("user_token") user_token: RequestBody,
        @Part("identity_proof") docType1: RequestBody,
        @Part("address_proof") docType2: RequestBody,
        @Part document1: MultipartBody.Part?,
        @Part document2: MultipartBody.Part?
    ): Call<ResponseBody>

    @Multipart
    @POST("/user/register-bvn")
    fun registerBVNLinked(
        @Part("user_id") user_id: RequestBody,
        @Part("bvn_number") bvn_number: RequestBody,
        @Part("user_token") user_token: RequestBody,
        @Part("identity_proof") docType1: RequestBody,
        @Part("address_proof") docType2: RequestBody,
        @Part document1: MultipartBody.Part?,
        @Part document2: MultipartBody.Part?,
        @Part("linked_user_id")linked_user_id:RequestBody
    ): Call<ResponseBody>


    @Multipart
    @POST("/user/update-bvn/")
    fun updateBVN(
        @Part("user_id") user_id: RequestBody,
        @Part("bvn_number") bvn_number: RequestBody,
        @Part("user_token") user_token: RequestBody,
        @Part("identity_proof") docType1: RequestBody,
        @Part("address_proof") docType2: RequestBody,
        @Part document1: MultipartBody.Part?,
        @Part document2: MultipartBody.Part?
    ): Call<ResponseBody>

    @Multipart
    @POST("/user/user-avatar")
    fun addUserAvatarByID(
        @Part("user_id") user_id: RequestBody,
        @Part("avatar_id") avatar_id: RequestBody,
        @Part("user_token") user_token: RequestBody
    ): Call<ResponseBody>

    @Multipart
    @POST("/user/user-avatar")
    fun addUserAvatarByFile(
        @Part("user_id") user_id: RequestBody,
        @Part avatarImage: MultipartBody.Part,
        @Part("user_token") user_token: RequestBody
    ): Call<ResponseBody>


    @Multipart
    @POST("/user/user-avatar")
    fun updateUserAvatarByID(
        @Part("user_id") user_id: RequestBody,
        @Part("avatar_id") avatar_id: RequestBody,
        @Part("user_token") user_token: RequestBody
    ): Call<ResponseBody>

    @Multipart
    @POST("/user/user-avatar")
    fun updateUserAvatarByFile(
        @Part("user_id") user_id: RequestBody,
        @Part avatarImage: MultipartBody.Part,
        @Part("user_token") user_token: RequestBody

    ): Call<ResponseBody>


    @GET("/user/avatar-image")
    fun avatarImage(
    ): Call<ResponseBody>


    @GET("/user/get-user-security-question")
    fun getUserSq(
    ): Call<ResponseBody>


    @GET("/user/security-question")
    fun getSQ(
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/user/check-pin-sq")
    fun checkPin(
        @Body body: CheckTpin
    ): Call<ResponseBody>

     @Headers("Content-Type: application/json")
    @POST("/user/check-bvn")
    fun checkBVN(
        @Body body: BvnCheck
    ): Call<ResponseBody>



    @Headers("Content-Type: application/json")
    @POST("user/unread-count")
    fun unReadCount(
        @Body body: UnReadNotification
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/account/create-saving-account")
    fun createSavingAc(
        @Body body: CreateAcRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/create-current-account")
    fun createCurrentAc(
        @Body body: CreateCurrentAcRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/get-accounts")
    fun getAccount(
        @Body body: SendAccountData
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/send_money_bank")
    fun sendMoneyBank(
        @Body body: SendMoneyBankRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/check_acc")
    fun checkAcc(
        @Body body: CheckAcRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/transaction/qrcode")
    fun getQrCode(
        @Body body: QrRequest
    ): Call<ResponseBody>

//http://15.206.71.3:3000/account/default-account
    @Headers("Content-Type: application/json")
    @PUT("/account/default-account")
    fun getDefaultAccount(
        @Body body: DefaultAccRequest
    ): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("/account/check-default_acc")
    fun checkDefaultWalletAc(
        @Body body: DefaultWalletRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @POST("/transaction/self-transfer")
    fun sendSelfMoney(
        @Body body: SelfTxnRequest
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @PUT("/account/create-saving-account")
    fun updateDefaultAccount(
        @Body body: UpdateDefaultAcc
    ): Call<ResponseBody>


    @Headers("Content-Type: application/json")
    @PUT("/account/create-current-account")
    fun updateDefaultCurrentAccount(
        @Body body: UpdateDefaultAcc
    ): Call<ResponseBody>



    /*@FormUrlEncoded
    @POST("api/login_controller/register_user")
    fun registration(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phone_number") phone_number: String,
        @Field("device_id") device_id: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/login_controller/check_user_existence")
    fun checkUser(
        @Field("user_email_phone") user_email_phone: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/login_controller/change_forget_password")
    fun updatePassword(
        @Field("phone_no") phone_no: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @GET("api/login_controller/get_user_detail")
    fun getProfileData(
        @Query("user_id") user_id: String
    ): Call<ResponseBody>


    // Get Categories, Types and services
    @FormUrlEncoded
    @POST("api/store_controller/get_CTS_detail")
    fun getCTSDetail(
        @Field("user_id") user_id: String,
        @Field("secure_key") secure_key: String
    ): Call<ResponseBody>

    @Multipart
    @POST("api/Login_controller/update_user_profile")
    fun updateProfile(
        @Part("user_id") user_id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("country") country: RequestBody

    ): Call<ResponseBody>

    @Multipart
    @POST("api/Login_controller/update_user_profile")
    fun updateProfile(
        @Part("user_id") user_id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("address") address: RequestBody,
        @Part("city") city: RequestBody,
        @Part("country") country: RequestBody,
        @Part profile_pic: MultipartBody.Part
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("api/store_controller/get_store")
    fun getStores(
        @Field("search") search: String,
        @Field("type_id") type_id: String,
        @Field("service_id") service_id: String,
        @Field("categories_id") categories_id: String,
        @Field("min_cost") min_cost: String,
        @Field("max_cost") max_cost: String,
        @Field("offset") offset: Int,
        @Field("offset_start_from") offset_start_from: Int,
        @Field("delivery_id") delivery_id: String
    ): Call<ResponseBody>

    @GET("api/store_controller/get_top_ten_store")
    fun getTop10(): Call<ResponseBody>


    // single store
    @FormUrlEncoded
    @POST("api/store_controller/get_single_store")
    fun getSingleStore(@Field("store_id") store_id: String): Call<ResponseBody>

    // single store
    @FormUrlEncoded
    @POST("api/store_controller/get_store_rating")
    fun getSingleStoreRating(@Field("store_id") store_id: String): Call<ResponseBody>


    @FormUrlEncoded
    @POST("api/store_controller/Insert_store_rating")
    fun addReview(
        @Field("user_id") user_id: String,
        @Field("store_id") store_id: String,
        @Field("rating") rating: Float,
        @Field("comment") comment: String
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("dokan/user-api/")
    fun getLogin(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("method") method: String = "login"
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("dokan/user-api/")
    fun getRegistration(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("phoneno") phoenno: String,
        @Field("method") method: String = "registration"
    ): Call<ResponseBody>


    @FormUrlEncoded
    @POST("dokan/user-api/")
    fun getHomeData(
        @Field("method") method: String = "home"
    ): Call<ResponseBody>

    *//*  @GET("")
    fun getDetails(

        @Url url: String,NBT
    ): Call<ResponseBody>
    *//*


    @GET("dokan/wp-json/wc/v3/products/{id}")
    fun getDetails(
        @Path("id") id: Int,
        @Query("consumer_key") conkey: String = "ck_f123d52cf9a34c945468d0d09c4e454bfd8418f6",
        @Query("consumer_secret") consecret: String = "cs_2b47d7bbece9e3eaae1df857d801df7077327561",
    ): Call<ResponseBody>


    @GET("dokan/wp-json/wc/v3/products/{id}/variations")
    fun getDetailsVariants(
        @Path("id") id: Int,
        @Query("consumer_key") conkey: String = "ck_f123d52cf9a34c945468d0d09c4e454bfd8418f6",
        @Query("consumer_secret") consecret: String = "cs_2b47d7bbece9e3eaae1df857d801df7077327561",
    ): Call<ResponseBody>

@FormUrlEncoded
    @POST("dokan/user-api/")
    fun getReview(
        @Field("product_id") product_id: Int,
        @Field("page_no") page_no: Int,
        @Field("method") method: String = "get_comments"
    ): Call<ResponseBody>*/

    @FormUrlEncoded
    @POST("/transaction/check_bal_forlinkedacc")
    fun check_balanc_for_Linked_account(
        @Field("user_id") user_id: Int,
        @Field("uuid") uuid: String,
    ): Call<ResponseBody>
}