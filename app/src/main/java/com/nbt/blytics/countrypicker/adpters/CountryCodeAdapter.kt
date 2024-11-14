package com.nbt.blytics.countrypicker.adpters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.countrypicker.models.Country
import com.nbt.blytics.databinding.RowCountryAdpterBinding
import com.nbt.blytics.utils.setImageAssets
import java.util.*

/**
 * Created bynbton 07-07-2021
 */
class CountryCodeAdapter (val context:Context, val  list:MutableList<Country>, val callback:(Int)->Unit):RecyclerView.Adapter<CountryCodeAdapter.CountryViewHolder>(){
   inner class CountryViewHolder(val binding: RowCountryAdpterBinding):RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowCountryAdpterBinding>(
            inflater, R.layout.row_country_adpter, parent, false
        )
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {

            if(data.code.lowercase(Locale.ENGLISH) == "do") {
                flagImv.setImageAssets(context,data.code.lowercase(Locale.ENGLISH) + "1.png")
            }else{
                flagImv.setImageAssets(context,data.code.lowercase(Locale.ENGLISH)+".png")
            }
            selectedCountryTv.text = data.name
            txtCountryCode.text = data.dial_code

        }
        holder.itemView.setOnClickListener {
            callback(position)
        }

    }

    override fun getItemCount(): Int =list.size
}