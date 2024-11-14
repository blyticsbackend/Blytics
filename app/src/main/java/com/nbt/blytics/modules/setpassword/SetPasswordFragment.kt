package com.nbt.blytics.modules.setpassword

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.SetPasswordFragmentBinding
import com.nbt.blytics.modules.setpassword.models.AddAvatarResponse
import com.nbt.blytics.modules.setpassword.models.SignUpResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.SignupProfileEditFragment
import com.nbt.blytics.modules.signupprofile.SignupProfileEditFragment.Companion.USER_IMAGE_BITMAP
import com.nbt.blytics.modules.signupprofile.SignupProfileEditFragment.Companion.USER_SELECTED_AVATAR
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.UtilityHelper
import com.nbt.blytics.utils.setImage

class SetPasswordFragment : BaseFragment<SetPasswordFragmentBinding, SetPasswordViewModel>() {
    private lateinit var binding: SetPasswordFragmentBinding
    private val setPasswordViewModel: SetPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        setProfileImage()

        val phone = requireArguments().getString(Constants.PHONE_NUMBER)
        val firstName = requireArguments().getString(Constants.USER_FIRST_NAME)
        val lastName = requireArguments().getString(Constants.USER_LAST_NAME)
        val dob = requireArguments().getString(Constants.USER_DOB)
        val email = requireArguments().getString(Constants.USER_EMAIL)
        val userAddress = requireArguments().getString(Constants.USER_ADDRESS)
        val userCountry = requireArguments().getString(Constants.USER_COUNTRY)
        val userState = requireArguments().getString(Constants.USER_STATE)
        val userPinCode = requireArguments().getString(Constants.USER_PIN_CODE)
        val gender = requireArguments().getString(Constants.USER_GENDER)
        observer()
        Log.d(
            "CompleteInfo==",
            "phone $phone\n first name $firstName\n  lastName $lastName\n dob $dob\n email $email\n userAddress $userAddress\n userCountry $userCountry\n userState $userState\n userPin $userPinCode\n "
        )
        binding.btnContinue.setOnClickListener {
            if (validation()) {
                showLoading()
                setPasswordViewModel.signUp(
                    firstName!!,
                    lastName!!,
                    email!!,
                    dob!!,
                    gender!!.toInt(),
                    userPinCode!!,
                    userAddress!!,
                    userCountry!!,
                    userState!!,
                    binding.edtPassword.text.toString().trim(),
                    phone!!,
                    true, "No Device Token", Constants.DEFAULT_COUNTRY_CODE, requireActivity()
                )

            }
        }
    }

    private fun uploadAvatar() {
        val userId = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        if (userId.isNotBlank()) {
            val avatarId = if (SignupProfileEditFragment.USER_SELECTED_AVATAR == null) {
                ""
            } else {
                SignupProfileEditFragment.USER_SELECTED_AVATAR!!.id.toString()
            }
            val file = if (SignupProfileEditFragment.USER_IMAGE_BITMAP != null) {
                UtilityHelper.bitmapToFile(
                    SignupProfileEditFragment.USER_IMAGE_BITMAP!!,
                    requireContext()
                )
            } else {
                null
            }
            setPasswordViewModel.addAvatar(userId, avatarId, file, userToken)


        }
    }

    private fun observer() {
        setPasswordViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is SignUpResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                          val deviceToken=  pref().getStringValue(SharePreferences.DEVICE_TOKEN,"")
                            pref().clearPreference()
                            saveLogin(userId, userToken, walletUuid,deviceToken)
                        }
                        showLoading()
                        if (USER_SELECTED_AVATAR == null && USER_IMAGE_BITMAP == null) {
                            hideLoading()
                            findNavController().navigate(R.id.action_setPasswordFragment_to_bvnVarificationFragment)
                        }else{
                            uploadAvatar()
                        }
                    } else {
                        showToast(it.message)
                    }
                    setPasswordViewModel.observerResponse.value =null
                }
                is AddAvatarResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            //showToast("Successful Registration.")
                            SignupProfileEditFragment.USER_IMAGE_BITMAP = null
                            SignupProfileEditFragment.USER_SELECTED_AVATAR = null
                            SignupProfileEditFragment.USER_SELECTED_AVATAR_URL = ""
                            findNavController().navigate(R.id.action_setPasswordFragment_to_bvnVarificationFragment)
                        }
                    } else {
                        showToast(it.message)
                    }
                    setPasswordViewModel.observerResponse.value =null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    setPasswordViewModel.observerResponse.value =null
                }
            }
        })
    }


    private fun setProfileImage() {
        SignupProfileEditFragment.apply {
            if (USER_SELECTED_AVATAR == null) {
                binding.imgProfile.setImageBitmap(USER_IMAGE_BITMAP)
            } else {
                binding.imgProfile.setImage(USER_SELECTED_AVATAR!!.image)
            }
        }

    }

    private fun saveLogin(userId: String, userToken: String, walletUuid: String, deviceToken:String) {
        pref().apply {
            setStringValue(USER_ID, userId)
            setStringValue(USER_TOKEN, userToken)
            //setStringValue(USER_WALLET_UUID, walletUuid)
            setBooleanValue(PRESENTATION_COMPLETED, true)
            setStringValue(DEVICE_TOKEN, deviceToken)
        }
    }

    private fun validation(): Boolean {
        binding.apply {
            if (edtPassword.text.toString().isBlank()) {
                showToast("Enter Password")
                return false
            }
            if (edtConfirmPassword.text.toString().isBlank()) {
                showToast("Enter Confirm Password")
                return false
            }
            if (edtPassword.text.toString().length < 4) {
                showToast("Password too short")
                return false
            }
            if (edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
                showToast("Confirm password not match.")
                return false
            }
        }
        return true
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBackgroundResource(R.color.yellow_500)
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.set_password_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): SetPasswordViewModel = setPasswordViewModel
}