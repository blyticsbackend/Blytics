package com.nbt.blytics.modules.chagnetpin

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ChangeTipFragmentBinding
import com.nbt.blytics.modules.chagnetpin.models.ChangeTpinResponse
import com.nbt.blytics.modules.chagnetpin.models.CheckTpinResponse
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences

class ChangeTipFragment : BaseFragment<ChangeTipFragmentBinding, ChangeTipViewModel>() {

    private val changeTipViewModel: ChangeTipViewModel by viewModels()
    private lateinit var binding: ChangeTipFragmentBinding
    private var userID: String? = ""
    private var userWalletUUID: String? = ""
    private var isComingFor: String? = ""
    private var phoneNumber: String? = ""
    private var oldTpin = false
    lateinit var pref: SharePreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        pref = SharePreferences.getInstance(requireContext())
        happyThemeChanges()
        /*   userID = getStringValue(SharePreferences.USER_ID, "")
           userWalletUUID = getStringValue(SharePreferences.USER_WALLET_UUID, "")
           isComingFor = ComingFor.BACCOUNT.name
           phoneNumber = getStringValue(SharePreferences.USER_MOBILE_NUMBER, "")*/

        userID = requireArguments().getString(Constants.USER_ID)
        userWalletUUID = requireArguments().getString(Constants.USER_WALLET_UUID)
        isComingFor = requireArguments().getString(Constants.COMING_FOR)
        phoneNumber = requireArguments().getString(Constants.PHONE_NUMBER)


        if (isComingFor.equals(ComingFor.BACCOUNT.name, true).not()) {

            binding.lblTitle.text = "Change TPIN"

        }
        showLoading()
        changeTipViewModel.checkTpin(userID!!, pref.getStringValue(pref.USER_TOKEN, ""))
        observer()
        binding.btnContinue.setOnClickListener {
            userID?.let { uID ->
                if (uID.isNotBlank()) {
                    if (validation()) {
                        showLoading()
                        if (oldTpin) {
                            changeTipViewModel.checkValidTPIN(
                                userID!!,
                                binding.oldTpinView.toString().trim()
                            )

                        } else {
                            changeTipViewModel.changeTpi(
                                userID!!,
                                userWalletUUID!!,
                                binding.tpinView.otp.toString().trim()
                            )
                        }

                    }
                }
            }
        }

    }

    private fun observer() {
        changeTipViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ChangeTpinResponse -> {
                    hideLoading()
                    changeTipViewModel.observerResponse.value = null

                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (isComingFor.equals(ComingFor.BACCOUNT.name, true).not()) {
                            findNavController().navigate(
                                R.id.action_changeTipFragment_to_phoneRegistrationFragment,
                                bundleOf(
                                    Constants.USER_ID to userID,
                                    Constants.USER_WALLET_UUID to userWalletUUID,
                                    Constants.COMING_FOR to isComingFor,
                                    Constants.PHONE_NUMBER to phoneNumber,

                                    )
                            )
                        } else {
                            showToast(it.message)
                            requireActivity().onBackPressed()
                        }
                    } else {
                        showToast(it.message)
                    }
                }
                is CheckTpinResponse -> {
                    hideLoading()
                    changeTipViewModel.observerResponse.value = null

                    if ((it.status.equals(Constants.Status.SUCCESS.name, true))) {
                        if (it.data!!.wtpin == 1) {
                            oldTpin = true
                            binding.oldLayoutTpin.visibility = View.VISIBLE

                        } else {
                            binding.lblTitle.text = "Generate Tpin"
                            binding.oldLayoutTpin.visibility = View.GONE

                        }
                    }
                }
                is CheckValidTpinResponse -> {
                    hideLoading()
                    changeTipViewModel.observerResponse.value = null

                    if (it.status.contains(Constants.Status.SUCCESS.name, true)) {
                        changeTipViewModel.changeTpi(
                            userID!!,
                            userWalletUUID!!,
                            binding.tpinView.otp.toString().trim()
                        )
                    } else {
                        showToast(it.message)
                    }


                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    changeTipViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun validation(): Boolean {

        binding.apply {
            if (oldTpin) {
                if (binding.oldTpinView.otp.toString().trim().isBlank()) {
                    showToast("Enter Old TPIN")
                    return false
                }
            }

            if (tpinView.otp.toString().isBlank()) {
                // layoutName.error = "Enter first name"
                showToast("Enter TPIN")
                return false
            }
            if (tpinView.otp.toString().length < 4) {
                showToast("Enter TPIN")
                return false
            }
            if (tpinViewConfirm.otp.toString().isBlank()) {
                showToast("Enter Confirm TPIN")
                return false
            }
            if (tpinViewConfirm.otp.toString().length < 4) {
                showToast("Enter Confirm TPIN")
                return false
            }
            if (tpinView.otp.toString() != tpinViewConfirm.otp.toString()) {
                showToast("Confirm TPIN not match.")
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
                if (isComingFor!!.equals(ComingFor.BACCOUNT.name).not()) {
                    binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)
                }
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

    override fun getLayoutId(): Int = R.layout.change_tip_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): ChangeTipViewModel = changeTipViewModel

}