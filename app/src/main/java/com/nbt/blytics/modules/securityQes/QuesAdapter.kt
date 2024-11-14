package com.nbt.blytics.modules.securityQes

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.ItemCircleWithTextBinding
import com.nbt.blytics.databinding.RowChargeBinding
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
/*import com.nbt.blytics.modules.payee.schedule.ScheduleAdapter*/
import com.nbt.blytics.modules.squpdate.model.SqResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.UtilityHelper


class QuesAdapter(val context: Context, val list:MutableList<SqResponse.Question>,val callback:(Int) -> Unit) :
    RecyclerView.Adapter<QuesAdapter.QuesViewHolder>(){
      class QuesViewHolder(val binding:ItemCircleWithTextBinding):RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: QuesViewHolder, position: Int) {
    val data  = list[position]
       holder.binding.apply {
         title.text = data.question
       }
        holder.binding.topLayout.setOnClickListener {
            callback(position)
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemCircleWithTextBinding>(inflater, R.layout.item_circle_with_text, parent, false)
        return QuesViewHolder(binding)
    }
}