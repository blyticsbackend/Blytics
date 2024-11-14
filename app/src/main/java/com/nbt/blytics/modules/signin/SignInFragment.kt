package com.nbt.blytics.modules.signin

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.LogoutRequest
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.SignInFragmentBinding
import com.nbt.blytics.modules.securityQes.SecurityQuestionFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signin.model.LoginResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqlRequest
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.Constants.COMING_FOR
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class SignInFragment : BaseFragment<SignInFragmentBinding, SignInViewModel>(),
    TextView.OnEditorActionListener {
    private val signInViewModel: SignInViewModel by viewModels()
    private lateinit var binding: SignInFragmentBinding
    private var spannableTextColor: Int = -1
    private var phoneVerificationId: String = ""
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var updateType: UpdateType
    private lateinit var bottomSheetMobileVerify: BottomSheetDialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()

        initUI()
        observerOTP()
        if(pref().getStringValue(SharePreferences.USER_FIRST_NAME,"").isNotBlank()) {
            binding.lblSignUp.hide()
        }else{
            binding.lblSignUp.show()
        }
       /* if(pref().getBooleanValue(SharePreferences.SET_DEFAULT_APPS).not()) {

        }*/

        /*binding.imageFilterView.setOnClickListener{
            (requireActivity() as BaseActivity).showThemeDialog()
        }*/
      /*  binding.lblLoginMobile.setOnClickListener {
            findNavController().navigate(
                R.id.action_signInFragment_to_phoneRegistrationFragment,
                bundleOf(COMING_FOR to ComingFor.LOGIN_MOBILE.name)
            )
        }*/

        binding.btnLogin.setOnClickListener {
            if (validation()) {
                showLoading()
                binding.apply {
                    signInViewModel.login(
                        edtEmail.text.toString(),
                        edtPassword.text.toString(),
                        pref().getStringValue(SharePreferences.DEVICE_TOKEN,""),
                        requireActivity(),
                        false
                    )
                }
            }
        }

        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(
                R.id.action_signInFragment_to_phoneRegistrationFragment,
                bundleOf(COMING_FOR to ComingFor.FORGET_PASSWORD.name)
            )
        }
        observer()

        binding.edtEmail.setOnEditorActionListener(this)
        binding.edtPassword.setOnEditorActionListener(this)



        pref().apply {
            binding.lytWelcome.invisible()
            if(getStringValue(USER_MOBILE_NUMBER,"").isNotBlank()){
                binding.lytWelcome.show()
               // binding.imgUserWelcome.setImage(getStringValue(USER_PROFILE_IMAGE,""))
               // binding.imgUserWelcome.setImage(getStringValue(USER_PROFILE_IMAGE,""))
              //  binding.imageFilterView.setImage(getStringValue(USER_PROFILE_IMAGE,""))
                binding.lblWelcomeBack.text="Hi, ${getStringValue(USER_FIRST_NAME,"")} welcome back"

                binding.edtEmail.setText(getStringValue(USER_MOBILE_NUMBER,""))
            /*    if(getStringValue(USER_PROFILE_IMAGE,"").isNotBlank()) {
                    binding.imageFilterView.setImage(getStringValue(USER_PROFILE_IMAGE, ""))
                }else{*/
                 //   binding.imageFilterView.setImage(R.drawable.logo_new)

               // }
            }
            /*binding.lytWelcome.setOnClickListener {
                binding.edtEmail.setText(getStringValue(USER_MOBILE_NUMBER,""))
                binding.imageFilterView.setImage(getStringValue(USER_PROFILE_IMAGE,""))
            }*/
        }





    }


    private var isDeviceChanged =false
    private fun observer() {
        signInViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is LoginResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            //   pref().clearPreference()
                            /* saveLogin(userId, userToken, binding.edtEmail.text.toString().trim())
                             startActivity(Intent(requireContext(), MainActivity::class.java))
                             requireActivity().finishAffinity()*/
                            if(isBlock){
                                showBlockDialog(it.message)
                                return@observe
                            }
                            else if(it.data.deviceChanged){
                                isDeviceChanged= true
                                showLogoutDialog(it.message,it.data.mobileNo,it.data.countryCode)
                            }else {
                                if(isDeviceChanged){
                                    saveLogin(userId, userToken, binding.edtEmail.text.toString().trim())
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finishAffinity()
                                }else {
                                    Constants.DEFAULT_COUNTRY_CODE = countryCode
                                    findNavController().navigate(
                                        R.id.action_signInFragment_to_otpFragment,
                                        bundleOf(
                                            Constants.COMING_FOR to ComingFor.LOGIN_MOBILE.name,
                                            Constants.PHONE_NUMBER to it.data.mobileNo,
                                            Constants.USER_ID to userId,
                                            Constants.USER_WALLET_UUID to walletUuid
                                        )
                                    )
                                }
                            }
                        }
                    } else {

                            showToast(it.message)

                    }
                    signInViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    signInViewModel.observerResponse.value = null

                }

            }
        }
    }


    private fun showLogoutDialog(msg:String,phoneNumber: String="", countryCode: String="") {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
            setTitle("Alert")
            setMessage(msg)

            setCancelable(false)
            setPositiveButton(
                "Yes"
            ) { dialog, which ->
                dialog.dismiss()
                sentOTP(phoneNumber, countryCode)
            }
            setNegativeButton(
                "No"
            ) { dialog, which ->
                dialog.dismiss()
            }
        }
        dialog.show()
    }
    private fun showBlockDialog(msg:String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
            setTitle("Alert")
            setMessage(msg)

            setCancelable(false)
            setPositiveButton(
                "Close"
            ) { dialog, which ->
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is BaseActivity.CodeSentModel -> {
                    hideLoading()
                    showMobileVerifyBottomSheet()
                    phoneVerificationId = it.verificationId
                    phoneToken = it.token
                    verificationLiveData.value = null
                }
                is BaseActivity.VerificationCompleteModel -> {
                    hideLoading()
                    binding.apply {
                        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        bottomSheetMobileVerify.dismiss()

                        when (updateType) {
                            UpdateType.VERIFY -> {
                                if (validation()) {
                                    showLoading()
                                    binding.apply {
                                        signInViewModel.login(
                                            edtEmail.text.toString(),
                                            edtPassword.text.toString(),
                                            pref().getStringValue(SharePreferences.DEVICE_TOKEN,""),
                                            requireActivity(),
                                            true
                                        )
                                    }
                                }
                            }

                        }

                    }
                    verificationLiveData.value = null
                }
                is BaseActivity.FirebaseExceptionModel -> {
                    hideLoading()
                    if (it.e is FirebaseAuthInvalidCredentialsException) {
                        showToast("The phone number provided is incorrect")
                    }

                    Log.d("FirebaseException==", it.e.toString())
                    verificationLiveData.value = null
                }
                is BaseActivity.CodeAutoRetrievalTimeOutModel -> {
                    hideLoading()
                    showToast(it.s0)
                    Log.d("CodeAutoRetrievalT", it.s0)
                    verificationLiveData.value = null
                }
            }
        }
    }

    private fun sentOTP( phoneNumber:String, countryCode:String ) {
       updateType = UpdateType.VERIFY
       /* val phoneNumber = pref().getStringValue(SharePreferences.USER_COUNTRY_CODE, "") +
                pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER, "")*/
        (requireActivity() as UserActivity).sendVerificationCode(countryCode+phoneNumber)
    }

    private fun showMobileVerifyBottomSheet() {
        var isKeyboardVisible:Boolean =false
        bottomSheetMobileVerify = BottomSheetDialog(requireContext()).apply {
         val bindingDialog =   DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(
                layoutInflater,
                R.layout.bottom_sheet_mobile_verify,
                null,
                false
            ).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (phoneVerificationId.isBlank()) {
                            showToast("The phone number provided is incorrect")
                            return false
                        }
                        if (otpView.otp.toString().isBlank()) {
                            showToast("Enter OTP")
                            return false
                        }
                        return true
                    }

                    if (validationOtp()) {
                        pref().apply {
                            (requireActivity() as UserActivity).verifyPhoneNumberWithCode(
                                phoneVerificationId,
                                otpView.otp.toString().trim()
                            )
                        }
                    } else {
                        showToast("error")
                    }

                }
               /* otpView.setOnClickListener {
                    if (!isKeyboardVisible){
                        showSoftKeyBoard(otpView)
                        otpView.requestFocus()
                    }
                }
                KeyboardVisibilityEvent.setEventListener(
                    requireActivity(),
                    viewLifecycleOwner
                ) { isOpen -> isKeyboardVisible = isOpen }*/

            }
            show()

            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                }

            }


        }


    }

    private enum class UpdateType(str: String) {
        VERIFY("VERIFY"),



    }

    private fun initUI() {
        pref().apply {
            if (getBooleanValue(IS_REMEMBER_PASSWORD)) {
                //  binding.ckbMemberMember.isChecked = true
                binding.edtEmail.setText(getStringValue(USER_LOGIN_EMAIL_MOBILE, ""))
            }
        }
        binding.lblSignUp.text =getString(R.string.lbl_not_have_account)
        binding.lblSignUp.setOnClickListener {
            findNavController().navigate(
                R.id.action_signInFragment_to_phoneRegistrationFragment,
                bundleOf(COMING_FOR to ComingFor.SIGN_UP.name)
            )
        }


       // binding.lblSignUp.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun saveLogin(userId: String, userToken: String, walletUuid:String,emailMobile: String = "") {
        pref().apply {
            /* if (binding.ckbMemberMember.isChecked) {
                 setStringValue(USER_LOGIN_EMAIL_MOBILE, emailMobile)
                 setBooleanValue(IS_REMEMBER_PASSWORD, true)
             }*/
            setStringValue(USER_ID, userId)
            setStringValue(USER_TOKEN, userToken)
            //setStringValue(USER_WALLET_UUID, walletUuid)
            setBooleanValue(PRESENTATION_COMPLETED, true)
        }
    }


    private fun validation(): Boolean {
        when {
            binding.edtEmail.text.toString().trim() == "" -> {
                showToast(resources.getString(R.string.error_msg_field_empty))
                return false
            }
            binding.edtPassword.text.toString().trim() == "" -> {
                showToast(resources.getString(R.string.error_msg_field_empty))
                return false
            }
            /*binding.edtPassword.text.toString().trim().length < 6 -> {
                showToast(resources.getString(R.string.error_msg_password_length))
                return false
            }*/
        }
        return true
    }

    private fun happyThemeChanges() {
     /*   if (isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                edtEmail.setBgTintHappyTheme()
                edtPassword.setBgTintHappyTheme()
            }
        }*/

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnLogin.setBackgroundResource(R.drawable.bg_gradient_black_btn)
                spannableTextColor = R.color.orange_dark
              //  binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)
                binding.topLayout.setBackgroundResource(R.color.orange_dark)
               // binding.lblSignUp.setTextColor(resources.getColor(R.color.orange_dark))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnLogin.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.blue_dark
                binding.topLayout.setBackgroundResource(R.color.new_bg_light)
               // binding.lblSignUp.setTextColor(resources.getColor(R.color.blue_700))



            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnLogin.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.blue_dark
                binding.topLayout.setBackgroundResource(R.color.new_bg_light)
               // binding.lblSignUp.setTextColor(resources.getColor(R.color.blue_700))
             //   binding.btnForgotPassword.setTextColor(resources.getColor(R.color.white))

            }
        }

    }


    override fun getLayoutId(): Int = R.layout.sign_in_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): SignInViewModel = signInViewModel
    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
            binding.edtEmail.clearFocus()
            binding.edtPassword.clearFocus()
        }
        return false
    }


}