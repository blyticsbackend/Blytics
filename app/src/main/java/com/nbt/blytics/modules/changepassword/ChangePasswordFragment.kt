package com.nbt.blytics.modules.changepassword

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.ChangePasswordFragmentBinding
import com.nbt.blytics.modules.changepassword.model.ChangePasswordResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants

class ChangePasswordFragment :
    BaseFragment<ChangePasswordFragmentBinding, ChangePasswordViewModel>() {

    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()
    private lateinit var binding: ChangePasswordFragmentBinding
    private var userMobileNumber: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        happyThemeChanges()
        requireArguments().getString(Constants.PHONE_NUMBER)?.let { moblie ->
            userMobileNumber = moblie
        }


        (requireActivity() as UserActivity).toolbarTitle("Reset Password")


        binding.apply {
            btnContinue.setOnClickListener {
                   if(validation()) {
                       showLoading()
                       changePasswordViewModel.changePassword(
                           userMobileNumber,
                           edtPassword.text.toString()
                       )
                   }
            }
        }

    }


    private fun validation():Boolean{
        binding.apply {
            if(edtPassword.text.toString().isBlank()){
                showToast("Enter Password")
                return false
            }
            if(edtConfirmPassword.text.toString().isBlank()){
                showToast("Enter Confirm Password")
                return false
            }
            if(edtPassword.text.toString().length<4){
                showToast("Password too short")
                return false
            }
            if (edtPassword.text.toString()!=edtConfirmPassword.text.toString()) {
                showToast("Confirm password does not match.")
                return false
            }

            return true
        }

    }

    private fun observer(){
        changePasswordViewModel.observerResponse.observe(viewLifecycleOwner, {
            when(it){
               is ChangePasswordResponse ->{
                   hideLoading()
                   if(it.status.equals(Constants.Status.SUCCESS.name, true)){
                        findNavController().navigate(R.id.action_changePasswordFragment_to_signInFragment)
                    }else{
                        showToast(it.message)
                    }
                   changePasswordViewModel.observerResponse.value = null
                }
                is FailResponse ->{
                    hideLoading()
                    showToast(it.message)
                    changePasswordViewModel.observerResponse.value = null
                }

            }
        })
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
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
        }
    }


    override fun getLayoutId(): Int = R.layout.change_password_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): ChangePasswordViewModel = changePasswordViewModel

}