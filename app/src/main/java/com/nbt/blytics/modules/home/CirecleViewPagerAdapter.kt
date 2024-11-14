package com.nbt.blytics.modules.home

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.nbt.blytics.R
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.utils.OnSwipeTouchListener
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show
import java.util.*

import kotlin.concurrent.schedule
/**
 * Created bynbton 01-07-2021
 */
class CirecleViewPagerAdapter(val context: Context, val list :MutableList<CircleModel>) : PagerAdapter() {

    private var moveAnimId = -1

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val  inflater  = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutScreen: View = inflater.inflate(R.layout.layout_circle, null)
        val txtAmt :TextView= layoutScreen.findViewById(R.id.txt_amount_center)
        val lytCircle :View= layoutScreen.findViewById(R.id.lyt_circle)
        val viewCircle :View= layoutScreen.findViewById(R.id.view_circle)
        val imgCircleTop :View= layoutScreen.findViewById(R.id.img_circle_top_center)
        val imgCircleLeft :View= layoutScreen.findViewById(R.id.img_circle_left_center)
        val imgCircleRight :View= layoutScreen.findViewById(R.id.img_circle_right_center)
        val imgCircleBottom :View= layoutScreen.findViewById(R.id.img_circle_bottom_center)

        when(position){
            0->{
                imgCircleLeft.hide()
            }
            1->{
                imgCircleLeft.show()
                imgCircleRight.show()
            }
            2->{
                imgCircleRight.hide()
            }
        }
        txtAmt.text = list[position].balance


        container.addView(layoutScreen)

        movicAnimation(lytCircle, viewCircle)

        return layoutScreen

    }
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view==obj
    }
    override fun getCount(): Int =list.size


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }


    fun movicAnimation(view: View, triggerView:View) {
        val TAG = "Swipe=="


        triggerView.setOnTouchListener(object : OnSwipeTouchListener(context) {


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
            context,
            id_anim
        )
        view.startAnimation(animation)
        centerMoveView(view)
    }

    private fun centerMoveView(view: View) {
        Timer().schedule(500) {
            val animation: Animation = AnimationUtils.loadAnimation(
                context,
                R.anim.center_animator
            )
            view.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    when (moveAnimId) {
                        R.anim.move_top_animator -> {
                            (context as MainActivity).showToast("Top")
                        }
                        R.anim.move_bottom_animator -> {
                            (context as MainActivity).showToast("Bottom")
                        }

                    }
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }

            })
        }
    }

}