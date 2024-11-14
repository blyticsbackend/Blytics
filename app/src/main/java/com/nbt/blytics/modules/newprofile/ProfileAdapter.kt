package com.nbt.blytics.modules.newprofile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.databinding.RowProfileCardBinding

class ProfileAdapter(val context: Context, val list: MutableList<ProfileModel>) :
    RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder?>() {
    inner class ProfileViewHolder(val binding: RowProfileCardBinding) : RecyclerView.ViewHolder(binding.root)

 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RowProfileCardBinding>(inflater, R.layout.row_profile_card, parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            txtName.text = data.subtitle
            txtTitle.text = data.title

            if (data.documentVerified) {
                imgVerifyStatus.setImageResource(R.drawable.ic_check)
            } else {
                imgVerifyStatus.setImageResource(R.drawable.ic_cross)
            }

            if (data.id == 4) {
                if (data.verified) {
                    imgVerifyStatus.setImageResource(R.drawable.ic_check)
                } else {
                    imgVerifyStatus.setImageResource(R.drawable.ic_cross)
                }
            }

            if (data.id == 2 || data.id == 3) {
                if (data.verified) {
                    imgVerifyStatus.setImageResource(R.drawable.ic_check)
                } else {
                    imgVerifyStatus.setImageResource(R.drawable.ic_cross)
                }
            }

        }

    }

    override fun getItemCount(): Int = list.size
}