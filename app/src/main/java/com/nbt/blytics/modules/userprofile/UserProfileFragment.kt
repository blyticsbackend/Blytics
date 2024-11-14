package com.nbt.blytics.modules.userprofile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.callback.verificationLiveData
import com.nbt.blytics.countrypicker.CallbackCountrySelected
import com.nbt.blytics.countrypicker.CallbackSelectState
import com.nbt.blytics.countrypicker.CountryPicker
import com.nbt.blytics.countrypicker.StatePicker
import com.nbt.blytics.countrypicker.models.CountriesStates
import com.nbt.blytics.databinding.AvatarBottomSheetBinding
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.UserProfileFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.adapter.AvatarAdapter
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.*
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.UtilityHelper.isEmailValid
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : BaseFragment<UserProfileFragmentBinding, UserProfileViewModel>() {
    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private lateinit var avatarBottomSheet: BottomSheetDialog
    private lateinit var avatarAdapter: AvatarAdapter
    private lateinit var binding: UserProfileFragmentBinding
    private val avatarList = mutableListOf<AvatarModel.Data>()
    private var datePicker: MaterialDatePicker<*>? = null
    private val TAG = "Profile Camera Permission=="
    var securityQuesList = mutableListOf<SQ>()
    private var lastClickedView: View? = null
    private var phoneVerificationId: String = ""
    private lateinit var bottomSheetMobileVerify: BottomSheetDialog
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    // private var selectCountryName: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        observerOTP()
        /* observerMobileVerify()*/
        setAvatarAdapter()
        showLoading()
        userProfileViewModel.getAvatars()
        setProfileData()
        binding.btnAddImage.setOnClickListener {
            showAvatarDialog()
        }
        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        binding.btnEditName.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutNameUpdate.show()
                    edtFullName.setText(getStringValue(USER_FIRST_NAME, ""))
                    edtLastName.setText(getStringValue(USER_LAST_NAME, ""))
                    btnCancel.setOnClickListener {
                        edtFullName.setText("")
                        edtLastName.setText("")
                        binding.layoutNameUpdate.hide()
                    }
                    btnSave.setOnClickListener {
                        chkName.text = edtFullName.text.toString().trim() + " " + edtLastName.text.toString().trim()
                        binding.layoutNameUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateName(userID, userToken, edtFullName.text.toString().trim(), edtLastName.text.toString().trim()))
                    }
                }
            }

            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutNameUpdate
        }

        binding.btnEditEmail.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutEmailUpdate.show()
                    edtEmail.setText(getStringValue(USER_EMAIL, ""))
                    btnCancelEmail.setOnClickListener {
                        edtEmail.setText("")
                        binding.layoutEmailUpdate.hide()
                    }
                    btnSaveEmail.setOnClickListener {
                        chkEmail.text = edtEmail.text.toString().trim()
                        if (edtEmail.text.toString().trim().isEmailValid()) {
                            showLoading()
                            userProfileViewModel.updateProfileInfo(UpdateEmail(userID, userToken, edtEmail.text.toString().trim(), false))
                            binding.layoutEmailUpdate.hide()
                        } else {
                            showToast("invalid email.")
                        }
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutEmailUpdate
        }

        binding.btnEditMobile.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutMobileUpdate.show()
                    edtMobile.setText(getStringValue(USER_MOBILE_NUMBER, ""))
                    btnCancelMobile.setOnClickListener {
                        edtMobile.setText("")
                        binding.layoutMobileUpdate.hide()
                    }
                    btnSaveMobile.setOnClickListener {
                        showLoading()
                        val newMobileNo = "+91${binding.edtMobile.text.toString().trim()}"
                        (requireActivity() as MainActivity).sendVerificationCode(newMobileNo)
                        /* findNavController().navigate(
                             R.id.action_userProfileFragment_to_phoneRegistrationFragment2, bundleOf(Constants.COMING_FOR to ComingFor.MOBILE_VERIFY.name,Constants.PHONE_NUMBER to edtMobile.text.toString().trim())
                         )*//* chkMobileNo.text = edtMobile.text.toString().trim()
                         binding.layoutMobileUpdate.hide()
                         showLoading()
                         userProfileViewModel.updateProfileInfo(UpdateMobile(userID, userToken, edtMobile.text.toString().trim(), true))*/
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutMobileUpdate
        }

        binding.btnBvn.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutBvnUpdate.show()
                    if (getStringValue(USER_BVN, "").equals("Register your BVN", true)) {
                        edtBvn.setText("")
                    } else {
                        edtBvn.setText(getStringValue(USER_BVN, ""))
                    }
                    btnCancelBvn.setOnClickListener {
                        edtBvn.setText("")
                        binding.layoutBvnUpdate.hide()
                    }
                    btnSaveBvn.setOnClickListener {
                        chkBvn.text = edtBvn.text.toString().trim()
                        binding.layoutBvnUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateBVN(userID, userToken, edtBvn.text.toString().trim()))
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutBvnUpdate
        }

        binding.btnEditAddress.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutAddressUpdate.show()
                    edtAddress.setText(getStringValue(USER_ADDRESS, ""))
                    btnCancelAddress.setOnClickListener {
                        edtAddress.setText("")
                        binding.layoutAddressUpdate.hide()
                    }
                    btnSaveAddress.setOnClickListener {
                        chkAddress.text = edtAddress.text.toString()
                        binding.layoutAddressUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateAddress(userID, userToken, edtAddress.text.toString().trim()))
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutAddressUpdate
        }
        binding.btnEditState.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutStateUpdate.show()
                    edtState.setText(getStringValue(USER_STATE, ""))
                    btnCancelState.setOnClickListener {
                        edtState.setText("")
                        binding.layoutStateUpdate.hide()
                    }
                    btnSaveState.setOnClickListener {
                        chkState.text = edtState.text.toString()
                        binding.layoutStateUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateState(userID, userToken, edtState.text.toString().trim()))
                    }

                    btnState.setOnClickListener {
                        /* if (selectCountryName.isNotBlank()) {*/
                        StatePicker(requireContext()).apply {
                            setOnStateChangeListener(object : CallbackSelectState {
                                override fun selectedState(state: CountriesStates.Country.State) {
                                    binding.edtState.setText(state.stateName)
                                }
                            })
                            init()
                        }/*
                        }else{
                            showToast("Please Select Country again.")
                        }*/
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutStateUpdate
        }
        binding.btnEditDob.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutDobUpdate.show()
                    edtDob.setText(getStringValue(USER_DOB, ""))
                    btnCancelDob.setOnClickListener {
                        edtDob.setText("")
                        binding.layoutDobUpdate.hide()
                    }
                    btnSaveDob.setOnClickListener {
                        chkDob.text = edtDob.text.toString()
                        binding.layoutDobUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateDob(userID, userToken, edtDob.text.toString().trim()))
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutDobUpdate
        }
        binding.btnDob.setOnClickListener {
            if (datePicker != null) {
                if (datePicker!!.isVisible.not()) {
                    datePicker!!.show(requireActivity().supportFragmentManager, null)
                }
            } else {
                datePicker()
            }
        }

        CountryPicker(requireContext()).findCountryDataByName(SharePreferences.getStringValue(SharePreferences.USER_COUNTRY, ""))
        binding.btnEditCountry.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutCountryUpdate.show()
                    edtCountry.setText(getStringValue(USER_COUNTRY, ""))
                    btnCancelCountry.setOnClickListener {
                        edtCountry.setText("")
                        binding.layoutCountryUpdate.hide()
                    }
                    btnSaveCountry.setOnClickListener {
                        chkCountry.text = edtCountry.text.toString()
                        binding.layoutCountryUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateCountry(userID, userToken, edtCountry.text.toString().trim(), ""))
                    }

                    btnCountry.setOnClickListener {
                        CountryPicker(requireContext()).apply {
                            setOnCountryChangeListener(object : CallbackCountrySelected {
                                override fun selectedCountry(country: CountriesStates.Country) {
                                    binding.edtCountry.setText(country.countryName)
                                    //selectCountryName = country.countryName
                                }
                            })
                            init()
                        }
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutCountryUpdate
        }
        binding.btnEditPinCode.setOnClickListener {
            pref().apply {
                binding.apply {
                    binding.layoutPinCodeUpdate.show()
                    edtPinCode.setText(getStringValue(USER_PIN_CODE, ""))
                    btnCancelPinCode.setOnClickListener {
                        edtPinCode.setText("")
                        binding.layoutPinCodeUpdate.hide()
                    }
                    btnSavePinCode.setOnClickListener {
                        chkPincode.text = edtPinCode.text.toString()
                        binding.layoutPinCodeUpdate.hide()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdatePinCode(userID, userToken, edtPinCode.text.toString().trim()))
                    }
                }
            }
            if (lastClickedView != null) {
                lastClickedView?.hide()
                lastClickedView = null
            } else lastClickedView = binding.layoutPinCodeUpdate
        }

        binding.btnEdtiSq.setOnClickListener {
            /*   if (securityQuesList.isEmpty()) {
                   pref().apply {
                       val updateSQFragment = UpdateSQFragment(requireActivity(),securityQuesList, getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, "")){it->
                           if(it==1)
                           (requireActivity() as MainActivity).getProfileInfo()
                           else if(it==0){
                              showLoading()
                           }else{
                               hideLoading()
                           }
                       }
                       updateSQFragment.show(requireActivity().supportFragmentManager, "")
                   }
               } else {
                       showToast("Security question already uploaded.")
               }*/
        }

        valueChangeLiveData.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    setProfileData()
                    //valueChangeLiveData.value =false
                }
                else -> {}
            }
        })

        binding.btnLogout.setOnClickListener {
            pref().apply {
                val userMobileNumber = getStringValue(USER_MOBILE_NUMBER, "")
                val userProfile = getStringValue(USER_PROFILE_IMAGE, "")
                val userName = getStringValue(USER_FIRST_NAME, "")
                val deviceToken = getStringValue(DEVICE_TOKEN, "")
                clearPreference()
                setStringValue(USER_MOBILE_NUMBER, userMobileNumber)
                setStringValue(USER_PROFILE_IMAGE, userProfile)
                setStringValue(USER_FIRST_NAME, userName)
                setStringValue(DEVICE_TOKEN, deviceToken)
                requireActivity().finish()
                startActivity(Intent(requireContext(), UserActivity::class.java))
            }
        }
    }

    private fun observerMobileVerify() {/* mobileNoUpdateLiveData.observe(viewLifecycleOwner, {
             when (it) {
                 true -> {
                     binding.apply {
                         val userID = pref().getStringValue(SharePreferences.USER_ID, "")
                         val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                         chkMobileNo.text = edtMobile.text.toString().trim()
                         binding.layoutMobileUpdate.hide()
                         showLoading()
                         userProfileViewModel.updateProfileInfo(UpdateMobile(userID, userToken, edtMobile.text.toString().trim(), true))
                     }
                 }
                 false -> {
                 }
             }
         })*/
    }

    private fun observer() {
        userProfileViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is AvatarModel -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            avatarList.clear()
                            val data1 = AvatarModel.Data(0, "https://www.freeiconspng.com/uploads/camera-icon-circle-21.png")
                            avatarList.add(data1)
                            avatarList.addAll(it.data)
                            avatarAdapter.notifyDataSetChanged()
                        }
                    } else {
                        showToast(it.status)
                    }
                    userProfileViewModel.observerResponse.value = null
                }

                is UpdateAvatarResponse -> {
                    hideLoading()
                    uploadingProfile(true)
                    (requireActivity() as MainActivity).getProfileInfo()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            if (updated.equals(Constants.UpdatedAvatar.AVATAR_IMAGE.name, true)) {
                                binding.imgProfile.setImage(it.data.imageUrl)
                            } else {
                                binding.imgProfile.setImage(it.data.avatarDefault)
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    userProfileViewModel.observerResponse.value = null
                }

                is UpdateProfileResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        (requireActivity() as MainActivity).getProfileInfo()
                        showToast(it.message)
                    } else {
                        showToast(it.message)
                    }
                    userProfileViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    userProfileViewModel.observerResponse.value = null
                }
            }
        })
    }

    private fun datePicker() {
        val dataBuilder = MaterialDatePicker.Builder.datePicker()
        dataBuilder.setCalendarConstraints(limitRange().build())
        datePicker = dataBuilder.setTheme(R.style.Theme_DatePicker).build()
        datePicker!!.show(requireActivity().supportFragmentManager, null)
        datePicker!!.addOnPositiveButtonClickListener {
            val simpleFormat = SimpleDateFormat("yyy-MM-dd", Locale.US)
            val date = Date(it as Long)
            binding.edtDob.setText(simpleFormat.format(date))
        }
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

    private fun showAvatarDialog() {
        avatarBottomSheet.show()
    }

    private fun setAvatarAdapter() {
        avatarBottomSheet = BottomSheetDialog(requireContext()).apply {
            val avatarBottomSheetBinding = DataBindingUtil.inflate<AvatarBottomSheetBinding>(layoutInflater, R.layout.avatar_bottom_sheet, null, false).apply {
                setContentView(root)
                avatarAdapter = AvatarAdapter(avatarList) { pos ->
                    for (i in avatarList.indices) {
                        if (pos != i) {
                            avatarList[i].isSelect = false
                        }
                    }
                    avatarAdapter.notifyDataSetChanged()
                    if (pos == 0) {
                        checkPermissions()
                    } else {
                        binding.imgProfile.setImage(avatarList[pos].image)
                        avatarBottomSheet.dismiss()
                        userProfileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), avatarList[pos].id.toString(), null, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                        uploadingProfile(false)
                    }
                }
                rvAvatar.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvAvatar.adapter = avatarAdapter
            }
        }
    }

    private fun uploadingProfile(isShow: Boolean) {
        if (isShow) binding.layoutLoadImg.hide()
        else binding.layoutLoadImg.show()
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
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))
        } else {
            if (requireContext().let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
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
        startActivityForResult(intent, Constants.CAMERA_)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CAMERA_) {
            if (data != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap
                thumbnail.let { bitMap ->
                    binding.imgProfile.setImage(bitMap)
                    val imageFile = UtilityHelper.bitmapToFile(bitMap, requireContext())
                    userProfileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), "", imageFile, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                    uploadingProfile(false)
                }
            }
        }
    }

    private fun setProfileData() {
        binding.apply {
            pref().apply {
                getStringValue(USER_ID, "")
                getStringValue(USER_TOKEN, "")
                chkMobileNo.text = getStringValue(USER_MOBILE_NUMBER, "")
                txtUserName.text = "${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}"
                chkName.text = "${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}"
                chkMobileNo.isChecked = getBooleanValue(USER_MOBILE_VERIFIED)
                chkEmail.text = getStringValue(USER_EMAIL, "")
                chkEmail.isChecked = getBooleanValue(USER_EMAIL_VERIFIED)
                if (getBooleanValue(USER_EMAIL_VERIFIED).not()) {
                    txtEmailStatus.show()
                    txtEmailStatus.text = "Please verify email."
                    txtEmailStatus.setTextColor(resources.getColor(R.color.light_red))
                } else {
                    txtEmailStatus.hide()
                }
                chkState.text = getStringValue(USER_ADDRESS, "")
                txtUserCountry.text = getStringValue(USER_COUNTRY, "")
                chkCountry.text = getStringValue(USER_COUNTRY, "")
                chkBvn.text = getStringValue(USER_BVN, "")
                chkBvn.isChecked = getBooleanValue(USER_BVN_VERIFIED)
                chkDob.text = getStringValue(USER_DOB, "")
                chkState.text = getStringValue(USER_STATE, "")
                chkPincode.text = getStringValue(USER_PIN_CODE, "")
                chkPincode.text = getStringValue(USER_PIN_CODE, "")
                chkAddress.text = getStringValue(USER_ADDRESS, "")

                if (chkBvn.isChecked) {
                    txtEmailStatus.hide()
                } else {
                    txtBnvStatus.text = "Please verify BVN."
                    txtBnvStatus.setTextColor(resources.getColor(R.color.light_red))
                }
                chkSecurityQues.text = "Security Questions"
                chkSecurityQues.isChecked = (requireActivity() as MainActivity).securityQuesList.isNotEmpty()
                if (getStringValue(USER_PROFILE_STATUS, "").isNotBlank()) {
                    profileStatusBar.progress = getStringValue(USER_PROFILE_STATUS, "").toBigDecimal().toInt()
                }
                getBooleanValue(USER_DOC_VERIFIED)
                getStringValue(USER_PROFILE_STATUS, "")
                if (getStringValue(USER_PROFILE_IMAGE, "").isNotBlank()) {
                    imgProfile.setImage(getStringValue(USER_PROFILE_IMAGE, ""))
                } else {
                    imgProfile.setImage("https://ui-avatars.com/api/?name=" + "${getStringValue(USER_FIRST_NAME, "").substring(0, 1)}" + "${getStringValue(USER_LAST_NAME, "").substring(0, 1)}")
                }
                val sqType = object : TypeToken<List<SQ>>() {}.type
                val sqList = Gson().fromJson<List<SQ>>(getStringValue(USER_SECURITY_QUES, ""), sqType)
                securityQuesList.clear()
                securityQuesList.addAll(sqList)
            }
        }
    }

    private fun showBottomSheetDialog() {
        bottomSheetMobileVerify = BottomSheetDialog(requireContext()).apply {
            DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
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
                            (requireActivity() as MainActivity).verifyPhoneNumberWithCode(phoneVerificationId, otpView.otp.toString().trim())
                        }
                    } else {
                        showToast("error")
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
            show()
        }
    }

    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is BaseActivity.CodeSentModel -> {
                    hideLoading()/*  updateCounterTextView()
                      startTimer()*/
                    showBottomSheetDialog()
                    phoneVerificationId = it.verificationId
                    phoneToken = it.token
                    verificationLiveData.value = null
                }

                is BaseActivity.VerificationCompleteModel -> {
                    hideLoading()
                    binding.apply {
                        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
                        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                        chkMobileNo.text = edtMobile.text.toString().trim()
                        binding.layoutMobileUpdate.hide()
                        bottomSheetMobileVerify.dismiss()
                        showLoading()
                        userProfileViewModel.updateProfileInfo(UpdateMobile(userID, userToken, edtMobile.text.toString().trim(), true, edtCountry.text.toString().trim()))
                    }
                    verificationLiveData.value = null
                }
                is BaseActivity.FirebaseExceptionModel -> {
                    hideLoading()/* counter = 0
                     finishCounter()*/
                    // updateCounterTextView()
                    if (it.e is FirebaseAuthInvalidCredentialsException) {
                        showToast("The phone number provided is incorrect")
                    }
                    Log.d("FirebaseException==", it.e.toString())
                    verificationLiveData.value = null
                }
                is BaseActivity.CodeAutoRetrievalTimeOutModel -> {
                    hideLoading()
                    showToast(it.s0)/*counter = 0
                    finishCounter()*/
                    // updateCounterTextView()
                    Log.d("CodeAutoRetrievalT", it.s0.toString())
                    verificationLiveData.value = null
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.user_profile_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): UserProfileViewModel = userProfileViewModel
}