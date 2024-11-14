package com.nbt.blytics.modules.payee.setschedule

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.MaterialToolbar
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.SetScheduleFragmentBinding
import com.nbt.blytics.modules.payee.schedulecreate.*
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.SignupProfileEditFragment
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import java.text.SimpleDateFormat
import java.util.*


class SetScheduleFragment : BaseFragment<SetScheduleFragmentBinding, SetScheduleViewModel>() {

    private val setScheduleViewModel: SetScheduleViewModel by viewModels()
    private lateinit var binding: SetScheduleFragmentBinding
    private var startDateMilisecond: Long = 0
    private var selectedRepeat = ""
    private lateinit var  calendarStart : Calendar
    private lateinit var calendarEnd : Calendar


    // private var selectedSchedule:String =""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        happyThemeChanges()

        if (ScheduleCreateFragment.editAble) {
            calendarStart = Calendar.getInstance()
            val dataStartArr = ScheduleCreateFragment.stateDate.split("-")
            calendarStart.set(Calendar.DAY_OF_MONTH, dataStartArr[2].toInt())
            calendarStart.set(Calendar.YEAR,  dataStartArr[0].toInt())
            calendarStart.set(Calendar.MONTH, dataStartArr[1].toInt()-1)
            startDateMilisecond = calendarStart.timeInMillis
            selectedRepeat = ScheduleCreateFragment.frequency


            calendarEnd = Calendar.getInstance()
            //End calender set time
            val dataStartArrEnd = ScheduleCreateFragment.endDate.split("-")
            calendarEnd.set(Calendar.DAY_OF_MONTH, dataStartArrEnd[2].toInt())
            calendarEnd.set(Calendar.YEAR,  dataStartArrEnd[0].toInt())
            calendarEnd.set(Calendar.MONTH, dataStartArrEnd[1].toInt()-1)

            binding.edtStartDate.setText(ScheduleCreateFragment.stateDate)
            binding.edtEndDate.setText(ScheduleCreateFragment.endDate)
            binding.edtRepeat.setText(ScheduleCreateFragment.frequency)
        }
        binding.btnStartDate.setOnClickListener {
            datePickerStart()
        }
        binding.btnRepeat.setOnClickListener {
            /* popMenu(binding.btnRepeat, 0)*/
            showRelationDialog()
        }
        binding.btnEndDate.setOnClickListener {
            datePickerEnd2(selectedRepeat)
        }
        binding.btnSetSchedule.setOnClickListener {
            if (validation()) {

                if (ScheduleCreateFragment.editAble) {
                    if (ScheduleCreateFragment.type == 0) {
                        showLoading()
                        setScheduleViewModel.editInternalSchedule(
                            InternalSchedule(
                                pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                ScheduleCreateFragment.listReceiverUser,
                                ScheduleCreateFragment.amount,
                                ScheduleCreateFragment.acHolderName,
                                ScheduleCreateFragment.reference,
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                selectedRepeat,
                                true,
                                binding.edtEndDate.text.toString().trim(),
                                binding.edtStartDate.text.toString().trim(),
                                ScheduleCreateFragment.uuidFrom,
                                schedule_id = ScheduleCreateFragment.schedulID


                            )
                        )
                    } else if(ScheduleCreateFragment.type ==1) {
                        showLoading()
                        setScheduleViewModel.editExternalPayment(
                            ExternalSchedule(
                                binding.edtStartDate.text.toString().trim(),
                                binding.edtEndDate.text.toString().trim(),
                                ScheduleCreateFragment.reference,
                                ExternalSchedule.Euser(
                                    destbankcode = "",
                                    accountname = ScheduleCreateFragment.acHolderName,
                                    recipientaccount = ScheduleCreateFragment.toAc,
                                ),
                                selectedRepeat,
                                ScheduleCreateFragment.uuidFrom,
                                pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                true,
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                ScheduleCreateFragment.acHolderName,
                                ScheduleCreateFragment.amount,
                                schedule_id = ScheduleCreateFragment.schedulID

                            ),

                            )
                    }else if(ScheduleCreateFragment.type ==2){
                        showLoading()
                        setScheduleViewModel.editSelfSchedule(
                                SelfSchedule(
                                    userId =   pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                    scheduleAmount =  ScheduleCreateFragment.amount,
                                    name=   ScheduleCreateFragment.acHolderName,
                                    reference=    ScheduleCreateFragment.reference,
                                    userToken=  pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                    endDate= binding.edtEndDate.text.toString().trim(),
                                    startDate=   binding.edtStartDate.text.toString().trim(),
                                    uuidSend =   ScheduleCreateFragment.uuidFrom,
                                    uuidReceive =ScheduleCreateFragment.uuidTo,
                                    txnType ="self_transaction",
                                    frequency= selectedRepeat,
                                    tpinVerified =true

                                )
                        )
                    }
                } else {
                    if (ScheduleCreateFragment.type == 0) {
                        showLoading()
                        setScheduleViewModel.internalSchedule(
                            InternalSchedule(
                                pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                ScheduleCreateFragment.listReceiverUser,
                                ScheduleCreateFragment.amount,
                                ScheduleCreateFragment.acHolderName,
                                ScheduleCreateFragment.reference,
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                selectedRepeat,
                                true,
                                binding.edtEndDate.text.toString().trim(),
                                binding.edtStartDate.text.toString().trim(),
                                ScheduleCreateFragment.uuidFrom,
                                txn_type ="internal_transaction"

                            )
                        )
                    } else  if (ScheduleCreateFragment.type == 1) {
                        showLoading()
                        setScheduleViewModel.externalSchedule(
                            ExternalSchedule(
                                binding.edtStartDate.text.toString().trim(),
                                binding.edtEndDate.text.toString().trim(),
                                ScheduleCreateFragment.reference,
                                ExternalSchedule.Euser(
                                    destbankcode = "",
                                    accountname = ScheduleCreateFragment.acHolderName,
                                    recipientaccount = ScheduleCreateFragment.toAc,
                                ),
                                selectedRepeat,
                               ScheduleCreateFragment.uuidFrom,
                                pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                                true,
                                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                ScheduleCreateFragment.acHolderName,
                                ScheduleCreateFragment.amount

                            ),

                            )
                    } else  if (ScheduleCreateFragment.type == 2){
                        showLoading()
                        val userId = pref().getStringValue(SharePreferences.USER_ID, "").toIntOrNull() ?: 0
                        val scheduleAmount = ScheduleCreateFragment.amount
                        val name = ScheduleCreateFragment.acHolderName
                        val reference = ScheduleCreateFragment.reference
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        val endDate = binding.edtEndDate.text.toString().trim()
                        val startDate = binding.edtStartDate.text.toString().trim()
                        val uuidSend = ScheduleCreateFragment.uuidFrom
                        val uuidReceive = ScheduleCreateFragment.uuidTo
                        val txnType = "self_transaction"
                        val frequency = selectedRepeat
                        val tpinVerified = true


                        // Log all values
                        Log.d("ScheduleData", "userId: $userId")
                        Log.d("ScheduleData", "scheduleAmount: $scheduleAmount")
                        Log.d("ScheduleData", "name: $name")
                        Log.d("ScheduleData", "reference: $reference")
                        Log.d("ScheduleData", "userToken: $userToken")
                        Log.d("ScheduleData", "endDate: $endDate")
                        Log.d("ScheduleData", "startDate: $startDate")
                        Log.d("ScheduleData", "uuidSend: $uuidSend")
                        Log.d("ScheduleData", "uuidReceive: $uuidReceive")
                        Log.d("ScheduleData", "txnType: $txnType")
                        Log.d("ScheduleData", "frequency: $frequency")
                        Log.d("ScheduleData", "tpinVerified: $tpinVerified")

                        // Pass data to ViewModel
                        setScheduleViewModel.selfSchedule(
                            SelfSchedule(
                                userId = userId,
                                scheduleAmount = scheduleAmount,
                                name = name,
                                reference = reference,
                                userToken = userToken,
                                endDate = endDate,
                                startDate = startDate,
                                uuidSend = uuidSend,
                                uuidReceive = uuidReceive,
                                txnType = txnType,
                                frequency = frequency,
                                tpinVerified = tpinVerified
                            )
                        )
                    }
                }
            }
        }
    }

    private fun popMenu(view: View, index: Int) {
        val popupMenu: PopupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add("Select Repeat").isEnabled = false
        popupMenu.menu.add("Daily")
        popupMenu.menu.add("Weekly")
        popupMenu.menu.add("Monthly")

        happyThemeChanges(popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            selectedRepeat = item.title.toString()
            binding.edtRepeat.setText(selectedRepeat)
            true
        })
        popupMenu.show()
    }

    private fun happyThemeChanges(menu: Menu) {

        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val s = SpannableString(menu.getItem(0).title.toString())
                s.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange_dark
                        )
                    ), 0, s.length, 0
                )
                menu.getItem(0).title = s
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                val s = SpannableString(menu.getItem(0).title.toString())
                s.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.b_currency_blue
                        )
                    ), 0, s.length, 0
                )
                menu.getItem(0).title = s
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                val s = SpannableString(menu.getItem(0).title.toString())
                s.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.b_currency_blue
                        )
                    ), 0, s.length, 0
                )
                menu.getItem(0).title = s
            }
        }

    }

    private fun validation(): Boolean {
        if (binding.edtStartDate.text.toString().isBlank()) {
            showToast("Select start date")
            return false
        }
        if (binding.edtRepeat.text.toString().isBlank()) {
            showToast("Select repeat")
            return false
        }
        if (binding.edtEndDate.text.toString().isBlank()) {
            showToast("Select end date")
            return false
        }
        return true
    }

    private fun datePickerStart() {
        calendarStart = Calendar.getInstance()
        calendarStart.set(Calendar.DAY_OF_MONTH,calendarStart.get(Calendar.DAY_OF_MONTH)+1)
        val day = calendarStart.get(Calendar.DAY_OF_MONTH)
        val year = calendarStart.get(Calendar.YEAR)
        val month = calendarStart.get(Calendar.MONTH)
        val datePicker = DatePickerDialog(
            requireContext(),
            { view, _year, _month, dayOfMonth ->

                    calendarStart = Calendar.getInstance()

                calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                calendarStart.set(Calendar.YEAR, _year)
                calendarStart.set(Calendar.MONTH, _month)
                startDateMilisecond = calendarStart.timeInMillis
                val showDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val showDate = showDateFormat.format(calendarStart.time)
                binding.edtStartDate.setText(showDate)
            }, year, month, day
        )

        datePicker.show()
        datePicker.datePicker.minDate = calendarStart.timeInMillis

    }


    private fun datePickerEnd2(type: String) {
        calendarEnd = Calendar.getInstance()
        calendarEnd.timeInMillis = startDateMilisecond
       /* if (ScheduleCreateFragment.editAble.not()) {
            calendarEnd = Calendar.getInstance()
            calendarEnd.timeInMillis = startDateMilisecond
        }*/
        val day = calendarEnd.get(Calendar.DAY_OF_MONTH)
        val year = calendarEnd.get(Calendar.YEAR)
        val month = calendarEnd.get(Calendar.MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { view, _year, _month, _dayOfMonth -> // adding the selected date in the edittext
               val cEnd = Calendar.getInstance()
                val d = cEnd.set(Calendar.DAY_OF_MONTH, _dayOfMonth)
                val y = cEnd.set(Calendar.YEAR,_year)
                val m = cEnd.set(Calendar.MONTH,_month)
                val showDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val showDate = showDateFormat.format(cEnd.time)
                binding.edtEndDate.setText(showDate)
            }, year, month, day
        )

        datePicker.show()

        when (type) {
            "Once" -> {
                datePicker.datePicker.minDate = calendarStart.timeInMillis
                datePicker.datePicker.maxDate = calendarStart.timeInMillis
            }
            "Weekly" -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 7)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis

            }
            "Monthly" -> {
                calendarEnd.add(Calendar.MONTH, 1)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
            "Every four weeks" -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 28)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
            "Every two months" -> {
                calendarEnd.add(Calendar.MONTH, 2)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
            "Quarterly" -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 91)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
            "Half yearly" -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 183)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
            "Yearly" -> {
                calendarEnd.add(Calendar.DAY_OF_MONTH, 365)
                datePicker.datePicker.minDate = calendarEnd.timeInMillis
            }
        }
    }

    private fun showRelationDialog() {

        binding.edtEndDate.setText("")
        val items = listOf(
            SignupProfileEditFragment.GenderModel(
                "Once", 0
            ), SignupProfileEditFragment.GenderModel(
                "Weekly", 1
            ), SignupProfileEditFragment.GenderModel(
                "Monthly", 2
            ), SignupProfileEditFragment.GenderModel(
                "Every four weeks", 3
            ), SignupProfileEditFragment.GenderModel(
                "Every two months", 4
            ), SignupProfileEditFragment.GenderModel(
                "Quarterly", 5
            ), SignupProfileEditFragment.GenderModel(
                "Half yearly", 6
            ), SignupProfileEditFragment.GenderModel(
                "Yearly", 7
            )
        )
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_shcedule)
        val btnOnce = dialog.findViewById<LinearLayout>(R.id.btn_once)
        val btnWeekly = dialog.findViewById<LinearLayout>(R.id.btn_weekly)
        val btnMonthly = dialog.findViewById<LinearLayout>(R.id.btn_monthly)
        val btnEveryFourWeek = dialog.findViewById<LinearLayout>(R.id.btn_every_four_week)
        val btnEveryTowMonth = dialog.findViewById<LinearLayout>(R.id.btn_every_two_months)
        val btnQuarterly = dialog.findViewById<LinearLayout>(R.id.btn_Quraterly)
        val btnHalfYearly = dialog.findViewById<LinearLayout>(R.id.btn_haly_yearly)
        val btnYear = dialog.findViewById<LinearLayout>(R.id.btn_yearly)
        val toolbar = dialog.findViewById<MaterialToolbar>(R.id.toolbar_top)

        toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            dialog.dismiss()
        }

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                toolbar.setBackgroundResource(R.color.black)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                toolbar.setBackgroundResource(R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                toolbar.setBackgroundResource(R.color.white)
            }
        }
        btnOnce.setOnClickListener {
            selectedRepeat = items[0].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnWeekly.setOnClickListener {
            selectedRepeat = items[1].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnMonthly.setOnClickListener {
            selectedRepeat = items[2].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnEveryFourWeek.setOnClickListener {
            selectedRepeat = items[3].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnEveryTowMonth.setOnClickListener {
            selectedRepeat = items[4].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnQuarterly.setOnClickListener {
            selectedRepeat = items[5].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnHalfYearly.setOnClickListener {
            selectedRepeat = items[6].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }
        btnYear.setOnClickListener {
            selectedRepeat = items[7].gender
            dialog.dismiss()
            binding.edtRepeat.setText(selectedRepeat)
        }

        dialog.show()
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation)
                    .setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation)
                    .setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation)
                    .setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }


    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnSetSchedule.setBackgroundResource(R.drawable.bg_gradient_orange_btn)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnSetSchedule.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnSetSchedule.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.b_bg_color_light))
                binding.topLayout.setBackgroundResource(R.color.white)
            }
        }
    }

    private fun observer() {
        setScheduleViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is ScheduleCrateRes -> {
                    hideLoading()
                    showToast(it.message)
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        requireActivity().finish()
                    } else {

                    }

                    setScheduleViewModel.observerResponse.value = null

                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    setScheduleViewModel.observerResponse.value = null
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.set_schedule_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): SetScheduleViewModel = setScheduleViewModel

}