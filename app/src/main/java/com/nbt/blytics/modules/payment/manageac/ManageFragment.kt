package com.nbt.blytics.modules.payment.manageac

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.ManageFragmentBinding
import com.nbt.blytics.modules.allaccount.GetUpdateDefaultAcc
import com.nbt.blytics.modules.linkedac.manageac.GetManageRequest
import com.nbt.blytics.modules.linkedac.manageac.ManageResponse
import com.nbt.blytics.modules.linkedac.manageac.ResponseMangeAc
import com.nbt.blytics.modules.linkedac.manageac.ResponseMangeCurrentAc
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*
import java.text.SimpleDateFormat
import java.util.*

class ManageFragment : BaseFragment<ManageFragmentBinding, ManageViewModel>(),
    CompoundButton.OnCheckedChangeListener {
    private val manageViewModel: ManageViewModel by viewModels()
    private lateinit var binding: ManageFragmentBinding
    private var selectedWithdrawDate = ""
    private var selectedStartDate = ""
    private var selectedEndDate = ""
    private var withDrawYear = ""
    private var withDrawMonth = ""
    private var withDrawDay = ""

    private var startYear = ""
    private var startMonth = ""
    private var startDay = ""
    private var accType: String? = ""
    private var uuid:String =""
    private val modeArr = arrayOf<Int>(1, 0, 0)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        happyThemeChanges()
        selectedMode(binding.btnDaily)

        startTrackerShow()
        accType = requireArguments().getString(AC_TYPE)
        uuid = requireArguments().getString(UUID)!!

        if (accType!!.equals("SAVING", true)) {
            binding.apply {
                txtDefault.hide()
                btnSwDefault.hide()
            }
        }
        if (accType!!.equals("CURRENT", true)) {
            binding.apply {
                lblStartTracker.hide()
                profileStatusBar.hide()
                innerLyt.hide()
                txtWithdraw.hide()
                layoutFrameWithdraw.hide()
                binding.view.hide()
            }
        }
        if (accType!!.equals("WALLET", true)) {
            binding.apply {
                txtActive.hide()
                txtExpire.hide()
                txtWithdraw.hide()
                layoutFrameWithdraw.hide()
                btnSwExpire.hide()
                btnSwDeactive.hide()
                binding.view.hide()
                view2.hide()
                view3.hide()
                lblStartTracker.hide()
                profileStatusBar.hide()

            }
            showLoading()
            manageViewModel.checkDefaultWallet(
                acc_uuid = uuid/*pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/,
                userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                token = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
            )
        }
        if (accType!!.equals("WALLET", true).not()) {
            showLoading()
            manageViewModel.getMangeAcc(
                GetManageRequest(
                   uuid/* pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/,
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                ), accType!!

            )
        }

        binding.btnWithDrawDate.setOnClickListener {
            datePicker()
        }
        binding.btnDateStart.setOnClickListener {
            if (binding.edtWithDrawDate.text.toString().isNotBlank()) {
                    startdatePicker()
                    /*AlertDialog.Builder(requireContext())
                        //.setTitle("")
                        .setCancelable(false)
                        .setMessage("Your withdrawal date smaller than current date.")
                        .setPositiveButton(
                            "Back",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                requireActivity().onBackPressed()
                            })
                        .create()
                        .show()*/

            } else {
                showToast("Select Withdraw Date")
            }
        }
        binding.btnDateEnd.setOnClickListener {
            if (startYear.isNotBlank())
                enddatePicker()
        }
        /*  binding.lblStartTracker.setOnClickListener {
              startTrackerHide()
          }*/
        binding.btnSwDeactive.setOnCheckedChangeListener(this@ManageFragment)
        binding.btnSwExpire.setOnCheckedChangeListener(this@ManageFragment)
        binding.btnSwDefault.setOnCheckedChangeListener(this@ManageFragment)

        binding.btnDaily.setOnClickListener { view ->
            modeArr[0] = 1
            modeArr[1] = 0
            modeArr[2] = 0
            mode(binding.btnDaily)
        }

        binding.btnWeekly.setOnClickListener { view ->
            modeArr[0] = 0
            modeArr[1] = 1
            modeArr[2] = 0
            mode(binding.btnWeekly)
        }
        binding.btnMonthly.setOnClickListener { view ->
            modeArr[0] = 0
            modeArr[1] = 0
            modeArr[2] = 1
            mode(binding.btnMonthly)
        }

        binding.txtUpdate.setOnClickListener {
            if (validation()) {
                var savingBasis = 0
                for (i in modeArr.indices) {
                    if (modeArr[i] == 1) {
                        savingBasis = i
                    }
                }
                showLoading()
                manageViewModel.startTrackerUpdate(
                    StartTrackerRequest(
                        pref().getStringValue(SharePreferences.USER_ID, ""),
                        pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                        selectedStartDate,
                        selectedEndDate,
                        binding.edtAmountSave.text.toString().trim(),
                        savingBasis,
                   uuid    /* pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/
                    )
                )
            }
        }

    }

    private fun validation(): Boolean {
        binding.apply {
            if (edtAmountSave.text.toString().trim().isBlank()) {
                showToast("Enter account number")
                return false
            }
            if (selectedStartDate.trim().isBlank()) {
                showToast("Select start date")
                return false
            }
            if (selectedEndDate.trim().isBlank()) {
                showToast("Select end date")
                return false
            }
        }
        return true
    }

    private fun mode(view: TextView) {
        when (view) {
            binding.btnDaily -> {
                selectedMode(binding.btnDaily)
                unSelectedMode(binding.btnWeekly)
                unSelectedMode(binding.btnMonthly)
            }
            binding.btnWeekly -> {
                selectedMode(binding.btnWeekly)
                unSelectedMode(binding.btnDaily)
                unSelectedMode(binding.btnMonthly)
            }
            binding.btnMonthly -> {
                selectedMode(binding.btnMonthly)
                unSelectedMode(binding.btnWeekly)
                unSelectedMode(binding.btnDaily)
            }

        }
    }

    private fun selectedMode(view: TextView) {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                view.background = (resources.getDrawable(R.drawable.bg_gradient_orange_layout_bg))
                view.setTextColor(resources.getColor(R.color.white))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                view.background = (resources.getDrawable(R.drawable.bg_light_green))
                view.setTextColor(resources.getColor(R.color.white))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                view.background = (resources.getDrawable(R.drawable.bg_light_green))
                view.setTextColor(resources.getColor(R.color.white))
            }
        }

    }

    private fun unSelectedMode(view: TextView) {
        view.background = (resources.getDrawable(R.drawable.border_light_grey))
        view.setTextColor(resources.getColor(R.color.black))
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                view.background = (resources.getDrawable(R.drawable.border_light_grey))
                view.setTextColor(resources.getColor(R.color.white))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                view.background = (resources.getDrawable(R.drawable.border_light_grey))
                view.setTextColor(resources.getColor(R.color.black))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                view.background = (resources.getDrawable(R.drawable.border_light_grey))
                view.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    private fun datePicker() {
        val localDate = Calendar.getInstance().time
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val currentDate = format.format(localDate)
        val currentYear = currentDate.split("-")[0]
        val currentMonth = currentDate.split("-")[1]
        val currentDay = currentDate.split("-")[2]

        val dialog =
            DatePickerFragment(
                requireActivity(), 0, 0, 0, currentDay.toInt(),
                currentMonth.toInt() - 1,
                currentYear.toInt()
            ) { year, month, day ->
                withDrawDay = "$day"
                withDrawMonth = "$month"
                withDrawYear = "$year"

                binding.edtWithDrawDate.setText("$day/$month/$year")
                selectedWithdrawDate = "$year-$month-$day"

            }

        dialog.show(requireActivity().supportFragmentManager, "timePicker")

    }
    fun compareDates(date1: Date, date2: Date?): Boolean {
        return date1.before(date2)
    }


    private fun datePickerStart2() {


        val nextDateCalender = Calendar.getInstance()
        val dateValidator: CalendarConstraints.DateValidator = DateValidatorPointForward
            .from(nextDateCalender.timeInMillis)

        val calendarStart = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val  startData= format.format(calendarStart.time)
        val s = format.parse(startData)
        /*  val startYear = startData.split("-")[0]
          val  startMonth= startData.split("-")[1]
          val  startDay= startData.split("-")[2]*/

        val calendarEnd = Calendar.getInstance()
        calendarEnd.add(Calendar.YEAR,withDrawYear.toInt())
        calendarEnd.add(Calendar.DAY_OF_MONTH,withDrawDay.toInt())
        calendarEnd.add(Calendar.MONTH,withDrawMonth.toInt())
        val endData = format.format(calendarEnd.time)
        /*  val  endYear= endData.split("-")[0]
          val  endMonth= endData.split("-")[1]
          val  endDay= endData.split("-")[2]*/

        val totalDate=withDrawYear+"-"+withDrawMonth+"-"+withDrawDay
        val secondDate:Date=format.parse(totalDate)
        if(s>secondDate){
            AlertDialog.Builder(requireContext())
                //.setTitle("")
                .setCancelable(false)
                .setMessage("Your withdrawal date smaller than current date.")
                .setPositiveButton(
                    "Back",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        requireActivity().onBackPressed()
                    })
                .create()
                .show()
            return
        }

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setStart(calendarStart.timeInMillis)
                .setEnd(calendarEnd.timeInMillis)

        constraintsBuilder.setValidator(dateValidator)
        val builder = MaterialDatePicker.Builder.datePicker()
        val picker = builder.setCalendarConstraints(constraintsBuilder.build()).build()

        picker.show(requireActivity().supportFragmentManager, picker.toString())

        picker.addOnPositiveButtonClickListener {selection->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection


            val current = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val dateString = dateFormat.format(calendar.time)
            val birthDate = dateFormat.parse(dateString)
            if (current.before(birthDate).not()) {
                showToast("Please select valid withdrawal date")
                binding.edtDateStart.setText("")
            } else {
              val  selectedDate = dateString
                val showDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
                val showDate= showDateFormat.format(calendar.time)
                binding.edtDateStart.setText(showDate)

            }

        }
    }
    private fun startdatePicker() {
        val localDate = Calendar.getInstance().time
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val currentDate = format.format(localDate)
        val currentYear = currentDate.split("-")[0]
        val currentMonth = currentDate.split("-")[1]
        val currentDay = currentDate.split("-")[2]
        val totalDate=withDrawYear+"-"+withDrawMonth+"-"+withDrawDay
        val secondDate:Date=format.parse(totalDate)
        if(localDate>secondDate){

            AlertDialog.Builder(requireContext())
                //.setTitle("")
                .setCancelable(false)
                .setMessage("Your withdrawal date smaller than current date.")
                .setPositiveButton(
                    "Back",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        requireActivity().onBackPressed()
                    })
                .create()
                .show()
            return
        }


        val dialog =
            DatePickerFragment(
                requireActivity(),
                withDrawDay.toInt() ,
                withDrawMonth.toInt() ,
                withDrawYear.toInt(),
                currentDay.toInt(),
                currentMonth.toInt() ,
                currentYear.toInt()
            ) { year, month, day ->
                /* binding.edtWithDrawDate.setText("$day/$month/$year")*/
                selectedStartDate = "$year-$month-$day"
                startDay = day.toString()
                startMonth = month.toString()
                startYear = year.toString()
                binding.edtDateStart.setText("$day/$month/$year")
            }

        dialog.show(requireActivity().supportFragmentManager, "timePicker")

    }


    private fun enddatePicker() {
        /*   val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
           val calendarStart = Calendar.getInstance()
           calendarStart.add(Calendar.DAY_OF_MONTH,+1)
           val startData = format.format(calendarStart.time)
           val startYear = startData.split("-")[0]
           val startMonth = startData.split("-")[1]
           val startDay = startData.split("-")[2]
   */

        val dialog =
            DatePickerFragment(
                requireActivity(),
                withDrawDay.toInt(),
                withDrawMonth.toInt() ,
                withDrawYear.toInt(),
                startDay.toInt() + 1,
                startMonth.toInt() ,
                startYear.toInt()
            ) { year, month, day ->
                // binding.edtWithDrawDate.setText("$day/$month/$year")
                selectedEndDate = "$year-$month-$day"
                binding.edtDateEnd.setText("$day/$month/$year")


            }

        dialog.show(requireActivity().supportFragmentManager, "timePicker")

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        binding.apply {
            when (buttonView?.id) {
                R.id.btn_sw_deactive -> {

                    if (buttonView.isPressed) {
                        if (btnSwDeactive.isChecked) {
                            showDialog("0 NGN is chargeable to reactivate the card.", true)
                        } else {
                            updateManager()
                        }

                    }

                }
                R.id.btn_sw_expire -> {
                    if (buttonView.isPressed) {
                        if (btnSwExpire.isChecked) {
                            showDialog(
                                "You'll lost amount if any. To reactivate 0 NGN will charge.\n" +
                                        "Expire:- You won't able to use it again", false
                            )
                        } else {
                            updateManager()
                        }
                    }
                }
                R.id.btn_sw_default -> {

                    if (buttonView.isPressed) {
                        if (btnSwDefault.isChecked) {
                            showLoading()
                            manageViewModel.makeAccDefault(
                                pref().getStringValue(SharePreferences.USER_ID, ""),
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                              uuid /* pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/,
                            )
                        } else {
                            btnSwDefault.isChecked = true
                            showToast("Already selected as default")
                        }
                        /* manageViewModel.updateAccount(
                             pref().getStringValue(SharePreferences.USER_ID, ""),
                             pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                             pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                             accType!!
                         )*/
                    }
                }
            }
        }
    }


    private fun observer() {
        manageViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {

                is DefaultWalletResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.btnSwDefault.isChecked = true
                    } else {
                        binding.btnSwDefault.isChecked = false
                    }
                    hideLoading()
                    // showToast(it.message)
                    manageViewModel.observerResponse.value = null
                }
                is DefaultAccResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        //showToast(it.message)
                    } else {
                        binding.btnSwDefault.isChecked = false
                    }
                    hideLoading()
                    showToast(it.message)
                    manageViewModel.observerResponse.value = null
                }
                is GetUpdateDefaultAcc -> {
                    hideLoading()
                    manageViewModel.observerResponse.value = null
                }
                is ManageResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {

                    }
                    showToast(it.message)
                    manageViewModel.observerResponse.value = null

                }
                is StartTrackerResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        startTrackerShow()
                        binding.profileStatusBar.progress = it.data!!.goal_achieve.toDouble().toInt()
                    } else {
                        startTrackerHide()
                        showToast(it.message)
                    }
                    manageViewModel.observerResponse.value = null
                }
                is ResponseMangeAc -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.apply {

                            btnSwExpire.isChecked = it.data!!.expire
                            btnSwDeactive.isChecked = it.data.active
                            btnSwDefault.isChecked = it.data.default
                            if (it.data.expire) {
                                mainLyt.hide()
                            }
                            if (it.data.withdrawDate.isNotBlank()) {
                                withDrawYear = it.data.withdrawDate.split("-")[0]
                                withDrawMonth = it.data.withdrawDate.split("-")[1]
                                withDrawDay = it.data.withdrawDate.split("-")[2]
                                edtWithDrawDate.setText("$withDrawDay-$withDrawMonth-$withDrawYear")
                                btnWithDrawDate.isClickable = false
                            } else {
                                edtWithDrawDate.setText("")
                                btnWithDrawDate.isClickable = true
                            }


                            if (it.data.trackerInfo.set) {
                                startTrackerShow()
                                edtDateStart.setText(it.data.trackerInfo.start_data)
                                edtDateEnd.setText(it.data.trackerInfo.complete_data)
                                edtAmountSave.setText(it.data.trackerInfo.target_amount)
                                selectedStartDate = edtDateStart.text.toString()
                                selectedEndDate = edtDateEnd.text.toString()
                                profileStatusBar.progress = it.data.trackerInfo.goal_achieved.toDouble().toInt()
                                when (it.data.trackerInfo.saving_basis) {
                                    0 -> {
                                        selectedMode(binding.btnDaily)
                                        modeArr[0] = 1
                                        modeArr[1] = 0
                                        modeArr[2] = 0
                                    }
                                    1 -> {
                                        selectedMode(binding.btnWeekly)
                                        modeArr[0] = 0
                                        modeArr[1] = 1
                                        modeArr[2] = 0
                                    }
                                    2 -> {
                                        selectedMode(binding.btnMonthly)
                                        modeArr[0] = 0
                                        modeArr[1] = 0
                                        modeArr[2] = 1
                                    }
                                }

                            } else {
                                //showToast(it.message)
                                startTrackerHide()
                                /*startTrackerHide()
                                selectedMode(binding.btnDaily)
                                modeArr[0]=1
                                modeArr[1]=0
                                modeArr[2]=0*/

                            }
                        }
                    } else {
                        if (it.error_code == "111") {
                            binding.mainLyt.hide()
                        }
                        showToast(it.message)
                    }
                    manageViewModel.observerResponse.value = null
                }
                is ResponseMangeCurrentAc -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        binding.apply {

                            btnSwExpire.isChecked = it.data!!.expire
                            btnSwDeactive.isChecked = it.data.active
                            btnSwDefault.isChecked = it.data.default
                            if (it.data.expire) {
                                mainLyt.hide()
                            }
                            if (it.data.withdrawDate.isNotBlank()) {
                                withDrawYear = it.data.withdrawDate.split("-")[0]
                                withDrawMonth = it.data.withdrawDate.split("-")[1]
                                withDrawDay = it.data.withdrawDate.split("-")[2]
                                edtWithDrawDate.setText("$withDrawDay-$withDrawMonth-$withDrawYear")
                                btnWithDrawDate.isClickable = false
                            } else {
                                edtWithDrawDate.setText("")
                                btnWithDrawDate.isClickable = true
                            }
                        }
                    } else {
                        if (it.error_code == "111") {
                            binding.mainLyt.hide()
                        }
                        showToast(it.message)
                    }
                    manageViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    manageViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    manageViewModel.observerResponse.value = null
                    showToast(it.message.toString())
                    hideLoading()
                }
            }
        })
    }

    fun startTrackerShow() {
        binding.lblStartTracker.show()
        binding.profileStatusBar.show()
        binding.lblStartTracker.text = "Tracker"
        binding.innerLyt.hide()
    }

    fun startTrackerHide() {
        binding.lblStartTracker.hide()
        binding.profileStatusBar.hide()
        binding.innerLyt.show()
    }

    private fun showDialog(des: String, isForDeactivate: Boolean) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
            setTitle("Alert!")
            setMessage(des)
            setCancelable(false)
            setPositiveButton(
                "Yes"
            ) { dialog, which ->
                dialog.dismiss()
                updateManager()

            }
            setNegativeButton(
                "No"
            ) { dialog, which ->
                dialog.dismiss()
                if (isForDeactivate)
                    binding.btnSwDeactive.isChecked = !binding.btnSwDeactive.isChecked
                else
                    binding.btnSwExpire.isChecked = !binding.btnSwExpire.isChecked
            }
        }
        dialog.show()
    }

    private fun updateManager() {
        showLoading()
        manageViewModel.mangeAcc(
            UpdateManageConfig(
              uuid /* pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/,
                pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                binding.btnSwDeactive.isChecked,
                binding.btnSwExpire.isChecked
            ),
            accType!!
        )
    }

    private fun happyThemeChanges() {

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.txtUpdate.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.txtUpdate.setBackgroundResource(R.drawable.bg_gradient_light_btn)


            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.txtUpdate.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
        }

    }

    companion object {
        var AC_TYPE = "ac_type"
        val UUID ="uuid"
    }

    override fun getLayoutId(): Int = R.layout.manage_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): ManageViewModel = manageViewModel


}
