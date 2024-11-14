package com.nbt.blytics.modules.phoneregistation

import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.countrypicker.CallbackSelectCountry
import com.nbt.blytics.countrypicker.CountryCodePicker
import com.nbt.blytics.countrypicker.models.Country
import com.nbt.blytics.databinding.PhoneRegistrationFragmentBinding
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.phoneregistation.models.CheckUserFullResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.Constants.COMING_FOR
import com.nbt.blytics.utils.Constants.DEFAULT_COUNTRY_CODE
import com.nbt.blytics.utils.Constants.HAVE_TPIN
import com.nbt.blytics.utils.Constants.PHONE_NUMBER
import com.nbt.blytics.utils.Constants.USER_ID
import com.nbt.blytics.utils.Constants.USER_WALLET_UUID
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class PhoneRegistrationFragment : BaseFragment<PhoneRegistrationFragmentBinding, PhoneRegistrationViewModel>() {
    private val phoneRegistrationViewModel: PhoneRegistrationViewModel by viewModels()
    private lateinit var binding: PhoneRegistrationFragmentBinding
    private var phoneNumber: String = ""
    private var isComingFor: String? = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        USER_DETAILS_FOR_LINKED = null
        isComingFor = requireArguments().getString(COMING_FOR)

        try {
            val phoneNo = requireArguments().getString(Constants.PHONE_NUMBER, "")
            if (phoneNo.isNotBlank()) {
                binding.edtPhoneNumber.setText(phoneNo)
            }
        } catch (e: Exception) { }

        when (isComingFor) {
            ComingFor.LOGIN_MOBILE.name -> {
                binding.lblTitle.text = "Login with otp"
                (requireActivity() as UserActivity).toolbarTitle("Login with otp")/* binding.lblMsg.text =
                     "Please enter your valid phone number. We will send you 6-digit code to verify account."
 */
            }

            ComingFor.FORGET_PASSWORD.name -> {
                binding.lblTitle.text = "Forgot password"
                binding.chkIntegrateWhatsapp.hide()
                (requireActivity() as UserActivity).toolbarTitle("Forgot password")/*  binding.lblMsg.text =
                      "Please enter your registered phone number. We will send You 6-digit code to verify account."*/
            }

            ComingFor.SIGN_UP.name -> {
                binding.lblTitle.text = "Phone registration"
                binding.lblMsg.show()
                binding.lblMsg.text = "Please enter your valid phone number"
                (requireActivity() as UserActivity).toolbarTitle("Phone registration")
            }

            ComingFor.MOBILE_VERIFY.name -> {
                binding.lblTitle.text = "Verify mobile"
                (requireActivity() as UserActivity).toolbarTitle("Verify mobile")/*  binding.lblMsg.text =
                  "Please enter your phone number. We will send You 6-digit code to verify phone number."*/
            }

            ComingFor.LINK_AC_PHONE_VERIFY.name -> {
                binding.lblTitle.show()
                binding.lblMsg.show()
                binding.lblTitle.text = "Phone registration"
                binding.extraMoney2.text = "We will Send Them A Notification"
                binding.worningImg.setImageResource(R.drawable.phone_register)
                binding.card3.show()
                binding.lblMsg.text = "Please enter your valid phone number"
                binding.switchBlytics.hide()
                binding.lytBtns.show()
                binding.btnContinue.hide()
                binding.chkIntegrateWhatsapp.hide()
                binding.edtPhoneNumber.setHint("Enter Linked User's Number")
                binding.lblSignIn.hide()
                binding.imageFilterView.hide()
            }
        }

        binding.lblSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_phoneRegistrationFragment_to_signInFragment)
        }

        binding.btnContinue.setOnClickListener {
            if (validation()) {
                showLoading()
                phoneNumber = binding.edtPhoneNumber.text.toString().trim()
                phoneRegistrationViewModel.checkExist2(binding.edtCountryCode.text.toString().trim() + binding.edtPhoneNumber.text.toString().trim(), CheckFor.FULL_DETAILS)
            }
        }

        binding.btnContinue2.setOnClickListener {
            if (validation()) {
                showLoading()
                phoneNumber = binding.edtPhoneNumber.text.toString().trim()
                phoneRegistrationViewModel.checkExist(binding.edtPhoneNumber.text.toString().trim(), CheckFor.FULL_DETAILS_LINKED)
            }
        }

        binding.switchBlytics.setOnCheckedChangeListener { p0, p1 ->
            if (binding.switchBlytics.isChecked) {
                if (binding.switchBlytics.isPressed) {
                    binding.btnContinue.hide()
                    binding.btnSkip.hide()
                    binding.btnContinue2.show()
                }
            } else {
                if (binding.switchBlytics.isPressed) {
                    binding.btnContinue.hide()
                    binding.btnSkip.show()
                    binding.btnContinue2.show()
                }
            }
        }

        binding.btnSkip.setOnClickListener {
            findNavController().navigate(R.id.signupProfileEditFragment3, bundleOf(Constants.PHONE_NUMBER to "", Constants.COMING_FOR to Constants.LINKED_ACC))
        }

        binding.edtPhoneNumber.setOnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                binding.edtPhoneNumber.clearFocus()
            }
            false
        }

        binding.edtCountryCode.setText(" $DEFAULT_COUNTRY_CODE")
        binding.btnCountryCodePicker.setOnClickListener {
            CountryCodePicker(requireContext()).apply {
                setOnCountryChangeListener(object : CallbackSelectCountry {
                    override fun selectedCountry(country: Country) {
                        DEFAULT_COUNTRY_CODE = country.dial_code
                        binding.edtCountryCode.setText(" " + DEFAULT_COUNTRY_CODE)
                    }
                })
                init()
            }
        }
    }

    private fun observer() {
        phoneRegistrationViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CheckUserFullResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        USER_DETAILS_FOR_LINKED = it.data
                        findNavController().navigate(R.id.signupProfileEditFragment3, bundleOf(Constants.PHONE_NUMBER to "", Constants.COMING_FOR to Constants.LINKED_ACC))
                    } else {
                        showToast(it.message)
                    }
                    phoneRegistrationViewModel.observerResponse.value = null
                }

                is CheckExistPhoneResponse -> {
                    if (it.status.equals(Constants.Status.FAILED.name, true)) {

                        when (isComingFor) {
                            ComingFor.LOGIN_MOBILE.name -> {
                                hideLoading()
                                showToast("User does not exist.")
                            }

                            ComingFor.FORGET_PASSWORD.name -> {
                                hideLoading()
                                showToast("User does not exist.")
                            }

                            ComingFor.MOBILE_VERIFY.name -> {
                                hideLoading()
                                it.data?.apply {
                                    findNavController().navigate(R.id.action_phoneRegistrationFragment2_to_otpFragment2, bundleOf(PHONE_NUMBER to phoneNumber, COMING_FOR to ComingFor.MOBILE_VERIFY.name, USER_ID to userId, USER_WALLET_UUID to walletUUID))
                                }
                            }

                            ComingFor.SIGN_UP.name -> {
                                hideLoading()
                                findNavController().navigate(R.id.action_phoneRegistrationFragment_to_otpFragment, bundleOf(PHONE_NUMBER to phoneNumber, COMING_FOR to ComingFor.SIGN_UP.name, USER_ID to "", USER_WALLET_UUID to ""))
                            }
                            ComingFor.LINK_AC_PHONE_VERIFY.name -> {}
                        }

                    } else {
                        hideLoading()
                        when (isComingFor) {
                            ComingFor.LOGIN_MOBILE.name -> {
                                it.data?.apply {
                                    findNavController().navigate(R.id.action_phoneRegistrationFragment_to_otpFragment, bundleOf(PHONE_NUMBER to phoneNumber, COMING_FOR to ComingFor.LOGIN_MOBILE.name, USER_ID to userId, USER_WALLET_UUID to walletUUID))
                                }
                            }

                            ComingFor.FORGET_PASSWORD.name -> {
                                it.data?.apply {
                                    val haveTpin = tpin == 1
                                    //   val haveSq = securityQuestion ==  0
                                    if (haveTpin /*|| haveSq*/) {
                                        findNavController().navigate(R.id.action_phoneRegistrationFragment_to_otpFragment, bundleOf(PHONE_NUMBER to phoneNumber, COMING_FOR to ComingFor.FORGET_PASSWORD.name, USER_ID to userId, USER_WALLET_UUID to walletUUID, HAVE_TPIN to haveTpin,))
                                    } else {
                                        val dialog = MaterialAlertDialogBuilder(requireContext())
                                        dialog.apply {
                                            setTitle("Alert!")
                                            setMessage("You can not reset password,\ncontact to customer care.")
                                            setCancelable(false)
                                            setPositiveButton("Call") { dialog, which -> 0
                                                dialog.dismiss()
                                            }
                                            setNegativeButton("Cancel") { dialog, which ->
                                                dialog.dismiss()
                                            }
                                        }
                                        dialog.show()
                                    }
                                }
                            }

                            ComingFor.SIGN_UP.name -> {
                                showToast("Mobile number already registered.")
                            }

                            ComingFor.LINK_AC_PHONE_VERIFY.name -> {
                            }
                        }
                    }
                    phoneRegistrationViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    phoneRegistrationViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun validation(): Boolean {
        if (binding.edtPhoneNumber.text.toString().isBlank()) {
            showToast(resources.getString(R.string.error_msg_phone_empty))
            return false
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
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnContinue2.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSkip.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.lblSignIn.setTextColor(resources.getColor(R.color.orange_dark))


            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnContinue2.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSkip.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.lblSignIn.setTextColor(resources.getColor(R.color.b_currency_blue))
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnContinue2.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSkip.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.lblSignIn.setTextColor(resources.getColor(R.color.b_currency_blue))
            }
        }
    }

    companion object {
        var USER_DETAILS_FOR_LINKED: CheckUserFullResponse.Data? = null
    }

    override fun getLayoutId(): Int = R.layout.phone_registration_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): PhoneRegistrationViewModel = phoneRegistrationViewModel
}