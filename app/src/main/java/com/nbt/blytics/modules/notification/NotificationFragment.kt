package com.nbt.blytics.modules.notification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.databinding.NotificationFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*

class NotificationFragment : BaseFragment<NotificationFragmentBinding, NotificationViewModel>() {
    private lateinit var binding: NotificationFragmentBinding
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val limit = 10
    private lateinit var adapter: NotificationAdapter
    private val list = mutableListOf<AllNotificationResponse.Data.Notification.Final>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        setAdapter()

        getNotification()


    }

    private fun getNotification() {
        showLoading()
        notificationViewModel.getNotification(
            NotificationListRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                page = 1,
                offset = limit

            )
        )
    }

    private fun observer() {
        notificationViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is AllNotificationResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        list.addAll(it.data.notification.finalList)
                        adapter.notifyDataSetChanged()
                    } else {

                    }
                    notificationViewModel.observerResponse.value = null

                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    notificationViewModel.observerResponse.value = null

                }
            }
        }
    }

    private fun setAdapter() {
        adapter = NotificationAdapter(requireContext(), list) {

        }
        binding.apply {
            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvNotification.layoutManager = layoutManager
            rvNotification.adapter = adapter

            val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    // binding.lytProgressCircular.show()

                    notificationViewModel.getNotification(
                        NotificationListRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            page = page + 1,
                            offset = limit

                        )
                    )

                }

            }
            binding.rvNotification.addOnScrollListener(endlessRecyclerView)
        }
    }



        override fun getLayoutId(): Int = R.layout.notification_fragment

        override fun getBindingVariable(): Int = 0

        override fun getViewModel(): NotificationViewModel = notificationViewModel


    }