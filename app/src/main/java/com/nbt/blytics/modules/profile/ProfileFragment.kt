package com.nbt.blytics.modules.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.nbt.blytics.R
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
import com.nbt.blytics.databinding.BottomSheetBvnVerifyBinding
import com.nbt.blytics.databinding.BottomSheetMobileVerifyBinding
import com.nbt.blytics.databinding.ProfileFragmentBinding
import com.nbt.blytics.modules.bvn.models.BvnRegisterResponse
import com.nbt.blytics.modules.otp.model.EmailOtpResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.signupprofile.adapter.AvatarAdapter
import com.nbt.blytics.modules.signupprofile.models.AvatarModel
import com.nbt.blytics.modules.userprofile.models.*
import com.nbt.blytics.testing.signup.RegFragment
import com.nbt.blytics.utils.*
import com.nbt.blytics.utils.UtilityHelper.isEmailValid
import com.nbt.blytics.widget.CircularHorizontalMode
import com.nbt.blytics.widget.CircularProgressBar
import com.nbt.blytics.widget.ItemViewMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Suppress("LABEL_NAME_CLASH", "DEPRECATION")
@SuppressLint("SetTextI18n")
class ProfileFragment : BaseFragment<ProfileFragmentBinding, ProfileViewModel>() {
    private val TAG = "Profile Camera Permission=="
    private lateinit var bindingBVN: BottomSheetBvnVerifyBinding
    private lateinit var avatarBottomSheet: BottomSheetDialog
    private lateinit var binding: ProfileFragmentBinding
    private lateinit var mItemViewMode: ItemViewMode
    private lateinit var avatarAdapter: AvatarAdapter
    private lateinit var emailDialog: BottomSheetDialog
    private lateinit var bottomSheetMobileVerify: BottomSheetDialog
    private val profileViewModel: ProfileViewModel by viewModels()
    private val avatarList = mutableListOf<AvatarModel.Data>()
    private var temp = -1
    private var phoneToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var bvnDialog: BottomSheetDialog
    private var phoneVerificationId: String = ""

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        observer()
        observerOTP()
        setAvatarAdapter()
        showLoading()
        profileViewModel.getAvatars()
        binding.imgProfile.setOnClickListener {
            showAvatarDialog()
        }

        val userID = pref().getStringValue(SharePreferences.USER_ID, "")
        val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
        view.findViewById<CircularProgressBar>(R.id.profile_progress_bar).progress = 30f

        binding.btnNameEdit.setOnClickListener {
            pref().apply {
                binding.apply {
                    txtName.hide()
                    txtNameTitle.hide()
                    layoutName.show()
                    layoutLastName.show()
                    btnNameSave.show()
                    btnNameEdit.hide()
                    edtFirstName.setText(getStringValue(USER_FIRST_NAME, ""))
                    edtLastName.setText(getStringValue(USER_LAST_NAME, ""))
                    btnNameSave.setOnClickListener {
                        if (edtFirstName.text.toString().trim().isNotBlank() && edtLastName.text.toString().trim().isNotBlank()) {
                            txtName.text = edtFirstName.text.toString().trim() + " " + edtLastName.text.toString().trim()
                            showLoading()
                            profileViewModel.updateProfileInfo(UpdateName(userID, userToken, edtFirstName.text.toString().trim(), edtLastName.text.toString().trim()))
                            txtName.show()
                            layoutName.hide()
                            layoutLastName.hide()
                            btnNameSave.hide()
                            btnNameEdit.show()
                        } else {
                            showToast("all filed are required.")
                        }
                    }
                }
            }
        }

        binding.btnEmailEdit.setOnClickListener {
            pref().apply {
                binding.apply {
                    txtEmail.hide()
                    txtEmailTitle.hide()
                    layoutEmail.show()
                    btnEmailSave.show()
                    btnEmailEdit.hide()
                    val email = getStringValue(USER_EMAIL, "")
                    if (email.isNotBlank()) {
                        edtEmail.setText(email)
                    }

                    btnEmailSave.setOnClickListener {
                        if (edtEmail.text.toString().trim().isEmailValid()) {
                            showLoading()
                            profileViewModel.otpEmail(edtEmail.text.toString().trim())
                        } else {
                            showToast("Invalid Email.")
                        }
                    }
                }
            }
        }

        binding.btnBvnEdit.setOnClickListener {
            pref().apply {
                binding.apply {
                    txtBvn.hide()
                    txtBvnTitle.hide()
                    layoutBvn.show()
                    btnBvnSave.show()
                    btnBvnEdit.hide()
                    val bvn = getStringValue(USER_BVN, "")
                    if (bvn.isNotBlank()) {
                        edtBvn.setText(bvn)
                    }
                    btnBvnSave.setOnClickListener {
                        if (edtBvn.text.toString().trim().isNotBlank()) {
                            if (edtBvn.text.toString().trim().length > 13) {
                                showBVNDialog()
                                txtBvn.show()
                                txtBvnTitle.show()
                                layoutBvn.hide()
                                btnBvnSave.hide()
                                btnBvnEdit.show()
                            } else {
                                showToast("Bvn should be 14 digit.")
                            }
                        } else {
                            showToast("Enter BVN.")
                        }
                    }
                }
            }
        }

        binding.btnPhoneEdit.setOnClickListener {
            pref().apply {
                binding.apply {
                    txtPhone.hide()
                    txtPhoneTitle.hide()
                    layoutPhone.show()
                    btnPhoneSave.show()
                    btnPhoneEdit.hide()
                    edtPhone.setText(getStringValue(USER_MOBILE_NUMBER, ""))
                    btnPhoneSave.setOnClickListener {
                        if (edtPhone.text.toString().trim().isNotBlank()) {
                            showLoading()
                            val newMobileNo = "${Constants.DEFAULT_COUNTRY_CODE}${binding.edtPhone.text.toString().trim()}"
                            (requireActivity() as MainActivity).sendVerificationCode(newMobileNo)
                            txtPhone.show()
                            layoutPhone.hide()
                            btnPhoneSave.hide()
                            btnPhoneEdit.show()
                        } else {
                            showToast("enter phone number.")
                        }
                    }
                }
            }
        }

        binding.btnAddressEdit.setOnClickListener {
            binding.apply {
                txtAddress.hide()
                txtAddressTitle.hide()
                layoutAddress.show()
                btnAddressSave.show()
                btnAddressEdit.hide()
                edtAddress.setText(SharePreferences.getStringValue(SharePreferences.USER_ADDRESS, ""))
                btnAddressSave.setOnClickListener {
                    if (edtAddress.text.toString().isNotBlank()) {
                        txtAddress.text = edtAddress.text.toString()
                        showLoading()
                        profileViewModel.updateProfileInfo(UpdateAddress(userID, userToken, edtAddress.text.toString().trim()))
                        txtAddress.show()
                        layoutAddress.hide()
                        btnAddressSave.hide()
                        btnAddressEdit.show()
                    } else {
                        showToast("enter address.")
                    }
                }
            }
        }

        CountryPicker(requireContext()).findCountryDataByName(SharePreferences.getStringValue(SharePreferences.USER_COUNTRY, ""))

        binding.btnCountryEdit.setOnClickListener {
            binding.apply {
                txtCountry.hide()
                txtCountryTitle.hide()
                txtState.hide()
                layoutFrameCountry.show()
                layoutFrameState.show()
                btnCountrySave.show()
                btnCountryEdit.hide()
                edtCountry.setText(SharePreferences.getStringValue(SharePreferences.USER_COUNTRY, ""))
                edtState.setText(SharePreferences.getStringValue(SharePreferences.USER_STATE, ""))
                btnCountrySave.setOnClickListener {
                    txtCountry.show()
                    txtCountryTitle.show()
                    txtState.show()
                    layoutFrameCountry.hide()
                    layoutFrameState.hide()
                    btnCountrySave.hide()
                    btnCountryEdit.show()
                    txtCountry.text = edtCountry.text.toString()
                    showLoading()
                    profileViewModel.updateProfileInfo(UpdateCountry(userID, userToken, edtCountry.text.toString().trim(), edtState.text.toString().trim()))
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
                btnState.setOnClickListener {
                    StatePicker(requireContext()).apply {
                        setOnStateChangeListener(object : CallbackSelectState {
                            override fun selectedState(state: CountriesStates.Country.State) {
                                binding.edtState.setText(state.stateName)
                            }
                        })
                        init()
                    }
                }
            }
        }

        binding.btnPincodeEdit.setOnClickListener {
            binding.apply {
                txtPincode.hide()
                txtPincodeTitle.hide()
                layoutPincode.show()
                btnPincodeSave.show()
                btnPincodeEdit.hide()
                edtPincode.setText(SharePreferences.getStringValue(SharePreferences.USER_PIN_CODE, ""))
                btnPincodeSave.setOnClickListener {
                    txtPincode.show()
                    layoutPincode.hide()
                    btnPincodeSave.hide()
                    btnPincodeEdit.show()
                    if (edtPincode.text.toString().trim().isBlank()) {
                        showToast("Enter pin code")
                        return@setOnClickListener
                    }
                    if (edtPincode.text.toString().trim().length < 5) {
                        showToast("pincode should be min 5 digit")
                        return@setOnClickListener
                    }
                    showLoading()
                    profileViewModel.updateProfileInfo(UpdatePinCode(userID, userToken, edtPincode.text.toString().trim()))
                }
            }
        }

        binding.btnDobEdit.setOnClickListener {
            binding.apply {
                txtDob.hide()
                txtDobTitle.hide()
                layoutFrameDob.show()
                btnDobSave.show()
                btnDobEdit.hide()
                edtDob.setText(SharePreferences.getStringValue(SharePreferences.USER_DOB, ""))
                btnDobSave.setOnClickListener {
                    txtDob.show()
                    txtDobTitle.show()
                    layoutFrameDob.hide()
                    btnDobSave.hide()
                    btnDobEdit.show()
                    showLoading()
                    profileViewModel.updateProfileInfo(UpdateDob(userID, userToken, edtDob.text.toString().trim()))
                }
                btnDob.setOnClickListener {
                    datePicker()
                }
            }
        }

        valueChangeLiveData.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    setProfileData()
                    // valueChangeLiveData.value =false
                }
                false -> {
                    // Handle false case (if necessary)
                }
            }
        })
        binding.btnLogout.setOnClickListener {
            (requireActivity() as MainActivity).showLogoutDialog()/* pref().apply {
                 val userMobileNumber = getStringValue(USER_MOBILE_NUMBER, "")
                 val userProfile = getStringValue(USER_PROFILE_IMAGE, "")
                 val userName = getStringValue(USER_FIRST_NAME, "")
                 clearPreference()
                 setStringValue(USER_MOBILE_NUMBER, userMobileNumber)
                 setStringValue(USER_PROFILE_IMAGE, userProfile)
                 setStringValue(USER_FIRST_NAME, userName)
                 requireActivity().finish()
                 startActivity(Intent(requireContext(), UserActivity::class.java))
             }*/
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observer() {
        profileViewModel.observerResponse.observe(viewLifecycleOwner, {
            when (it) {
                is UpdateMobileResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        (requireActivity() as MainActivity).getProfileInfo()
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is BvnRegisterResponse -> {
                    hideLoading()
                    bvnDialog.dismiss()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showToast(it.message)
                        binding.apply {
                            pref().apply {
                                if (edtBvn.text.toString().trim().isNotBlank()) {
                                    txtBvn.text = edtBvn.text.toString().trim()
                                    showLoading()
                                    profileViewModel.updateProfileInfo(UpdateBVN(getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, ""), edtBvn.text.toString().trim()))
                                    txtBvn.show()
                                    layoutBvn.hide()
                                    btnBvnSave.hide()
                                    btnBvnEdit.show()
                                } else {
                                    showToast("enter bvn")
                                }
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is EmailOtpResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        showEmailOTP(it.message)
                    } else {
                        showToast(it.status)
                    }
                    profileViewModel.observerResponse.value = null
                }

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
                    profileViewModel.observerResponse.value = null
                }

                is UpdateAvatarResponse -> {
                    hideLoading()
                    uploadingProfile(true)
                    (requireActivity() as MainActivity).getProfileInfo()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            if (updated.equals(Constants.UpdatedAvatar.AVATAR_IMAGE.name, true)) {
                                binding.imgProfile.setImage(it.data.imageUrl)
                                binding.navUserProfile.setImage(it.data.imageUrl)
                            } else {
                                binding.imgProfile.setImage(it.data.avatarDefault)
                                binding.navUserProfile.setImage(it.data.avatarDefault)
                            }
                        }
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }

                is UpdateProfileResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        (requireActivity() as MainActivity).getProfileInfo()
                        showToast(it.message)
                    } else {
                        showToast(it.message)
                    }
                    profileViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    profileViewModel.observerResponse.value = null
                }
            }
        })
    }

    fun showHideLayout(index: Int) {
        Log.d("Test==", index.toString())/*  val myAnim: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bounce)
        val animationDuration: Double = 0.5 * 1000
        myAnim.duration = animationDuration.toLong()
        val interpolator = MyBounceInterpolator(0.4, 0.2)
        myAnim.interpolator = interpolator*/
        allViewHide()
        when (index) {
            0 -> {
                binding.cardName.show()
                binding.txtName.show()
                binding.txtNameTitle.show()
                binding.layoutName.hide()
                binding.btnNameEdit.show()
                binding.layoutLastName.hide()
                binding.btnNameSave.hide()/*
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardName.startAnimation(myAnim)
                }*/
            }

            1 -> {
                binding.cardEmail.show()
                binding.txtEmailTitle.show()
                binding.txtEmail.show()
                binding.btnEmailEdit.show()
                binding.layoutEmail.hide()
                binding.btnEmailSave.hide()/*
                if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardEmail.startAnimation(myAnim)
                }*/
            }

            2 -> {
                binding.cardBvn.show()
                binding.txtBvn.show()
                binding.txtBvnTitle.show()
                binding.btnBvnEdit.show()
                binding.layoutBvn.hide()
                binding.btnBvnSave.hide()
                /* if(temp>1){
                     temp =-1

                 }else{
                     temp++
                     binding.cardBvn.startAnimation(myAnim)
                 }*/
            }

            3 -> {
                binding.cardPhone.show()
                binding.txtPhone.show()
                binding.txtPhoneTitle.show()
                binding.btnPhoneEdit.show()
                binding.layoutPhone.hide()
                binding.btnPhoneSave.hide()/*if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardPhone.startAnimation(myAnim)
                }*/
            }

            4 -> {
                binding.cardAddress.show()
                binding.txtAddress.show()
                binding.txtAddressTitle.show()
                binding.btnAddressEdit.show()
                binding.layoutAddress.hide()
                binding.btnAddressSave.hide()/*if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardPhone.startAnimation(myAnim)
                }*/
            }

            5 -> {
                binding.cardCountry.show()
                binding.txtCountry.show()
                binding.txtState.show()
                binding.txtCountryTitle.show()
                binding.btnCountryEdit.show()
                binding.layoutFrameCountry.hide()
                binding.layoutFrameState.hide()
                binding.btnCountrySave.hide()/* if(temp>1){
                     temp =-1
                 }else{
                     temp++
                     binding.cardCountry.startAnimation(myAnim)
                 }*/
            }

            /*6->{
                binding.cardState.show()
                binding.txtState.show()
                binding.btnStateEdit.show()
                binding.layoutFrameState.hide()
                binding.btnStateSave.hide()
             *//*   if(temp>1){
                    temp =-1
                }else{
                    temp++
                    binding.cardState.startAnimation(myAnim)
                }*//*
            }*/
            6 -> {
                binding.cardPincode.show()
                binding.txtPincode.show()
                binding.txtPincodeTitle.show()
                binding.btnPincodeEdit.show()
                binding.layoutPincode.hide()
                binding.btnPincodeSave.hide()/* if(temp>1){
                     temp =-1
                 }else{
                     temp++
                     binding.cardPincode.startAnimation(myAnim)
                 }*/
            }

            7 -> {
                binding.cardDob.show()
                binding.txtDob.show()
                binding.txtDobTitle.show()
                binding.btnDobEdit.show()
                binding.layoutFrameDob.hide()
                binding.btnDobSave.hide()/* if(temp>1){
                     temp =-1
                 }else{
                     temp++
                     binding.cardDob.startAnimation(myAnim)
                 }*/
            }

            else -> {
                temp = -1
            }
        }
    }

    private fun allViewHide() {
        binding.apply {
            val listCardView = mutableListOf<View>(cardName, cardEmail, cardBvn, cardPhone, cardAddress, cardCountry, cardPincode, cardDob)
            for (i in listCardView.indices) {
                listCardView[i].hide()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun datePicker() {
        val dialog = DatePickerFragment(requireActivity()) { year, month, day ->
            val current = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val birthDate = dateFormat.parse("$year-$month-$day")
            if (current.after(birthDate).not()) {
                showToast(requireActivity().resources.getString(R.string.date))
            } else {
                binding.edtDob.setText("$year-$month-$day")
            }
        }
        dialog.show(requireActivity().supportFragmentManager, "timePicker")
    }

    private fun setProfileData() {
        binding.apply {
            pref().apply {
                val data = mutableListOf<IconModel>()
                data.add(IconModel(0, R.drawable.icon_name, Status.VERIFIED))
                if (getBooleanValue(USER_EMAIL_VERIFIED)) {
                    data.add(IconModel(1, R.drawable.icon_email, Status.VERIFIED))
                } else {
                    data.add(IconModel(1, R.drawable.icon_email, Status.UNVERIFIED))
                }
                if (getBooleanValue(USER_BVN_VERIFIED)) {
                    data.add(IconModel(1, R.drawable.icon_bvn, Status.VERIFIED))
                } else {
                    if (getStringValue(USER_BVN, "").isBlank()) {
                        data.add(IconModel(1, R.drawable.icon_bvn, Status.UNVERIFIED))
                    } else {
                        data.add(IconModel(1, R.drawable.icon_bvn, Status.PENDING))
                    }
                }

                // if(getBooleanValue(USER_MOBILE_VERIFIED)) {
                data.add(IconModel(2, R.drawable.icon_phone, Status.VERIFIED))/*}else{
                    data.add(IconModel(2, R.drawable.icon_red_phone, Status.UNVERIFIED))
                }*/
                data.add(IconModel(3, R.drawable.icon_address, Status.VERIFIED))
                data.add(IconModel(4, R.drawable.icon_globe, Status.VERIFIED))
                data.add(IconModel(5, R.drawable.icon_pincode, Status.VERIFIED))
                data.add(IconModel(6, R.drawable.icon_dob, Status.VERIFIED))
                getStringValue(USER_ID, "")
                getStringValue(USER_TOKEN, "")
                txtPhone.text = getStringValue(USER_MOBILE_NUMBER, "")
                txtUserName.text = "${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}"
                txtName.text = "${getStringValue(USER_FIRST_NAME, "")} ${getStringValue(USER_LAST_NAME, "")}"
                //chkMobileNo.isChecked = getBooleanValue(USER_MOBILE_VERIFIED)

                if (getStringValue(USER_BVN, "").isBlank()) {
                    txtBvn.text = "Register your BVN"
                } else {
                    txtBvn.text = getStringValue(USER_BVN, "")
                }
                if (getStringValue(USER_EMAIL, "").isBlank()) {
                    txtEmail.text = "Register your email"
                } else {
                    txtEmail.text = getStringValue(USER_EMAIL, "")
                }

                /*if (getBooleanValue(USER_EMAIL_VERIFIED).not()) {
                    txtEmailStatus.show()
                    txtEmailStatus.text = "Please verify email."
                    txtEmailStatus.setTextColor(resources.getColor(R.color.light_red))
                } else {
                    txtEmailStatus.hide()
                }*/

                txtAddress.text = getStringValue(USER_ADDRESS, "")
                txtUserCountry.text = getStringValue(USER_COUNTRY, "")
                txtCountry.text = getStringValue(USER_COUNTRY, "")
                //  chkBvn.text = getStringValue(USER_BVN, "")
                //chkBvn.isChecked = getBooleanValue(USER_BVN_VERIFIED)
                txtDob.text = getStringValue(USER_DOB, "")
                txtState.text = getStringValue(USER_STATE, "")
                txtPincode.text = getStringValue(USER_PIN_CODE, "")
                /*if (chkBvn.isChecked) {
                    txtEmailStatus.hide()
                } else {
                    txtBnvStatus.text = "Please verify BVN."
                    txtBnvStatus.setTextColor(resources.getColor(R.color.light_red))
                }
                chkSecurityQues.text = "Security Questions"
               */
                if (getStringValue(USER_PROFILE_STATUS, "").isNotBlank()) {
                    profileProgressBar.progress = getStringValue(USER_PROFILE_STATUS, "").toBigDecimal().toFloat()
                }
                getBooleanValue(USER_DOC_VERIFIED)
                getStringValue(USER_PROFILE_STATUS, "")
                if (getStringValue(USER_PROFILE_IMAGE, "").isNotBlank()) {
                    imgProfile.setImage(getStringValue(USER_PROFILE_IMAGE, ""))
                    navUserProfile.setImage(getStringValue(USER_PROFILE_IMAGE, ""))
                } else {
                    try {
                        imgProfile.setImage("https://ui-avatars.com/api/?name=" + getStringValue(USER_FIRST_NAME, "").substring(0, 1) + getStringValue(USER_LAST_NAME, "").substring(0, 1))
                        navUserProfile.setImage("https://ui-avatars.com/api/?name=" + getStringValue(USER_FIRST_NAME, "").substring(0, 1) + getStringValue(USER_LAST_NAME, "").substring(0, 1))
                    } catch (_: Exception) {
                    }
                }
                setAdapter(data)
            }
        }
    }

    private fun showAvatarDialog() {
        avatarBottomSheet.show()
    }

    @SuppressLint("NotifyDataSetChanged")
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
                        profileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), avatarList[pos].id.toString(), null, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
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

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
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
            requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA))
        } else {
            if (requireContext().let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA)
            } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                isBVNSelected = false
                avatarBottomSheet.dismiss()
                takePhotoFromCamera()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun checkPermissionForBVN() {
        if (requireContext().let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
        } != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request Permissions")
            requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else {
            if (requireContext().let {
                ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
            } != PackageManager.PERMISSION_GRANTED) {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            } else {
                avatarBottomSheet.dismiss()
                isBVNSelected = true
                fetchFile()
                Log.d(TAG, "Permission Already Granted")
            }
        }
    }

    private fun fetchFile() {
        startActivityForResult(Intent.createChooser(getFileChooserIntentForImageAndPdf(), "Select File"), Constants.GALLERY)
    }

    private fun getFileChooserIntentForImageAndPdf(): Intent {
        val mimeTypes = arrayOf("image/*", "application/pdf")
        return Intent(Intent.ACTION_GET_CONTENT).setType("image/*|application/pdf")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
    }
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constants.CAMERA_)
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CAMERA_) {
            if (data != null) {
                val thumbnail = data.extras!!.get("data") as Bitmap
                thumbnail.let { bitMap ->
                    binding.imgProfile.setImage(bitMap)
                    val imageFile = UtilityHelper.bitmapToFile(bitMap, requireContext())
                    profileViewModel.updateAvatar(pref().getStringValue(SharePreferences.USER_ID, ""), "", imageFile, pref().getStringValue(SharePreferences.USER_TOKEN, ""))
                    uploadingProfile(false)
                }
            }
        }
        if (requestCode == Constants.GALLERY) {
            if (data != null) {
                try {
                    if (isDoc1Selected) {
                        data.data?.let { documentUri ->
                            document1 = getFile(requireContext(), documentUri)
                            bindingBVN.btnDoc1.setBackgroundResource(R.color.app_green_light)
                            Log.d("File_Path", documentUri.path.toString())
                        }
                    } else {
                        data.data?.let { documentUri ->
                            document2 = getFile(requireContext(), documentUri)
                            bindingBVN.btnDoc2.setBackgroundResource(R.color.app_green_light)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun getFile(context: Context, uri: Uri): File {
        val destinationFilename = File(context.filesDir.path + File.separatorChar + queryName(context, uri))
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

    private fun queryName(context: Context, uri: Uri): String {
        val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name: String = returnCursor.getString(nameIndex)
        returnCursor.close()
        Log.d("File_Name=======", name)
        return name
    }

    private fun showBottomSheetDialog() {
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
                            (requireActivity() as MainActivity).verifyPhoneNumberWithCode(phoneVerificationId, otpView.otp.toString().trim())
                        }
                    } else {
                        showToast("error")
                    }
                }
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

    private fun showEmailOTP(otp: String) {
        emailDialog = BottomSheetDialog(requireContext()).apply {
            val bindingDialog = DataBindingUtil.inflate<BottomSheetMobileVerifyBinding>(layoutInflater, R.layout.bottom_sheet_mobile_verify, null, false).apply {
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
                                        txtEmail.text = edtEmail.text.toString().trim()
                                        showLoading()
                                        profileViewModel.updateProfileInfo(UpdateEmail(getStringValue(USER_ID, ""), getStringValue(USER_TOKEN, ""), edtEmail.text.toString().trim(), true))
                                        dismiss()
                                        txtEmail.show()
                                        layoutEmail.hide()
                                        btnEmailSave.hide()
                                        btnEmailEdit.show()
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

    private var isDoc1Selected = false
    private var isBVNSelected = false
    private var document1: File? = null
    private var document2: File? = null
    private fun showBVNDialog() {
        bvnDialog = BottomSheetDialog(requireContext()).apply {
            bindingBVN = DataBindingUtil.inflate<BottomSheetBvnVerifyBinding>(layoutInflater, R.layout.bottom_sheet_bvn_verify, null, false).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }

                btnDoc1.setOnClickListener {
                    isDoc1Selected = true
                    checkPermissionForBVN()
                }
                btnDoc2.setOnClickListener {
                    isDoc1Selected = false
                    checkPermissionForBVN()
                }
                btnVerify.setOnClickListener {
                    fun validationOtp(): Boolean {
                        if (binding.edtBvn.text.toString().isBlank()) {
                            showToast("Enter BVN.")
                            return false
                        }

                        if (document1 == null) {
                            showToast("Select ID proof")
                            return false
                        }
                        if (document2 == null) {
                            showToast("Select address proof")
                            return false
                        }
                        return true
                    }

                    if (validationOtp()) {
                        binding.apply {
                            pref().apply {
                                showLoading()
                                val userId = pref().getStringValue(SharePreferences.USER_ID, "")
                                val userToken = pref().getStringValue(SharePreferences.USER_TOKEN, "")
                                if (userId.isNotBlank()) {
                                    profileViewModel.registerBVN(userId, binding.edtBvn.text.toString().trim(), userToken, document1!!, document2!!)
                                }
                            }
                        }
                    } else {
                        showToast("error")
                    }
                }
            }
            show()
        }
    }

    private fun observerOTP() {
        verificationLiveData.observe(viewLifecycleOwner, {
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
                        txtPhone.text = edtPhone.text.toString().trim()
                        bottomSheetMobileVerify.dismiss()
                        showLoading()
                        profileViewModel.updateMobile(userID, userToken, edtPhone.text.toString().trim(), "device_")
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
        })
    }

    private fun setAdapter(data: MutableList<IconModel>) {
        mItemViewMode = CircularHorizontalMode()
        val padding: Int = RegFragment.ScreenUtils.getScreenWidth(requireContext()) / 2 - RegFragment.ScreenUtils.dpToPx(requireContext(), 40)
        binding.apply {
            circleRv.setPadding(padding, 0, padding, 0)
            circleRv.layoutManager = SliderLayoutManager(requireContext()).apply {
                callback = object : SliderLayoutManager.OnItemSelectedListener {
                    override fun onItemSelected(layoutPosition: Int) {
                        showHideLayout(layoutPosition)
                        Log.d("Tag====", layoutPosition.toString())
                    }
                }
            }
            circleRv.setViewMode(mItemViewMode)
            circleRv.setNeedCenterForce(true)
            circleRv.setNeedLoop(false)
            val snapHelper = LinearSnapHelper()
            circleRv.onFlingListener = null
            snapHelper.attachToRecyclerView(circleRv)
            circleRv.adapter = SliderAdapter(requireContext()).apply {
                setData(data)
                callback = object : SliderAdapter.Callback {
                    override fun onItemClicked(view: View) {
                        circleRv.smoothScrollToPosition(circleRv.getChildLayoutPosition(view))
                    }
                }
            }
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.profileBack.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.imageView.setBackgroundResource(R.drawable.bg_gradient_orange_half_circle)
                binding.imageView2.setBackgroundResource(R.drawable.bg_black_curve)
                binding.imgCircleRv.setBackgroundResource(R.drawable.bg_circler_black_rv)
                binding.btnDobEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnPincodeEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnCountryEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnAddressEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnPhoneEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnBvnEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnEmailEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnNameEdit.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.profileBack.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                binding.imageView.setBackgroundResource(R.drawable.bg_gradient_blue_half_circle)
                binding.imageView2.setBackgroundResource(R.drawable.bg_white_curve)
                binding.imgCircleRv.setBackgroundResource(R.drawable.bg_circler_white_rv)
                binding.btnDobEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPincodeEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnCountryEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnAddressEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPhoneEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnBvnEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnEmailEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNameEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_btn)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.profileBack.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                binding.imageView.setBackgroundResource(R.drawable.bg_gradient_blue_half_circle)
                binding.imageView2.setBackgroundResource(R.drawable.bg_white_curve)
                binding.imgCircleRv.setBackgroundResource(R.drawable.bg_circler_white_rv)
                binding.btnDobEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnDobSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPincodeEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPincodeSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnCountryEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnCountrySave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnAddressEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnAddressSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPhoneEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnPhoneSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnBvnEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnBvnSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnEmailEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnEmailSave.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNameEdit.setBackgroundResource(R.drawable.bg_gradient_btn)
                binding.btnNameSave.setBackgroundResource(R.drawable.bg_gradient_btn)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.profile_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): ProfileViewModel = profileViewModel
}