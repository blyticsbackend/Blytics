package com.nbt.blytics.modules.securityQes

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.nbt.blytics.R

class MyViewPageAdapter(
    private val context: Context,
    private val list: MutableList<Page>,
    private val colorCodeDay: Int = -1,
    private val colorCodeNight: Int = -1,
    private val textColor: Int = -1,
    private val bgDay: Int = -1
) : PagerAdapter() {

    @SuppressLint("SuspiciousIndentation")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layoutScreen = inflater.inflate(R.layout.item_page_adapter, container, false)

        val title = layoutScreen.findViewById<TextView>(R.id.title)
        val card = layoutScreen.findViewById<MaterialCardView>(R.id.card_1)
        val frm = layoutScreen.findViewById<ImageView>(R.id.frm)
        val images = layoutScreen.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.circleImageView)

        // Set text color if provided
        if (textColor != -1) {
            title.setTextColor(context.resources.getColor(textColor, null))
        }

        // Set title
        title.text = list[position].title

        // Load image
        val page = list[position]
        when {
            page.imageUrl != null -> {
                // Load from URL
                Glide.with(context)
                    .load(page.imageUrl)
                    .placeholder(R.drawable.dummy_user)
                    .error(R.drawable.ic_baseline_error_24)
                    .into(images)
            }
            page.drawableRes != null -> {
                // Load from drawable resource
                images.setImageResource(page.drawableRes)
            }
            else -> {
                // Fallback drawable if both are null
                images.setImageResource(R.drawable.dummy_user)
            }
        }

        // Set background color based on UI mode
        when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                frm.background = context.getDrawable(bgDay)
                // Uncomment below line if you need to set card background color
                // card.setCardBackgroundColor(context.resources.getColor(colorCodeNight, null))
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                frm.background = context.getDrawable(bgDay)
                // Uncomment below line if you need to set card background color
                // card.setCardBackgroundColor(context.resources.getColor(colorCodeDay, null))
            }
        }

        container.addView(layoutScreen)
        return layoutScreen
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
    override fun getCount(): Int = list.size
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}

data class Page(
    val title: String,
    val imageUrl: String? = null,
    val drawableRes: Int? = null,
    val backgroundColor: Int = 0
)