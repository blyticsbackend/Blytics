package com.nbt.blytics.modules.editaddress

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.countrypicker.CallbackCountrySelected
import com.nbt.blytics.countrypicker.CallbackSelectState
import com.nbt.blytics.countrypicker.CountryPicker
import com.nbt.blytics.countrypicker.StatePicker
import com.nbt.blytics.countrypicker.models.CountriesStates
import com.nbt.blytics.databinding.UserEditAddressFragmentBinding
import com.nbt.blytics.modules.signupprofile.SignupProfileEditFragment
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.setImage

class UserEditAddressFragment :
    BaseFragment<UserEditAddressFragmentBinding, UserEditAddressViewModel>() {

    private lateinit var binding: UserEditAddressFragmentBinding
    private val userEditAddressViewModel: UserEditAddressViewModel by viewModels()
    private var selectCountryName: String = ""
    private var selectStateName: String = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()

        setProfileImage()

        binding.btnContinue.setOnClickListener {
            if (validation()) {
                binding.apply {
                    findNavController().navigate(
                        R.id.action_userEditAddressFragment_to_setPasswordFragment,
                        bundleOf(
                            Constants.PHONE_NUMBER to requireArguments().getString(Constants.PHONE_NUMBER),
                            Constants.USER_FIRST_NAME to requireArguments().getString(Constants.USER_FIRST_NAME),
                            Constants.USER_LAST_NAME to requireArguments().getString(Constants.USER_LAST_NAME),
                            Constants.USER_DOB to requireArguments().getString(Constants.USER_DOB),
                            Constants.USER_GENDER to requireArguments().getString(Constants.USER_GENDER),
                            Constants.USER_EMAIL to requireArguments().getString(Constants.USER_EMAIL),
                            Constants.USER_ADDRESS to edtAddress.text.toString().trim(),
                            Constants.USER_COUNTRY to selectCountryName,
                            Constants.USER_STATE to selectStateName,
                            Constants.USER_PIN_CODE to edtPinCode.text.toString().trim()
                        )
                    )
                }
            }
        }

        binding.btnCountryPicker.setOnClickListener {
            CountryPicker(requireContext()).apply {
                setOnCountryChangeListener(object : CallbackCountrySelected {
                    override fun selectedCountry(country: CountriesStates.Country) {
                        binding.countrySpinner.setText(country.countryName)
                        selectCountryName = country.countryName
                    }
                })
                init()
            }
        }
        binding.btnStatePicker.setOnClickListener {
            if (selectCountryName.isNotBlank()) {
                StatePicker(requireContext()).apply {
                    setOnStateChangeListener(object : CallbackSelectState {
                        override fun selectedState(state: CountriesStates.Country.State) {
                            binding.stateSpinner.setText(state.stateName)
                            selectStateName = state.stateName
                        }

                    })
                    init()
                }
            } else {
                showToast("Please select country")
            }
        }


    }

    private fun setProfileImage() {
        SignupProfileEditFragment.apply {
            if(USER_SELECTED_AVATAR ==null){
                binding.imgProfile.setImageBitmap(USER_IMAGE_BITMAP)
            }else{
                binding.imgProfile.setImage(USER_SELECTED_AVATAR!!.image)
            }
        }

    }


    private fun validation(): Boolean {
        binding.apply {
            if (edtAddress.text.toString().isBlank()) {
                showToast("Enter address")
                return false
            }
            if(selectCountryName.isBlank()){
                showToast("Select Country")
                return false
            }
            if(selectStateName.isBlank()){
                showToast("Select State")
                return false
            }

            if (edtPinCode.text.toString().isBlank()) {
                showToast("Enter pin code")
                return false
            }
        }
        return true
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBackgroundColor(requireActivity().resources.getColor(R.color.yellow_500))
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

    override fun getLayoutId(): Int = R.layout.user_edit_address_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): UserEditAddressViewModel = userEditAddressViewModel

}