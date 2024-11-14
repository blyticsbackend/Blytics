package com.nbt.blytics.modules.signupprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.countrypicker.CallbackCountrySelected
import com.nbt.blytics.countrypicker.CallbackSelectState
import com.nbt.blytics.countrypicker.CountryPicker
import com.nbt.blytics.countrypicker.StatePicker
import com.nbt.blytics.countrypicker.models.CountriesStates
import com.nbt.blytics.databinding.AvatarBottomSheetBinding
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.SignupProfileEditFragmentBinding
import com.nbt.blytics.modules.allaccount.AllAccountFragment.Companion.LINKED_AC_ADDRESS
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.otp.model.EmailOtpResponse
import com.nbt.blytics.modules.phoneregistation.PhoneRegistrationFragment
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.setpassword.models.AddAvatarResponse
import com.nbt.blytics.modules.setpassword.models.SignUpResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.adapter.AvatarAdapter
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.signupprofile.models.BvnCheckResult
import com.nbt.blytics.modules.signupprofile.models.LInkedAcRequest
import com.nbt.blytics.modules.signupprofile.models.LinkedAccResponse
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.UtilityHelper.isEmailValid
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class SignupProfileEditFragment : BaseFragment<SignupProfileEditFragmentBinding, SignupProfileEditViewModel>() {
    private lateinit var binding: SignupProfileEditFragmentBinding
    private val signupProfileEditViewModel: SignupProfileEditViewModel by viewModels()
    private lateinit var avatarBottomSheet: BottomSheetDialog
    private var selectedGenderCode: Int = -1
    private var selectedRelation: String = ""
    private var phoneNumber: String? = ""
    private lateinit var avatarAdapter: AvatarAdapter
    private var datePicker: MaterialDatePicker<*>? = null
    private val avatarList = mutableListOf<AvatarModel.Data>()
    private var selectCountryName: String = ""
    private var selectStateName: String = ""
    private lateinit var emailDialog: BottomSheetDialog
    private var isDoc1Selected = false
    private var isDoc2Selected = false
    private var document1: File? = null
    private var document2: File? = null
    private var selectedDOB: String = ""
    private var comingFor: String = ""
    private val localCounties = arrayListOf("NIGERIA", "India", "Ireland", "United Kingdom")
    private val TAG = "Signup_Profile_Edit_Fra"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        phoneNumber = requireArguments().getString(Constants.PHONE_NUMBER)
        comingFor = requireArguments().getString(Constants.COMING_FOR, "")
        CountryPicker(requireContext()).apply {
            findCountryDataByName("NIGERIA")
            binding.countrySpinner.setText("Nigeria")
            selectCountryName = "Nigeria"
        }

        if (comingFor == Constants.LINKED_ACC) {
            binding.imgProfile.hide()
            binding.btnAddImage.hide()
            binding.apply {
                edtAddress.setText(pref().getStringValue(SharePreferences.USER_ADDRESS, ""))
                countrySpinner.setText(pref().getStringValue(SharePreferences.USER_COUNTRY, ""))
                stateSpinner.setText(pref().getStringValue(SharePreferences.USER_STATE, ""))
                edtPinCode.setText(pref().getStringValue(SharePreferences.USER_PIN_CODE, ""))
                selectStateName = pref().getStringValue(SharePreferences.USER_STATE, "")
                selectCountryName = pref().getStringValue(SharePreferences.USER_COUNTRY, "")
            }

            if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) {
                binding.apply {
                    PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED?.apply {
                        edtFirstName.setText(firstName)
                        edtLastName.setText(lastName)
                        edtEmail.setText(email)
                        val linkAcAdd = LINKED_AC_ADDRESS

                        if (linkAcAdd != null) {
                            /* edtAddress.setText(linkAcAdd.address)
                             countrySpinner.setText(linkAcAdd.country)
                             stateSpinner.setText(linkAcAdd.state)
                             edtPinCode.setText(linkAcAdd.pin_code)
                             selectStateName = linkAcAdd.state
                             selectCountryName = linkAcAdd.country*/
                            // isUserLocal(selectCountryName)
                        }
                        edtFirstName.isEnabled = false
                        edtFirstName.isFocusable = false
                        edtLastName.isEnabled = false
                        edtLastName.isFocusable = false
                    }
                }
            }
        } else {
            try {
                (requireActivity() as UserActivity).toolbarTitle("Sign up")
            } catch (ex: Exception) {
            }
        }
        observer()
        setAvatarAdapter()
        showLoading()
        fillGenderList()/*   fillRelationList()*/
        signupProfileEditViewModel.getAvatars()
        binding.btnContinue.setOnClickListener {
            if (validation()) {
                nextView()
            }
        }
        binding.relationSpinner.setOnClickListener {
            showRelationDialog()
        }
        binding.btnContinueAddress.setOnClickListener {
            if (validationAddress()) {
                nextView()
            }
        }

        binding.btnContinueDob.setOnClickListener {
            if (validationDob()) {
                nextView()
            }
        }
        binding.btnDob.setOnClickListener {
            /* if (datePicker != null) {
                 if (datePicker!!.isVisible.not()) {
                     datePicker!!.show(requireActivity().supportFragmentManager, null)
                 }
             } else {*/
            datePicker()
            // }
        }

        binding.btnAddImage.setOnClickListener {
            showAvatarDialog()
        }
        binding.txtUpload.setOnClickListener {
            showAvatarDialog()
        }

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (comingFor.equals(Constants.LINKED_ACC, true)) {
                        binding.cardImg.hide()
                        if (binding.viewFlipper.displayedChild != 8) {
                            binding.profileStatusBar.show()
                        }
                        if (binding.viewFlipper.displayedChild == 0) {
                            remove()
                            requireActivity().onBackPressed()
                        } else {
                            backView()
                        }
                    } else {
                        binding.cardImg.hide()
                        if (binding.viewFlipper.displayedChild != 8) {
                            binding.cardImg.hide()
                            binding.profileStatusBar.show()
                        }
                        if (binding.viewFlipper.displayedChild == 8) {
                            binding.viewFlipper.displayedChild = 6
                            viewFlipperChange(binding.viewFlipper.displayedChild)
                        } else if (binding.viewFlipper.displayedChild == 0) {
                            remove()
                            requireActivity().onBackPressed()
                        } else {
                            backView()
                        }
                    }
                }
            })

        binding.btnContinueEmail.setOnClickListener {
            if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) {
                nextView()
            } else if (binding.edtEmail.text.toString().isNotBlank()) {
                showLoading()
                signupProfileEditViewModel.checkExist(binding.edtEmail.text.toString().trim())
            } else {
                showToast("Enter email")
            }
        }
        binding.btnSkipEmail.setOnClickListener {
            binding.edtEmail.setText("")
            nextView()
        }
        binding.btnCountryPicker.setOnClickListener {
            binding.edtBvn.setText("")
            CountryPicker(requireContext()).apply {
                setOnCountryChangeListener(object : CallbackCountrySelected {
                    override fun selectedCountry(country: CountriesStates.Country) {
                        binding.countrySpinner.setText(country.countryName)
                        selectCountryName = country.countryName
                        isUserLocal(selectCountryName)
                        binding.stateSpinner.setText("")
                        selectStateName = ""
                    }
                })
                init()
            }
        }
        binding.btnStatePicker.setOnClickListener {
            if (selectCountryName.isNotBlank()) {
                StatePicker(requireContext()).apply {
                    setOnStateChangeListener(object : CallbackSelectState {
                        override fun selectedState(state: CountriesStates.Country.State) {
                            binding.stateSpinner.setText(state.stateName)
                            selectStateName = state.stateName
                        }
                    })
                    init()
                }
            } else {
                showToast("Please select country")
            }
        }
        binding.btnContinueGender.setOnClickListener {
            if (selectedGenderCode > -1) {
                nextView()
            } else {
                showToast("Please select gender")
            }
        }
        if (comingFor.equals(Constants.LINKED_ACC, true)) {
            // binding.edtBvn
        }
        binding.btnCreateForm.setOnClickListener {
            if (comingFor.equals(Constants.LINKED_ACC, true)) {

                if (validationSignUp()) {
                    binding.apply {
                        val address = edtAddressShow.text.toString().trim()
                        showLoading()
                        val linkedReq = LInkedAcRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            edtFirstNameShow.text.toString(),
                            edtLastShow.text.toString(),
                            edtEmailShow.text.toString(),
                            address,
                            edtPinCode.text.toString(),
                            selectCountryName,
                            selectStateName,
                            LInkedAcRequest.UserPhone(if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.mobNo else "", Constants.DEFAULT_COUNTRY_CODE, "false",),
                            selectedRelation, PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null,
                            if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.userId else "",
                            false,
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            false
                        )
                        signupProfileEditViewModel.linkedAcCreate(linkedReq)
                    }
                }
            } else {
                if (validationSignUp()) {
                    binding.apply {
                        val address = edtAddressShow.text.toString().trim()
                        showLoading()
                        signupProfileEditViewModel.signUp(
                            edtFirstNameShow.text.toString(),
                            edtLastShow.text.toString(),
                            edtEmailShow.text.toString(),
                            selectedDOB,
                            selectedGenderCode,
                            edtPinCode.text.toString(),
                            address,
                            selectCountryName,
                            selectStateName,
                            edtPassword.text.toString(),
                            phoneNumber!!,
                            true,
                            pref().getStringValue(SharePreferences.DEVICE_TOKEN, ""),
                            Constants.DEFAULT_COUNTRY_CODE,
                            requireActivity()
                        )
                    }
                }
            }
        }

        binding.btnSetPassword.setOnClickListener {
            if (validationPassword()) {
                nextView()/* binding.apply {
                     val address = edtAddress.text.toString().trim()
                     showLoading()
                     signupProfileEditViewModel.signUp(
                         edtFirstName.text.toString(),
                         edtLastName.text.toString(),
                         edtEmail.text.toString(),
                         selectedDOB,
                         selectedGenderCode,
                         edtPinCode.text.toString(),
                         address,
                         selectCountryName,
                         selectStateName,
                         edtPassword.text.toString(),
                         phoneNumber!!,
                         true, pref().getStringValue(SharePreferences.DEVICE_TOKEN, ""),
                         Constants.DEFAULT_COUNTRY_CODE, requireActivity()

                     )
                 }*/
            }
        }
        binding.btnDoc1.setOnClickListener {
            isDoc1Selected = true
            isDoc2Selected = false
            checkPermissionsBVN()
        }
        binding.btnDoc2.setOnClickListener {
            isDoc1Selected = false
            isDoc2Selected = true
            checkPermissionsBVN()
        }
        binding.btnContinueBvn.setOnClickListener {
            fillShowForm()
            if (binding.edtBvn.isVisible) {
                if (validationBVN()) {
                    showLoading()
                    signupProfileEditViewModel.checkBVN(binding.edtBvn.text.toString().trim())
                }
            } else {
                if (document1 != null && document2 != null) {
                    nextView()
                } else {
                    showToast("please select documents.")
                }
            }/*    if (comingFor.equals(Constants.LINKED_ACC, true)) {
                    if (validationBVN()) {
                        nextView()
                        *//*  showLoading()
                      val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                      val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                      if (userId.isNotBlank()) {
                          signupProfileEditViewModel.registerBVN(
                              userId,
                              binding.edtBvn.text.toString().trim(),
                              userToken,
                              document1,
                              document2,
                              linkeUserID,
                              true
                          )
                      }*//*
                }
                return@setOnClickListener
            }

            if (validationBVN()) {
                nextView()
                *//*      showLoading()
                      val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                      val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                      if (userId.isNotBlank()) {
                          signupProfileEditViewModel.registerBVN(
                              userId,
                              binding.edtBvn.text.toString().trim(),
                              userToken,
                              document1,
                              document2,
                              isLinkedUser = false
                          )
                      }
      *//*
            }*/
        }

        binding.imgInfoProof.setOnClickListener {
            //showToast("Passport\nDriver Licence")
            /*   val ballon=  Balloon.Builder(requireContext())
                 .setWidthRatio(1.0f)
                 .setHeight(BalloonSizeSpec.WRAP)
                 .setText("Passport\nDriver Licence")
                 .setTextColorResource(R.color.black)
                 .setTextSize(15f)
                 .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                 .setArrowSize(10)
                 .setArrowPosition(0.5f)
                 .setPadding(12)
                 .setCornerRadius(8f)
                 .setBackgroundColorResource(R.color.gray_light)
                 .setBalloonAnimation(BalloonAnimation.ELASTIC)
                 .setLifecycleOwner(viewLifecycleOwner)
                 .build()
             ballon.dismissWithDelay(1000L)*/
        }
        binding.imgInfoAddress.setOnClickListener {
            //  showToast("Utility Bill\nBank Statement\nPayment Slip\nCredit Card Statment")
            /*  val ballon=  Balloon.Builder(requireContext())
                  .setWidthRatio(1.0f)
                  .setHeight(BalloonSizeSpec.WRAP)
                  .setText("Utility Bill\nBank Statement\nPayment Slip\nCredit Card Statment")
                  .setTextColorResource(R.color.black)
                  .setTextSize(15f)
                  .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                  .setArrowSize(10)
                  .setArrowPosition(0.5f)
                  .setPadding(12)
                  .setCornerRadius(8f)
                  .setBackgroundColorResource(R.color.gray_light)
                  .setBalloonAnimation(BalloonAnimation.ELASTIC)
                  .setLifecycleOwner(viewLifecycleOwner)
                  .build()
              ballon.dismissWithDelay(1000L)*/
        }
        binding.btnSkipBvn.setOnClickListener {
            goToMainActivity()
        }

        binding.btnCancelForm.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            if (selectedRelation == "") {
                return@setOnClickListener
            } else {
                fillShowForm()
                nextView()
            }
            /*   binding.apply {
                   val address = edtAddress.text.toString().trim()
                   showLoading()
                   val linkedReq = LInkedAcRequest(
                       pref().getStringValue(SharePreferences.USER_ID, ""),
                       pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                       edtFirstName.text.toString(),
                       edtLastName.text.toString(),
                       edtEmail.text.toString(),
                       address,
                       edtPinCode.text.toString(),
                       selectCountryName,
                       selectStateName,
                       LInkedAcRequest.UserPhone(
                           if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.mobNo else "",
                           Constants.DEFAULT_COUNTRY_CODE,
                           "false",),
                       selectedRelation,
                       PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null,
                       if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.userId else "",
                       false,
                       pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                       false
                   )
                   signupProfileEditViewModel.linkedAcCreate(linkedReq)
               }*/
        }
    }
    private fun validationSignUp(): Boolean {
        binding.apply {
            if (edtFirstNameShow.text.toString().isBlank()) {
                // layoutName.error = "Enter first name"
                showToast("Enter first name")
                return false
            }
            if (edtLastShow.text.toString().isBlank()) {
                // layoutLastName.error = "Enter last name"
                showToast("Enter last name")
                return false
            }
            val pattern = Pattern.compile(Constants.SPECIAL_CHAR)
            if (pattern.matcher(edtFirstNameShow.text.toString()).matches() && pattern.matcher(
                    edtLastShow.text.toString()
                ).matches()
            ) {
                showToast("Special characters are not allowed.")
                return false
            }
            if (edtAddressShow.text.toString().trim().isBlank()) {
                showToast("Enter address")
                return false
            }
            if (selectCountryName.isBlank()) {
                showToast("Select Country")
                return false
            }
            if (selectStateName.isBlank()) {
                showToast("Select State")
                return false
            }
            if (edtBvnShow.isVisible) {
                if (edtBvnShow.text.toString().isBlank()) {
                    showToast("Enter BVN")
                    return false
                }
            }
            if (edtEmailShow.text.toString().replace(" ", "").isNotBlank()) {
                if (edtEmailShow.text.toString().replace(" ", "").isEmailValid().not()) {
                    showToast("Please enter correct email")
                    return false
                }
            }/* if (comingFor.equals(Constants.LINKED_ACC,true).not()) {
                 if (USER_SELECTED_AVATAR == null && USER_IMAGE_BITMAP == null) {
                     showToast("Upload profile image")
                     return false
                 }
             }*/
            return true
        }
    }

    private fun uploadBVN() {
        var isLinked = comingFor.equals(Constants.LINKED_ACC, true)
        showLoading()
        val userId = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        if (userId.isNotBlank()) {
            signupProfileEditViewModel.registerBVN(userId, binding.edtBvn.text.toString().trim(), userToken, document1, document2, linkeUserID, isLinked)
        }
    }

    private fun fillShowForm() {
        binding.apply {
            edtFirstNameShow.isEnabled = false
            edtLastShow.isEnabled = false
            edtAddressShow.isEnabled = false
            edtStateShow.isEnabled = false
            edtCountryShow.isEnabled = false
            edtBvnShow.isEnabled = false
            edtEmailShow.isEnabled = false
            edtFirstNameShow.setText(edtFirstName.text.toString())
            edtLastShow.setText(edtLastName.text.toString())
            edtAddressShow.setText(edtAddress.text.toString())
            edtStateShow.setText(selectStateName)
            edtCountryShow.setText(selectCountryName)
            edtBvnShow.setText(edtBvn.text.toString())
            edtEmailShow.setText(edtEmail.text.toString())
            btnNameEdt.setOnClickListener {
                if (binding.btnNameEdt.text.toString() == "Edit") {
                    binding.btnNameEdt.text = "Save"
                    edtFirstNameShow.isEnabled = true
                    edtFirstNameShow.isFocusable = true
                } else {
                    binding.btnNameEdt.text = "Edit"
                    edtFirstNameShow.isEnabled = false
                    edtFirstNameShow.isFocusable = false
                }
            }
            btnLastEdt.setOnClickListener {
                if (binding.btnLastEdt.text.toString() == "Edit") {
                    binding.btnLastEdt.text = "Save"
                    edtLastShow.isEnabled = true
                    edtLastShow.isFocusable = true
                } else {
                    binding.btnLastEdt.text = "Edit"
                    edtLastShow.isEnabled = false
                    edtLastShow.isFocusable = false
                }
            }
            btnAddressEdt.setOnClickListener {
                if (binding.btnAddressEdt.text.toString() == "Edit") {
                    binding.btnAddressEdt.text = "Save"
                    edtAddressShow.isEnabled = true
                    //edtAddressShow.isFocusable = true
                } else {
                    binding.btnAddressEdt.text = "Edit"
                    edtAddressShow.isEnabled = false
                    //edtAddressShow.isFocusable = false
                }
            }
            btnEmailEdt.setOnClickListener {
                if (!binding.edtEmailShow.isEnabled) {
                    binding.btnEmailEdt.text = "Save"
                    binding.edtEmailShow.isEnabled = true
                    binding.edtEmailShow.isFocusableInTouchMode = true
                    binding.edtEmailShow.requestFocus() // Ensure focus is applied when enabled
                } else {
                    binding.btnEmailEdt.text = "Edit"
                    binding.edtEmailShow.isEnabled = false
                    binding.edtEmailShow.isFocusableInTouchMode = false
                }
            }

            btnStateEdt.setOnClickListener {
                if (binding.btnStateEdt.text.toString() == "Edit") {
                    binding.btnStateEdt.text = "Save"
                    if (selectCountryName.isNotBlank()) {
                        StatePicker(requireContext()).apply {
                            setOnStateChangeListener(object : CallbackSelectState {
                                override fun selectedState(state: CountriesStates.Country.State) {
                                    binding.edtStateShow.setText(state.stateName)
                                    selectStateName = state.stateName
                                }
                            })
                            init()
                        }
                    } else {
                        showToast("Please select country")
                    }
                } else {
                    binding.btnStateEdt.text = "Edit"
                }
            }
            btnCountryEdt.setOnClickListener {
                if (binding.btnCountryEdt.text.toString() == "Edit") {
                    binding.btnCountryEdt.text = "Save"
                    CountryPicker(requireContext()).apply {
                        setOnCountryChangeListener(object : CallbackCountrySelected {
                            override fun selectedCountry(country: CountriesStates.Country) {
                                binding.edtCountryShow.setText(country.countryName)
                                selectCountryName = country.countryName
                                isUserLocal(selectCountryName)
                                binding.edtStateShow.setText("")
                                selectStateName = ""
                            }
                        })
                        init()
                    }
                } else {
                    binding.btnCountryEdt.text = "Edit"
                }
            }
            btnBvnEdt.setOnClickListener {
                if (binding.btnBvnEdt.text.toString() == "Edit") {
                    binding.btnBvnEdt.text = "Save"
                    edtBvnShow.isEnabled = true
                    edtBvnShow.isFocusable = true
                } else {
                    binding.btnBvnEdt.text = "Edit"
                    edtBvnShow.isEnabled = false
                    edtCountryShow.isFocusable = false
                }
            }
        }
    }

    private fun validationBVN(): Boolean {
        if (comingFor.equals(Constants.LINKED_ACC, true)) {
            if (binding.edtBvn.isVisible) {
                if (binding.edtBvn.text.toString().isBlank()) {
                    showToast("Enter BVN.")
                    return false
                }
            }
            if (isUserLocal(selectCountryName).not()) {
                if (document1 == null || document2 == null) {
                    showToast("Select Document.")
                    return false
                }
            } else {
                return true
            }
            ///*if (isUserLocal(selectCountryName)) {*/
        } else if (binding.edtBvn.text.toString().isBlank()) {
            showToast("Enter BVN.")
            return false
        } else {/*if (document1 == null || document2 == null) {
                showToast("Select Document.")
                return false
            } else {
                return true
            }*/
        }
        /* if (document1 == null) {
             showToast("Select ID proof")
             return false
         }
         if (document2 == null) {
             showToast("Select address proof")
             return false
         }*/
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun viewFlipperChange(id: Int) {
        if (comingFor != Constants.LINKED_ACC) {
            binding.btnAddImage.show()
        }
        when (id) {
            0 -> {
                binding.lblTitleName.text = "What is your name?"
                binding.profileStatusBar.progress = 0
            }

            1 -> {
                binding.lblTitleDob.text = "${binding.edtFirstName.text},\nWhen were you born?"
                binding.profileStatusBar.progress = 1
            }
            2 -> {
                binding.lblTitleGender.text = "${binding.edtFirstName.text},\nPlease select your gender?"
                binding.profileStatusBar.progress = 2
            }

            3 -> {
                if (comingFor.equals(Constants.LINKED_ACC)) {
                    val userId = pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")
                 //   binding.lblTitleAddress.text = "${binding.edtFirstName.text},\nAddress to send card"
                    binding.lblTitleAddress.text = "$userId,\nAddress to send card"
                    binding.extraMoney2.visibility = View.VISIBLE
                } else {
                    binding.lblTitleAddress.text = "${binding.edtFirstName.text},\nWhere do you live?"
                    binding.extraMoney2.visibility = View.GONE
                }
                binding.profileStatusBar.progress = 3
            }

            4 -> {
                binding.lblTitleEmail.text = "We will like to know the email address for new user,\n${binding.edtFirstName.text}."
                binding.profileStatusBar.progress = 4
            }
            5 -> {
                binding.lblTitleSetPassword.text = "${binding.edtFirstName.text},\nPlease set a secure password"
                binding.profileStatusBar.progress = 5
            }
            6 -> {
                binding.btnAddImage.hide()
                binding.lblTitleBvn.text = "${pref().getStringValue(Constants.USER_FIRST_NAME,"")},\nPlease submit your BVN"
            }
            7 -> {
                binding.btnAddImage.hide()
                binding.lblTitleRelation.text = "${binding.edtFirstName.text}'s,\nrelation with ${pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")}"
                binding.profileStatusBar.progress = 5
            }
        }
    }

    private fun nextView() {
        fun next() {
            binding.viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
            binding.viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
            binding.viewFlipper.showNext()
            viewFlipperChange(binding.viewFlipper.displayedChild)
            hideSoftKeyBoard()/* if(binding.viewFlipper.displayedChild == 8){
                 binding.cardImg.show()
             }else{
                 binding.cardImg.hide()
             }*/
        }
        binding.cardImg.hide()
        binding.profileStatusBar.show()
        isUserLocal(selectCountryName)
        if (comingFor.equals(Constants.LINKED_ACC, true)) {
            if (binding.viewFlipper.displayedChild == 0) {
                binding.viewFlipper.displayedChild = 3
                viewFlipperChange(binding.viewFlipper.displayedChild)
            } else if (binding.viewFlipper.displayedChild == 3) {
                next()
                viewFlipperChange(binding.viewFlipper.displayedChild)
            } else if (binding.viewFlipper.displayedChild == 9) {
                binding.viewFlipper.displayedChild = 6
                viewFlipperChange(binding.viewFlipper.displayedChild)
            } else if (binding.viewFlipper.displayedChild == 7) {
                binding.viewFlipper.displayedChild = 6
                viewFlipperChange(binding.viewFlipper.displayedChild)
                isUserLocal(selectCountryName)
            } else if (binding.viewFlipper.displayedChild == 8) {
                binding.cardImg.hide()
            } else if (binding.viewFlipper.displayedChild == 6) {
                binding.viewFlipper.displayedChild = 8
                binding.cardImg.hide()
                binding.profileStatusBar.hide()
                viewFlipperChange(binding.viewFlipper.displayedChild)
            } else {
                binding.viewFlipper.displayedChild = 7
                viewFlipperChange(binding.viewFlipper.displayedChild)
                isUserLocal(selectCountryName)
            }
        } else {
            if (binding.viewFlipper.displayedChild == 6) {
                binding.viewFlipper.displayedChild = 8
                binding.cardImg.show()
                binding.profileStatusBar.hide()
                viewFlipperChange(binding.viewFlipper.displayedChild)
            } else {
                next()
            }
        }
    }

    private fun isUserLocal(country: String): Boolean {
        var isLocal = false
        for (i in localCounties.indices) {
            if (localCounties[i].equals(country, true)) {
                isLocal = true
            }
        }

        binding.edtBvn.show()
        binding.edtBvnShow.show()
        binding.btnBvnEdt.show()
        if (isLocal) {
            /* binding.edtBvn.show()
             binding.edtBvnShow.show()
             binding.btnBvnEdt.show()*/
            return true
        } else {
            if (comingFor.equals(Constants.LINKED_ACC, true)) {
                binding.edtBvnShow.hide()
                binding.btnBvnEdt.hide()
                binding.edtBvn.hide()
            } else {
                binding.edtBvnShow.show()
                binding.btnBvnEdt.show()
                binding.edtBvn.show()
            }
            //  binding.lblTitleBvn.text = "Document Verification"
            return false
        }
    }

    private fun backView() {
        fun back() {
            binding.viewFlipper.setInAnimation(requireContext(), android.R.anim.slide_in_left)
            binding.viewFlipper.setOutAnimation(requireContext(), android.R.anim.slide_out_right)
            binding.viewFlipper.showPrevious()
            viewFlipperChange(binding.viewFlipper.displayedChild)
            hideSoftKeyBoard()
        }
        if (binding.viewFlipper.displayedChild > 0) {
            if (comingFor.equals(Constants.LINKED_ACC)) {
                if (binding.viewFlipper.displayedChild == 8) {
                    binding.viewFlipper.displayedChild = 6
                    viewFlipperChange(binding.viewFlipper.displayedChild)

                } else if (binding.viewFlipper.displayedChild == 6) {
                    binding.viewFlipper.displayedChild = 7
                    viewFlipperChange(binding.viewFlipper.displayedChild)

                } else if (binding.viewFlipper.displayedChild == 7) {
                    binding.viewFlipper.displayedChild = 4
                    viewFlipperChange(binding.viewFlipper.displayedChild)
                } else if (binding.viewFlipper.displayedChild == 3) {
                    binding.viewFlipper.displayedChild = 0
                    viewFlipperChange(binding.viewFlipper.displayedChild)
                } else {
                    back()
                }
            } else {
                back()
            }
        }
    }

    private fun validationDob(): Boolean {
        if (selectedDOB.isBlank()) {
            showToast("Select your DOB")
            return false
        }
        return true
    }

    private fun validationAddress(): Boolean {
        binding.apply {
            if (edtAddress.text.toString().isBlank()) {
                showToast("Enter Address")
                return false
            }
            if (selectCountryName.isBlank()) {
                showToast("Select Country")
                return false
            }
            if (selectStateName.isBlank()) {
                showToast("Select State")
                return false
            }/* if (edtPinCode.text.toString().length < 5) {
                 showToast("Pin code should be 5 digit.")
                 return false
             }*/
        }
        return true
    }

    private var linkeUserID = -1
    private fun observer() {
        signupProfileEditViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is BvnCheckResult -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        nextView()
                    } else {
                        showToast(it.message)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is LinkedAccResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        if (it.bvnVerified) {
                            val dialog = MaterialAlertDialogBuilder(requireContext())
                            dialog.apply {
                                //setTitle(it.data?.BVN_message)
                                setMessage(it.data?.BVN_message)
                                setCancelable(false)
                                setPositiveButton("OK") { dialog, which ->
                                    goToMainActivity()
                                }
                            }
                            dialog.show()
                            return@observe
                        }
                        linkeUserID = it.linkedUserId
                        goToMainActivity()/* binding.viewFlipper.displayedChild = 7
                         nextView()*/
                    } else {
                        if (it.data!!.chargeable || it.data.link_charge) {
                            val errorDialog = MaterialAlertDialogBuilder(requireContext())
                            errorDialog.apply {
                                setTitle(it.message)
                                setMessage("Do you want to proceed?")
                                setCancelable(false)
                                setPositiveButton("Yes") { dialog, which ->
                                    dialog.dismiss()
                                    showLoading()
                                    binding.apply {
                                        val address = edtAddress.text.toString().trim()
                                        val linkedReq = LInkedAcRequest(
                                            pref().getStringValue(SharePreferences.USER_ID, ""),
                                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                                            edtFirstName.text.toString(),
                                            edtLastName.text.toString(),
                                            edtEmail.text.toString(),
                                            address,
                                            edtPinCode.text.toString(),
                                            selectCountryName,
                                            selectStateName,
                                            LInkedAcRequest.UserPhone(if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.mobNo else "", Constants.DEFAULT_COUNTRY_CODE, "false"),
                                            selectedRelation, PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null,
                                            if (PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED != null) PhoneRegistrationFragment.USER_DETAILS_FOR_LINKED!!.userId else "", it.data.link_charge, pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""), it.data.chargeable)
                                        signupProfileEditViewModel.linkedAcCreate(linkedReq)
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
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is AvatarModel -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            avatarList.clear()/*  val data1 = AvatarModel.Data(
                                  0,
                                  "https://www.freeiconspng.com/uploads/camera-icon-circle-21.png"
                              )
                              avatarList.add(data1)*/
                            avatarList.addAll(it.data)
                            avatarAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(it.status)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is BvnRegisterResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        if (comingFor.equals(Constants.LINKED_ACC, true)) {
                            goToMainActivity()
                        } else {
                            if (USER_SELECTED_AVATAR != null && USER_IMAGE_BITMAP != null) {
                                showLoading()
                                uploadAvatar()
                            } else {
                                goToMainActivity()
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is SignUpResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            val deviceToken = pref().getStringValue(SharePreferences.DEVICE_TOKEN, "")
                            pref().clearPreference()
                            saveLogin(userId, userToken, walletUuid, deviceToken)
                        }
                        showLoading()
                        uploadBVN()
                        //  uploadAvatar()
                        /* if (USER_SELECTED_AVATAR == null && USER_IMAGE_BITMAP == null) {
                             hideLoading()
                             nextView()
                             *//* findNavController().navigate(R.id.action_signInFragment_to_bvnVarificationFragment)*//*
                        } else {
                            uploadAvatar()
                        }*/
                    } else {
                        showToast(it.message)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is AddAvatarResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            //showToast("Successful Registration.")
                            USER_IMAGE_BITMAP = null
                            USER_SELECTED_AVATAR = null
                            USER_SELECTED_AVATAR_URL = ""
                            //  showToast(it.message)
                            goToMainActivity()
                        }
                    } else {
                        showToast(it.message)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }

                is CheckExistPhoneResponse -> {
                    if (it.status.equals(Constants.Status.FAILED.name, true)) {
                        if (comingFor.equals(Constants.LINKED_ACC, true)) {
                            signupProfileEditViewModel.otpEmail(binding.edtEmail.text.toString().trim(), it.forPreview)
                        } else {
                            hideLoading()
                            nextView()
                        }
                    } else {
                        hideLoading()
                        showToast("this email already registered")
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }
                is EmailOtpResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showEmailOTP(it.message, it.preview)
                    } else {
                        showToast(it.status)
                    }
                    signupProfileEditViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    signupProfileEditViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun showEmailOTP(otp: String, review: Boolean = false) {
        Toast.makeText(requireContext(), "${binding.edtFirstName.text}", Toast.LENGTH_SHORT).show()
        emailDialog = BottomSheetDialog(requireContext()).apply {
            setCancelable(false)
            val binding = DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
                setContentView(root)
                lblTitle.text = "Verify Email"
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (otpView.otp.toString().isBlank()) {
                            showToast("Enter OTP")
                            return false
                        }
                        return true
                    }
                        if (validationOtp()) {
                            binding.apply {
                                pref().apply {
                                    if (edtEmail.text.toString().trim().isEmailValid()) {
                                        if (otp.equals(otpView.otp.toString(), false)) {
                                            nextView()
                                            dismiss()
                                        } else {
                                            showToast("invalid otp.")
                                        }
                                    } else {
                                        showToast("invalid email.")
                                    }
                                }
                            }
                        } else {
                            showToast("error")
                        }
                }
            }
            show()
            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)

                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    binding.btnVerify.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                }
            }
        }
    }

    private fun uploadAvatar() {
        val userId = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        if (userId.isNotBlank()) {
            val avatarId = if (USER_SELECTED_AVATAR == null) {
                ""
            } else {
                USER_SELECTED_AVATAR!!.id.toString()
            }
            val file = if (USER_IMAGE_BITMAP != null) {
                UtilityHelper.bitmapToFile(USER_IMAGE_BITMAP!!, requireContext())
            } else {
                null
            }
            signupProfileEditViewModel.addAvatar(userId, avatarId, file, userToken)
        }
    }

    private fun validationPassword(): Boolean {
        binding.apply {
            if (edtPassword.text.toString().isBlank()) {
                showToast("Enter Password")
                return false
            }
            if (edtConfirmPassword.text.toString().isBlank()) {
                showToast("Enter Confirm Password")
                return false
            }
            if (edtPassword.text.toString().length < 4) {
                showToast("Password too short")
                return false
            }
            if (edtPassword.text.toString() != edtConfirmPassword.text.toString()) {
                showToast("Confirm password not match.")
                return false
            }
        }
        return true
    }

    private fun saveLogin(userId: String, userToken: String, walletUuid: String, deviceToken: String) {
        pref().apply {
            setStringValue(USER_ID, userId)
            setStringValue(USER_TOKEN, userToken)
            //setStringValue(USER_WALLET_UUID, walletUuid)
            setStringValue(DEVICE_TOKEN, deviceToken)
            setBooleanValue(PRESENTATION_COMPLETED, true)
        }
    }

    private fun fillGenderList() {
        val items = listOf(
            GenderModel("Female", 0),
            GenderModel("Male", 1),
            GenderModel("Other", 2)
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        binding.genderSpinner.setAdapter(adapter)
        binding.btnGender.setOnClickListener {
            binding.genderSpinner.showDropDown()
        }
        binding.genderSpinner.setOnItemClickListener { parent, view, position, id ->
            selectedGenderCode = items[position].code
        }
    }

    private fun fillRelationList() {
        val items = listOf(
            GenderModel("Business Partner", 0),
            GenderModel("Family - Partner", 1),
            GenderModel("Family - Dependant", 2),
            GenderModel(" Family - Sibling", 3),
            GenderModel("Family - Others", 4),
            GenderModel("Colleague", 5),
            GenderModel("Employee", 6),
            GenderModel("Others", 7))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (binding.relationSpinner as? AutoCompleteTextView)?.setAdapter(adapter)
        (binding.relationSpinner as? AutoCompleteTextView)?.setOnItemClickListener { parent, view, position, id ->
            selectedRelation = items[position].gender
        }
    }

    data class
    GenderModel(val gender: String, val code: Int) {
        override fun toString(): String {
            return gender
        }
    }

    private fun datePicker() {
        val calendarEnd = Calendar.getInstance()
        calendarEnd.add(Calendar.YEAR, -14)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val endData = format.format(calendarEnd.time)
        val endYear = endData.split("-")[0]
        val endMonth = endData.split("-")[1]
        val endDay = endData.split("-")[2]
        val calendarStart = Calendar.getInstance()
        calendarStart.add(Calendar.YEAR, -100)
        val startData = format.format(calendarStart.time)
        val startYear = startData.split("-")[0]
        val startMonth = startData.split("-")[1]
        val startDay = startData.split("-")[2]
        val dialog = DatePickerFragment(
            requireActivity(),
            endDay.toInt(),
            endMonth.toInt(),
            endYear.toInt(),
            startDay.toInt(),
            startMonth.toInt(),
            startYear.toInt()
        ) { year, month, day ->
            binding.edtDobDate.setText("$year-$month-$day")/* binding.edtDobMonth.setText(month.toString())
                 binding.edtDobYear.setText(year.toString())*/
            selectedDOB = "$year-$month-$day"
        }

        dialog.show(requireActivity().supportFragmentManager, "timePicker")/*      val c = Calendar.getInstance()
              val  dialog =  DatePickerDialog(requireContext(),
                  { view, year, m, dayOfMonth ->
                      val month = m+1
                      binding.edtDobDate.setText(dayOfMonth.toString())
                      binding.edtDobMonth.setText(month.toString())
                      binding.edtDobYear.setText(year.toString())
                      selectedDOB ="$year-$month-$dayOfMonth"
                  },
                  c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                  c.get(Calendar.DAY_OF_MONTH))

              dialog.show()*/
        /*    val dataBuilder = MaterialDatePicker.Builder.datePicker()
            dataBuilder.setCalendarConstraints(limitRange().build())
            datePicker = dataBuilder
                .setTheme(R.style.Theme_DatePicker)
                .build()
            datePicker!!.show(requireActivity().supportFragmentManager, null)

            datePicker!!.addOnPositiveButtonClickListener {
                val simpleFormat = SimpleDateFormat("yyy-MM-dd", Locale.US)
                val date = Date(it as Long)
                binding.edtDob.setText(simpleFormat.format(date))
            }*/
    }

    private fun limitRange(): CalendarConstraints.Builder {
        val constraintsBuilderRange = CalendarConstraints.Builder()
        val calendarStart = Calendar.getInstance()
        val calendarEnd = Calendar.getInstance()
        val yearStart = 1970
        val yearEnd = 2020
        val startMonth = 2
        val startDate = 1
        val endMonth = 3
        val endDate = 20
        calendarStart[yearStart, 1] = startDate - 1
        calendarEnd[yearEnd, 2] = endDate
        val minDate = calendarStart.timeInMillis
        val maxDate = calendarEnd.timeInMillis
        constraintsBuilderRange.setStart(minDate)
        constraintsBuilderRange.setEnd(maxDate)
        constraintsBuilderRange.setValidator(RangeValidator(minDate, maxDate))
        return constraintsBuilderRange
    }

    private fun validation(): Boolean {
        binding.apply {
            if (edtFirstName.text.toString().isBlank()) {
                // layoutName.error = "Enter first name"
                showToast("Enter first name")
                return false
            }
            if (edtLastName.text.toString().isBlank()) {
                // layoutLastName.error = "Enter last name"
                showToast("Enter last name")
                return false
            }
            val pattern = Pattern.compile(Constants.SPECIAL_CHAR)
            if (pattern.matcher(edtFirstName.text.toString()).matches() && pattern.matcher(edtLastName.text.toString()).matches()) {
                showToast("special characters are not allowed.")
                return false
            }
        }
        return true
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.CAMERA] == true) {
                Log.d(TAG, "Permission granted")
                avatarBottomSheet.dismiss()
                takePhotoFromCamera()
            } else {
                Log.d(TAG, "Permission not granted")
            }
        }

    private fun checkPermissions() {
        if (requireContext().let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
        } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))
        } else {
            if (requireContext().let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                avatarBottomSheet.dismiss()
                takePhotoFromCamera()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncherCamera.launch(intent)
    }

    private fun showAvatarDialog() {
        avatarBottomSheet.show()
    }

    private fun setAvatarAdapter() {
        avatarBottomSheet = BottomSheetDialog(requireContext()).apply {
            val avatarBottomSheetBinding = DataBindingUtil.inflate<AvatarBottomSheetBinding>(layoutInflater, R.layout.avatar_bottom_sheet, null, false).apply {
                setContentView(root)
                avatarAdapter = AvatarAdapter(avatarList) { pos ->
                    /*  for (i in avatarList.indices) {
                          if (pos != i) {
                              avatarList[i].isSelect = false
                          }
                      }*/
                    //avatarAdapter.notifyDataSetChanged()
                    USER_SELECTED_AVATAR = avatarList[pos]
                    USER_IMAGE_BITMAP = null
                    binding.imgProfile.setImage(avatarList[pos].image)
                    avatarBottomSheet.dismiss()
                    rvAvatar.hide()
                }
                rvAvatar.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvAvatar.adapter = avatarAdapter
                btnAvatar.setOnClickListener {
                    if (rvAvatar.isVisible) {
                        rvAvatar.hide()
                    } else {
                        rvAvatar.show()
                    }
                }
                btnPhoto.setOnClickListener {
                    checkPermissionGallary()
                }
                btnCamera.setOnClickListener {
                    checkPermissions()
                }
            }
        }
    }

    private val requestMultiplePermissionsBVN = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Log.d(TAG, "${it.key} = ${it.value}")
        }
        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.CAMERA] == true) {
            Log.d(TAG, "Permission granted")
            fetchFileBVN()
        } else {
            Log.d(TAG, "Permission not granted")
        }
    }

    fun fetchFileBVN() {
        resultLauncherBVN.launch(Intent.createChooser(getFileChooserIntentForImageAndPdf(), "Select File"))
    }

    private fun checkPermissionsBVN() {
        if (requireContext().let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
        } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (requireContext().let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                fetchFileBVN()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun fetchFile() {
        resultLauncherGallary.launch(Intent.createChooser(getFileChooserIntentForImageAndPdf(), "Select File"))
    }

    fun getFileChooserIntentForImageAndPdf(): Intent {
        val mimeTypes = arrayOf("image/*", "application/pdf")
        return Intent(Intent.ACTION_GET_CONTENT).setType("image/*|application/pdf").putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }

    private fun checkPermissionBVN() {
        fun getFileChooserIntentForImage(): Intent {
            val mimeTypes = arrayOf("image/*")
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        fun fetchFile() {
            resultLauncherGallary.launch(Intent.createChooser(getFileChooserIntentForImage(), "Select Photo"))
        }
        if (requireContext().let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
        } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (requireContext().let {
                    ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
                } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun checkPermissionGallary() {
        fun getFileChooserIntentForImage(): Intent {
            val mimeTypes = arrayOf("image/*")
            return Intent(Intent.ACTION_GET_CONTENT).setType("image/*").putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }

        fun fetchFile() {
            resultLauncherGallary.launch(Intent.createChooser(getFileChooserIntentForImage(), "Select Photo"))
        }

        if (requireContext().let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
        } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (requireContext().let {
                    ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
                } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissionsBVN.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    var resultLauncherGallary = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    try {
                        data.data?.let { documentUri ->
                            val file = getFile(requireContext(), documentUri)
                            val bmOptions = BitmapFactory.Options()
                            val imageBit = BitmapFactory.decodeFile(file.absolutePath, bmOptions)
                            USER_IMAGE_BITMAP = imageBit
                            USER_SELECTED_AVATAR = null
                            binding.imgProfile.setImage(imageBit)
                            avatarBottomSheet.dismiss()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    var resultLauncherBVN = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    try {
                        if (isDoc1Selected) {
                            data.data?.let { documentUri ->
                                document1 = getFile(requireContext(), documentUri)
                                // binding.btnDoc1.setBackgroundResource(R.color.app_green_light)
                                // binding.imgCheck1.show()
                                binding.imgDoc1.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_green_light), android.graphics.PorterDuff.Mode.SRC_IN)
                                Log.d("File_Path", documentUri.path.toString())
                            }
                        } else if (isDoc2Selected) {
                            data.data?.let { documentUri ->
                                document2 = getFile(requireContext(), documentUri)
                                // binding.btnDoc2.setBackgroundResource(R.color.app_green_light)
                                //  binding.imgCheck2.show()
                                binding.imgDoc2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_green_light), android.graphics.PorterDuff.Mode.SRC_IN)
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    var resultLauncherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    val thumbnail = data.extras!!.get("data") as Bitmap
                    thumbnail.let { bitMap ->
                        USER_IMAGE_BITMAP = bitMap
                        USER_SELECTED_AVATAR = null
                        binding.imgProfile.setImage(bitMap)
                        avatarBottomSheet.dismiss()
                    }
                }
            }
        }

    /*   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
           if (requestCode == Constants.CAMERA_) {
               if (data != null) {
                   val thumbnail = data.extras!!.get("data") as Bitmap
                   thumbnail.let { bitMap ->
                       USER_IMAGE_BITMAP = bitMap
                       USER_SELECTED_AVATAR = null
                       binding.imgProfile.setImage(bitMap)
                       avatarBottomSheet.dismiss()
                   }
               }
           }
           if (requestCode == Constants.GALLERY) {
               if (data != null) {
                   try {
                       if (isDoc1Selected) {
                           data.data?.let { documentUri ->
                               document1 = getFile(requireContext(), documentUri)
                               // binding.btnDoc1.setBackgroundResource(R.color.app_green_light)
                              // binding.imgCheck1.show()
                               binding.imgDoc1.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_green_light), android.graphics.PorterDuff.Mode.SRC_IN)
                               Log.d("File_Path", documentUri.path.toString())
                           }
                       } else if (isDoc2Selected) {
                           data.data?.let { documentUri ->
                               document2 = getFile(requireContext(), documentUri)
                               // binding.btnDoc2.setBackgroundResource(R.color.app_green_light)
                             //  binding.imgCheck2.show()
                               binding.imgDoc2.setColorFilter(ContextCompat.getColor(requireContext(), R.color.app_green_light), android.graphics.PorterDuff.Mode.SRC_IN)
                           }
                       } else {
                           data.data?.let { documentUri ->
                               val file = getFile(requireContext(), documentUri)
                               val bmOptions = BitmapFactory.Options()
                               val imageBit =
                                   BitmapFactory.decodeFile(file.absolutePath, bmOptions)
                               USER_IMAGE_BITMAP = imageBit
                               USER_SELECTED_AVATAR = null
                               binding.imgProfile.setImage(imageBit)
                               avatarBottomSheet.dismiss()
                           }
                       }
                   } catch (e: IOException) {
                       e.printStackTrace()
                   }
               }
           }
       }*/

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename =
            File(context.filesDir.path + File.separatorChar + queryName(context, uri))
        try {
            context.contentResolver.openInputStream(uri).use { ins ->
                createFileFromStream(ins!!, destinationFilename)
            }
        } catch (e: Exception) {
            Log.e("Save File", e.message!!)
            e.printStackTrace()
        }
        return destinationFilename
    }

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        Log.d("File_Name=======", name)
        return name
    }

    private fun createFileFromStream(ins: InputStream, destination: File?) {
        try {
            FileOutputStream(destination).use { os ->
                val buffer = ByteArray(4096)
                var length: Int
                while (ins.read(buffer).also { length = it } > 0) {
                    os.write(buffer, 0, length)
                }
                os.flush()
            }
        } catch (ex: java.lang.Exception) {
            Log.e("Save File", ex.message!!)
            ex.printStackTrace()
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra(Constants.COMING_FOR, ComingFor.SIGN_UP.name)
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBackgroundColor(requireActivity().resources.getColor(R.color.yellow_500))
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)
                btnBackgroundNight()
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                btnBackground()
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
                btnBackground()
            }
        }
    }

    private fun btnBackground() {
        binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnContinueDob.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnContinueAddress.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnContinueEmail.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnSkipEmail.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnContinueGender.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnSetPassword.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnContinueBvn.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnSkipBvn.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnCancelForm.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.btnCreateForm.setBackgroundResource(R.drawable.bg_gradient_light_btn)
        binding.cardTitleName.setCardBackgroundColor(resources.getColor(R.color.white))
        binding.cardTitleGender.setCardBackgroundColor(resources.getColor(R.color.white))
        binding.cardTitleDob.setCardBackgroundColor(resources.getColor(R.color.white))
        binding.cardEdt.setCardBackgroundColor(resources.getColor(R.color.white))
        binding.cardDobEdt.setCardBackgroundColor(resources.getColor(R.color.white))
        binding.cardGenderEdt.setCardBackgroundColor(resources.getColor(R.color.white))
    }

    private fun btnBackgroundNight() {
        binding.btnContinue.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnContinueDob.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnContinueAddress.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnContinueEmail.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnSkipEmail.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnContinueGender.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnSetPassword.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnContinueBvn.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnSubmit.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnSkipBvn.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnCancelForm.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.btnCreateForm.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
        binding.cardTitleName.setCardBackgroundColor(resources.getColor(R.color.black))
        binding.cardTitleGender.setCardBackgroundColor(resources.getColor(R.color.black))
        binding.cardTitleDob.setCardBackgroundColor(resources.getColor(R.color.black))
        binding.cardEdt.setCardBackgroundColor(resources.getColor(R.color.black))
        binding.cardDobEdt.setCardBackgroundColor(resources.getColor(R.color.black))
        binding.cardGenderEdt.setCardBackgroundColor(resources.getColor(R.color.black))
    }

    /*  public inner class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
          override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
              val c = Calendar.getInstance()
              val hour = c.get(Calendar.HOUR_OF_DAY)
              val minute = c.get(Calendar.MINUTE)
              // Create a new instance of TimePickerDialog and return it
              return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
          }
          override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
              binding.edtDobDate.setText(view.toString())
          }
      }*/

    private fun showRelationDialog() {
        val items = listOf(
            GenderModel("Business Partner", 0),
            GenderModel("Family - Partner", 1),
            GenderModel("Family - Dependant", 2),
            GenderModel(" Family - Sibling", 3),
            GenderModel("Family - Others", 4),
            GenderModel("Colleague", 5),
            GenderModel("Employee", 6),
            GenderModel("Others", 7)
        )
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_relation)
        val btnBpartner = dialog.findViewById<LinearLayout>(R.id.btn_bpartner)
        val btnFpartner = dialog.findViewById<LinearLayout>(R.id.btn_fpartner)
        val bntFdependant = dialog.findViewById<LinearLayout>(R.id.btn_f_dependant)
        val btnFsibling = dialog.findViewById<LinearLayout>(R.id.btn_sibling)
        val btnFother = dialog.findViewById<LinearLayout>(R.id.btn_f_other)
        val btnColleague = dialog.findViewById<LinearLayout>(R.id.btn_colleague)
        val btnEmployee = dialog.findViewById<LinearLayout>(R.id.btn_employee)
        val btnOther = dialog.findViewById<LinearLayout>(R.id.btn_other)
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

        btnBpartner.setOnClickListener {
            selectedRelation = items[0].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnFpartner.setOnClickListener {
            selectedRelation = items[1].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        bntFdependant.setOnClickListener {
            selectedRelation = items[2].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnFsibling.setOnClickListener {
            selectedRelation = items[3].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnFother.setOnClickListener {
            selectedRelation = items[4].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnColleague.setOnClickListener {
            selectedRelation = items[5].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnEmployee.setOnClickListener {
            selectedRelation = items[6].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }
        btnOther.setOnClickListener {
            selectedRelation = items[7].gender
            dialog.dismiss()
            binding.relationSpinner.setText(selectedRelation)
        }

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

    companion object {
        var USER_IMAGE_BITMAP: Bitmap? = null
        var USER_SELECTED_AVATAR: AvatarModel.Data? = null
        var USER_SELECTED_AVATAR_URL: String = ""
    }
    override fun getLayoutId(): Int = R.layout.signup_profile_edit_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): SignupProfileEditViewModel = signupProfileEditViewModel
}