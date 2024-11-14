package com.nbt.blytics.modules.payee.schedulecreate

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetScheduleBinding
import com.nbt.blytics.databinding.ScheduleCreateFragmentBinding
import com.nbt.blytics.modules.acinfo.AllAcInfoFragment
import com.nbt.blytics.modules.actobank.ActoBankFragment
import com.nbt.blytics.modules.actobank.ActoBankFragment.Companion.AC_TO_BANK_SCHEDULE
import com.nbt.blytics.modules.actobank.CheckAcRequest
import com.nbt.blytics.modules.actobank.CheckAcResponse
import com.nbt.blytics.modules.payee.schedule.ScheduleFragment
import com.nbt.blytics.modules.paynow.PayNowFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import java.util.Calendar

class ScheduleCreateFragment : BaseFragment<ScheduleCreateFragmentBinding, ScheduleCreateViewModel>() {
    private val scheduleCreateViewModel: ScheduleCreateViewModel by viewModels()
    private lateinit var binding: ScheduleCreateFragmentBinding
    private lateinit var bottomSheet: BottomSheetDialog
    private var userType = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uuidFrom = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        scheduleFun()
        binding.edtFromAc.setText(pref().getStringValue(SharePreferences.DEFAULT_ACCOUNT, ""))
        try {
            binding.edtToAc.setText(requireArguments().getString(ActoBankFragment.RECEIVER_AC_NUMBER))
            val comingForEdit = requireArguments().getBoolean(COMING_FOR_EDIT)
            if (comingForEdit) {
                val schedule = ScheduleFragment.SCHEDUEL!!
                editAble = true
                stateDate = schedule.nextDate
                endDate = schedule.endDate
                frequency = schedule.frequency
                schedulID = schedule.scheduleId
                binding.apply {
                    edtAmount.setText(schedule.amount)
                    edtReference.setText(schedule.reference)
                    edtAmountHolderName.setText(schedule.name)
                    if (schedule.type.equals("internal Schedule", true)) {
                        edtToAc.setText(schedule.mobNo)
                    } else {
                        edtToAc.setText(schedule.accountnumber)
                    }
                }
            } else {
                editAble = false
            }
        } catch (ex: Exception) {
        }

        binding.btnPickFromAc.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.SELF_TXN)
            intent.putExtra(Constants.RESULT_CODE, 123)
            resultLauncher.launch(intent)
        }

        binding.btnPickFromTo.setOnClickListener {
            bottomSheet()
        }

        binding.btnSubmit.setOnClickListener {
            if (validation()) {
                type = userType
                fromAc = binding.edtFromAc.text.toString().trim()
                toAc = binding.edtToAc.text.toString().trim()
                acHolderName = binding.edtAmountHolderName.text.toString().trim()
                reference = binding.edtReference.text.toString().trim()
                amount = binding.edtAmount.text.toString().trim()
                findNavController().navigate(R.id.setScheduleFragment)
            }
        }
        observer()
    }
    private fun scheduleFun() {
        binding.today.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textView7.text = "Change Date"
            }
        }

        binding.textView7.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.textView7.text = selectedDate
                binding.today.isChecked = false
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun validation(): Boolean {
        if (binding.edtFromAc.text.toString().isBlank()) {
            showToast("Select form account")
            return false
        }
        if (binding.edtToAc.text.toString().isBlank()) {
            showToast("Enter account")
            return false
        }
        if (binding.edtAmountHolderName.text.toString().isBlank()) {
            showToast("Select name")
            return false
        }
        if (binding.edtAmount.text.toString().isBlank()) {
            showToast("Select amount")
            return false
        }
        if (binding.edtReference.text.toString().isBlank()) {
            showToast("Select reference")
            return false
        }
        return true
    }

    private val resultExternalUser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // There are no request codes
            result.data?.let {
                if (result.resultCode == 12345) {
                    binding.edtToAc.setText(AC_TO_BANK_SCHEDULE?.toAc)
                    binding.edtFromAc.setText(AC_TO_BANK_SCHEDULE?.fromAc)
                    binding.edtAmountHolderName.setText(AC_TO_BANK_SCHEDULE?.acHolderName)
                    binding.edtReference.setText(AC_TO_BANK_SCHEDULE?.reference)
                    binding.edtAmount.setText(AC_TO_BANK_SCHEDULE?.amount)
                    AC_TO_BANK_SCHEDULE = null
                    binding.edtAmountHolderName.isFocusable = false
                    binding.edtAmountHolderName.isFocusableInTouchMode = false
                    binding.edtAmountHolderName.isClickable = false
                }
            }
        }

    private val resultSelectUser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // There are no request codes
            result.data?.let { intent ->
                if (result.resultCode == 1234) {
                    var userName = intent.getStringExtra("name")
                    //var acc  = intent.getStringExtra("acc")
                    var mobileNo = intent.getStringExtra("mobileNo")
                    binding.edtAmountHolderName.isFocusable = false
                    binding.edtAmountHolderName.isFocusableInTouchMode = false
                    binding.edtAmountHolderName.isClickable = false
                    listReceiverUser.clear()
                    listReceiverUser.add(InternalSchedule.UserReceive(mobileNo!!))
                    binding.edtToAc.setText(mobileNo)
                    binding.edtAmountHolderName.setText(userName)
                }
            }
        }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // There are no request codes
            result.data?.let {
                val selectedAC = it.getStringExtra(AllAcInfoFragment.SELECTED_AC)
                val uuid = it.getStringExtra(AllAcInfoFragment.SELECTED_UUID)
                if (result.resultCode == 123) {
                    uuidFrom = uuid!!
                    binding.edtFromAc.setText(selectedAC)
                }
                if (result.resultCode == 1234) {
                    uuidTo = uuid!!
                    binding.edtToAc.setText(selectedAC)
                    binding.edtAmountHolderName.setText(pref().getStringValue(SharePreferences.USER_FIRST_NAME, "") + " " + pref().getStringValue(SharePreferences.USER_LAST_NAME, ""))
                    userType = 2
                    binding.edtAmountHolderName.isFocusable = false
                    binding.edtAmountHolderName.isFocusableInTouchMode = false
                    binding.edtAmountHolderName.isClickable = false
                }
            }
        }
    private fun bottomSheet() {
        bottomSheet = BottomSheetDialog(requireContext()).apply {
            val bindingBottomSheet = DataBindingUtil.inflate<BottomSheetScheduleBinding>(layoutInflater, R.layout.bottom_sheet_schedule, null, false).apply {
                setContentView(root)
                happyThemeChanges(btnDismiss)
                btnBUser.setOnClickListener {
                    binding.edtAmountHolderName.setText("")
                    binding.edtToAc.setText("")
                    /* binding.edtAmountHolderName.setFocusable(false)
                       binding.edtAmountHolderName.setFocusableInTouchMode(false)
                       binding.edtAmountHolderName.setClickable(false) */
                    PayNowFragment.SELECTED_TXN_PAYEES.clear()
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_NOW.name)
                    intent.putExtra(Constants.PAY_MODE, Constants.PayType.SCHEDULE.name)
                    resultSelectUser.launch(intent)
                    dismiss()
                    userType = 0
                }

                btnSelf.setOnClickListener {
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.SELF_TXN)
                    intent.putExtra(Constants.SELECTED_AC, binding.edtFromAc.text.toString().trim())
                    intent.putExtra(Constants.RESULT_CODE, 1234)
                    resultLauncher.launch(intent)
                    dismiss()
                }

                btnExternal.setOnClickListener {
                    binding.edtAmountHolderName.setText("")
                    binding.edtToAc.setText("")
                    /*binding.edtAmountHolderName.setFocusable(true)
                    binding.edtAmountHolderName.setFocusableInTouchMode(true)
                    binding.edtAmountHolderName.setClickable(true)*/
                    val inflater: LayoutInflater = requireActivity().getLayoutInflater()
                    val dialogView: View = inflater.inflate(R.layout.dialog_bank_ac, null)
                    AlertDialog.Builder(requireContext()).setView(dialogView)
                        .setTitle("Add account number")
                        .setCancelable(false)/*.setMessage("need permission to scan code")*/
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                        }).setPositiveButton("ADD", DialogInterface.OnClickListener { dialogInterface, i ->
                                binding.edtToAc.setText(dialogView.findViewById<EditText>(R.id.edt_ac_number).text.toString())
                                showLoading()
                                scheduleCreateViewModel.checkAcc(CheckAcRequest(userId = pref().getStringValue(SharePreferences.USER_ID, "").toInt(), userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""), accountnumber = binding.edtToAc.text.toString().trim()))
                                dialogInterface.dismiss()
                            }).create().show()
                    dismiss()
                    userType = 1
                }

                btnDismiss.setOnClickListener {
                    dismiss()
                }
            }
        }
        if (bottomSheet.isShowing.not()) {
            bottomSheet.show()
        }
    }

    private fun happyThemeChanges(view: View) {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                view.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                view.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                view.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
            }
        }
    }

    companion object {
        val COMING_FOR_EDIT = "comingForEdit"
        val listReceiverUser = mutableListOf<InternalSchedule.UserReceive>()
        var amount: String = ""
        var acHolderName: String = ""
        var reference: String = ""
        var fromAc: String = ""
        var toAc: String = ""
        var stateDate = ""
        var endDate = ""
        var frequency = ""
        var type = -1
        var editAble = false
        var schedulID = ""
        var uuidFrom = ""
        var uuidTo = ""
    }

    private fun observer() {
        scheduleCreateViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CheckAcResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.edtAmountHolderName.setText(it.accountname)
                        binding.edtAmountHolderName.isFocusable = false
                        binding.edtAmountHolderName.isFocusableInTouchMode = false
                        binding.edtAmountHolderName.isClickable = false
                        binding.edtAmountHolderName.inputType = InputType.TYPE_NULL
                    } else {
                        binding.edtAmountHolderName.isFocusable = true
                        binding.edtAmountHolderName.isFocusableInTouchMode = true
                        binding.edtAmountHolderName.isClickable = true
                    }
                    scheduleCreateViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    scheduleCreateViewModel.observerResponse.value = null
                }

                is Throwable -> {
                    hideLoading()
                    scheduleCreateViewModel.observerResponse.value = null
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.schedule_create_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): ScheduleCreateViewModel = scheduleCreateViewModel
}