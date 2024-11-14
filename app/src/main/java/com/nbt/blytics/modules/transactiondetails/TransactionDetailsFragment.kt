package com.nbt.blytics.modules.transactiondetails

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseBottomSheetFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.BottomSheetDatePickerBinding
import com.nbt.blytics.databinding.TrasactionDetailsFragmentBinding
import com.nbt.blytics.modules.actobank.ActoBankFragment
import com.nbt.blytics.modules.payment.adapter.TransactionAdapter
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.singletransaction.SingleTransactionFragment
import com.nbt.blytics.modules.transactiondetails.adapter.TransactionDetailAdapter
import com.nbt.blytics.modules.transactionhistory.TransactionRequest
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*
import java.text.SimpleDateFormat
import java.util.*


class TransactionDetailsFragment :
    BaseBottomSheetFragment<TrasactionDetailsFragmentBinding, TransactionDetailsViewModel>() {

    private val transactionDetailsViewModel: TransactionDetailsViewModel by viewModels()
    private lateinit var binding: TrasactionDetailsFragmentBinding

    // lateinit var bottomDrawerDialog: BottomDrawerDialog
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private lateinit var adapter: TransactionDetailAdapter
    private var limit: String = "12"
    private var bindingDatePicker: BottomSheetDatePickerBinding? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var startDay = -1
    private var startMonth = -1
    private var startYear = -1
    private var sortBy = ""
    private var seletctedDate =""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

        binding.progess.show()
        setAdapter()
        observer()

        val offsetFromTop = 50
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            isFitToContents = false
            expandedOffset = offsetFromTop
            state = BottomSheetBehavior.STATE_EXPANDED
            setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        dismiss()
                    }
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }


            })

        }

        binding.imgFilterList.setOnClickListener {
            showFilterPopUpMenu(binding.imgFilterList)
        }
        /*  binding.btnDownTransaction.setOnClickListener {
              bottomDrawerDialog.dismiss()
          }*/
        happyThemeChanges()
        binding.searchClear.setOnClickListener {
            binding.searchKeyword.setText("")
            sortBy = ""
            txnList.clear()
            adapter.notifyDataSetChanged()
            showLoading()
            transactionDetailsViewModel.transactionHistory(
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
                    transactionDetailsViewModel.transactionHistory(
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
        transactionDetailsViewModel.transactionHistory(
            TransactionRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                frequentPayee = "False",
                reverse = 1,
                /* bank = "Internal",*/
                offset = limit
            )
        )

    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {
            binding.apply {
                topLayout.setBgColorHappyTheme()
                btnDownTransaction.setImageResource(R.drawable.img_yellow_down)
            }
        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnDownTransaction.setImageResource(R.drawable.img_down_transaction_ornage)
                if (bottomSheetDialog != null)
                    bindingDatePicker!!.btnSearch.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnDownTransaction.setImageResource(R.drawable.img_down_transaction)
                if (bottomSheetDialog != null)
                    bindingDatePicker!!.btnSearch.setBackgroundResource(R.drawable.bg_gradient_light_btn)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                if (bottomSheetDialog != null)
                    bindingDatePicker!!.btnSearch.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnDownTransaction.setImageResource(R.drawable.img_down_transaction)
            }
        }
    }

    private fun observer() {
        transactionDetailsViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is TransactionResponse -> {
                    hideLoading()
                    binding.progess.hide()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.addAll(it.data!!.txnList)
                        var temp =""
                        for(i in txnList.indices){
                            if(i==0){
                                temp =UtilityHelper.txnDateFormat(txnList[i].date)
                                txnList[i].lytType =1
                            }else{
                                if(temp.equals(UtilityHelper.txnDateFormat(txnList[i].date) ,true)){
                                    txnList[i].lytType =0
                                }else{
                                    temp = UtilityHelper.txnDateFormat(txnList[i].date)
                                    txnList[i].lytType =1
                                }
                            }
                        }
                        adapter.notifyDataSetChanged()
                        if (txnList.isEmpty()) {
                            binding.txtNoTxn.show()
                        } else {
                            binding.txtNoTxn.hide()
                        }
                    }
                    transactionDetailsViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    binding.progess.hide()
                    showToast(it.message)
                    transactionDetailsViewModel.observerResponse.value = null

                }
                is Throwable ->{
                    hideLoading()
                    transactionDetailsViewModel.observerResponse.value = null
                }
            }
        }
    }


    /*  override fun getTheme(): Int {
          return R.style.CustomBottomSheetDialog
      }*/
    /*  override fun configureBottomDrawer(): BottomDrawerDialog {
          bottomDrawerDialog = BottomDrawerDialog.build(requireContext()) {
              theme = R.style.Plain
              handleView = PullHandleView(context).apply {
                  val widthHandle =
                      resources.getDimensionPixelSize(R.dimen.bottom_sheet_handle_width)
                  val heightHandle =
                      resources.getDimensionPixelSize(R.dimen.bottom_sheet_handle_height)
                  val params =
                      FrameLayout.LayoutParams(widthHandle, heightHandle, Gravity.CENTER_HORIZONTAL)
                  params.topMargin =
                      resources.getDimensionPixelSize(R.dimen.bottom_sheet_handle_top_margin)
                  layoutParams = params
              }
          }
          return bottomDrawerDialog
      }*/

    private fun setAdapter() {
        adapter = TransactionDetailAdapter(requireContext(), txnList) {
            txnList[it].let { data ->
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
                   /* val intent = Intent(requireActivity(), BconfigActivity::class.java)
                    intent.putExtra(Constants.COMING_FOR, ComingFor.AC_TO_BANK.name)
                    intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                    intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                    intent.putExtra(ActoBankFragment.RECEIVER_AC_NUMBER, data.bank_acc)
                    intent.putExtra(ActoBankFragment.BANK_CODE, data.bank_code)
                    intent.putExtra(ActoBankFragment.AMOUNT, data.amount)
                    startActivity(intent)*/
                }
            }
        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvTransaction.layoutManager = layoutManager

        binding.rvTransaction.adapter = adapter

        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                transactionDetailsViewModel.transactionHistory(
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
        binding.rvTransaction.addOnScrollListener(endlessRecyclerView)
        val metrics = requireContext().resources.displayMetrics
        binding.rvTransaction.layoutParams.height = metrics.heightPixels
    }

    companion object {
        fun newInstance(): TransactionDetailsFragment {
            return TransactionDetailsFragment()
        }
    }


    private fun showFilterPopUpMenu(button: View) {
        val popup = PopupMenu(requireContext(), button)
        popup.menuInflater.inflate(R.menu.filter_pop_up_menu, popup.menu)


        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.all ->{
                    binding.searchKeyword.setText("")
                    sortBy = ""
                    txnList.clear()
                    adapter.notifyDataSetChanged()
                    showLoading()
                    transactionDetailsViewModel.transactionHistory(
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
                    seletctedDate =""
                    txnList.clear()
                    adapter.notifyDataSetChanged()
                    showLoading()
                    transactionDetailsViewModel.transactionHistory(
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
        popup.show() //showing popup menu

    }


    private fun datePickerStart() {


        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)


        val calendarStart = Calendar.getInstance()
        val dateCurrent = format.format(calendarStart.time)
        val  startYear= dateCurrent.split("-")[0]
        val  startMonth= dateCurrent.split("-")[1]
        val  startDay= dateCurrent.split("-")[2]
        val dialog = DatePickerFragment(requireActivity(), startDay.toInt(), startMonth.toInt()-1, startYear.toInt()) { year, month, day ->

            this.startDay = day
            this.startMonth = month
            this.startYear = year

            sortBy = ""
            txnList.clear()
            adapter.notifyDataSetChanged()
            seletctedDate ="$year-$month-$day"
            showLoading()
            transactionDetailsViewModel.transactionHistory(
                TransactionRequest(
                    pref().getStringValue(SharePreferences.USER_ID, ""),
                    pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                    pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                    frequentPayee = "False",
                    reverse = 1,
                    offset = limit,
                    date =seletctedDate
                )
            )
        }
        dialog.show(requireActivity().supportFragmentManager, "timePicker")
    }

/*    private fun datePickerEnd() {

        val dialog = DatePickerFragment(
            requireActivity(), 0, 0, 0,
            startDay, startMonth, startYear
        ) { year, month, day ->
            bindingDatePicker!!.edtEndd.setText("$year-$month-$day")
        }
        dialog.show(requireActivity().supportFragmentManager, "timePicker")
    }*/

  /*  private fun showBottomDialog() {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = BottomSheetDialog(requireContext())
        }
        bottomSheetDialog?.apply {

            bindingDatePicker = DataBindingUtil.inflate<BottomSheetDatePickerBinding>(
                layoutInflater,
                R.layout.bottom_sheet_date_picker,
                null,
                false
            ).apply {
                setContentView(root)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnEndd.setOnClickListener {
                    datePickerEnd()
                }
                btnstartd.setOnClickListener {

                }

                btnSearch.setOnClickListener {

                }

                bottomSheetDialog?.setOnDismissListener {

                }


            }
            setCancelable(true)


        }
        if (bottomSheetDialog != null)
            if (bottomSheetDialog?.isShowing!!.not()) {
                bottomSheetDialog?.show()
            }

        happyThemeChanges()
    }*/



    override fun getLayoutId(): Int = R.layout.trasaction_details_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): TransactionDetailsViewModel = transactionDetailsViewModel


}