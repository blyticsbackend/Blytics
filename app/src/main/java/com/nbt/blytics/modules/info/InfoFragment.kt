package com.nbt.blytics.modules.info

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.InfoFragmentBinding
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.hideFadeAnim
import com.nbt.blytics.utils.show
import com.nbt.blytics.utils.showFadeAnim

class InfoFragment : BaseFragment<InfoFragmentBinding, InfoViewModel>() {

    private val infoViewModel:InfoViewModel by viewModels()
    private lateinit var binding :InfoFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        hideLoading()
        binding.apply {
            motionPillCards.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    p0?.post {
                        when (p1) {
                            R.id.start -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(1)
                            }
                            R.id.end -> {
                                txtHeader1.showFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(2)
                            }
                            R.id.end2 -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(3)
                            }
                            R.id.end3 -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(4)
                            }

                        }
                    }
                }

                override fun onTransitionChange(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Int,
                    p3: Float
                ) {
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                    p0?.post {
                        when (p1) {
                            R.id.start -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.showFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(1)
                            }
                            R.id.end -> {
                                txtHeader1.showFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.showFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(2)
                            }
                            R.id.end2 -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.showFadeAnim(motionPillCards)
                                txtHeader3.hideFadeAnim(motionPillCards)
                                txtHeader4.showFadeAnim(motionPillCards)
                                visibityField(3)
                            }
                            R.id.end3 -> {
                                txtHeader1.hideFadeAnim(motionPillCards)
                                txtHeader2.hideFadeAnim(motionPillCards)
                                txtHeader3.showFadeAnim(motionPillCards)
                                txtHeader4.hideFadeAnim(motionPillCards)
                                visibityField(4)
                            }
                        }
                    }
                }

                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {

                }

            })
        }
    }
    private fun visibityField(i: Int) {
        binding.apply {
            when (i) {
                1 -> {
                    consNotification.hide()
                    txtTitle1.text = "Financial Charges"
                }
                2 -> {
                    consNotification.hide()
                    txtTitle2.text = "Operational Charges"
                }
                3 -> {
                    consNotification.show()
                    txtTitle3.text = "Notification Charges"
                }
                4 -> {
                    consNotification.hide()
                    txtTitle4.text = "Security Charges"
                }
            }
        }
    }

    override fun getLayoutId(): Int =R.layout.info_fragment

    override fun getBindingVariable(): Int =0

    override fun getViewModel(): InfoViewModel =infoViewModel


}