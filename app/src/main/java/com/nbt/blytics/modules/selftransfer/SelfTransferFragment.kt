package com.nbt.blytics.modules.selftransfer

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetVerifyTpinBinding
import com.nbt.blytics.databinding.SelfTransferFragmentBinding
import com.nbt.blytics.modules.acinfo.AcInfoAdapter
import com.nbt.blytics.modules.acinfo.AcInfoResponse
import com.nbt.blytics.modules.acinfo.AllAcInfoFragment
import com.nbt.blytics.modules.allaccount.AllAccountAdapter
import com.nbt.blytics.modules.allaccount.AllAccountModel
import com.nbt.blytics.modules.allaccount.LinkedAccResponse
import com.nbt.blytics.modules.allaccount.LinkedAccountAdapter
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.payee.schedulecreate.ScheduleCrateRes
import com.nbt.blytics.modules.payee.schedulecreate.ScheduleCreateFragment
import com.nbt.blytics.modules.payee.schedulecreate.SelfSchedule
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelfTransferFragment : BaseFragment<SelfTransferFragmentBinding, SelfTransferViewModel>() {

    private val selfTransferViewModel: SelfTransferViewModel by viewModels()
    private lateinit var binding: SelfTransferFragmentBinding
    private val acList = mutableListOf<AcInfoResponse.Data>()
    private var uuid: String = ""
    private var uuidFrom=""
    private var uuidTo=""
    private var acHolderName=""
    private lateinit var bottomSheetVerifyTpin: BottomSheetDialog
    private lateinit var bindingBottomSheet: BottomSheetVerifyTpinBinding
    private var selectedAcc: String = ""
    private lateinit var  acInfoAdapter : AcInfoAdapter
    private var isWallet = false
    private val linkedAccList: MutableList<LinkedAccResponse.AccList> = mutableListOf()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        setLinkedAccAdapter()
        binding.btnPickFromAc.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.SELF_TXN)
            intent.putExtra(Constants.RESULT_CODE, 123)
            resultLauncher.launch(intent)
        }
        binding.btnPickToAc.setOnClickListener {
           //binding.accountFlipper.showNext()
            if(binding.edtFromAc.text.toString().isBlank()){
                showToast("Select from account.")
                return@setOnClickListener
            }
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
            intent.putExtra(Constants.ACC_TYPE, Constants.SELF_TXN)
            intent.putExtra(Constants.SELECTED_AC, binding.edtFromAc.text.toString().trim())
            intent.putExtra(Constants.RESULT_CODE, 1234)
            resultLauncher.launch(intent)
        }

        verifyTpinBottomSheet()
        happyThemeChanges()
        scheduleFun()
        binding.btnSend.setOnClickListener {
            if (validation()) {
                if (binding.today.isChecked) {
                    bottomSheetVerifyTpin.show()
                } else {
                    val selectedDate = binding.textView7.text.toString()
                    if (selectedDate != "Change Date") {
                        schedulePayment(selectedDate)
                    } else {
                        showToast("Please select a date to schedule the payment.")
                    }
                }
            }
        }

        val currentUuid = pref().getStringValue(SharePreferences.DEFAULT_ACCOUNT, "")
        uuidFrom =  pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
        binding.edtFromAc.setText(currentUuid)
    }

    private fun schedulePayment(selectedDate: String) {
        val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val userId = pref().getStringValue(SharePreferences.USER_ID, "").toIntOrNull() ?: 0
        val scheduleAmount = binding.edtAmount.text.toString()
        val name = acHolderName
        val reference =binding.edtReference.text.toString()
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        val endDate = dateFormatOutput.format(dateFormatInput.parse(selectedDate) ?: "")
        val startDate = dateFormatOutput.format(dateFormatInput.parse(selectedDate) ?: "")
        val uuidSend = uuidFrom
        val uuidReceive = uuidTo
        val txnType = "self_transaction"
        val frequency = "Once"
        val tpinVerified = true

        val selfSchedule = SelfSchedule(
            userId =  userId,
            scheduleAmount = scheduleAmount ,
            name=  name,
            reference= reference,
            userToken= userToken,
            endDate= endDate,
            startDate=   startDate,
            uuidSend =  uuidSend,
            uuidReceive =uuidReceive,
            txnType = txnType,
            frequency= frequency,
            tpinVerified =tpinVerified
        )
        selfTransferViewModel.selfSchedule(selfSchedule)
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

    val resultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.let {
            val selectedAC=  it.getStringExtra(AllAcInfoFragment.SELECTED_AC)
            val uuid =it.getStringExtra(AllAcInfoFragment.SELECTED_UUID)
            val acHolder =it.getStringExtra(AllAcInfoFragment.AC_HOLDER)
            if(result.resultCode==123){
                uuidFrom = uuid!!
                binding.edtFromAc.setText(selectedAC)
            }
            if(result.resultCode==1234){
                acHolderName = acHolder!!
                uuidTo = uuid!!
                binding.edtToAc.setText(selectedAC)
            }
        }
    }
    private fun validation(): Boolean {
        binding.apply {
            if (edtFromAc.text.toString().trim().isBlank()) {
                showToast("Enter acc number")
                return false
            }
            if (edtToAc.text.toString().trim().isBlank()) {
                showToast("Enter acc number")
                return false
            }
            if (edtAmount.text.toString().trim().isBlank()) {
                showToast("Enter amount")
                return false
            }
            if (edtReference.text.toString().trim().isBlank()) {
                showToast("Enter reference")
                return false
            }
            return true
        }
    }

    private fun backView() {
        fun back(){
            binding.accountFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
            binding.accountFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
            binding.accountFlipper.showPrevious()
            hideSoftKeyBoard()
        }
        if( isWallet.not()) {
            back()
        }else{
            if( binding.accountFlipper.displayedChild ==2) {
                binding.accountFlipper.displayedChild = 0
            }else{
                back()
            }
        }
    }

    private fun setLinkedAccAdapter() {
      val  lindedAdapter = LinkedAccountAdapter(requireContext(), linkedAccList) {it, view ->
            uuid = linkedAccList[it].accUuid
            selectedAcc = linkedAccList[it].accNo
            binding.accountFlipper.showNext()
        }
        binding.allAccountRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allAccountRecycler.adapter = lindedAdapter
    }

    private fun allAccountRecycler(acType: String) {
        val adapter = AllAccountAdapter(requireContext(), selfTransferViewModel.allAccountList, acType) { acc, purpose, acc_uid ->
            uuid = acc_uid
            selectedAcc = acc
            binding.accountFlipper.showNext()
            binding.edtAmount.setText("")
        }
        binding.allAccountRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allAccountRecycler.adapter = adapter
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.accountFlipper.setBackgroundResource(R.color.b_bg_color_dark)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.accountFlipper.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnSend.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                bindingBottomSheet.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.accountFlipper.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }

    private fun verifyTpinBottomSheet() {
        var isKeyboardVisible:Boolean =false
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
                lblTpin.text=""
                btnVerify.setOnClickListener {
                    if (validation()) {
                        val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        val uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
                        userId.let { uID ->
                            showLoading()
                            selfTransferViewModel.checkValidTPIN(uID, tpinView.otp.toString(), uuid, userToken)
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
           /* bindingBottomSheet.tpinView.setOnClickListener {
                if (!isKeyboardVisible){
                    showSoftKeyBoard(bindingBottomSheet.tpinView)
                    bindingBottomSheet.tpinView.requestFocus()
                }
            }
            KeyboardVisibilityEvent.setEventListener(
                requireActivity(),
                viewLifecycleOwner
            ) { isOpen -> isKeyboardVisible = isOpen }*/
        }
    }

    private fun observer() {
        selfTransferViewModel.observerResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ScheduleCrateRes -> {
                    Toast.makeText(requireContext(), "Payment scheduled", Toast.LENGTH_SHORT).show()
                    requireActivity().finish()
                }

                is FailResponse -> {
                    Toast.makeText(requireContext(), "Failed to schedule payment: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        selfTransferViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CheckValidTpinResponse -> {
                    hideLoading()
                    bottomSheetVerifyTpin.dismiss()
                    bindingBottomSheet.tpinView.resetState()
                    bindingBottomSheet.tpinView.setOTP("")
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showLoading()
                        selfTransferViewModel.sendMoney(
                            SelfTxnRequest(
                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                uuidTo,
                                uuidFrom,
                                false,
                                binding.edtAmount.text.toString().trim(),
                                reference = binding.edtReference.text.toString().trim()))
                    } else {
                        showToast(it.message)
                    }
                    selfTransferViewModel.observerResponse.value = null
                }

                is AcInfoResponse -> {
                    hideLoading()
                    acList.clear()
                    acList.addAll(it.list)
                    acInfoAdapter.notifyDataSetChanged()
                    selfTransferViewModel.observerResponse.value = null
                }

                is WalletResponse -> {
                    hideLoading()
                    uuid = it.uuid
                    selectedAcc = it.acc_no
                    Log.d("WalletFilpper_bef", binding.accountFlipper.displayedChild.toString())
                    binding.accountFlipper.showNext()
                    binding.accountFlipper.showNext()
                    Log.d("WalletFilpper_aft", binding.accountFlipper.displayedChild.toString())
                    selfTransferViewModel.observerResponse.value = null
                }

                is LinkedAccResponse -> {
                    hideLoading()
                    if (it.data.acc_list.isEmpty()) {
                        showToast("please create an account.")
                    } else {
                        binding.accountFlipper.showNext()
                        linkedAccList.clear()
                        linkedAccList.addAll(it.data.acc_list)
                        setLinkedAccAdapter()
                    }
                    selfTransferViewModel.observerResponse.value = null
                }

                is AllAccountModel -> {
                    hideLoading()
                    if (it.data.isEmpty()) {
                        showToast("please create an account.")
                    }
                    selfTransferViewModel.observerResponse.value = null
                }

                is SelfTxnResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        findNavController().navigate(R.id.transcationStatusFragment, bundleOf(PayAmountFragment.IS_MULTI_PAY to "no", PayAmountFragment.MESSAGE to it.message))
                    } else {
                        if (it.data!!.chargeable) {
                            val errorDialog = MaterialAlertDialogBuilder(requireContext())
                            errorDialog.apply {
                                //setTitle()
                                setMessage("${it.message}\nDo you want to proceed?")
                                setCancelable(false)
                                setPositiveButton("Yes") { dialog, which ->
                                    dialog.dismiss()
                                    showLoading()
                                    binding.apply {
                                        showLoading()
                                        selfTransferViewModel.sendMoney(
                                            SelfTxnRequest(
                                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                                uuidTo,
                                                uuidFrom,
                                                true,
                                                edtAmount.text.toString().trim(),
                                                reference = binding.edtReference.text.toString().trim()))
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
                    selfTransferViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    selfTransferViewModel.observerResponse.value = null
                }

                is Throwable -> {
                    showToast(it.message.toString())
                    hideLoading()
                    selfTransferViewModel.observerResponse.value = null

                }
            }
        }
    }
    override fun getLayoutId(): Int = R.layout.self_transfer_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): SelfTransferViewModel = selfTransferViewModel
}