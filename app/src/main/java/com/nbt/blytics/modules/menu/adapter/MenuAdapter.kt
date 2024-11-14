package com.nbt.blytics.modules.menu.adapter

import android.app.Activity
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.activity.main.models.MenuItem
import com.nbt.blytics.databinding.RowMenuBinding
import com.nbt.blytics.utils.setImage

class MenuAdapter(
    private val ctx: Activity,
    val list: List<MenuItem>,
    val callback: (Int, View) -> Unit,
    val changeCallback: () -> Unit
) :
    RecyclerView.Adapter<MenuAdapter.MenuViewModel>() {
    inner class MenuViewModel(val binding: RowMenuBinding) : RecyclerView.ViewHolder(binding.root) {
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewModel {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowMenuBinding>(inflater, R.layout.row_menu, parent, false)
        return MenuViewModel(binding)
    }

    override fun onBindViewHolder(holder: MenuViewModel, position: Int) {
        val data = list[position]
        holder.binding.apply {
            imgMenu.setImage(data.image)
            txtname.text = data.label
        }

        holder.itemView.setOnClickListener {
            callback(position, holder.itemView)
        }

        when (ctx.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holder.binding.imgMenu.setColorFilter(ctx.getResources().getColor(R.color.white))
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                holder.binding.imgMenu.setColorFilter(null)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                holder.binding.imgMenu.setColorFilter(null)
            }
        }
    }

    /*  override fun onRowMoved(fromPosition: Int, toPosition: Int) {
          if (fromPosition < toPosition) {
              for (i in fromPosition until toPosition) {
                  Collections.swap(list, i, i + 1)
              }
          } else {
              for (i in fromPosition downTo toPosition + 1) {
                  Collections.swap(list, i, i - 1)
              }
          }
          notifyItemMoved(fromPosition, toPosition)
          changeCallback()
      }*/
    override fun getItemCount(): Int = list.size
}