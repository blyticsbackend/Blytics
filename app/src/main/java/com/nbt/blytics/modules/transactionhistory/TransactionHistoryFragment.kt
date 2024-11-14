package com.nbt.blytics.modules.transactionhistory

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.TransactionHistoryFragmentBinding
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.singletransaction.SingleTransactionFragment
import com.nbt.blytics.modules.transactiondetails.adapter.TransactionDetailAdapter
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.DatePickerFragment
import com.nbt.blytics.utils.EndlessRecyclerViewScrollListener
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.UtilityHelper
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.show
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionHistoryFragment :
    BaseFragment<TransactionHistoryFragmentBinding, TransactionHistoryViewModel>() {
    private val limit = "12"
    private val transactionHistoryViewModel: TransactionHistoryViewModel by viewModels()
    private lateinit var binding: TransactionHistoryFragmentBinding
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private lateinit var adapter: TransactionDetailAdapter// TransactionAdapter
    private var startDay = -1
    private var startMonth = -1
    private var startYear = -1
    private var sortBy = ""
    private var seletctedDate = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        observer()
        setAdapter()
        txnList.clear()
        showLoading()
        binding.imgFilterList.setOnClickListener {
            showFilterPopUpMenu(binding.imgFilterList)
        }
        binding.searchClear.setOnClickListener {
            binding.searchKeyword.setText("")
            sortBy = ""
            txnList.clear()
            adapter.notifyDataSetChanged()
            showLoading()
            transactionHistoryViewModel.transactionHistory(
                TransactionRequest(
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                    frequentPayee = "False",
                    reverse = 1,
                    offset = limit
                )
            )
        }

        binding.searchKeyword.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (binding.searchKeyword.text.trim().length > 2) {
                    sortBy = ""
                    showLoading()
                    txnList.clear()
                    adapter.notifyDataSetChanged()
                    transactionHistoryViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            name = binding.searchKeyword.text.toString(),
                            offset = limit
                        )
                    )
                } else {
                    showToast("minimum 3 characters")
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onResume() {
        super.onResume()
        txnList.clear()
        adapter.notifyDataSetChanged()
        showLoading()
        transactionHistoryViewModel.transactionHistory(
            TransactionRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                frequentPayee = "False",
                reverse = 1,
                offset = limit
            )
        )
    }

    private fun setAdapter() {
        adapter = TransactionDetailAdapter(requireContext(), txnList) { position ->
            val data = txnList[position]
            if (data.bankType.equals("Internal", true)) {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.userId)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
            } else {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.bank_acc)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
            }
        }
        binding.apply {
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvHistory.layoutManager = layoutManager
            rvHistory.adapter = adapter
            val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    transactionHistoryViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            page = page + 1,
                            offset = limit,
                            name = binding.searchKeyword.text.toString(),
                            sortBy = sortBy,
                            date = seletctedDate
                        )
                    )
                }
            }
            binding.rvHistory.addOnScrollListener(endlessRecyclerView)
        }
    }

    private fun observer() {
        transactionHistoryViewModel.observerResponse.observe(viewLifecycleOwner) {
             when (it) {
                 is TransactionResponse -> {
                     hideLoading()
                     if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                         txnList.addAll(it.data!!.txnList)
                         var temp = ""
                         for (i in txnList.indices) {
                             if (i == 0) {
                                 temp = UtilityHelper.txnDateFormat(txnList[i].date)
                                 txnList[i].lytType = 1
                             } else {
                                 if (temp.equals(UtilityHelper.txnDateFormat(txnList[i].date), true)) {
                                     txnList[i].lytType = 0
                                 } else {
                                     temp = UtilityHelper.txnDateFormat(txnList[i].date)
                                     txnList[i].lytType = 1
                                 }
                             }
                         }
                         adapter.notifyDataSetChanged()
                     }
                     if (txnList.isEmpty()) {
                         binding.txtNoTxn.show()
                         binding.lytSearch.hide()
                         binding.rvHistory.hide()
                     } else {
                         binding.txtNoTxn.hide()
                         binding.lytSearch.show()
                         binding.rvHistory.show()
                     }
                     transactionHistoryViewModel.observerResponse.value = null
                 }
                 is FailResponse -> {
                     hideLoading()
                     showToast(it.message)
                     transactionHistoryViewModel.observerResponse.value = null
                 }
                 is Throwable -> {
                     hideLoading()
                     transactionHistoryViewModel.observerResponse.value = null
                 }
             }
         }
    }

    private fun showFilterPopUpMenu(button: View) {
        val popup = PopupMenu(requireContext(), button)
        popup.menuInflater.inflate(R.menu.filter_pop_up_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.all -> {
                    binding.searchKeyword.setText("")
                    sortBy = ""
                    txnList.clear()
                    adapter.notifyDataSetChanged()
                    showLoading()
                    transactionHistoryViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            offset = limit,
                            sortBy = sortBy
                        )
                    )
                }

                R.id.name -> {
                    binding.searchKeyword.setText("")
                    sortBy = "user_name"
                    seletctedDate = ""
                    txnList.clear()
                    adapter.notifyDataSetChanged()
                    showLoading()
                    transactionHistoryViewModel.transactionHistory(
                        TransactionRequest(
                            pref().getStringValue(SharePreferences.USER_ID, ""),
                            pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                            pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                            frequentPayee = "False",
                            reverse = 1,
                            offset = limit,
                            sortBy = sortBy
                        )
                    )
                }
                R.id.date -> {
                    binding.searchKeyword.setText("")
                    datePickerStart()
                }
            }
            true
        }
        popup.show()
    }

    private fun datePickerStart() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendarStart = Calendar.getInstance()
        val dateCurrent = format.format(calendarStart.time)
        val startYear = dateCurrent.split("-")[0]
        val startMonth = dateCurrent.split("-")[1]
        val startDay = dateCurrent.split("-")[2]
        val dialog = DatePickerFragment(requireActivity(), startDay.toInt(), startMonth.toInt() - 1, startYear.toInt()) { year, month, day ->
            this.startDay = day
            this.startMonth = month
            this.startYear = year
            sortBy = ""
            txnList.clear()
            adapter.notifyDataSetChanged()
            seletctedDate = "$year-$month-$day"
            showLoading()
            transactionHistoryViewModel.transactionHistory(
                TransactionRequest(
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                    frequentPayee = "False",
                    reverse = 1,
                    offset = limit,
                    date = seletctedDate
                )
            )
        }
        dialog.show(requireActivity().supportFragmentManager, "timePicker")
    }

    override fun getLayoutId(): Int = R.layout.transaction_history_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): TransactionHistoryViewModel = transactionHistoryViewModel
}