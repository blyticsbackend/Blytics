package com.nbt.blytics.modules.home

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetSwitchAcBinding
import com.nbt.blytics.databinding.HomeFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactiondetails.TransactionDetailsFragment
import com.nbt.blytics.utils.*
import java.util.*
import kotlin.concurrent.schedule


class HomeFragment : BaseFragment<HomeFragmentBinding, HomeViewModel>() {


    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var binding: HomeFragmentBinding
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var isDismissDialog: Boolean = true

    //private lateinit var circleViewPager: ViewPager
    private var moveAnimId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        //circleViewPager = binding.circleViewpager

        //setCircleViewPager()


        binding.btnUpTransaction.setOnTouchListener(object :
            OnSwipeTouchListener(requireActivity()) {
            override fun onSwipeTop() {
                transactionDetailsDialog()
            }
        })

        /*binding.btnUpTransaction.setOnClickListener {
            showBottomSheetDialog()
        }*/


        happyThemeChanges()


        /*  binding.lytCircle.setOnClickListener {
              anim2(binding.lytCircle)
          }*/
        motionObserver()

    }

    /*   private fun setCircleViewPager() {
           val list = mutableListOf<CircleModel>()
           list.add(CircleModel("1", "2"))
           list.add(CircleModel("2", "2"))
           list.add(CircleModel("3", "2"))
           val adapter = CirecleViewPagerAdapter(requireContext(), list)
           circleViewPager.adapter = adapter
           circleViewPager.currentItem =1

       }*/

    private fun motionObserver() {
        binding.motionPillCards.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                binding.circleContainer.hide()
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                binding.circleContainer.show()
                when (currentId) {
                    R.id.start -> {
                        binding.apply {

                            imgLeftDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgRightDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgBottomDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgTopDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )


                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    imgCenterDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    imgCenterDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )

                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    imgCenterDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                            }
                        }
                    }
                    R.id.move_left -> {
                        binding.apply {
                            imgCenterDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgLeftDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            imgBottomDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgTopDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    imgRightDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {

                                    imgRightDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    imgRightDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                            }
                        }
                    }
                    R.id.move_right -> {
                        binding.apply {
                            imgCenterDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            imgRightDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgBottomDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgTopDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    imgLeftDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    imgLeftDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )

                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    imgLeftDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                            }
                        }
                    }
                    R.id.move_bottom -> {
                        binding.apply {
                            imgCenterDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgLeftDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgRightDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgBottomDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    imgTopDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    imgTopDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )

                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    imgTopDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                            }
                        }

                    }
                    R.id.move_top -> {
                        binding.apply {
                            imgCenterDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgLeftDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            imgRightDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )

                            imgTopDot.setColorFilter(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.gray_dark
                                ), android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    imgBottomDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )

                                }
                                Configuration.UI_MODE_NIGHT_NO -> {

                                    imgBottomDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    imgBottomDot.setColorFilter(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        ), android.graphics.PorterDuff.Mode.SRC_IN
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
            }

        })
    }

    fun anim2(view: View) {
        val TAG = "Swipe=="


        view.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeLeft() {
                moveAnimId = R.anim.move_right_animator
                moveView(R.anim.move_right_animator, view)
                Log.d(TAG, "onSwipeLeft: ")
            }

            override fun onSwipeRight() {
                moveAnimId = R.anim.move_left_animator
                moveView(R.anim.move_left_animator, view)
                Log.d(TAG, "onSwipeRight: ")
            }

            override fun onSwipeTop() {
                moveAnimId = R.anim.move_top_animator
                moveView(R.anim.move_top_animator, view)
                super.onSwipeTop()
            }

            override fun onSwipeBottom() {
                moveAnimId = R.anim.move_bottom_animator
                moveView(R.anim.move_bottom_animator, view)
                super.onSwipeBottom()
            }
        })


    }

    fun moveView(id_anim: Int, view: View) {
        val animation: Animation = AnimationUtils.loadAnimation(
            requireContext(),
            id_anim
        )
        view.startAnimation(animation)
        centerMoveView(view)
    }

    private fun centerMoveView(view: View) {
        Timer().schedule(500) {
            val animation: Animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.center_animator
            )
            view.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    when (moveAnimId) {
                        R.anim.move_top_animator -> {
                            showToast("Top")
                        }
                        R.anim.move_bottom_animator -> {
                            showToast("Bottom")
                        }
                        R.anim.move_left_animator -> {
                            showToast("Left")
                        }
                        R.anim.move_right_animator -> {
                            showToast("Right")
                        }
                    }
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }

            })
        }
    }


    private fun transactionDetailsDialog() {
        /* requireActivity().supportFragmentManager.beginTransaction()
             .add(TransactionDetailsFragment(), "transaction")
             .commit()*/
        val transactionSheet = TransactionDetailsFragment.newInstance()
        transactionSheet.show(requireActivity().supportFragmentManager, "transaction")

    }


    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                //  btnUpTransaction.setImageResource(R.drawable.img_yellow_up)
            }
        }

        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                //binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_black)

                binding.btnUpTransaction.setBackgroundResource(R.drawable.bg_gradient_black_home)
                /*binding.viewRingCenter.setBackgroundResource(R.drawable.bg_gradient_ring_orange)
                binding.viewRingLeft.setBackgroundResource(R.drawable.bg_gradient_ring_orange)
                binding.viewRingRight.setBackgroundResource(R.drawable.bg_gradient_ring_orange)
                binding.viewRingBottom.setBackgroundResource(R.drawable.bg_gradient_ring_orange)
            */
                //binding.viewRingTop.setBackgroundResource(R.drawable.bg_gradient_ring_orange)
                binding.topLayout.setBackgroundResource(R.color.orange_light)
                /*       binding.txtAmountCenter.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountLeft.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountRight.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountBottom.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountTop.appTextGradiant(R.color.b_light_300, R.color.b_light_300)*/
                binding.imgCenterDot.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.centerWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black_light
                    )
                )
                binding.leftWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black_light
                    )
                )
                binding.rightWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black_light
                    )
                )
                binding.topWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black_light
                    )
                )
                binding.bottomWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black_light
                    )
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnUpTransaction.setBackgroundResource(R.drawable.bg_gradient_blue_home)

                /*  binding.viewRingCenter.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                  binding.viewRingLeft.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                  binding.viewRingRight.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                  binding.viewRingBottom.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                  binding.viewRingTop.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
  */
                /*       binding.txtAmountCenter.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountLeft.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountRight.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountBottom.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                       binding.txtAmountTop.appTextGradiant(R.color.b_light_300, R.color.b_light_300)*/
                binding.imgCenterDot.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.rightWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.new_button_light
                    )
                )
                binding.topLayout.setBackgroundResource(R.color.new_bg_light)

                binding.centerWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.leftWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.rightWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.topWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.bottomWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )


                binding.innerCircleCenter.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleLeft.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleRight.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleTop.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleBottom.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.topLayout.setBackgroundResource(R.drawable.bg_gradient_white)
                binding.btnUpTransaction.setBackgroundResource(R.drawable.bg_gradient_blue_home)
                /*binding.viewRingCenter.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                binding.viewRingLeft.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                binding.viewRingRight.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                binding.viewRingBottom.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
                binding.viewRingTop.setBackgroundResource(R.drawable.bg_gradient_ring_blue)
*/
                /*   binding.txtAmountCenter.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                   binding.txtAmountLeft.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                   binding.txtAmountRight.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                   binding.txtAmountBottom.appTextGradiant(R.color.b_light_300, R.color.b_light_300)
                   binding.txtAmountTop.appTextGradiant(R.color.b_light_300, R.color.b_light_300)*/
                binding.imgCenterDot.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.rightWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.new_button_light
                    )
                )
                binding.topLayout.setBackgroundResource(R.color.new_bg_light)
                binding.centerWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.leftWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.rightWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.topWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )
                binding.bottomWhiteCircle.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_200
                    )
                )



                binding.innerCircleCenter.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleLeft.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleRight.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleTop.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )
                binding.innerCircleBottom.setCardBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.b_light_100
                    )
                )

            }
        }
    }


    override fun onResume() {
        super.onResume()
        /*  if (IS_SWITCH_SHOWN.not()) {
              showAccountDialog()
          }*/
        getWallet(true)
        getBalance()
    }

    fun getWallet(isDismiss: Boolean) {
        val uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
        if (uuid.isBlank()) {
            isDismissDialog = isDismiss
            SELECTED_AC_TYPE = "Wallet Account"
            showLoading()
            homeViewModel.getAllAccount(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""), "wallet"

            )
        }
    }

    private fun getBalance() {
        val uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
        if (uuid.isNotBlank()) {
            homeViewModel.getSpentAmt(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                uuid,
            )
        }
    }

    private fun showAccountDialog() {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = BottomSheetDialog(requireContext())
        }
        bottomSheetDialog?.apply {

            DataBindingUtil.inflate<BottomSheetSwitchAcBinding>(
                layoutInflater,
                R.layout.bottom_sheet_switch_ac,
                null,
                false
            ).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }

                var selectedAcView: ImageView? = null

                fun resetSelection() {
                    selectedAcView?.let {
                        it.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                    }
                }

                fun getWallet(isDismiss: Boolean) {
                    isDismissDialog = isDismiss
                    resetSelection()
                    selectedAcView = imgCheckWallet
                    // selectAcName = ""
                    imgCheckWallet.setImageResource(R.drawable.ic_baseline_check_green_circle_outline_24)

                    SELECTED_AC_TYPE = "Wallet Account"
                    showLoading()
                    homeViewModel.getAllAccount(
                        pref().getStringValue(SharePreferences.USER_ID, ""),
                        pref().getStringValue(SharePreferences.USER_TOKEN, ""), "wallet"

                    )
                }
                getWallet(false)


                btnSavingAc.setOnClickListener {
                    resetSelection()
                    selectedAcView = imgCheckSa
                    //  selectAcName = "Saving Account"
                    imgCheckSa.setImageResource(R.drawable.ic_baseline_check_green_circle_outline_24)

                    // dismiss()
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.SAVING_ACC)
                    startActivity(intent)


                }
                btnCurrentAc.setOnClickListener {
                    resetSelection()
                    selectedAcView = imgCheckCa
                    //   selectAcName = "Current Account"
                    imgCheckCa.setImageResource(R.drawable.ic_baseline_check_green_circle_outline_24)
                    //SELECTED_AC_TYPE = selectAcName
                    // dismiss()
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.CURRENT_ACC)
                    startActivity(intent)
                }
                btnLinkedAc.setOnClickListener {
                    resetSelection()
                    selectedAcView = imgCheckLa
                    // selectAcName = "Linked Account"
                    imgCheckLa.setImageResource(R.drawable.ic_baseline_check_green_circle_outline_24)
                    //  SELECTED_AC_TYPE = selectAcName
                    // dismiss()
                    val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.ALL_AC.name)
                    intent.putExtra(Constants.ACC_TYPE, Constants.LINKED_ACC)
                    startActivity(intent)
                }
                btnWalletAc.setOnClickListener {

                    getWallet(true)

                }

                bottomSheetDialog?.setOnDismissListener {
                    getWallet(true)
                }
            }
            setCancelable(true)


        }
        if (bottomSheetDialog != null)
            if (bottomSheetDialog?.isShowing!!.not()) {
                bottomSheetDialog?.show()
            }


    }


    private fun observer() {
        homeViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is WalletAccountModel -> {
                    homeViewModel.observerResponse.value = null
                    hideLoading()
                    HomeFragment.SELECTED_AC_TYPE = "Wallet Account"
                    /*     if(isDismissDialog.not()){
                             pref().setStringValue(
                                 SharePreferences.USER_WALLET_UUID,
                                 it.data[0].acc_uuid
                             )
                             pref().setStringValue(SharePreferences.DEFAULT_ACCOUNT, it.data[0].acc_no)

                             return@Observer
                         }*/

                    if (it.data.isNotEmpty()) {
                        pref().setStringValue(
                            SharePreferences.USER_WALLET_UUID,
                            it.data[0].acc_uuid
                        )
                        pref().setStringValue(SharePreferences.DEFAULT_ACCOUNT, it.data[0].acc_no)
                        pref().setStringValue(SharePreferences.DEFAULT_PURPOSE, "")
                        /*pref().setStringValue(
                            SharePreferences.USER_WALLET_UUID,
                            it.data[0].acc_uuid
                        )*/
                        HomeFragment.IS_SWITCH_SHOWN = true
                        pref().setStringValue(
                            SharePreferences.DEFAULT_AC_TYPE,
                            SharePreferences.AcType.WALLET.name
                        )

                        //bottomSheetDialog?.dismiss()
                        //(requireActivity() as MainActivity).recreate()

                    }
                    getBalance()
                    (requireActivity() as MainActivity).getBalance()


                    homeViewModel.observerResponse.value = null
                }
                is SpentResponse -> {
                    hideLoading()
                    TODAY_AMOUNT = Constants.DEFAULT_CURRENCY + it.data.amountActual.balance
                    //binding.txtAmount.text = Constants.DEFAULT_CURRENCY + it.data.balance
                    binding.txtAmountCenter.text =
                        Constants.DEFAULT_CURRENCY + it.data.amountDisplay.balance
                    binding.txtAmountRight.text =
                        Constants.DEFAULT_CURRENCY + it.data.amountDisplay.todaysSpent
                    binding.txtAmountLeft.text =
                        Constants.DEFAULT_CURRENCY + it.data.amountDisplay.monthlySpent
                    binding.txtAmountTop.text =
                        Constants.DEFAULT_CURRENCY + it.data.amountDisplay.monthlyReceived
                    binding.txtAmountBottom.text =
                        Constants.DEFAULT_CURRENCY + it.data.amountDisplay.todaysReceive
                    binding.txtDayCenterLeft.text = it.data.month
                    binding.txtDayCenterTop.text = it.data.month
                    homeViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    homeViewModel.observerResponse.value = null

                }
                is Throwable -> {
                    homeViewModel.observerResponse.value = null
                    showToast(it.message.toString())
                    hideLoading()
                    /*bottomSheetDialog?.let {
                        if(it.isShowing.not()){
                            it.show()
                        }
                    }*/
                }
            }
        })
    }

    companion object {
        var IS_SWITCH_SHOWN = false
        var SELECTED_AC_TYPE = ""
        var TODAY_AMOUNT = "0"
    }

    override fun getLayoutId(): Int = R.layout.home_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): HomeViewModel = homeViewModel


}