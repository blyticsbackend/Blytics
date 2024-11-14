package com.nbt.blytics.modules.securityQes

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.profileUpdatedLiveData
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.SecurityQuestionFragmentBinding
import com.nbt.blytics.modules.chagnetpin.models.ChangeTpinResponse
import com.nbt.blytics.modules.chagnetpin.models.CheckTpinResponse
import com.nbt.blytics.modules.otp.model.CheckValidTpinResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.squpdate.model.SqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqResponse
import com.nbt.blytics.modules.squpdate.model.UpdateSqlRequest
import com.nbt.blytics.modules.userprofile.models.SQ
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show
import java.lang.reflect.Type

class SecurityQuestionFragment :
    BaseFragment<SecurityQuestionFragmentBinding, SecurityQuestionViewModel>() {

    private val sqList = mutableListOf<SqResponse.Question>()
    private val mainSqList = mutableListOf<SqResponse.Question>()
    private val filterSqList = mutableListOf<SqResponse.Question>()
    private val securityQuestionViewModel: SecurityQuestionViewModel by viewModels()
    private lateinit var binding: SecurityQuestionFragmentBinding
    private var userQueList = mutableListOf<SQ>()
    private var firstSpinnerIsSelected: Boolean = false
    private var secondSpinnerIsSelected: Boolean = false
    private var userID: String = ""
    private var userToken: String = ""
    private var oldTpin = false
    private lateinit var bottomSheetMobileVerify: BottomSheetDialog
    private var userWalletUUID: String? = ""
    private lateinit var arrayAdapter2: ArrayAdapter<SqResponse.Question>
    private lateinit var arrayAdapter: ArrayAdapter<SqResponse.Question>
    private val selectedSq = mutableListOf<UpdateSqlRequest.QueData>()
    private var phoneVerificationId: String = ""
    private lateinit var updateType: UpdateType
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()/*  setAdapterSpinner1()
        setAdapterSpinner2()*/
        userQueList.clear()
        observerOTP()
        showLoading()
        setViewPager()
        //  visibleStateChange(0)
        securityQuestionViewModel.getQuestion()
        userID = pref().getStringValue(SharePreferences.USER_ID, "")
        userWalletUUID = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
        userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        showLoading()
        fillUserSQ()
        binding.apply {
            btnSaveQ1.setOnClickListener {
                updateType = UpdateType.QUE1
                if (firstSpinnerIsSelected.not()) {
                    showToast("Select Question")
                } else if (binding.edtAns.text.toString().equals("")) {
                    showToast("enter answer")
                } else if (binding.edtAnsHint.text.toString().isBlank()) {
                    showToast("enter hint")
                } else {
                    sentOTP()
                }
            }
            btnSaveQ2.setOnClickListener {
                updateType = UpdateType.QUE2
                if (validation()) {
                    sentOTP()
                }
            }

            btnSavePassword.setOnClickListener {
                updateType = UpdateType.PASSWORD
                if (edtOldPassword.text.toString().isEmpty()) {
                    showToast("Enter Old Password")
                    return@setOnClickListener
                }
                if (edtPassword.text.toString().isEmpty()) {
                    showToast("Enter Password")
                    return@setOnClickListener
                }
                if (edtConfirmPassword.text.toString().isEmpty()) {
                    showToast("Enter Confirm Password")
                    return@setOnClickListener
                }
                if (edtPassword.text.toString().length < 4) {
                    showToast("Password too short")
                    return@setOnClickListener
                }
                if (edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
                    showToast("Confirm password does not match")
                    return@setOnClickListener
                }
                sentOTP()
            }

            showLoading()
            securityQuestionViewModel.checkTpin(userID, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
            binding.btnSaveTpin.setOnClickListener {
                updateType = UpdateType.TPIN
                if (validationTpin()) {
                    sentOTP()
                }
            }
        }

        profileUpdatedLiveData.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    fillUserSQ()
                    notifySpinner1()
                    profileUpdatedLiveData.value = null
                }
            }
        }

        binding.btnQue1.setOnClickListener {
            showRelationDialog()
        }
        binding.btnQue2.setOnClickListener {
            showRelationDialog2()
        }
    }

    private fun setViewPager() {
        val list = mutableListOf(
            Page("Change Password", drawableRes = R.drawable.address_new),
            Page("Change Tpin", drawableRes = R.drawable.password_24dp),
            Page("Question 1", drawableRes = R.drawable.question_new),
            Page("Question 2", drawableRes = R.drawable.question_new)
        )
        val viewPagerAdapter = MyViewPageAdapter(requireContext(), list, -1, R.color.orange_light, R.color.white, R.drawable.img_samll_bg_1_square)
        binding.screenViewpager.adapter = viewPagerAdapter

        // Setup tab indicators based on night mode
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tabIndicatorNight.show()
                binding.tabIndicator.hide()
                binding.tabIndicatorNight.setupWithViewPager(binding.screenViewpager)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tabIndicatorNight.hide()
                binding.tabIndicator.show()
                binding.tabIndicator.setupWithViewPager(binding.screenViewpager)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.tabIndicatorNight.hide()
                binding.tabIndicator.show()
                binding.tabIndicator.setupWithViewPager(binding.screenViewpager)
            }
        }

        // Add a page change listener to handle page selection
        binding.screenViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // No action needed here
            }
            override fun onPageSelected(position: Int) {
                visibleStateChange(position) // Call your function to handle visibility changes
            }
            override fun onPageScrollStateChanged(state: Int) {
                // No action needed here
            }
        })
    }

    private fun fillUserSQ() {
        try {
            userQueList.clear()
            val type: Type = object : TypeToken<MutableList<SQ>>() {}.type
            val gson = Gson().fromJson<MutableList<SQ>>(pref().getStringValue(pref().USER_SECURITY_QUES, ""), type)
            userQueList.addAll(gson)
        } catch (ex: Exception) {
            print(ex.toString())
        }
        try {
            var have2rdQus = false
            var have1rdQus = false
            if (userQueList.isNotEmpty()) {
                for (i in userQueList.indices) {
                    if (userQueList[i].ques_no == "1") {
                        binding.que1Spinner.setText(userQueList[i].ques)
                        //   binding.edtAns.setText(userQueList[i].ans)
                        binding.edtAnsHint.setText(userQueList[i].hint)
                        have1rdQus = true
                        binding.btnSaveQ1.text = "Update"
                    }
                    if (userQueList[i].ques_no == "2") {
                        binding.que2Spinner.setText(userQueList[i].ques)
                        // binding.txtTitle2.text = userQueList[i].ques
                        //   binding.edtAns2.setText(userQueList[i].ans)
                        binding.edtAnsHint2.setText(userQueList[i].hint)
                        have2rdQus = true
                        binding.btnSaveQ2.text = "Update"
                    }
                }
                if (have1rdQus.not()) {
                    //  binding.txtTitle1.text = "Please set Your first Security question."
                    binding.edtAns.setText("")
                    binding.edtAnsHint.setText("")
                    binding.btnSaveQ1.text = "Save"
                }
                if (have2rdQus.not()) {
                    // binding.txtTitle2.text = "Please set Your Second Security Question."
                    binding.edtAns2.setText("")
                    binding.edtAnsHint2.setText("")
                    binding.btnSaveQ2.text = "Save"
                }
            } else {
                //binding.txtTitle1.text = "Please set Your first Security question."
                // binding.txtTitle2.text = "Please set Your Second Security Question."
                binding.btnSaveQ1.text = "Save"
                binding.btnSaveQ2.text = "Save"
                binding.edtAns.setText("")
                binding.edtAnsHint.setText("")
                binding.edtAns2.setText("")
                binding.edtAnsHint2.setText("")
            }
        } catch (ex: Exception) {
            print(ex.toString())
        }
    }

    /*    private fun visibityField(i: Int) {
            binding.apply {
                when (i) {
                    1 -> {
                        conQue1.show()
                        conQue2.hide()
                        conTpin.hide()
                        conChangePassword.hide()
                        *//*if (userQueList.isEmpty()) {
                        txtTitle1.text = "Please set Your first Security question"
                    }*//*
                }
                2 -> {
                    conQue1.hide()
                    conQue2.show()
                    conTpin.hide()
                    conChangePassword.hide()
                    *//*if (userQueList.isEmpty()) {
                        txtTitle2.text = "Please set Your Second Security Question."
                    } else {
                        if (userQueList.size < 2) {
                            txtTitle2.text = "Please set Your Second Security Question."
                        }
                    }*//*
                }
                3 -> {
                    conQue1.hide()
                    conQue2.hide()
                    conTpin.show()
                    conChangePassword.hide()
                    txtTitle3.text = "Change Tpin"
                }
                4 -> {
                    conQue1.hide()
                    conQue2.hide()
                    conTpin.hide()
                    conChangePassword.show()
                    txtTitle4.text = "Change Password"
                }
            }
        }
    }*/

    fun validation(): Boolean {
        binding.apply {
            /* if (firstSpinnerIsSelected.not()) {
                 showToast("Select Question")
                 return false
             }*/
            if (secondSpinnerIsSelected.not()) {
                showToast("Select Question")
                return false
            }/*   if (edtAns.text.toString().isBlank()) {
                   showToast("enter answer")
                   return false
               }
               if (edtAnsHint.text.toString().isBlank()) {
                   showToast("enter hint")
                   return false
               }*/
            if (edtAns2.text.toString().isBlank()) {
                showToast("enter answer")
                return false
            }
            if (edtAnsHint2.text.toString().isBlank()) {
                showToast("enter hint")
                return false
            }
        }
        return true
    }

    private fun observer() {
        securityQuestionViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is SqResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.question.let { list ->
                            sqList.clear()
                            sqList.addAll(list)
                            filterSqList.clear()
                            filterSqList.addAll(list)
                            mainSqList.clear()
                            mainSqList.addAll(list)
                            try {
                                var have2rdQus = false
                                var have1rdQus = false
                                if (userQueList.isNotEmpty()) {

                                    for (i in userQueList.indices) {
                                        if (userQueList[i].ques_no == "1") {
                                            binding.que1Spinner.setText(userQueList[i].ques)
                                            // binding.edtAns.setText(userQueList[i].ans)
                                            binding.edtAnsHint.setText(userQueList[i].hint)
                                            have1rdQus = true
                                            binding.btnSaveQ1.text = "Update"
                                        }
                                        if (userQueList[i].ques_no == "2") {
                                            binding.que2Spinner.setText(userQueList[i].ques)
                                            //  binding.edtAns2.setText(userQueList[i].ans)
                                            binding.edtAnsHint2.setText(userQueList[i].hint)
                                            have2rdQus = true
                                            binding.btnSaveQ2.text = "Update"
                                        }
                                    }
                                    if (have1rdQus.not()) {
                                        //binding.txtTitle1.text = "Please set Your first Security question."
                                        binding.edtAns.setText("")
                                        binding.edtAnsHint.setText("")
                                        binding.btnSaveQ1.text = "Save"
                                    }
                                    if (have2rdQus.not()) {
                                        // binding.txtTitle2.text = "Please set Your Second Security Question."
                                        binding.edtAns2.setText("")
                                        binding.edtAnsHint2.setText("")
                                        binding.btnSaveQ2.text = "Save"
                                    }
                                } else {
                                    //  binding.txtTitle1.text = "Please set Your first Security question."
                                    // binding.txtTitle2.text = "Please set Your Second Security Question."
                                    binding.btnSaveQ1.text = "Save"
                                    binding.btnSaveQ2.text = "Save"
                                    binding.edtAns.setText("")
                                    binding.edtAnsHint.setText("")
                                    binding.edtAns2.setText("")
                                    binding.edtAnsHint2.setText("")
                                }
                            } catch (ex: Exception) {
                                print(ex.toString())
                            }
                        }
                    } else {
                    }
                    notifySpinner1()
                    securityQuestionViewModel.observerResponse.value = null
                }

                is UpdateSqResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        //   showToast(it.message)
                        showLoading()
                        (requireActivity() as BconfigActivity).getProfileInfo()
                        binding.edtAns.setText("")
                        binding.edtAns2.setText("")
                        showSuccessDilao(it.message)
                    } else {
                        showToast(it.message)
                    }
                    securityQuestionViewModel.observerResponse.value = null
                }

                is PasswordUpdateChangeResponse -> {
                    hideLoading()
                    securityQuestionViewModel.observerResponse.value = null
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showSuccessDilao(it.message)
                    } else {
                        showToast(it.message)
                    }
                }

                is ChangeTpinResponse -> {
                    hideLoading()
                    securityQuestionViewModel.observerResponse.value = null
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showSuccessDilao(it.message)
                    } else {
                        showToast(it.message)
                    }
                }
                is CheckTpinResponse -> {
                    hideLoading()
                    securityQuestionViewModel.observerResponse.value = null
                    if ((it.status.equals(Constants.Status.SUCCESS.name, true))) {
                        if (it.data!!.wtpin == 1) {
                            oldTpin = true
                            binding.oldLayoutTpin.visibility = View.VISIBLE
                            binding.btnSaveTpin.text = "Update"
                        } else {
                            oldTpin = false
                            //  binding.lblTitle.text ="Generate Tpin"
                            binding.oldLayoutTpin.visibility = View.GONE
                            binding.btnSaveTpin.text = "Save"
                        }
                    }
                }

                is CheckValidTpinResponse -> {
                    hideLoading()
                    securityQuestionViewModel.observerResponse.value = null
                    if (it.status.contains(Constants.Status.SUCCESS.name, true)) {
                        if (pref().getStringValue(SharePreferences.DEFAULT_AC_TYPE, "").equals(SharePreferences.AcType.WALLET.name, true)) {
                            securityQuestionViewModel.changeTpi(userID, userWalletUUID!!, binding.tpinView.text.toString().trim())
                        } else {
                            securityQuestionViewModel.changeSavingCurrentTpi(
                                userID,
                                userToken,
                                userWalletUUID!!,
                                binding.tpinView.text.toString().trim())
                        }
                    } else {
                        showToast(it.message)
                    }
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    securityQuestionViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun validationTpin(): Boolean {
        binding.apply {
            if (oldTpin) {
                if (binding.oldTpinView.text.toString().trim().isBlank()) {
                    showToast("Enter Current Tpin")
                    return false
                }
            }

            if (tpinView.text.toString().isBlank()) {
                // layoutName.error = "Enter first name"
                showToast("Enter Tpin")
                return false
            }
            if (tpinView.text.toString().length < 4) {
                showToast("Enter Tpin")
            }
            if (tpinViewConfirm.text.toString().isBlank()) {
                showToast("Enter Confirm Tpin")
                return false
            }
            if (tpinViewConfirm.text.toString().length < 4) {
                showToast("Enter Confirm Tpin")
            }
            if (tpinView.text.toString() != tpinViewConfirm.text.toString()) {
                showToast("Confirm Tpin not match.")
                return false
            }
        }
        return true
    }

    private fun setAdapterSpinner1() {
        arrayAdapter = ArrayAdapter(requireContext(), R.layout.list_item, sqList)/*(binding.firstSpinner as? AutoCompleteTextView)?.setAdapter(arrayAdapter)
        (binding.firstSpinner as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            for (i in sqList.indices) {
                sqList[i].selected = false
            }
            binding.apply {
                sqList[position].selected = true
                sqList[position].ans = edtAns.text.toString()
                sqList[position].hint = edtAnsHint.text.toString()
            }
            firstSpinnerIsSelected = true
            Log.d("printListSq", sqList[position].ans)
            Log.d("printListSq", sqList[position].hint)
        }*/
    }

    private fun notifySpinner2() {
        filterSqList.clear()
        filterSqList.addAll(mainSqList)
        for (i in filterSqList.indices) {
            if (binding.que1Spinner.text.toString() == filterSqList[i].question.trim()) {
                filterSqList.remove(filterSqList[i])
                return
            }
        }
        //  arrayAdapter2.notifyDataSetChanged()
    }

    private fun notifySpinner1() {
        sqList.clear()
        sqList.addAll(mainSqList)
        for (i in sqList.indices) {
            if (binding.que2Spinner.text.toString() == sqList[i].question.trim()) {
                sqList.remove(sqList[i])
                notifySpinner2()
                return
            }
        }
        //  arrayAdapter.notifyDataSetChanged()
    }

    private fun setAdapterSpinner2() {
        arrayAdapter2 = ArrayAdapter(requireContext(), R.layout.list_item, filterSqList)/* (binding.secondSpinner as? AutoCompleteTextView)?.setAdapter(arrayAdapter2)
         (binding.secondSpinner as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
             for (i in filterSqList.indices) {
                 filterSqList[i].selected = false
             }
             binding.apply {
                 filterSqList[position].selected = true
                 filterSqList[position].ans = edtAns2.text.toString()
                 filterSqList[position].hint = edtAnsHint2.text.toString()
             }

             secondSpinnerIsSelected = true
             Log.d("printListSqfilter", filterSqList[position].ans)
             Log.d("printListSqfilter", filterSqList[position].hint)
             setAdapterSpinner1()
         }*/
        sqList.clear()
        sqList.addAll(mainSqList)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showRelationDialog() {
        notifySpinner1()
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_question)
        val title = dialog.findViewById<TextView>(R.id.lbl_title_1)
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

        val rvAllQue = dialog.findViewById<RecyclerView>(R.id.rv_all_question)
        val adapter = QuesAdapter(requireContext(), sqList) { position ->
            dialog.dismiss()
            for (i in sqList.indices) {
                sqList[i].selected = false
            }
            binding.apply {
                sqList[position].selected = true
                sqList[position].ans = edtAns.text.toString()
                sqList[position].hint = edtAnsHint.text.toString()
                que1Spinner.setText(sqList[position].question)
            }
            firstSpinnerIsSelected = true
            notifySpinner1()
            Log.d("printListSq", sqList[position].ans)
            Log.d("printListSq", sqList[position].hint)
        }
        rvAllQue.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvAllQue.adapter = adapter
        dialog.show()
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    private fun showRelationDialog2() {
        notifySpinner2()
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_question)
        val title = dialog.findViewById<TextView>(R.id.lbl_title_1)
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

        val rvAllQue = dialog.findViewById<RecyclerView>(R.id.rv_all_question)
        val adapter = QuesAdapter(requireContext(), filterSqList) { position ->
            dialog.dismiss()
            for (i in filterSqList.indices) {
                filterSqList[i].selected = false
            }
            binding.apply {
                filterSqList[position].selected = true
                filterSqList[position].ans = edtAns2.text.toString()
                filterSqList[position].hint = edtAnsHint2.text.toString()
                que2Spinner.setText(filterSqList[position].question)
            }

            secondSpinnerIsSelected = true
            Log.d("printListSqfilter", filterSqList[position].ans)
            Log.d("printListSqfilter", filterSqList[position].hint)
            notifySpinner1()
            // setAdapterSpinner1()
        }
        sqList.clear()
        sqList.addAll(mainSqList)
        rvAllQue.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvAllQue.adapter = adapter
        dialog.show()
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_dark)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                dialog.findViewById<ConstraintLayout>(R.id.top_layout_relation).setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    private fun happyThemeChanges() {/*  if (BaseActivity.isCustomMode) {
              binding.apply {
                  topLayout.setBackgroundResource(R.color.yellow_500)
              }
          }*/
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.btnSaveQ1.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSaveQ2.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSaveTpin.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnSavePassword.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnSaveQ1.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSaveQ2.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSaveTpin.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSavePassword.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.btnSaveQ1.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSaveQ2.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSaveTpin.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnSavePassword.setBackgroundResource(R.drawable.bg_gradient_light_btn)
            }
        }
    }

    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is BaseActivity.CodeSentModel -> {
                    hideLoading()
                    showMobileVerifyBottomSheet()
                    phoneVerificationId = it.verificationId
                    phoneToken = it.token
                    verificationLiveData.value = null
                }
                is BaseActivity.VerificationCompleteModel -> {
                    hideLoading()
                    binding.apply {
                        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        bottomSheetMobileVerify.dismiss()
                        when (updateType) {
                            UpdateType.QUE1 -> {
                                selectedSq.clear()
                                binding.apply {
                                    for (i in sqList.indices) {
                                        if (sqList[i].selected) {
                                            selectedSq.add(
                                                UpdateSqlRequest.QueData(
                                                    quesNo = "1",
                                                    ans = edtAns.text.toString(),
                                                    hint = edtAnsHint.text.toString(),
                                                    ques = sqList[i].id
                                                )
                                            )
                                            return@apply
                                        }
                                    }
                                }
                                Log.d("Tag", selectedSq.toString())
                                showLoading()
                                securityQuestionViewModel.postSQ(
                                    UpdateSqlRequest(
                                        selectedSq, SharePreferences.getStringValue(SharePreferences.USER_ID, ""), SharePreferences.getStringValue(SharePreferences.USER_TOKEN, "")
                                    )
                                )
                            }

                            UpdateType.QUE2 -> {
                                pref().apply {
                                    selectedSq.clear()
                                    binding.apply {
                                        for (i in filterSqList.indices) {
                                            if (filterSqList[i].selected) {
                                                selectedSq.add(
                                                    UpdateSqlRequest.QueData(
                                                        quesNo = "2",
                                                        ans = edtAns2.text.toString(),
                                                        hint = edtAnsHint2.text.toString(),
                                                        ques = filterSqList[i].id
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Log.d("Tag", selectedSq.toString())
                                    showLoading()
                                    securityQuestionViewModel.postSQ(
                                        UpdateSqlRequest(
                                            selectedSq,
                                            getStringValue(USER_ID, ""),
                                            getStringValue(USER_TOKEN, "")
                                        )
                                    )
                                }
                            }

                            UpdateType.PASSWORD -> {
                                showLoading()
                                securityQuestionViewModel.changeUpdatePassword(
                                    pref().getStringValue(SharePreferences.USER_ID, ""),
                                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                    edtOldPassword.text.toString(),
                                    edtPassword.text.toString())
                            }
                            UpdateType.TPIN -> {
                                userID.let { uID ->
                                    if (uID.isNotBlank()) {
                                        showLoading()
                                        if (oldTpin) {
                                            securityQuestionViewModel.checkValidTPIN(userID, binding.oldTpinView.text.toString().trim())
                                        } else {
                                            if (pref().getStringValue(SharePreferences.DEFAULT_AC_TYPE, "").equals(SharePreferences.AcType.WALLET.name, true)) {
                                                securityQuestionViewModel.changeTpi(userID, userWalletUUID!!, binding.tpinView.text.toString().trim())
                                            } else {
                                                securityQuestionViewModel.changeSavingCurrentTpi(userID, userToken, userWalletUUID!!, binding.tpinView.text.toString().trim())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    verificationLiveData.value = null
                }

                is BaseActivity.FirebaseExceptionModel -> {
                    hideLoading()
                    if (it.e is FirebaseAuthInvalidCredentialsException) {
                        showToast("The phone number provided is incorrect")
                    }
                    Log.d("FirebaseException==", it.e.toString())
                    verificationLiveData.value = null
                }

                is BaseActivity.CodeAutoRetrievalTimeOutModel -> {
                    hideLoading()
                    showToast(it.s0)
                    Log.d("CodeAutoRetrievalT", it.s0)
                    verificationLiveData.value = null
                }
            }
        }
    }

    private fun sentOTP() {
        val phoneNumber = pref().getStringValue(SharePreferences.USER_COUNTRY_CODE, "") + pref().getStringValue(SharePreferences.USER_MOBILE_NUMBER, "")
        (requireActivity() as BconfigActivity).sendVerificationCode(phoneNumber)
    }

    private fun showMobileVerifyBottomSheet() {
        var isKeyboardVisible: Boolean = false
        bottomSheetMobileVerify = BottomSheetDialog(requireContext()).apply {
            val bindingDialog = DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (phoneVerificationId.isBlank()) {
                            showToast("The phone number provided is incorrect")
                            return false
                        }
                        if (otpView.otp.toString().isBlank()) {
                            showToast("Enter OTP")
                            return false
                        }
                        return true
                    }

                    if (validationOtp()) {
                        pref().apply {
                            (requireActivity() as BconfigActivity).verifyPhoneNumberWithCode(phoneVerificationId, otpView.otp.toString().trim())
                        }
                    } else {
                        showToast("error")
                    }
                }/*     otpView.setOnClickListener {
                         if (!isKeyboardVisible){
                             showSoftKeyBoard(otpView)
                             otpView.requestFocus()
                         }
                     }
                     KeyboardVisibilityEvent.setEventListener(
                         requireActivity(),
                         viewLifecycleOwner
                     ) { isOpen -> isKeyboardVisible = isOpen }*/
            }
            show()
            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    bindingDialog.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
            }
        }
    }

    private enum class UpdateType(str: String) {
        QUE1("Que1"), QUE2("Que2"), PASSWORD("PASS"), TPIN("TPIN"),
    }

    private fun visibleStateChange(i: Int) {
        binding.apply {
            when (i) {
                0 -> {
                    cardPassword.show()
                    cardQue1.hide()
                    cardQue2.hide()
                    cardTpin.hide()
                    btnSavePassword.show()
                    btnSaveQ1.hide()
                    btnSaveQ2.hide()
                    btnSaveTpin.hide()
                }
                1 -> {
                    cardTpin.show()
                    cardPassword.hide()
                    cardQue1.hide()
                    cardQue2.hide()
                    btnSavePassword.hide()
                    btnSaveQ1.hide()
                    btnSaveQ2.hide()
                    btnSaveTpin.show()
                }
                2 -> {
                    cardQue1.show()
                    cardPassword.hide()
                    cardQue2.hide()
                    cardTpin.hide()
                    btnSavePassword.hide()
                    btnSaveQ1.show()
                    btnSaveQ2.hide()
                    btnSaveTpin.hide()
                }
                3 -> {
                    cardQue2.show()
                    cardPassword.hide()
                    cardQue1.hide()
                    cardTpin.hide()
                    btnSavePassword.hide()
                    btnSaveQ1.hide()
                    btnSaveQ2.show()
                    btnSaveTpin.hide()
                }
            }
        }
    }

    private fun showSuccessDilao(msg: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
            // setTitle(msg)
            setMessage("Update Successful.")
            setCancelable(false)
            setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                requireActivity().finish()
            }
        }
        dialog.show()
    }

    override fun getLayoutId(): Int = R.layout.security_question_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): SecurityQuestionViewModel = securityQuestionViewModel
}