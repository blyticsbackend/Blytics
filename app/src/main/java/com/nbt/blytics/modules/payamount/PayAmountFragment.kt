package com.nbt.blytics.modules.payamount

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.BottomSheetVerifyTpinBinding
import com.nbt.blytics.databinding.PayAmountFragmentBinding
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.model.RequestMoney
import com.nbt.blytics.modules.payamount.model.RequestMoneyResponse
import com.nbt.blytics.modules.payamount.model.SendMoneyRequest
import com.nbt.blytics.modules.payamount.model.SendMoneyResponse
import com.nbt.blytics.modules.payee.payment.ApprovalRequest
import com.nbt.blytics.modules.payee.payment.ApprovalResponse
import com.nbt.blytics.modules.paynow.PayNowFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.UtilityHelper.isDecimal

class PayAmountFragment : BaseFragment<PayAmountFragmentBinding, PayAmountViewModel>() {
    private val payAmountViewModel: PayAmountViewModel by viewModels()
    private lateinit var binding: PayAmountFragmentBinding
    private lateinit var bottomSheetVerifyTpin: BottomSheetDialog
    private var isTipVerified: Boolean = false
    private var receiverContact: String? = null
    private var receiverId: String? = ""
    private lateinit var bindingBottomSheet: BottomSheetVerifyTpinBinding
    private var isMultiPay: String? = null
    private var payMode: String? = ""
    private var requestId: Int = 0

    /* private var requestAmount:String=""*/
    private var statusRequest = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        hideLoading()
        observer()


        payAmountViewModel.getBalance(
            pref().getStringValue(SharePreferences.USER_ID, ""),
            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
        )

        requireArguments().apply {

            isMultiPay = getString(IS_MULTI_PAY)
            payMode = getString(Constants.PAY_MODE)



            if (isMultiPay == "no") {
                binding.lytMulti.hide()
                binding.lytSingle.show()
                binding.cardMainMulti.hide()
                binding.btnSendMulti.hide()
                binding.btnRequestMulti.hide()
                try {
                    binding.edtAmount.setText(getString(RECEIVER_AMT, ""))
                } catch (ex: Exception) {

                }

                val receiverName = getString(RECEIVER_NAME)
                receiverContact = getString(RECEIVER_CONTACT)
                receiverId = getString(RECEIVER_ID)
                val receiverImg = getString(RECEIVER_IMG, "")
                Log.d("onViewCreated", "onViewCreated: $receiverImg")
                binding.receiver.text = receiverName
                binding.receiverContact.text = receiverContact
                binding.edtAmountFrom.setText("${pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER,"")}")
                binding.edtAmountTo.setText("${receiverContact}")
                if (receiverImg.isNotBlank()) {
                    binding.receiverImg.setImage(receiverImg.toString())
                } else {
                    binding.receiverImg.setImage(R.drawable.dummy_user)

                }
                if (payMode!!.equals(Constants.PayType.SENT_MONEY.name, true)) {
                    (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                    binding.btnSend.show()
                    binding.btnRequest.hide()
                } else if (payMode!!.equals(Constants.PayType.REQUEST_MONEY.name, true)) {
                    (requireContext() as BconfigActivity).setToolbarTitle("Request money")
                    binding.btnSend.hide()
                    binding.btnRequest.show()
                } else {
                    (requireContext() as BconfigActivity).setToolbarTitle("Send or request money")
                    binding.btnSend.show()
                    binding.btnRequest.show()
                }
                if (payMode!!.equals(Constants.PayType.REQUESTER_SEND_MONEY.name, true)) {
                    (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                    binding.btnSend.show()
                    binding.btnRequest.hide()
                    binding.btnReject.show()
                    requestId = getInt(REQUEST_ID, 0)
                    val requestAmount = getString(REQUEST_MONEY, "")
                    binding.edtAmount.setText(requestAmount)
                }
            } else {
                binding.apply {

                    if (PayNowFragment.SELECTED_TXN_PAYEES.size < 2) {
                        lytSingle.show()
                        lytMulti.hide()
                        binding.cardMainMulti.hide()
                        binding.btnSendMulti.hide()
                        binding.btnRequestMulti.hide()
                        binding.receiver.text = PayNowFragment.SELECTED_TXN_PAYEES[0].userName
                        binding.receiverContact.text = PayNowFragment.SELECTED_TXN_PAYEES[0].mobNo
                        if (PayNowFragment.SELECTED_TXN_PAYEES[0].userImage.isNotBlank()) {
                            binding.receiverImg.setImage(PayNowFragment.SELECTED_TXN_PAYEES[0].userImage)
                        } else {
                            binding.receiverImg.setImage(R.drawable.dummy_user)

                        }
                        receiverId = PayNowFragment.SELECTED_TXN_PAYEES[0].userId.toString()
                        edtAmountFrom.setText("${pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER,"")}")
                        edtAmountTo.setText("${PayNowFragment.SELECTED_TXN_PAYEES[0].mobNo}")
                        if (payMode!!.equals(Constants.PayType.SENT_MONEY.name, true)) {
                            binding.btnSend.show()
                            binding.btnRequest.hide()
                            (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                        } else {
                            binding.btnSend.hide()
                            binding.btnRequest.show()
                            (requireContext() as BconfigActivity).setToolbarTitle("Request money")
                        }

                        if (payMode!!.equals(Constants.PayType.REQUESTER_SEND_MONEY.name, true)) {
                            binding.btnSend.show()
                            binding.btnRequest.hide()
                            binding.btnReject.show()
                            (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                        }
                    } else {
                        lytMulti.show()
                        lytSingle.hide()
                        binding.cardMainMulti.show()
                        binding.btnSendMulti.show()
                        binding.btnRequestMulti.show()
                        (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                        receiver1.text = PayNowFragment.SELECTED_TXN_PAYEES[0].userName
                        receiverContact1.text = PayNowFragment.SELECTED_TXN_PAYEES[0].mobNo
                        if (PayNowFragment.SELECTED_TXN_PAYEES[0].userImage.isNotBlank()) {
                            receiverImg1.setImage(PayNowFragment.SELECTED_TXN_PAYEES[0].userImage)
                        } else {
                            receiverImg1.setImage(R.drawable.dummy_user)
                        }
                        receiver2.text = PayNowFragment.SELECTED_TXN_PAYEES[1].userName
                        edtAmountFromMulti.setText("${pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER,"")}")
                        edtAmountToMulti.setText("${PayNowFragment.SELECTED_TXN_PAYEES[0].mobNo},${PayNowFragment.SELECTED_TXN_PAYEES[1].mobNo}")
                        receiverContact2.text = PayNowFragment.SELECTED_TXN_PAYEES[1].mobNo
                        if (PayNowFragment.SELECTED_TXN_PAYEES[1].userImage.isNotBlank()) {
                            receiverImg2.setImage(PayNowFragment.SELECTED_TXN_PAYEES[1].userImage)
                        } else {
                            receiverImg2.setImage(R.drawable.dummy_user)
                        }
                        if (payMode!!.equals(Constants.PayType.SENT_MONEY.name, true)) {
                            binding.btnSendMulti.show()
                            binding.btnRequestMulti.hide()
                            (requireContext() as BconfigActivity).setToolbarTitle("Send money")
                        } else {
                            binding.btnSendMulti.hide()
                            binding.btnRequestMulti.show()
                            (requireContext() as BconfigActivity).setToolbarTitle("Request money")
                        }

                    }

                }
            }

        }


        binding.btnReject.setOnClickListener {
            statusRequest = 2
            payAmountViewModel.statusChange(
                ApprovalRequest(
                    pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    requestId.toInt(),
                    receiverId!!.toInt(),
                    2
                )
            )

        }
        binding.btnSend.setOnClickListener {
            if (validationSingleTnx()) {
                bottomSheetVerifyTpin.show()
                bindingBottomSheet.tpinView.resetState()
                bindingBottomSheet.tpinView.setOTP("")
                bindingBottomSheet.lblMsg.hide()
            }

        }
        binding.btnSendMulti.setOnClickListener {
            if (validationMultiTnx()) {
                bottomSheetVerifyTpin.show()
                bindingBottomSheet.tpinView.resetState()
                bindingBottomSheet.tpinView.setOTP("")
                bindingBottomSheet.lblMsg.hide()

            }

        }
        binding.btnRequest.setOnClickListener {
            val requestUserList = mutableListOf<RequestMoney.RequestedToUser>()
            requestUserList.add(RequestMoney.RequestedToUser(receiverId!!))

            if (validationSingleTnx()) {
                if (binding.edtAmount.text.toString().trim().isDecimal().not()) {
                    hideLoading()
                    bottomSheetVerifyTpin.dismiss()
                    showToast("enter correct amount.")
                    return@setOnClickListener
                }
                showLoading()
                val requestMoney = RequestMoney(
                    requestedAmount = binding.edtAmount.text.toString().replace(",","").toDouble(),
                    requestedStatus = "0",
                    requestedToUser = requestUserList,
                    userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                    userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                )
                payAmountViewModel.requestMoney(requestMoney)
                bindingBottomSheet.tpinView.resetState()
                bindingBottomSheet.tpinView.setOTP("")
            }
        }
        binding.btnRequestMulti.setOnClickListener {
            if (validationMultiTnx()) {
                if (binding.edtAmountMulti.text.toString().trim().isDecimal().not()) {
                    hideLoading()
                    bottomSheetVerifyTpin.dismiss()
                    showToast("enter correct amount.")
                    return@setOnClickListener
                }
                showLoading()
                val requestUserList = mutableListOf<RequestMoney.RequestedToUser>()
                for (i in PayNowFragment.SELECTED_TXN_PAYEES.indices) {
                    requestUserList.add(RequestMoney.RequestedToUser(PayNowFragment.SELECTED_TXN_PAYEES[i].userId))
                }
                val requestMoney = RequestMoney(
                    requestedAmount = binding.edtAmountMulti.text.toString().replace(",","").toDouble(),
                    requestedStatus = "0",
                    requestedToUser = requestUserList,
                    userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                    userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                )
                payAmountViewModel.requestMoney(requestMoney)
                bindingBottomSheet.tpinView.resetState()
                bindingBottomSheet.tpinView.setOTP("")
            }
        }

        verifyTpinBottomSheet()
        happyThemeChanges()




    }

    private fun validationSingleTnx(): Boolean {
        if (binding.edtAmount.text.toString().isBlank()) {
            showToast("enter amount")
            return false
        }
        /* if(binding.edtAmount.text.toString().toDouble()<1){
             showToast("")
             return false
         }*/
        return true
    }

    private fun validationMultiTnx(): Boolean {
        if (binding.edtAmountMulti.text.toString().isBlank()) {
            showToast("enter amount")
            return false
        }
        /*  if(binding.edtAmountMulti.text.toString().toDouble()<1){
              return false
          }*/
        return true
    }

    private fun verifyTpinBottomSheet() {
        var isKeyboardVisible: Boolean = false
        bottomSheetVerifyTpin = BottomSheetDialog(requireContext()).apply {
            bindingBottomSheet = DataBindingUtil.inflate<BottomSheetVerifyTpinBinding>(
                layoutInflater,
                R.layout.bottom_sheet_verify_tpin,
                null,
                false
            ).apply {
                setContentView(root)
                fun validation(): Boolean {
                    binding.apply {
                        if (tpinView.otp.toString().isBlank()) {
                            showToast("Enter TPIN")
                            return false
                        }
                    }
                    return true
                }
                lblTpin.text = ""
                btnVerify.setOnClickListener {
                    if (validation()) {
                        val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        val uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
                        userId.let { uID ->
                            showLoading()
                            payAmountViewModel.checkValidTPIN(
                                uID,
                                tpinView.otp.toString(),
                                uuid,
                                userToken
                            )
                        }
                    }
                }
                when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                        btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                    }

                }
            }
            /*   KeyboardVisibilityEvent.setEventListener(
                   requireActivity(),
                   viewLifecycleOwner
               ) { isOpen ->
                   isKeyboardVisible = isOpen
               }

               bindingBottomSheet.tpinView.setOnClickListener {
                   if (!isKeyboardVisible){
                       showSoftKeyBoard(bindingBottomSheet.tpinView)
                      // bindingBottomSheet.tpinView.requestFocus()
                   }
               }*/


        }
    }

    private val receiverList = mutableListOf<SendMoneyRequest.UserReceive>()

    private fun observer() {
        payAmountViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {

                is RequestMoneyResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        findNavController().navigate(
                            R.id.action_payAmountFragment_to_transcationStatusFragment_single,
                            bundleOf(IS_MULTI_PAY to "no", MESSAGE to "Request successful")
                        )
                    }
                    showToast(it.message)
                    payAmountViewModel.observerResponse.value = null
                }
                is SendMoneyResponse -> {
                    hideLoading()
                    bottomSheetVerifyTpin.dismiss()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (isMultiPay == "no") {
                            if (payMode!!.equals(
                                    Constants.PayType.REQUESTER_SEND_MONEY.name,
                                    true
                                )
                            ) {

                                statusRequest = 1
                                payAmountViewModel.statusChange(
                                    ApprovalRequest(
                                        pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                        pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                        requestId.toInt(),
                                        receiverId!!.toInt(),
                                        1
                                    )
                                )
                            } else {

                                findNavController().navigate(
                                    R.id.action_payAmountFragment_to_transcationStatusFragment_single,
                                    bundleOf(IS_MULTI_PAY to "no", MESSAGE to "Payment successful")
                                )
                            }
                        } else {
                            findNavController().navigate(
                                R.id.action_payAmountFragment_to_transcationStatusFragment_multi,
                                bundleOf(IS_MULTI_PAY to "yes", MESSAGE to "Payment successful")
                            )
                        }

                    } else {

                        showToast(it.message)
                        if (it.data!!.chargeable) {
                            // bindingBottomSheet.tpinView.setText("")
                            bindingBottomSheet.tpinView.resetState()
                            bindingBottomSheet.tpinView.setOTP("")
                            val errorDialog = MaterialAlertDialogBuilder(requireContext())
                            errorDialog.apply {
                                setTitle(it.message)
                                setMessage("Do you want to proceed?")

                                setCancelable(false)
                                setPositiveButton(
                                    "Yes"
                                ) { dialog, which ->
                                    dialog.dismiss()
                                    showLoading()
                                    val receiverList = mutableListOf<SendMoneyRequest.UserReceive>()
                                    fun singleUserSend(receiverContact: String) {
                                        if (binding.edtAmount.text.toString().trim().isDecimal()
                                                .not()
                                        ) {
                                            hideLoading()
                                            bottomSheetVerifyTpin.dismiss()
                                            showToast("enter correct amount.")
                                            return
                                        }
                                        receiverList.clear()
                                        receiverList.add(
                                            SendMoneyRequest.UserReceive(
                                                receiverContact
                                            )
                                        )
                                        val sendMoneyRequest = SendMoneyRequest(
                                            binding.edtAmount.text.toString().trim().replace(",","").toDouble(),
                                            pref().getStringValue(
                                                SharePreferences.USER_WALLET_UUID,
                                                ""
                                            ),
                                            true,
                                            pref().getStringValue(SharePreferences.USER_ID, ""),
                                            receiverList,
                                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                            chargeable = it.data.chargeable.toString(),
                                            reference= binding.edtReference.text.toString().trim()

                                        )
                                        payAmountViewModel.sendMoney(sendMoneyRequest)
                                        bindingBottomSheet.tpinView.resetState()
                                        bindingBottomSheet.tpinView.setOTP("")

                                    }

                                    if (isMultiPay == "no") {
                                        singleUserSend(receiverContact!!)
                                    } else {

                                        receiverList.clear()
                                        for (i in PayNowFragment.SELECTED_TXN_PAYEES.indices) {
                                            receiverList.add(
                                                SendMoneyRequest.UserReceive(
                                                    PayNowFragment.SELECTED_TXN_PAYEES[i].mobNo
                                                )
                                            )
                                        }

                                        if (receiverList.size < 2) {
                                            singleUserSend(receiverList[0].receivePhoneNo)
                                        } else {
                                            if (binding.edtAmount.text.toString().trim().isDecimal()
                                                    .not()
                                            ) {
                                                hideLoading()
                                                bottomSheetVerifyTpin.dismiss()
                                                showToast("enter correct amount.")
                                                return@setPositiveButton
                                            }
                                            val sendMoneyRequest = SendMoneyRequest(
                                                binding.edtAmountMulti.text.toString().trim().replace(",","")
                                                    .toDouble(),
                                                pref().getStringValue(
                                                    SharePreferences.USER_WALLET_UUID,
                                                    ""
                                                ),
                                                true,
                                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                                receiverList,
                                                pref().getStringValue(
                                                    SharePreferences.USER_TOKEN,
                                                    ""
                                                ),
                                                chargeable = "True",
                                                        reference= binding.edtReferenceMulti.text.toString().trim()

                                            )
                                            payAmountViewModel.sendMoney(sendMoneyRequest)
                                            bindingBottomSheet.tpinView.resetState()
                                            bindingBottomSheet.tpinView.setOTP("")
                                        }
                                    }

                                }
                                setNegativeButton(
                                    "No"
                                ) { dialog, which ->
                                    dialog.dismiss()

                                }
                            }
                            errorDialog.show()
                        }
                        /*if (it.errorCode == "106") {
                            (requireActivity() as BconfigActivity).logout()
                        }*/
                    }
                    payAmountViewModel.observerResponse.value = null


                }
                is ApprovalResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (statusRequest == 1) {
                            //  singleUserSend(receiverContact!!)
                            findNavController().navigate(
                                R.id.action_payAmountFragment_to_transcationStatusFragment_single,
                                bundleOf(IS_MULTI_PAY to "no", MESSAGE to "Payment successful")
                            )
                        } else {
                            requireActivity().finish()
                        }
                    }
                    payAmountViewModel.observerResponse.value = null
                }
                is CheckValidTpinResponse -> {
                    hideLoading()

                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showLoading()


                        if (isMultiPay == "no") {
                            if (payMode!!.equals(
                                    Constants.PayType.REQUESTER_SEND_MONEY.name,
                                    true
                                )
                            ) {

                                /* statusRequest =1
                                   payAmountViewModel.statusChange(
                                           ApprovalRequest(
                                               pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                               pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                               requestId.toInt(),
                                               receiverId!!.toInt(),
                                               1
                                           )
                                       )*/
                                singleUserSend(receiverContact!!)
                            } else {
                                singleUserSend(receiverContact!!)
                            }
                        } else {

                            receiverList.clear()
                            for (i in PayNowFragment.SELECTED_TXN_PAYEES.indices) {
                                receiverList.add(SendMoneyRequest.UserReceive(PayNowFragment.SELECTED_TXN_PAYEES[i].mobNo))
                            }

                            if (receiverList.size < 2) {
                                singleUserSend(receiverList[0].receivePhoneNo)
                            } else {

                                if (binding.edtAmountMulti.text.toString().trim().isDecimal().not()) {
                                    hideLoading()
                                    bottomSheetVerifyTpin.dismiss()
                                    showToast("enter correct amount.")
                                    return@observe
                                }
                                val sendMoneyRequest = SendMoneyRequest(
                                    binding.edtAmountMulti.text.toString().trim().replace(",","").toDouble(),
                                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                                    true,
                                    pref().getStringValue(SharePreferences.USER_ID, ""),
                                    receiverList,
                                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                    reference= binding.edtReferenceMulti.text.toString().trim()


                                )
                                payAmountViewModel.sendMoney(sendMoneyRequest)
                                bindingBottomSheet.tpinView.resetState()
                                bindingBottomSheet.tpinView.setOTP("")
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    payAmountViewModel.observerResponse.value = null


                }
                is FailResponse -> {
                    bottomSheetVerifyTpin.dismiss()
                    hideLoading()
                    showToast(it.message)
                    payAmountViewModel.observerResponse.value = null
                }
                is Throws -> {
                    hideLoading()
                    payAmountViewModel.observerResponse.value = null
                }
            }
        }
    }

    fun timerHideLoading() {
        val timer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                hideLoading()
                showToast("time out")
            }
        }
        timer.start()
    }

    private fun singleUserSend(receiverContact: String) {
        if (binding.edtAmount.text.toString().trim().isDecimal().not()) {
            hideLoading()
            bottomSheetVerifyTpin.dismiss()
            showToast("enter correct amount.")
            return
        }
        receiverList.clear()
        receiverList.add(SendMoneyRequest.UserReceive(receiverContact))
        val sendMoneyRequest = SendMoneyRequest(
            binding.edtAmount.text.toString().trim().replace(",","").toDouble(),
            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
            true,
            pref().getStringValue(SharePreferences.USER_ID, ""),
            receiverList,
            pref().getStringValue(SharePreferences.USER_TOKEN, ""),

            reference= binding.edtReference.text.toString().trim()

        )
        payAmountViewModel.sendMoney(sendMoneyRequest)
        bindingBottomSheet.tpinView.resetState()
        bindingBottomSheet.tpinView.setOTP("")

    }


    private fun happyThemeChanges() {
        /*  if (BaseActivity.isCustomMode) {
              binding.apply {
                  topLayout.setBackgroundResource(R.color.yellow_500)
              }
          }*/
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                // binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cardMainMulti.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSendMulti.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnRequestMulti.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnRequest.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnReject.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                //  binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)

                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.cardMainMulti.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSendMulti.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnReject.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                binding.btnRequestMulti.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnRequest.setBackgroundResource(R.drawable.bg_gradient_light_btn)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                //  binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.cardMainMulti.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSendMulti.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnRequestMulti.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnRequest.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnReject.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)


            }
        }
    }

    companion object {
        const val RECEIVER_ID = "receiver_id"
        const val RECEIVER_NAME = "receiver_name"
        const val RECEIVER_AMT = "receiver_amt"
        const val RECEIVER_CONTACT = "receiver_contact"
        const val RECEIVER_IMG = "receiver_img"
        const val IS_MULTI_PAY = "is_multi_pay"
        const val MESSAGE = "message"
        const val REQUEST_ID = "request_id"
        const val REQUEST_MONEY = "request_money"
    }

    override fun getLayoutId(): Int = R.layout.pay_amount_fragment

    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): PayAmountViewModel = payAmountViewModel
}