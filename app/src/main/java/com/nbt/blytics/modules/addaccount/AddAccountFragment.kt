package com.nbt.blytics.modules.addaccount

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.AddAccountFragmentBinding
import com.nbt.blytics.modules.addaccount.models.CreateAcRequest
import com.nbt.blytics.modules.addaccount.models.CreateAcResponse
import com.nbt.blytics.modules.addaccount.models.CreateCurrentAcRequest
import com.nbt.blytics.modules.addaccount.models.CreateCurrentResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class AddAccountFragment : BaseFragment<AddAccountFragmentBinding, AddAccountViewModel>() {
    private val addAccountViewModel: AddAccountViewModel by viewModels()
    private lateinit var binding: AddAccountFragmentBinding
    private var btn_saving_acselectedDate: String = ""
    private var selectAcName = ""
    private var selectedDate: String = ""
    lateinit var pref: SharePreferences

    /*var accountNumber=""
    var accountPurpose=""*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        pref = SharePreferences.getInstance(requireContext())
        happyThemeChanges()
        fillData()
        observer()
        hideLoading()
        binding.btnPickDate.setOnClickListener {
            datePicker()
        }

        if (arguments != null) {
            if (requireArguments().getString(Constants.COMING_FOR).equals(ComingFor.HOME.name)) {
                if (requireArguments().getString(Constants.ACC_TYPE).equals(Constants.SAVING_ACC)) {
                    resetField()
                    selectAcName = "Saving Account"
                    binding.lblTitle.text = "Add $selectAcName"
                    accountNextView()
                } else if (requireArguments().getString(Constants.ACC_TYPE).equals(Constants.CURRENT_ACC)) {
                    resetCurrentAcField()
                    selectAcName = "Current Account"
                    binding.lblTitle.text = "Add $selectAcName"
                    accountNextView()
                    accountNextView()
                }
            }
        }

        binding.apply {
            btnCreateAcCurrent.setOnClickListener {
                if (validationCurrentAc()) {
                    val pin = edtPinCurrent.text.toString()
                    val isDefault = false
                    val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                    val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                    showLoading()
                    val request = CreateCurrentAcRequest(userId, userToken, isDefault, pin)
                    addAccountViewModel.addCurrentAccount(request)
                }
            }

            btnCreateAc.setOnClickListener {
                if (validation()) {
                    val acPurpose = edtAcName.text.toString()
                    val pin = edtPin.text.toString()
                    val isDefault = setDefault(chkDefault)
                    val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                    val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                 //   val date = selectedDate
                  //  val request = CreateAcRequest(userId, userToken, acPurpose, isDefault, date, pin)
                    showLoading()
                    val request = CreateAcRequest(userId, userToken, acPurpose, isDefault, pin)
                    addAccountViewModel.addAccount(request)
                }
            }

            btnSavingAc.setOnClickListener {
                showCratePage(0)
            }
            btnCurrentAc.setOnClickListener {
                showCratePage(1)
            }
            btnLinkedAc.setOnClickListener {
                showCratePage(2)
            }
            /*    btnWalletAc.setOnClickListener {
                    resetField()
                    showToast("coming soon")
                    return@setOnClickListener
                    selectAcName = "Wallet Account"
                    lblTitle.text = "Add $selectAcName"
                    accountNextView()
                }*/
        }

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (requireArguments().getString(Constants.COMING_FOR).equals(ComingFor.HOME.name)) {
                        remove()
                        requireActivity().onBackPressed()
                    } else if (binding.accountFlipper.displayedChild == 0) {
                        remove()
                        requireActivity().onBackPressed()
                    } else {
                        if (binding.accountFlipper.displayedChild == 1) {
                            backView()
                        } else {
                            binding.accountFlipper.displayedChild = 0
                        }
                    }

                   when(binding.accountFlipper.displayedChild){
                       0 ->{
                           (requireContext() as BconfigActivity).setToolbarTitle("New account")
                       }
                       1 ->{
                           (requireContext() as BconfigActivity).setToolbarTitle("Add saving account")
                       }
                       2 ->{
                           (requireContext() as BconfigActivity).setToolbarTitle("Add current account")
                       }
                   }
                }
        })
    }

    private fun backView() {
        binding.accountFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.accountFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
        binding.accountFlipper.showPrevious()
        hideSoftKeyBoard()
    }

    fun setDefault(img: ImageView): Boolean {
        return img.tag != "false"
    }

    private fun resetField() {
        binding.apply {
            edtAcName.text?.clear()
            //edtBvn.text?.clear()
            edtPin.text?.clear()
            edtConfirmPin.text?.clear()
            edtDobDate.text?.clear()
            // chkDefault.isChecked = false
        }
    }

    private fun resetCurrentAcField() {
        binding.apply {
            edtPinCurrent.text?.clear()
            edtConfirmPinCurrent.text?.clear()
            //chkDefaultCurrent.isChecked = false
        }
    }

    private fun validation(): Boolean {
        binding.apply {
            if (edtAcName.text.toString().trim().isBlank()) {
                showToast("Enter account purpose.")
                return false
            }
            if (edtPin.text.toString().trim().isBlank()) {
                showToast("Enter pin.")
                return false
            }
            if (edtPin.text.toString().trim().length != 4) {
                showToast("Enter pin should be 4 digit.")
                return false
            }
            if (edtConfirmPin.text.toString().trim().isBlank()) {
                showToast("Enter confirm Tpin")
                return false
            }
            if (edtConfirmPin.text.toString() != edtPin.text.toString()) {
                showToast("Confirm Tpin does not match")
                return false
            }
           /* if (selectedDate.isBlank()) {
                showToast("Select withdrawal date ")
                return false
            }*/
        }
        return true
    }

    private fun validationCurrentAc(): Boolean {
        binding.apply {
            if (edtPinCurrent.text.toString().trim().isBlank()) {
                showToast("Enter pin.")
                return false
            }
            if (edtPinCurrent.text.toString().trim().length != 4) {
                showToast("Enter pin should be 4 digit.")
                return false
            }
            if (edtConfirmPinCurrent.text.toString().trim().isBlank()) {
                showToast("Enter confirm Tpin")
                return false
            }
            if (edtConfirmPinCurrent.text.toString() != edtPinCurrent.text.toString()) {
                showToast("Confirm Tpin does not match")
                return false
            }
        }
        return true
    }

    private fun observer() {
        addAccountViewModel.observerResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is AddAccountViewModel.BalanceResponse -> {
                    if (response.data) {
                      //  binding.accountFlipper.show()
                     //   binding.lytCreate.hide()
                        (requireContext() as BconfigActivity).setToolbarTitle("Add linked account")
                        findNavController().navigate(R.id.phoneRegistrationFragment3, bundleOf(Constants.COMING_FOR to ComingFor.LINK_AC_PHONE_VERIFY.name, Constants.ACC_TYPE to Constants.LINKED_ACC))
                    } else {
                        Toast.makeText(requireContext(), "Insufficient balance", Toast.LENGTH_SHORT).show()
                    }
                }
                is FailResponse -> {
                    showToast(response.message)
                }
                else -> {
                    Log.e("Error", "Unexpected response: $response")
                }
            }
        }

        addAccountViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CreateCurrentResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        pref.setStringValue(pref.DEFAULT_ACCOUNT, it.data!!.accNo)
                        resetField()
                        requireActivity().onBackPressed()
                    } else {
                        showToast(it.message)
                    }
                    addAccountViewModel.observerResponse.value = null
                }

                is CreateAcResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        pref.setStringValue(pref.DEFAULT_ACCOUNT, it.data!!.accNo)
                        pref.setStringValue(pref.DEFAULT_PURPOSE, it.data.purpose)
                        //backView()
                        /* requireActivity().finish()
                         fragmentChangeLiveData.value = ComeBack.PAYMENT_FRAGMENT*/
                        resetField()
                        requireActivity().onBackPressed()
                    } else {
                        showToast(it.message)
                    }
                    addAccountViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    addAccountViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    hideLoading()
                    addAccountViewModel.observerResponse.value = null
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillData() {
        pref().apply {
            binding.edtFullName.setText("${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}")
            binding.edtFullNameCurrent.setText("${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}")
        }
    }

    private fun showCratePage(i: Int) {
        if (i == 2){
            binding.view.visibility = View.VISIBLE
        }

        val msgArr = arrayListOf<String>(
            "Please create a saving account",
            "Please create a current account",
            "Please create an account you can link to other people"
        )

        val msgWarningArr = arrayListOf<String>(
            "",
            "For added security, please set a PIN for you transactions. It helps keep your account secured.",
            "This account can be linked to family. friends, or co-workers, allowing them to access fund you manage, set up allowances, view activity, and more"
        )

        val imgArr = arrayListOf(
            R.drawable.dummy_user,
            R.drawable.current,
            R.drawable.linked_account
        )

        binding.accountFlipper.hide()
        binding.lytCreate.show()
        binding.lblTitleCrateAc.text = "Hey'${pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")}',\n\n${msgArr[i]}"
        binding.extraMoney2.text = "Hey'${pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")}',\n\n${msgWarningArr[i]}"
        binding.worningImg.setImageResource(imgArr[i])
        binding.imgUserProfile.setImage(pref().getStringValue(SharePreferences.USER_PROFILE_IMAGE, ""))
        (requireContext() as BconfigActivity).setToolbarTitle("Create account")
        binding.btnCreate.setOnClickListener {
            binding.apply {
                when (i) {
                    0 -> {
                        binding.accountFlipper.show()
                        binding.lytCreate.hide()
                        resetField()
                        selectAcName = "Saving Account"
                        lblTitle.text = "Add $selectAcName"
                        accountNextView()
                        (requireContext() as BconfigActivity).setToolbarTitle("Add saving account")
                    }
                    1 -> {
                        binding.accountFlipper.show()
                        binding.lytCreate.hide()
                        resetCurrentAcField()
                        selectAcName = "Current Account"
                        lblTitle.text = "Add $selectAcName"
                        binding.accountFlipper.displayedChild = 2
                        binding.card4.visibility= View.VISIBLE
                        binding.worningImg1.setImageResource(R.drawable.current)
                        binding.worningImg1.visibility = View.VISIBLE
                        binding.extraMoney3.visibility= View.VISIBLE
                        binding.extraMoney3.text = "For added security, please set a PIN for you transactions. It helps keep your account secured."
                        (requireContext() as BconfigActivity).setToolbarTitle("Add current account")
                    }
                    2 -> {
                        gio()
                        //  binding.accountFlipper.show()
                        //   binding.lytCreate.hide()
                     //   (requireContext() as BconfigActivity).setToolbarTitle("Add linked account")
                       // findNavController().navigate(R.id.phoneRegistrationFragment3, bundleOf(Constants.COMING_FOR to ComingFor.LINK_AC_PHONE_VERIFY.name, Constants.ACC_TYPE to Constants.LINKED_ACC))
                      //  checkAndNavigate()
                    }
                }
            }
        }
    }

    private fun gio() {
          binding.accountFlipper.show()
           binding.lytCreate.hide()
        (requireContext() as BconfigActivity).setToolbarTitle("Add linked account")
        findNavController().navigate(R.id.phoneRegistrationFragment3, bundleOf(Constants.COMING_FOR to ComingFor.LINK_AC_PHONE_VERIFY.name, Constants.ACC_TYPE to Constants.LINKED_ACC))
    }

    private fun checkAndNavigate() {
        //Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
        val userId = pref().getStringValue(SharePreferences.USER_ID, "").toIntOrNull()
        val uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
        if (userId != null && uuid != null) {
            try {
                addAccountViewModel.checkForBalance(userId, uuid)
            } catch (e: Exception) {
                Log.e("Error", "Exception: ${e.message}")
                Toast.makeText(requireContext(), "Error checking balance", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Invalid User ID or Wallet UUID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun datePicker() {
        val nextDateCalender = Calendar.getInstance()
        val dateValidator: CalendarConstraints.DateValidator = DateValidatorPointForward.from(nextDateCalender.timeInMillis)
        val calendarStart = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val startData = format.format(calendarStart.time)
        /*  val startYear = startData.split("-")[0]
          val  startMonth= startData.split("-")[1]
          val  startDay= startData.split("-")[2]*/
        val calendarEnd = Calendar.getInstance()
        calendarEnd.add(Calendar.YEAR, +20)
        val endData = format.format(calendarEnd.time)
        /*  val  endYear= endData.split("-")[0]
          val  endMonth= endData.split("-")[1]
          val  endDay= endData.split("-")[2]*/
        val constraintsBuilder = CalendarConstraints.Builder().setStart(calendarStart.timeInMillis).setEnd(calendarEnd.timeInMillis)
        constraintsBuilder.setValidator(dateValidator)
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.setCalendarConstraints(constraintsBuilder.build()).build()
        picker.show(requireActivity().supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val current = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dateString = dateFormat.format(calendar.time)
            val birthDate = dateFormat.parse(dateString)
            if (current.before(birthDate).not()) {
                showToast("Please select valid withdrawal date")
                binding.edtDobDate.setText("")
            } else {
                selectedDate = dateString
                val showDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
                val showDate = showDateFormat.format(calendar.time)
                binding.edtDobDate.setText(showDate)
            }
        }
    }

    private fun accountNextView() {
        binding.accountFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
        binding.accountFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
        binding.accountFlipper.showNext()
        hideSoftKeyBoard()
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                edtFullName.setBgTintHappyTheme()
                edtAcNumber.setBgTintHappyTheme()
                edtPin.setBgTintHappyTheme()
                //   edtBvn.setBgTintHappyTheme()
                edtConfirmPin.setBgTintHappyTheme()
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnCreate.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnCreateAcCurrent.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.cardAc.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cvSaving.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cvCurrent.setCardBackgroundColor(resources.getColor(R.color.black))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnCreate.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnCreateAcCurrent.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardAc.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cvSaving.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cvCurrent.setCardBackgroundColor(resources.getColor(R.color.white))
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnCreate.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnCreateAcCurrent.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.cardAc.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cvSaving.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cvCurrent.setCardBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.add_account_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): AddAccountViewModel = addAccountViewModel
}