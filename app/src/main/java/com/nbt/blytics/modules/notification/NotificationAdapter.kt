package com.nbt.blytics.modules.notification

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowCountryAdpterBinding
import com.nbt.blytics.databinding.RowNotificationListBinding
import com.nbt.blytics.utils.UtilityHelper

/**
 * Created bynbton 15-12-2021
 */
class NotificationAdapter(
    val context: Context,
    val list: MutableList<AllNotificationResponse.Data.Notification.Final>,
    val callback: (Int) -> Unit
):RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>(){
     class NotificationViewHolder(val binding:RowNotificationListBinding) :RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowNotificationListBinding>(
            inflater, R.layout.row_notification_list, parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            txtTitle.text = data.title
            txtMessage.text = data.message
            txtDate.text ="${UtilityHelper.txnDateFormat(data.date)} ${UtilityHelper.txnTimeFormat(data.date)}"


            txtSeeMore.setOnClickListener {
                if(txtMessage.tag =="single"){
                    txtMessage.isSingleLine = false
                    txtSeeMore.text ="see less"
                    txtMessage.tag="multi"
                }else{
                    txtMessage.tag="single"
                    txtSeeMore.text ="see more"
                    txtMessage.isSingleLine = true
                }
            }
            happyThemeChanges( holder.binding)
        }

    }

    override fun getItemCount(): Int = list.size


    private fun happyThemeChanges(binding: RowNotificationListBinding) {


        when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.txtSeeMore.setTextColor(ContextCompat.getColor(context, R.color.orange_light))
                binding.txtTitle.setTextColor(ContextCompat.getColor(context, R.color.white_400))
                binding.txtMessage.setTextColor(ContextCompat.getColor(context, R.color.white_400))

                // view.setBackgroundResource(R.color.black_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.mainLayout.setBackgroundResource(R.color.white_400)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.mainLayout.setBackgroundResource(R.color.white_400)

            }
        }


    }

}
