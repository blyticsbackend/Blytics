package com.nbt.blytics.modules.payee.schedule

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowPaymentHistoryBinding
import com.nbt.blytics.databinding.RowScheduleBinding
import com.nbt.blytics.databinding.RowTransactionSubDetailsBinding
import com.nbt.blytics.modules.transactiondetails.models.TranscationDetailsModel
import com.nbt.blytics.utils.Constants

/**
 * Created bynbton 14-10-2021
 */
class ScheduleAdapter (val context: Context, val list: MutableList<RecentScheduleRes.Data.Schedule>, val callback:(Int)->Unit) :
    RecyclerView.Adapter<ScheduleAdapter.HistoryViewHolder>() {
    class HistoryViewHolder (val binding: RowScheduleBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowScheduleBinding>(
            inflater, R.layout.row_schedule, parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            txtAmount.text = Constants.DEFAULT_CURRENCY +data.amount
            txtDate.text = data.nextDate
            txtSchedule.text = data.frequency
            txtName.text = data.name
            txtReference.text = data.reference
        }
        happyThemeChanges(holder.binding.cardMain)
    }



    fun deleteItem(i :Int){
        //list.removeAt(i)
        notifyDataSetChanged()
    }
    fun addItem(i :Int, item:RecentScheduleRes.Data.Schedule){
        list.add(i,item)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int  = list.size


    private fun happyThemeChanges(view: MaterialCardView) {

        when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                view.setCardBackgroundColor(context.resources.getColor(R.color.black))

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                view.setCardBackgroundColor(context.resources.getColor(R.color.white))

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                view.setCardBackgroundColor(context.resources.getColor(R.color.white))

            }
        }

    }
}