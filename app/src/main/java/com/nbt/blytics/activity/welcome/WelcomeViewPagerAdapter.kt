package com.nbt.blytics.activity.welcome

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.nbt.blytics.R
import com.nbt.blytics.activity.welcome.WelcomeActivity


/**
 * Created bynbton 01-07-2021
 */
class WelcomeViewPagerAdapter(val context: Context, val listScreen :MutableList<WelcomeActivity.ScreenItem>) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val  inflater  = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutScreen: View = inflater.inflate(R.layout.layout_screen, null)
        val imgSlide :ImageView= layoutScreen.findViewById(R.id.intro_img)
        val title = layoutScreen.findViewById<TextView>(R.id.intro_title)
        val description = layoutScreen.findViewById<TextView>(R.id.intro_description)

        title.text = listScreen.get(position).title
        description.text = listScreen.get(position).description
        imgSlide.setImageResource(listScreen[position].img)
        container.addView(layoutScreen)
        return layoutScreen

    }
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view==obj
    }
    override fun getCount(): Int =listScreen.size


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}