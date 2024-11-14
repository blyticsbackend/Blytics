package com.nbt.blytics.modules.payee.schedule

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.ScheduleFragmentBinding
import com.nbt.blytics.modules.payee.schedulecreate.ScheduleCreateFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.EndlessRecyclerViewScrollListener
import com.nbt.blytics.utils.SharePreferences

class ScheduleFragment : BaseFragment<ScheduleFragmentBinding, ScheduleViewModel>() {
    private lateinit var binding: ScheduleFragmentBinding
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    val scheduleList = mutableListOf<RecentScheduleRes.Data.Schedule>()
    private val limit = "8"
    private lateinit var adapter: ScheduleAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        setAdapter()
        observer()
        happyThemeChanges()

        binding.btnAddNewSchedule.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_SCHEDULE.name)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getRecentSchedule()
    }

    private fun getRecentSchedule() {
        scheduleList.clear()
        adapter.notifyDataSetChanged()
        showLoading()
        scheduleViewModel.getRecentSchedule(
            RecentScheduleRequest(
                offset = limit,
                frequent_payee = false,
                page = "1",
                user_id = pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                user_token = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
            )
        )
    }

    private lateinit var tempDeleteItem: RecentScheduleRes.Data.Schedule
    fun setAdapter() {
        val swipeGusture = object : SwipeGusture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        SCHEDUEL = scheduleList[viewHolder.absoluteAdapterPosition]
                        val intent = Intent(requireActivity(), BconfigActivity::class.java)
                        intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_SCHEDULE.name)
                        intent.putExtra(ScheduleCreateFragment.COMING_FOR_EDIT, true)
                        startActivity(intent)
                    }

                    ItemTouchHelper.RIGHT -> {
                        tempDeleteItem = scheduleList[viewHolder.absoluteAdapterPosition]
                        adapter.deleteItem(viewHolder.absoluteAdapterPosition)
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                        dialog.apply {
                            // setTitle(msg)
                            setMessage("Do you to delete?")
                            setCancelable(false)
                            setNegativeButton("No") { dialog, which ->
                                getRecentSchedule()
                                //   adapter.addItem( viewHolder.absoluteAdapterPosition, tempDeleteItem  )
                            }
                            setPositiveButton("Yes") { dialog, which ->
                                dialog.dismiss()
                                showLoading()
                                scheduleViewModel.deleteSchedule(DeleteScheduleRequest(scheduleList[viewHolder.absoluteAdapterPosition].scheduleId.toInt(), pref().getStringValue(SharePreferences.USER_ID, "").toInt(), pref().getStringValue(SharePreferences.USER_TOKEN, "")), scheduleList[viewHolder.absoluteAdapterPosition].type.equals("internal Schedule", true))
                            }
                        }
                        dialog.show()
                    }
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGusture)
        touchHelper.attachToRecyclerView(binding.rvSchedule)
        adapter = ScheduleAdapter(requireContext(), scheduleList) {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_SCHEDULE.name)
            startActivity(intent)
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvSchedule.layoutManager = layoutManager
        binding.rvSchedule.adapter = adapter
        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                scheduleViewModel.getRecentSchedule(
                    RecentScheduleRequest(
                        offset = limit,
                        frequent_payee = false,
                        page = "${page + 1}",
                        user_id = pref().getStringValue(SharePreferences.USER_ID, "").toInt(),
                        user_token = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                        uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, "")
                    )
                )
            }
        }
        binding.rvSchedule.addOnScrollListener(endlessRecyclerView)
    }

    private fun observer() {
        scheduleViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DeleteScheduleResponse -> {
                    hideLoading()
                    getRecentSchedule()
                    scheduleViewModel.observerResponse.value = null
                }
                is RecentScheduleRes -> {
                    hideLoading()
                    if (it.data.scheduleList.isEmpty()) {
                        binding.txtNoTxn.visibility = View.VISIBLE
                        binding.rvSchedule.visibility = View.GONE
                    } else {
                        scheduleList.addAll(it.data.scheduleList)
                        binding.rvSchedule.visibility = View.VISIBLE
                        binding.txtNoTxn.visibility = View.GONE
                    }
                    adapter.notifyDataSetChanged()
                    scheduleViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    scheduleViewModel.observerResponse.value = null
                }
            }
        }
    }

    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnAddNewSchedule.setBackgroundResource(R.drawable.bg_gradient_orange_btn_without_redius)
                //  binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_orange_layout_bg)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnAddNewSchedule.setBackgroundResource(R.drawable.bg_gradient_light_btn_without_redius)
                //  binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnAddNewSchedule.setBackgroundResource(R.drawable.bg_gradient_light_btn_without_redius)
                // binding.lytTopBar.setBackgroundResource(R.drawable.bg_gradient_blue_layout_bg)
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    companion object {
        fun newInstance(): ScheduleFragment {
            val frg = ScheduleFragment()
            return frg
        }
        var SCHEDUEL: RecentScheduleRes.Data.Schedule? = null
    }

    override fun getLayoutId(): Int = R.layout.schedule_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): ScheduleViewModel = scheduleViewModel
}