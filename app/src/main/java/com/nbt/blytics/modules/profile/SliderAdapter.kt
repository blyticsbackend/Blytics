package com.nbt.blytics.modules.profile

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show
import kotlin.contracts.contract

/**
 * Created by nbtk on 5/4/18.
 */
class SliderAdapter (val context:Context): RecyclerView.Adapter<SliderAdapter.SliderItemViewHolder>() {

    val data: ArrayList<IconModel> = ArrayList();
    var callback: Callback? = null
    val clickListener = View.OnClickListener { v -> v?.let { callback?.onItemClicked(it) } }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_slider_item, parent, false)

      //  itemView.setOnClickListener(clickListener)

        return SliderItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SliderItemViewHolder, position: Int) {
        holder.imgIcon!!.setImageResource(data[position].img)

        holder.imgFlag?.hide()
        if(data[position].status ==Status.VERIFIED ){
            holder.title?.text = "Verified"
            holder.title?.setTextColor(ContextCompat.getColor(context, R.color.app_green_dark))
        }else if (data[position].status == Status.UNVERIFIED){
            holder.title?.text = "Unverified"
            holder.imgFlag?.show()
            holder.title?.setTextColor(ContextCompat.getColor(context, R.color.light_red))
        }else{
            holder.title?.text = "Pending"
            holder.imgFlag?.show()
            holder.title?.setTextColor(ContextCompat.getColor(context, R.color.light_red))
            //holder.title?.setTextColor(ContextCompat.getColor(context, R.color.yellow))
        }


    }

    fun setData(data: MutableList<IconModel>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    interface Callback {
        fun onItemClicked(view: View)
    }

   /* override fun getItemId(position: Int): Long {
        return if (false) data.size else Int.MAX_VALUE
    }*/
   /* override fun getItemCount(): Int {
        return if (false) data.size else Int.MAX_VALUE
    }*/



    class SliderItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val imgIcon: ImageView? = itemView?.findViewById(R.id.img_icon)
        val title: TextView?= itemView?.findViewById(R.id.title)
        val imgFlag: ImageView?= itemView?.findViewById(R.id.img_flag)
    }
}