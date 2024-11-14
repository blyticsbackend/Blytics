package com.nbt.blytics.activity.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.tabs.TabLayout
import com.nbt.blytics.R
import com.nbt.blytics.activity.UserActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.databinding.ActivityWelcomeBinding
import com.nbt.blytics.utils.SharePreferences


class WelcomeActivity : BaseActivity() {
    private lateinit var mBinding: ActivityWelcomeBinding

    private lateinit var screenPager: ViewPager
    private lateinit var introViewPagerAdapter: WelcomeViewPagerAdapter
    private lateinit var tabIndicator: TabLayout
    private lateinit var btnNext: Button
    var position = 0
    lateinit var btnGetStarted: Button

    //  lateinit var btnAnim : Animation
    lateinit var tvSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_welcome)
        supportActionBar?.apply {
            hide()
        }

        screenPager = mBinding.screenViewpager
        btnNext = mBinding.btnNext
        btnGetStarted = mBinding.btnGetStarted
        tabIndicator = mBinding.tabIndicator
        tvSkip = mBinding.tvSkip


        val mList: MutableList<ScreenItem> = ArrayList()
        mList.add(
            ScreenItem(
                "Money Transfer",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.img_welcome_1
            )
        )
        mList.add(
            ScreenItem(
                "Payment",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.img_welcome_2
            )
        )
        mList.add(
            ScreenItem(
                "Banking",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.img_welcome_3
            )
        )
        mList.add(
            ScreenItem(
                "Banking",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.img_welcome_4
            )
        )

        introViewPagerAdapter = WelcomeViewPagerAdapter(this, mList)
        screenPager.adapter = introViewPagerAdapter
        tabIndicator.setupWithViewPager(screenPager)

        btnNext.setOnClickListener {
            position = screenPager.currentItem
            if (position < mList.size) {
                position++
                screenPager.currentItem = position
            }
            if (position == mList.size - 1) {
                loadLastScreen()
            } else {
                resetScreen()
            }
        }

        tabIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    if (it.position == mList.size - 1) {
                        loadLastScreen()
                    } else {
                        resetScreen()
                        if (position == 0) {
                            mBinding.btnBack.visibility = View.INVISIBLE
                        }
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        tvSkip.setOnClickListener {
            screenPager.currentItem = mList.size
        }

        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
            finish()

        }
        mBinding.btnBack.setOnClickListener {
            position = screenPager.currentItem
            if (position < mList.size) {
                position--
                screenPager.currentItem = position
            }
            if (position < 1) {
                mBinding.btnBack.visibility = View.INVISIBLE
            }

        }

        screenPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    mBinding.btnBack.visibility = View.INVISIBLE
                } else {
                    mBinding.btnBack.visibility = View.VISIBLE
                }
            }
        })
    }


    private fun loadLastScreen() {
        btnNext.visibility = View.INVISIBLE
        btnGetStarted.visibility = View.VISIBLE
        tvSkip.visibility = View.INVISIBLE
        tabIndicator.visibility = View.INVISIBLE
        mBinding.btnBack.visibility = View.VISIBLE
    }

    private fun resetScreen() {
        btnNext.visibility = View.VISIBLE
        btnGetStarted.visibility = View.INVISIBLE
        tvSkip.visibility = View.VISIBLE
        tabIndicator.visibility = View.VISIBLE
        mBinding.btnBack.visibility = View.VISIBLE
    }

    data class ScreenItem(
        val title: String,
        val description: String,
        val img: Int
    )


}