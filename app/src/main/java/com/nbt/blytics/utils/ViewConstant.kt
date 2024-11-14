package com.nbt.blytics.utils

import android.R.color
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.*
import android.transition.Fade
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.nbt.blytics.R
import java.io.IOException
import android.graphics.drawable.BitmapDrawable

import android.view.WindowManager
import android.widget.PopupWindow


/**
 * Created bynbton 30-06-2021
 */


fun View.setBgTintHappyTheme(){
   this.backgroundTintList = ColorStateList.valueOf( resources.getColor(R.color.yellow_500))
}
fun View.setBgColorHappyTheme(){
   this.setBackgroundColor( resources.getColor(R.color.yellow_500))
}

fun TextView.setTextColorHappyTheme(){
   this.setTextColor(resources.getColor(R.color.yellow_700))
}

fun TextView.setTextDrawableTint(){
   for (drawable in this.compoundDrawables) {
      if (drawable != null) {
         drawable.colorFilter = PorterDuffColorFilter(resources.getColor(R.color.yellow_700), PorterDuff.Mode.SRC_IN)
      }
   }

}
fun ImageView.setTintColorHappyTheme(){
   this.setColorFilter(resources.getColor(R.color.yellow_700))
}


fun ImageView.setImage(url: Any, placeholder: Int = R.drawable.logo) {
    Glide.with(context).let { it ->
        it.clear(this)
        it.load(url).centerCrop()/*.placeholder(placeholder)*/.into(this)

    }
}
   /* Glide.with(context)
    Glide.with(context)}*/




fun ImageView.setImageAssets(context: Context,fileName: String){
    try {
        with(context.assets.open(fileName)){
            setImageBitmap(BitmapFactory.decodeStream(this))
        }
    }catch (e: IOException) {
        // log error
    }
}

 fun TextView.appTextGradiant(lightColor:Int, darkColor:Int){
   val shader = LinearGradient(0f, 0f, 0f, this.textSize, resources.getColor(lightColor),resources.getColor(darkColor), Shader.TileMode.CLAMP)
   this.paint.shader = shader
}

fun String.toCamelCase() =
    split('_').joinToString("", transform = String::capitalize)
fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
fun View.hide(){
    this.visibility =View.GONE
}
fun View.hideFadeAnim(v :ViewGroup){
   /* val transition =  Fade()
    transition.duration = 200
    transition.addTarget(this.id)
    TransitionManager.beginDelayedTransition(v, transition)*/
    this.visibility =View.GONE
}

fun View.showFadeAnim(vg :ViewGroup ){
  /*  val transition =  Fade()
    transition.duration = 200
    transition.addTarget(this.id)
    TransitionManager.beginDelayedTransition(vg, transition)*/
    this.visibility =View.VISIBLE
}

fun View.show(){
    this.visibility =View.VISIBLE
}
fun View.invisible(){
    this.visibility =View.INVISIBLE
}





