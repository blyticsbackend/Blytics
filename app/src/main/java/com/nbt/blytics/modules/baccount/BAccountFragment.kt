package com.nbt.blytics.modules.baccount

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.valueChangeLiveData
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BAccountFragmentBinding
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.setImage

class BAccountFragment :BaseFragment<BAccountFragmentBinding, BAccountViewModel>() {
    private val bAccountViewModel: BAccountViewModel by viewModels()
    private lateinit var binding:BAccountFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()

        binding.btnSecurity.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.SECURITY_QUES.name)
            startActivityForResult(intent, 11)

        }
        binding.btnProfile.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.PROFILE.name)
            startActivity(intent)
        }
        binding.btnApps.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.APPS.name)
            startActivity(intent)
        }

        binding.btnInfo.setOnClickListener{
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.INFO.name)
            startActivity(intent)
        }
        valueChangeLiveData.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    pref().apply {
                        binding.apply {
                            val imageUrl =   getStringValue(
                                USER_PROFILE_IMAGE,
                                ""
                            )
                            imgProfile.setImage(imageUrl)
                        }
                    }
                }false -> {
                // Handle false case (if necessary)
            }
            }
        })


   /*     binding.btnSystemMode.setOnClickListener {
            (requireActivity() as BaseActivity).showThemeDialog()
        }

        binding.btnChangeTpin.setOnClickListener {
            pref().apply {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)

                intent.putExtra(Constants.COMING_FOR, ComingFor.CHANGE_TPIN.name)
*//*                intent.putExtra(Constants.USER_ID, getStringValue(USER_ID, ""))
                intent.putExtra(Constants.USER_WALLET_UUID, getStringValue(USER_WALLET_UUID, ""))
                intent.putExtra(Constants.COMING_FOR, ComingFor.BACCOUNT.name)
                intent.putExtra(Constants.PHONE_NUMBER, getStringValue(USER_MOBILE_NUMBER, ""))*//*
                startActivity(intent)

            }
        }*/




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            if (resultCode == RESULT_OK) {
                showLoading()
                (requireActivity() as MainActivity).getProfileInfo()
            }
        }
    }
    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
             binding.mainLayout.setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }
    override fun getLayoutId(): Int=R.layout.b_account_fragment

    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): BAccountViewModel = bAccountViewModel
}