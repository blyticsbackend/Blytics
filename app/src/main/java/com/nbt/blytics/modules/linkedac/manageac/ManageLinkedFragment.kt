package com.nbt.blytics.modules.linkedac.manageac

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.ManageLinkedFragmentBinding
import com.nbt.blytics.modules.allaccount.AllAccountFragment
import com.nbt.blytics.modules.allaccount.LinkedAccResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show

class ManageLinkedFragment : BaseFragment<ManageLinkedFragmentBinding, ManageLinkedViewModel>(),
    CompoundButton.OnCheckedChangeListener {

    private lateinit var binding: ManageLinkedFragmentBinding
    private val manageLinkedViewModel: ManageLinkedViewModel by viewModels()
    private val selectedDays = arrayOf<Int>(0, 0, 0, 0, 0, 0, 0)
    private var txtDayColor: Int = -1
    private var uuid: String = ""
    private var accId: String = ""
    private var comingForDelete :Boolean =false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

        observer()
        happyThemeChanges()
        (requireContext() as BconfigActivity).setToolbarTitle("Manage account")

        uuid= requireArguments().getString(UUID,"")
        accId= requireArguments().getString(AccId,"")
        try {
            comingForDelete= requireArguments().getBoolean(COMING_FOR_DELETE,false)
            val from = requireArguments().getString(FROM,"")
            val to= requireArguments().getString(TO,"")
            val amount= requireArguments().getString(AMOUNT,"")
            if(comingForDelete){
                binding.edtFromAc.setText(from)
                binding.edtToAc.setText(to)
                binding.edtAmount.setText(amount)
                binding.layout1.hide()
                binding.card1.show()
            }else{
                binding.layout1.show()
                binding.card1.hide()
            }
        }catch (ex:Exception){
            comingForDelete = false
        }


        binding.txtM.setOnClickListener { view ->
            selectDay(view as TextView, 0)
        }
        binding.txtT.setOnClickListener { view ->
            selectDay(view as TextView, 1)
        }
        binding.txtW.setOnClickListener { view ->
            selectDay(view as TextView, 2)
        }
        binding.txtTh.setOnClickListener { view ->
            selectDay(view as TextView, 3)
        }
        binding.txtF.setOnClickListener { view ->
            selectDay(view as TextView, 4)
        }
        binding.txtS.setOnClickListener { view ->
            selectDay(view as TextView, 5)
        }
        binding.txtSu.setOnClickListener { view ->
            selectDay(view as TextView, 6)
        }

        binding.switchExpire.setOnCheckedChangeListener(this@ManageLinkedFragment)
        binding.switchActive.setOnCheckedChangeListener(this@ManageLinkedFragment)


        showLoading()
        manageLinkedViewModel.getMangeAcc(
            GetManageLinkedRequest(
                accUuid = uuid/* pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")*/,
                userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                acc_id = accId
            )
        )


        binding.btnManageSubmit.setOnClickListener {

            sendData()

        }

        binding.btnDelete.setOnClickListener {
            showLoading()
            manageLinkedViewModel.deleteLinked(
                ManageRequestLinked(
                    accUuid =     uuid,
                  accId =       accId,
                    userId =     pref().getStringValue(SharePreferences.USER_ID, ""),
                      isActive =   false,
                       isExpire =  true,
                    userToken =     pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    deactivate = true
            )
            )
        }
    }

    private fun observer() {
        manageLinkedViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ManageResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {

                        requireActivity().onBackPressed()
                    }
                    showToast(it.message)
                    manageLinkedViewModel.observerResponse.value = null

                }
                is MangeLinkedResponse ->{
                    hideLoading()
                    SELECTED_LINKED_AC_1 = it.data
                    SELECTED_LINKED_AC_1!!.days = it.data.days
                    SELECTED_LINKED_AC_1!!.active= it.data.active
                    SELECTED_LINKED_AC_1!!.expire= it.data.expire
                    fillDays()
                    if (SELECTED_LINKED_AC_1!!.active) {
                        binding.switchActive.isChecked = true
                    }
                    if (SELECTED_LINKED_AC_1!!.expire) {
                        binding.switchExpire.isChecked = true
                    }
                    manageLinkedViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    manageLinkedViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    manageLinkedViewModel.observerResponse.value = null
                    showToast(it.message.toString())
                    hideLoading()
                }
            }
        })
    }

    private fun fillDays() {
        fun fillColor(view: TextView) {
            view.background = (resources.getDrawable(R.drawable.bg_light_green))
            view.setTextColor(resources.getColor(R.color.white))
        }
        for (i in SELECTED_LINKED_AC_1!!.days.indices) {
            when (SELECTED_LINKED_AC_1!!.days[i]) {
                "0" -> {
                    selectedDays[0] = 1
                    fillColor(binding.txtM)
                }
                "1" -> {
                    selectedDays[1] = 1
                    fillColor(binding.txtT)
                }
                "2" -> {
                    selectedDays[2] = 1
                    fillColor(binding.txtW)
                }
                "3" -> {
                    selectedDays[3] = 1
                    fillColor(binding.txtTh)
                }

                "4" -> {
                    selectedDays[4] = 1
                    fillColor(binding.txtF)
                }
                "5" -> {
                    selectedDays[5] = 1
                    fillColor(binding.txtS)
                }
                "6" -> {
                    selectedDays[6] = 1
                    fillColor(binding.txtSu)
                }
            }
        }
    }

    private fun selectDay(view: TextView, i: Int) {
        if (selectedDays[i] == 0) {

            view.background = (resources.getDrawable(R.drawable.bg_light_green))
            view.setTextColor(resources.getColor(R.color.white))
            selectedDays[i] = 1
        } else {
            view.background = (resources.getDrawable(R.drawable.border_light_grey))
            view.setTextColor(txtDayColor)
            selectedDays[i] = 0
        }
    }


    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {

        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnManageSubmit.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnDelete.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)

                txtDayColor = resources.getColor(R.color.white)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnManageSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnDelete.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                txtDayColor = resources.getColor(R.color.black)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnManageSubmit.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnDelete.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                txtDayColor = resources.getColor(R.color.black)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)

            }
        }


    }

    override fun getLayoutId(): Int = R.layout.manage_linked_fragment
    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): ManageLinkedViewModel = manageLinkedViewModel
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        binding.apply {
            when (buttonView?.id) {
                R.id.switch_active -> {
                    if (switchActive.isChecked) {
                        switchExpire.isChecked = false
                    } else {
                        if (buttonView.isPressed) {
                            showDialog("0 NGN is chargeable to reactivate the card.", buttonView)
                        }
                    }


                }
                R.id.switch_expire -> {
                    if (switchExpire.isChecked) {
                        switchActive.isChecked = false
                        if (buttonView.isPressed) {
                            showDialog(
                                "You'll lost amount if any. To reactivate 0 NGN will charge.\n" +
                                        "Expire:- You won't able to use it again", buttonView
                            )
                        }
                    } else {
                    }
                }
            }
        }
    }

    private fun showDialog(des: String, buttonView: CompoundButton) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
            setTitle("Alert!")
            setMessage(des)
            setCancelable(false)
            setPositiveButton(
                "Yes"
            ) { dialog, which ->
                dialog.dismiss()
                sendData()

            }
            setNegativeButton(
                "No"
            ) { dialog, which ->
                dialog.dismiss()
                binding.switchExpire.isChecked = !binding.switchExpire.isChecked
                binding.switchActive.isChecked = !binding.switchActive.isChecked
            }
        }
        dialog.show()
    }

    private fun sendData() {
        showLoading()
        val dayList = arrayListOf<Int>()
        for (i in selectedDays.indices) {
            if (selectedDays[i] == 1) {
                dayList.add(i)
            }
        }
     /*   AllAccountFragment.SELECTED_LINKED_AC!!.apply {*/
            manageLinkedViewModel.mangeAcc(
                ManageRequest(
                    uuid,
                    accId,
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    dayList,
                    binding.switchActive.isChecked,
                    binding.switchExpire.isChecked,
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                )
            )
        //}
    }



    companion object {
        var SELECTED_LINKED_AC_1: MangeLinkedResponse.AccList? =null
        val UUID = "uuid"
        val AccId = "accId"
        val COMING_FOR_DELETE ="coming_for_delete"
        val FROM="from"
        val TO ="to"
        val AMOUNT ="amount"
    }

}