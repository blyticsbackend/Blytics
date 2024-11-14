package com.nbt.blytics.modules.tpinverify

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.TpinVerifyFragmentBinding
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class TpinVerifyFragment : BaseFragment<TpinVerifyFragmentBinding, TpinVerifyViewModel>() {

    private var userID: String? = ""
    private var userWalletUUID: String? = ""
    private var isComingFor: String? = ""
    private var phoneNumber: String? = ""
    private var spannableTextColor: Int = -1
    private var  haveTpin:Boolean = false
    private var  haveSQ:Boolean = false
    private val tpinVerifyViewModel: TpinVerifyViewModel by viewModels()
    private lateinit var binding: TpinVerifyFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        try {
           binding.lblTitle.hide()
            (requireActivity() as UserActivity).toolbarTitle("TPin")
        }catch (ex:Exception){
            binding.lblTitle.show()
        }
        isComingFor = requireArguments().getString(Constants.COMING_FOR)
        phoneNumber = requireArguments().getString(Constants.PHONE_NUMBER)
        userID = requireArguments().getString(Constants.USER_ID)
        userWalletUUID = requireArguments().getString(Constants.USER_WALLET_UUID)

        haveTpin= requireArguments().getBoolean(Constants.HAVE_TPIN)
        haveSQ= requireArguments().getBoolean(Constants.HAVE_SECURITY_QUESTION)
        val userName = pref().getStringValue(SharePreferences.USER_FIRST_NAME,"")

        binding.lblMsg.text = "Enter 4 digit T-Pin (wallet)"
        binding.lblTpin.hide()
        if(!haveTpin && !haveSQ){
            val  dialog = MaterialAlertDialogBuilder(requireContext())
            dialog.apply {
                setTitle("Alert!")
                setMessage("Hey $userName, call customer care.\nBecause you haven't set the security question  and wallet tpin.")
                setCancelable(false)
                setPositiveButton("Call"
                ) { dialog, which ->
                    dialog.dismiss()
                }
                setNegativeButton("Cancel"
                ) { dialog, which ->
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        binding.btnForgotTpin.setOnClickListener {
            if(haveSQ) {
                findNavController().navigate(
                    R.id.action_tpinVerifyFragment_to_securityQuestionVerityFragment,
                    bundleOf(
                        Constants.COMING_FOR to isComingFor,
                        Constants.PHONE_NUMBER to phoneNumber,
                        Constants.USER_ID to userID,
                        Constants.USER_WALLET_UUID to userWalletUUID
                    )
                )
            }else{
                val  dialog = MaterialAlertDialogBuilder(requireContext())
                dialog.apply {
                    setTitle("Alert!")
                    setMessage("Hey $userName, you haven't set the security question.\nContact to customer care 28880877")
                    setCancelable(false)
                    setPositiveButton("Call"
                    ) { dialog, which ->
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

        binding.btnContinue.setOnClickListener {
                if(haveTpin) {
                    if (validation()) {
                        userID?.let { uID ->
                            showLoading()
                            tpinVerifyViewModel.checkValidTPIN(
                                uID,
                                binding.tpinView.otp.toString()
                            )
                        }
                    }
                }else{
                val  dialog = MaterialAlertDialogBuilder(requireContext())
                dialog.apply {
                    setTitle("Alert!")
                    setMessage("Hey $userName, set your tpin to set your password by answering your security question.")
                    setCancelable(false)

                    setNegativeButton("Cancel"
                    ) { dialog, which ->
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }

        }

    }

    private fun validation(): Boolean {
        binding.apply {
            if (tpinView.otp.toString().isBlank()) {
                showToast("Enter TPIN")
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
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                spannableTextColor = R.color.orange_dark

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.night_black
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                spannableTextColor = R.color.night_black
            }
        }
    }
    private fun observer() {
        tpinVerifyViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is CheckValidTpinResponse -> {
                    hideLoading()
                    tpinVerifyViewModel.observerResponse.value =null

                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        findNavController().navigate(
                            R.id.action_tpinVerifyFragment_to_changePasswordFragment, bundleOf(
                                Constants.PHONE_NUMBER to
                                        phoneNumber
                            )
                        )
                    } else {
                        showToast(it.message)
                    }
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    tpinVerifyViewModel.observerResponse.value =null
                }
            }
        })
    }

    override fun getLayoutId(): Int = R.layout.tpin_verify_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): TpinVerifyViewModel = tpinVerifyViewModel

}