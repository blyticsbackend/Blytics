package com.nbt.blytics.modules.otp

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.mobileNoUpdateLiveData
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.OtpFragmentBinding
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.otp.model.LoginWithMobileResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.Constants.DEFAULT_COUNTRY_CODE
import com.nbt.blytics.utils.Constants.PHONE_NUMBER
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setTextSpanColor
import com.nbt.blytics.utils.show


class OtpFragment : BaseFragment<OtpFragmentBinding, OtpViewModel>() {


    private val otpViewModel: OtpViewModel by viewModels()
    private lateinit var binding: OtpFragmentBinding
    private lateinit var timer: CountDownTimer
    private var counter = 60
    private var phoneVerificationId: String = ""
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    private var userID: String? = ""
    private var userWalletUUID: String? = ""
    private var isComingFor: String? = ""
    private var phoneNumber: String? = ""
    private var spannableTextColor: Int = -1
    private var  haveTpin:Boolean = false
    private var  haveSQ:Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        counter = 60
        isComingFor = requireArguments().getString(Constants.COMING_FOR)
        phoneNumber = requireArguments().getString(Constants.PHONE_NUMBER)
        userID = requireArguments().getString(Constants.USER_ID)
        userWalletUUID = requireArguments().getString(Constants.USER_WALLET_UUID)


        observerOTP()
        observer()
        happyThemeChanges()


        showLoading()


        try {
            (requireActivity() as UserActivity).toolbarTitle("Verify mobile number")
        }catch (ex:Exception){
            binding.lblTitle.text = "Verify mobile number"
        }

        fun sendVerificationCode() {
            (requireContext() as UserActivity).sendVerificationCode(
                "$DEFAULT_COUNTRY_CODE$phoneNumber"
            )
        }

        setSubTitle()
        when (isComingFor) {
            ComingFor.LOGIN_MOBILE.name -> {
                sendVerificationCode()
            }
            ComingFor.FORGET_PASSWORD.name -> {
                haveTpin= requireArguments().getBoolean(Constants.HAVE_TPIN)
                haveSQ= requireArguments().getBoolean(Constants.HAVE_SECURITY_QUESTION)
                if(haveSQ || haveTpin) {
                    sendVerificationCode()
                }else{
                    val  dialog = MaterialAlertDialogBuilder(requireContext())
                    dialog.apply {
                        setTitle("Alert!")
                        setMessage("You can not forgot password,\ncontact to customer care.")
                        setCancelable(false)
                        setPositiveButton("85285285282"
                        ) { dialog, which ->0
                            dialog.dismiss()
                        }
                        setNegativeButton("Cancel"
                        ) { dialog, which ->
                            dialog.dismiss()
                        }
                    }
                    dialog.show()
                }
            }
            ComingFor.SIGN_UP.name -> {
                sendVerificationCode()
            }
            ComingFor.MOBILE_VERIFY.name->{
                sendVerificationCode()
            }
        }
        /*   binding.btnForgotTpin.setOnClickListener {
               findNavController().navigate(
                   R.id.action_otpFragment_to_securityQuestionVerityFragment,
                   bundleOf(
                       Constants.COMING_FOR to isComingFor,
                       Constants.PHONE_NUMBER to phoneNumber,
                       Constants.USER_ID to userID,
                       Constants.USER_WALLET_UUID to userWalletUUID
                   )
               )
           }*/

        binding.btnContinue.setOnClickListener {
            /*Log.d("OTP_View==", binding.otpView.text.toString())
            fun checkTPin() {
                if (validation()) {
                    userID?.let { uID ->
                        showLoading()
                        otpViewModel.checkValidTPIN(uID, binding.tpinView.text.toString())
                    }
                }
            }*/

            when (isComingFor) {
                ComingFor.LOGIN_MOBILE.name -> {
                    verifyMobileOTP()
                }
                ComingFor.FORGET_PASSWORD.name -> {
                    verifyMobileOTP()

                }
                ComingFor.SIGN_UP.name -> {
                    verifyMobileOTP()
                }
                ComingFor.MOBILE_VERIFY.name->{
                    verifyMobileOTP()
                }
            }

        }


        binding.btnResendCode.setOnClickListener {
            if (counter > 1) {
                showToast("Please Wait")
            } else {
                binding.otpView.setOTP("")
                showToast("Resend OTP")
                if (phoneToken != null) {
                    (requireActivity() as UserActivity).resendVerificationCode(
                        "$DEFAULT_COUNTRY_CODE$phoneNumber",
                        phoneToken!!
                    )
                } else {
                    showToast("Please try after sometime.")
                }

            }
        }
    }

    private fun verifyMobileOTP() {
        if(validationOtp()) {
            (requireActivity() as UserActivity).verifyPhoneNumberWithCode(
                phoneVerificationId, binding.otpView.otp.toString()
            )
        }

    }
    private fun validationOtp():Boolean{
        if(phoneVerificationId.isBlank())
        {
            showToast("The phone number provided is incorrect")
            return   false
        }
        if(binding.otpView.otp.toString().isBlank()){
            showToast("Enter OTP")
            return   false
        }
        return  true
    }


    /*   private fun validation(): Boolean {
           binding.apply {
               if (tpinView.text.toString().isBlank()) {
                   showToast("Enter TPIN")
                   return false
               }
           }
           return true
       }*/

    private fun setSubTitle() {
        val lblmes = "Enter 6-digit Code we have sent at\n$DEFAULT_COUNTRY_CODE$phoneNumber"
        binding.lblMsg.text = requireContext().setTextSpanColor(
            lblmes,
            34,
            lblmes.length,
            spannableTextColor
        ) {
            requireActivity().onBackPressed()
            /* findNavController().navigate(
                 R.id.action_otpFragment_to_phoneRegistrationFragment, bundleOf(
                     Constants.COMING_FOR to isComingFor,
                     Constants.PHONE_NUMBER to phoneNumber
                 )
             )*/
        }
        binding.lblMsg.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun updateCounterTextView() {
        try {


            val msg =
                if(counter>1){"This session will end in $counter seconds."}else { "Didn't get code? "}

            binding.txtResendMsg.text =msg
            if(counter>1){
                binding.btnResendCode.hide()
            }else{
                binding.btnResendCode.show()
            }

        } catch (ex: Exception) {

        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter -= 1
                updateCounterTextView()
            }

            override fun onFinish() {

            }
        }
        timer.start()

    }

    override fun onDestroy() {
        super.onDestroy()
        finishCounter()
    }

    private fun observer() {
        otpViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is LoginWithMobileResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            // pref().clearPreference()
                            saveLogin(userId, userToken, uuid)
                            startActivity(Intent(requireContext(), MainActivity::class.java))
                            requireActivity().finishAffinity()
                        }

                    } else {
                        showToast(it.message)
                    }
                    otpViewModel.observerResponse.value =null
                }
                is CheckValidTpinResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        verifyMobileOTP()
                    } else {
                        showToast(it.message)
                    }
                    otpViewModel.observerResponse.value =null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    otpViewModel.observerResponse.value =null
                }
            }
        })
    }

    private fun saveLogin(userId: String, userToken: String, walletUuid: String) {
        pref().apply {
            setStringValue(USER_ID, userId)
            setStringValue(USER_TOKEN, userToken)
           // setStringValue(USER_WALLET_UUID, walletUuid)
            setBooleanValue(PRESENTATION_COMPLETED, true)
        }
    }

    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is BaseActivity.CodeSentModel -> {
                    hideLoading()
                    counter = 60
                    updateCounterTextView()
                    startTimer()

                    phoneVerificationId = it.verificationId
                    phoneToken = it.token
                    verificationLiveData.value = null
                }
                is BaseActivity.VerificationCompleteModel -> {
                    hideLoading()
                    when (isComingFor) {
                        ComingFor.LOGIN_MOBILE.name -> {
                            showLoading()
                            otpViewModel.loginWithMobile(phoneNumber!!)
                        }
                        ComingFor.FORGET_PASSWORD.name -> {
                            findNavController().navigate(
                                R.id.action_otpFragment_to_tpinVerifyFragment,
                                bundleOf(
                                    PHONE_NUMBER to phoneNumber,
                                    Constants.COMING_FOR to ComingFor.FORGET_PASSWORD.name,
                                    Constants.USER_ID to userID,
                                    Constants.USER_WALLET_UUID to userWalletUUID,
                                    Constants.HAVE_TPIN to haveTpin,
                                    Constants.HAVE_SECURITY_QUESTION to haveSQ
                                )
                            )
                            /*  findNavController().navigate(
                                  R.id.action_otpFragment_to_changePasswordFragment, bundleOf(
                                      PHONE_NUMBER to
                                              phoneNumber
                                  )
                              )*/
                        }
                        ComingFor.SIGN_UP.name -> {
                            findNavController().navigate(
                                R.id.action_otpFragment_to_signupProfileEditFragment, bundleOf(
                                    PHONE_NUMBER to
                                            phoneNumber,
                                    Constants.COMING_FOR to ""
                                )
                            )
                        }
                        ComingFor.MOBILE_VERIFY.name -> {
                            requireActivity().onBackPressed()
                            mobileNoUpdateLiveData.value = true
                        }
                    }
                    verificationLiveData.value = null
                }
                is BaseActivity.FirebaseExceptionModel -> {
                    hideLoading()
                    counter = 0
                    finishCounter()
                    // updateCounterTextView()
                    if (it.e is FirebaseAuthInvalidCredentialsException) {
                        showToast(it.e.message.toString()/*"The phone number provided is incorrect"*/)
                    }

                    Log.d("FirebaseException==", it.e.toString())
                    verificationLiveData.value = null
                }
                is BaseActivity.CodeAutoRetrievalTimeOutModel -> {
                    hideLoading()
                    //  showToast(it.s0)
                    counter = 0
                    finishCounter()
                    // updateCounterTextView()
                    Log.d("CodeAutoRetrievalT", it.s0.toString())
                    verificationLiveData.value = null
                }
            }
        }
    }


    private fun finishCounter() {
        try {
            counter = 0
            timer.cancel()
            updateCounterTextView()
        } catch (e: Exception) {

        }


    }


    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBackgroundResource(R.color.yellow_500)
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                spannableTextColor = R.color.orange_dark
                binding.btnResendCode.setTextColor(resources.getColor(R.color.orange_dark))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.night_black
                binding.btnResendCode.setTextColor(resources.getColor(R.color.b_currency_blue))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.night_black
                binding.btnResendCode.setTextColor(resources.getColor(R.color.b_currency_blue))
            }
        }
    }


    override fun getLayoutId(): Int = R.layout.otp_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): OtpViewModel = otpViewModel

}