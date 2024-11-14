package com.nbt.blytics.modules.payee

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.PayeeFragmentBinding
import com.nbt.blytics.modules.payee.payment.PayeePaymentFragment
import com.nbt.blytics.modules.payee.schedule.ScheduleFragment
import com.nbt.blytics.utils.*

class PayeeFragment : BaseFragment<PayeeFragmentBinding, PayeeViewModel>() {


    private val fragmentList = mutableListOf<PayeeModel>()
    private lateinit var binding: PayeeFragmentBinding
    private val payeeViewModel: PayeeViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        fragmentList.add(
            PayeeModel(
                PayeePaymentFragment.newInstance(), "Payment"
            )
        )
        fragmentList.add(
            PayeeModel(
                ScheduleFragment.newInstance(), "Schedule"
            )
        )
        setHomeViewPagerAdapter()
        binding.btnSendMoney.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_NOW.name)
            intent.putExtra(Constants.PAY_MODE, Constants.PayType.SENT_MONEY.name)
            startActivity(intent)
        }
        binding.btnRequestMoney.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_NOW.name)
            intent.putExtra(Constants.PAY_MODE, Constants.PayType.REQUEST_MONEY.name)
            startActivity(intent)
        }

       // fillData()
      /*  binding.btnLogout.setOnClickListener {
            (requireActivity() as MainActivity).showLogoutDialog()
        }*/

       /* binding.btnNotification.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.NOTIFICATION_LIST.name)
            startActivity(intent)
        }*/

    }

/*    private fun fillData() {
        val imageUrl = pref().getStringValue(
            SharePreferences.USER_PROFILE_IMAGE,
            ""
        )
        binding.imgUserProfile.setImage(imageUrl)
        binding.txtUserName.text =
            "${
                SharePreferences.getStringValue(
                    SharePreferences.USER_FIRST_NAME,
                    ""
                )
            } ${SharePreferences.getStringValue(SharePreferences.USER_LAST_NAME, "")}"
        binding.txtUserCountry.text = pref().getStringValue(SharePreferences.USER_COUNTRY, "")
    }*/

    private fun buttonVisibility(isVisiable: Boolean) {
        if (isVisiable)
            binding.lytBottom.show()
        else
            binding.lytBottom.hide()
    }


    private fun setHomeViewPagerAdapter() {
        binding.payeeViewPager.adapter = createViewPageAdapter()
        binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        TabLayoutMediator(
            binding.tabLayout, binding.payeeViewPager
        ) { tab, position ->
            tab.text = fragmentList[position].title

        }.attach()

        binding.payeeViewPager.isUserInputEnabled = false

        binding.tabLayout.setOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab?.position==1){
                    buttonVisibility(false)
                }else{
                    buttonVisibility(true)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

    }

    private fun createViewPageAdapter(): ViewPagerAdapter {
        return ViewPagerAdapter(requireActivity(), fragmentList)
    }

    private fun happyThemeChanges() {

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnSendMoney.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnRequestMoney.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_dark)
                binding.tabLayout.setBackgroundResource(R.color.b_bg_color_dark)
              //  binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnSendMoney.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnRequestMoney.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
                binding.tabLayout.setBackgroundResource(R.color.b_bg_color_light)
              //  binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnSendMoney.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnRequestMoney.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
                binding.tabLayout.setBackgroundResource(R.color.b_bg_color_light)
               // binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)

            }
        }

    }

    override fun getLayoutId(): Int = R.layout.payee_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): PayeeViewModel = payeeViewModel

}