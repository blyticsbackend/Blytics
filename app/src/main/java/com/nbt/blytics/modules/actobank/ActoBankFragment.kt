package com.nbt.blytics.modules.actobank

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ActoBankFragmentBinding
import com.nbt.blytics.databinding.BottomSheetVerifyTpinBinding
import com.nbt.blytics.modules.acinfo.AllAcInfoFragment
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.UtilityHelper.isDecimal
import com.nbt.blytics.utils.hide
import java.util.Calendar

class ActoBankFragment : BaseFragment<ActoBankFragmentBinding, ActoBankViewModel>() {
    private val actoBankViewModel: ActoBankViewModel by viewModels()
    private lateinit var binding: ActoBankFragmentBinding
    private lateinit var bottomSheetVerifyTpin: BottomSheetDialog
    private lateinit var bindingBottomSheet: BottomSheetVerifyTpinBinding
    private var isComingForSchedule = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        AC_TO_BANK_SCHEDULE = null
        hideLoading()
        observer()
        setupAutoCheckOnFocusLost()
        try {
            val comingFor = requireArguments().getString(Constants.COMING_FOR)
            if (comingFor.equals(ComingFor.AC_TO_BANK.name)) {
                val accNo = requireArguments().getString(RECEIVER_AC_NUMBER)
                val receiverName = requireArguments().getString(RECEIVER_NAME)
                val bankCode = requireArguments().getString(BANK_CODE)
                val amount = requireArguments().getString(AMOUNT)
                binding.apply {
                    edtFromAc.setText(pref().getStringValue(SharePreferences.DEFAULT_ACCOUNT, ""))
                    edtToAc.setText(accNo)
                    //edtBankCode.setText(bankCode)
                    edtAmountHolderName.setText(receiverName)
                    edtAmount.setText(amount)
                }
            }
        } catch (ex: Exception) {}

        try {
            isComingForSchedule = requireArguments().getBoolean(COMING_FOR_SCHEDULE, false)
            if (isComingForSchedule) {
                val scheduleFromAc = requireArguments().getString(SCHEDULE_FORO_AC, "")
                binding.edtFromAc.setText(scheduleFromAc)
            }
        } catch (ex: Exception) {
        }
        binding.checkAc.setOnClickListener {
            showLoading()
            actoBankViewModel.checkAcc(
                CheckAcRequest(
                    userId = pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                    userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    accountnumber = binding.edtToAc.text.toString().trim()
                )
            )
        }

        binding.btnSubmit.setOnClickListener {
            if (validation()) {
                if (isComingForSchedule) {
                    val intent = Intent()
                    AC_TO_BANK_SCHEDULE = AcToBankScheduleModel(
                        amount = binding.edtAmount.text.toString(),
                        fromAc = binding.edtFromAc.text.toString(),
                        toAc = binding.edtToAc.text.toString(),
                        reference = binding.edtReference.text.toString(),
                        acHolderName = binding.edtAmountHolderName.text.toString(),
                        backCode = binding.edtBankcode.text.toString(),
                        bankName = binding.edtBankname.text.toString())
                    requireActivity().setResult(12345, intent)
                    requireActivity().finish()
                } else {
                    bottomSheetVerifyTpin.show()
                    bindingBottomSheet.tpinView.setOTP("")
                    bindingBottomSheet.tpinView.resetState()
                    bindingBottomSheet.lblMsg.hide()
                }
            }
        }
        binding.btnPickFromAc.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.SELF_TXN)
            intent.putExtra(Constants.RESULT_CODE, 123)
            resultLauncher.launch(intent)
        }
        verifyTpinBottomSheet()
        happyThemeChanges()
        scheduleFun()
    }

    private fun setupAutoCheckOnFocusLost() {
        binding.edtToAc.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // If focus is lost
                val accountNumber = binding.edtToAc.text.toString().trim()
                if (accountNumber.isNotEmpty()) {
                    showLoading()
                    actoBankViewModel.checkAcc(
                        CheckAcRequest(
                            userId = pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                            userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            accountnumber = accountNumber
                        )
                    )
                }
            }
        }
    }

    private fun scheduleFun() {
        binding.today.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textView7.text = "Change Date"
            }
        }

        binding.textView7.setOnClickListener {
            // Display DatePickerDialog for scheduling a date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.textView7.text = selectedDate
                binding.today.isChecked = false
            }, year, month, day)

            // Set the minimum date to the current date
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let {
                val selectedAC = it.getStringExtra(AllAcInfoFragment.SELECTED_AC)
                val uuid = it.getStringExtra(AllAcInfoFragment.SELECTED_UUID)
                if (result.resultCode == 123) {
                    binding.edtFromAc.setText(selectedAC)
                }
            }
        }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.black))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.white))
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.card2.setCardBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }

    private fun observer() {
        actoBankViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CheckValidTpinResponse -> {
                    hideLoading()
                    bottomSheetVerifyTpin.dismiss()
                    // bindingBottomSheet.tpinView.setText("")
                    bindingBottomSheet.tpinView.setOTP("")
                    bindingBottomSheet.tpinView.resetState()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showLoading()
                        actoBankViewModel.sendMoneyBank(
                            SendMoneyBankRequest(
                                binding.edtAmount.text.toString(),
                                SendMoneyBankRequest.Euser(
                                    binding.edtBankcode.text.toString(),
                                    binding.edtToAc.text.toString(),
                                    binding.edtAmountHolderName.text.toString(),
                                    binding.edtBankname.text.toString()),
                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                                chargeable = false,
                                tpin_verified = true,
                                reference = binding.edtReference.text.toString().trim()
                            )
                        )
                    } else {
                        showToast(it.message)
                    }
                    actoBankViewModel.observerResponse.value = null
                }

                is SendMoneyBankRespone -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        findNavController().navigate(R.id.action_actoBankFragment_to_transcationStatusFragment, bundleOf(PayAmountFragment.IS_MULTI_PAY to "no", PayAmountFragment.MESSAGE to it.message))
                    } else {
                        if (it.data!!.chargeable) {
                            val errorDialog = MaterialAlertDialogBuilder(requireContext())
                            errorDialog.apply {
                                setTitle(it.message)
                                setMessage("Do you want to proceed?")
                                setCancelable(false)
                                setPositiveButton("Yes") { dialog, which ->
                                    dialog.dismiss()
                                    showLoading()
                                    binding.apply {
                                        showLoading()
                                        actoBankViewModel.sendMoneyBank(
                                            SendMoneyBankRequest(
                                                binding.edtAmount.text.toString(),
                                                SendMoneyBankRequest.Euser(
                                                    binding.edtBankcode.text.toString(),
                                                    binding.edtToAc.text.toString(),
                                                    binding.edtAmountHolderName.text.toString(),
                                                    binding.edtBankname.text.toString()),
                                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                                                chargeable = true,
                                                true
                                            )
                                        )
                                    }
                                }
                                setNegativeButton("No") { dialog, which ->
                                    dialog.dismiss()
                                }
                            }
                            errorDialog.show()
                        } else {
                            showToast(it.message)
                        }
                    }
                    actoBankViewModel.observerResponse.value = null
                }
                is CheckAcResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.edtAmountHolderName.setText(it.accountname)
                    }
                    actoBankViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    actoBankViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    hideLoading()
                    actoBankViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun validation(): Boolean {
        binding.apply {
            if (edtFromAc.text.toString().trim().isBlank()) {
                showToast("Select account number")
                return false
            }
            if (edtToAc.text.toString().trim().isBlank()) {
                showToast("Enter account number")
                return false
            }
            if (edtAmountHolderName.text.toString().trim().isBlank()) {
                showToast("Enter account holder name")
                return false
            }
            if (edtAmount.text.toString().trim().isBlank()) {
                showToast("Enter amount")
                return false
            }

            if (binding.edtAmount.text.toString().trim().isDecimal().not()) {
                showToast("Enter correct amount")
                return false
            }
            if (edtReference.text.toString().trim().isBlank()) {
                showToast("Enter reference")
                return false
            }
            return true
        }
    }

    private fun verifyTpinBottomSheet() {
        var isKeyboardVisible: Boolean = false
        bottomSheetVerifyTpin = BottomSheetDialog(requireContext()).apply {
            bindingBottomSheet = DataBindingUtil.inflate<BottomSheetVerifyTpinBinding>(layoutInflater, R.layout.bottom_sheet_verify_tpin, null, false).apply {
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
                            actoBankViewModel.checkValidTPIN(uID, tpinView.otp.toString(), uuid, userToken)
                        }
                    }
                }
            }
        }
    }

    companion object {
        val RECEIVER_AC_NUMBER = "receiver_ac_no"
        val BANK_CODE = "bank_code"
        val RECEIVER_NAME = "receiver_name"
        val AMOUNT = "amount"
        val COMING_FOR_SCHEDULE = "coming_for_schedule"
        val SCHEDULE_FORO_AC = "schedule_ac"
        var AC_TO_BANK_SCHEDULE: AcToBankScheduleModel? = null

    }

    data class AcToBankScheduleModel(
        val amount: String,
        val fromAc: String,
        val toAc: String,
        val acHolderName: String,
        val reference: String,
        val backCode: String,
        var bankName: String,
        var currency: String = "NGN"
    )
    override fun getLayoutId(): Int = R.layout.acto_bank_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): ActoBankViewModel = actoBankViewModel
}