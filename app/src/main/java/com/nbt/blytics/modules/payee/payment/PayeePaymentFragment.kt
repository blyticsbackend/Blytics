package com.nbt.blytics.modules.payee.payment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.LogoutRequest
import com.nbt.blytics.activity.main.UnReadNotification
import com.nbt.blytics.activity.qrcode.QrCodeActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.callback.notificationCallBack
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.database.BlyticsDatabase
import com.nbt.blytics.database.Contact
import com.nbt.blytics.databinding.PayeePaymentFragmentBinding
import com.nbt.blytics.modules.actobank.ActoBankFragment
import com.nbt.blytics.modules.contact.adapter.ContactAdapter
import com.nbt.blytics.modules.contact.model.ContactData
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.singletransaction.SingleTransactionFragment
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.*
import kotlinx.coroutines.*


class PayeePaymentFragment : BaseFragment<PayeePaymentFragmentBinding, PayeePaymentViewModel>() {
    private val TAG = "Contact Permission=="
    private val limit = "5"
    private lateinit var binding: PayeePaymentFragmentBinding
    private val payeePaymentViewModel: PayeePaymentViewModel by viewModels()
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var adapter: PaymentHistoryAdapter
    private var filerContactList: MutableList<ContactData> = mutableListOf()
    private var contactList: MutableList<ContactData> = mutableListOf()
    private lateinit var db: BlyticsDatabase
    private var showRecent = true
    private var isPaginationActive = false
    private val txnList = mutableListOf<TransactionResponse.Data.Txn>()
    private val reqList = mutableListOf<RequestMoneyResponse.Data>()
    private lateinit var reqAdapter: RequestMoneyAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        db = (requireActivity() as BaseActivity).db

        setAdapter()
        setReqAdater()
        setContactAdapter()
        observer()
        checkPermissions()
        happyThemeChanges()


        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (binding.toggleButton.checkedButtonId) {
                    R.id.btn_recent -> {

                        showRecent = true
                        getTxnList()

                    }
                    R.id.btn_a_z -> {
                        showRecent = false
                        getTxnList()

                    }
                }
            }
        }


        binding.edtSearch.doOnTextChanged { text, start, count, after ->
            if (binding.edtSearch.text.toString().isNotBlank()) {
                binding.cardMainContact.show()
                binding.rvContact.show()
                binding.rvPayment.hide()
                binding.txtSeeAll.hide()
                filter()
                binding.cardMain.hide()
                binding.cardMain2.hide()
                binding.txtRequest.hide()
                binding.txtRecent.hide()
                binding.txtNoTxn.hide()
                binding.txtNoReq.hide()
            } else {
                binding.txtSeeAll.hide()
                binding.cardMainContact.hide()
                binding.rvContact.hide()
                binding.rvPayment.show()
                binding.cardMain.show()
                binding.cardMain2.show()
                binding.txtRecent.show()
                binding.rvRequest.show()
                binding.txtRequest.show()
                binding.txtNoReq.show()
                binding.txtNoTxn.show()
                if(reqList.isNotEmpty()){
                    binding.txtNoReq.hide()
                }
                if(txnList.isNotEmpty()){
                    binding.txtNoTxn.hide()
                }
            }
        }
        binding.txtSeeAll.setOnClickListener {

            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_NOW.name)
            intent.putExtra(Constants.PAY_MODE, Constants.PayType.RECENT_PAYEE.name)
            startActivity(intent)
        }



        binding.imgScanner.setOnClickListener {
            startActivity(Intent(requireContext(), QrCodeActivity::class.java))
        }

        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (binding.edtSearch.text.toString().isNotBlank()) {
                    payeePaymentViewModel.checkExist(
                        binding.edtSearch.text.toString().trim()
                    )
                }
                true
            } else false
        }


        notificationCallBack.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    getTxnList()
                    notificationCallBack.value = false
                }false -> {
                // Handle false case (if necessary)
            }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getTxnList()


    }

    private fun showApprovalDialog(str:String){
        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.apply {
           // setTitle("Logout")
            setMessage(str)

            setCancelable(false)
            setPositiveButton(
                "Approve"
            ) { dialog, which ->
                dialog.dismiss()
                showLoading()

                /*mainViewModel.logOut(
                    LogoutRequest(
                        pref.getStringValue(SharePreferences.USER_ID, ""),
                        pref.getStringValue(SharePreferences.USER_TOKEN, ""),
                    )
                )*/
            }
            setNegativeButton(
                "Decline"
            ) { dialog, which ->
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun getRequestList() {
        reqList.clear()
        reqAdapter.notifyDataSetChanged()
        // showLoading()
        payeePaymentViewModel.allRequest(
            RequestMoneyReq(
                userId = pref().getStringValue(SharePreferences.USER_ID, "-1").toInt(),
                userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
            )
        )
    }

    private fun getTxnList() {


        showLoading()
        isPaginationActive = false
        payeePaymentViewModel.transactionHistory(
            TnxListRequest(
                frequentPayee = "True",
                page = 1,
                offset = limit,
                userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                sortBy = if (showRecent) "" else "user_name"
            )
        )
    }



    private fun observer() {
        payeePaymentViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is RequestMoneyResponse -> {
                    hideLoading()
                    reqList.clear()
                    reqList.addAll(it.data)
                    reqAdapter.notifyDataSetChanged()
                    if (reqList.isEmpty()) {
                        if(binding.rvContact.isVisible.not()) {
                            binding.txtNoReq.show()
                        }else{
                            binding.txtNoReq.hide()
                        }

                    } else {
                        binding.txtNoReq.hide()

                    }

                    payeePaymentViewModel.observerResponse.value = null
                }
                is TransactionResponse -> {
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        txnList.clear()
                        txnList.addAll(it.data!!.txnList)
                        adapter.notifyDataSetChanged()
                        if (txnList.isEmpty()) {
                            binding.txtNoTxn.show()

                        } else {
                            binding.txtNoTxn.hide()
                        }
                    }
                    getRequestList()
                    payeePaymentViewModel.observerResponse.value = null
                }
                is CheckExistPhoneResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            binding.edtSearch.setText("")
                            val intent = Intent(requireActivity(), BconfigActivity::class.java)
                            intent.apply {
                                putExtra(PayAmountFragment.IS_MULTI_PAY, "no")
                                putExtra(PayAmountFragment.RECEIVER_NAME, userName)
                                putExtra(PayAmountFragment.RECEIVER_ID, userId)
                                putExtra(PayAmountFragment.RECEIVER_CONTACT, mobNo)
                                putExtra(PayAmountFragment.RECEIVER_IMG, avatarUrl)
                                putExtra(Constants.PAY_MODE, Constants.PayType.BOTH.name)
                            }
                            intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_AMOUNT.name)
                            startActivity(intent)

                            /* findNavController().navigate(
                                 R.id.action_contactFragment_to_payAmountFragment, bundleOf(
                                     PayAmountFragment.IS_MULTI_PAY to "no",
                                     PayAmountFragment.RECEIVER_NAME to userName,
                                     PayAmountFragment.RECEIVER_ID to userId,
                                     PayAmountFragment.RECEIVER_CONTACT to mobNo,
                                     PayAmountFragment.RECEIVER_IMG to avatarUrl
                                 )
                             )*/

                        }

                    } else {
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                        dialog.apply {
                            setTitle("Alert!")
                            setMessage(it.message)
                            setNegativeButton(
                                "Cancel"
                            ) { dialog, which ->
                                dialog.dismiss()
                            }
                        }
                        dialog.show()
                    }

                    payeePaymentViewModel.observerResponse.value = null
                }
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    payeePaymentViewModel.observerResponse.value = null

                }
            }
        }
    }

    private fun setReqAdater() {
        reqAdapter = RequestMoneyAdapter(requireContext(), reqList) { pos ->
            val data = reqList[pos]
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.apply {
                putExtra(PayAmountFragment.IS_MULTI_PAY, "no")
                putExtra(PayAmountFragment.RECEIVER_NAME, data.requestByUserName)
                putExtra(PayAmountFragment.RECEIVER_ID, data.requestedByUserId.toString())
                putExtra(PayAmountFragment.RECEIVER_CONTACT, data.requestMobNo)
                putExtra(PayAmountFragment.RECEIVER_IMG, data.imageUrl)
                putExtra(PayAmountFragment.REQUEST_ID, data.requestId)
                putExtra(PayAmountFragment.REQUEST_MONEY, data.requestedAmount)
                putExtra(Constants.PAY_MODE, Constants.PayType.REQUESTER_SEND_MONEY.name)
            }
            intent.putExtra(Constants.COMING_FOR, ComingFor.PAY_AMOUNT.name)
            startActivity(intent)
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRequest.layoutManager = layoutManager

        binding.rvRequest.adapter = reqAdapter
    }

    private fun setAdapter() {
        adapter = PaymentHistoryAdapter(requireContext(), txnList) { pos ->

            val data = txnList[pos]
            if (data.bankType.equals("Internal", true)) {
                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(
                    SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER,
                    data.amount
                )
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(
                    SingleTransactionFragment.TRANSACTION_USER_ID,
                    if (data.bankType.equals("External", true)) data.txnId else data.userId
                )
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
                //adapter.notifyDataSetChanged()
            } else {
       /*         val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.AC_TO_BANK.name)
                intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                intent.putExtra(ActoBankFragment.RECEIVER_NAME, data.userName)
                intent.putExtra(ActoBankFragment.RECEIVER_AC_NUMBER, data.bank_acc)
                intent.putExtra(ActoBankFragment.BANK_CODE, data.bank_code)
                intent.putExtra(ActoBankFragment.AMOUNT, data.amount)
                startActivity(intent)*/

                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_DETAILS.name)
                intent.putExtra(SingleTransactionFragment.NAME, data.userName)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_SENT, data.amount)
                intent.putExtra(SingleTransactionFragment.TOTAL_AMOUNT_RECEIVER, data.amount)
                intent.putExtra(SingleTransactionFragment.USER_IMG_URL, data.userImage)
                intent.putExtra(SingleTransactionFragment.TRANSACTION_USER_ID, data.bank_acc)
                intent.putExtra(SingleTransactionFragment.BANK_TYPE, data.bankType)
                startActivity(intent)
                //adapter.notifyDataSetChanged()
            }
        }
        val layoutManager =  LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//GridLayoutManager(requireContext(), 4)
        binding.rvPayment.layoutManager = layoutManager
        binding.rvPayment.adapter = adapter


/*        val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                isPaginationActive = true
                payeePaymentViewModel.transactionHistory(
                    TnxListRequest(
                        frequentPayee = "True",
                        page = page + 1,
                        offset = limit,
                        userId = pref().getStringValue(SharePreferences.USER_ID, ""),
                        userToken = pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                        uuid = pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                        sortBy = if (showRecent) "" else "user_name"

                    )
                )

            }
        }
        binding.rvPayment.addOnScrollListener(endlessRecyclerView)*/


    }
    private fun setContactAdapter(){
        contactAdapter =
            ContactAdapter(requireContext(), filerContactList) { pos ->

                if (filerContactList[pos].phoneNumber.isNotEmpty()) {
                    showLoading()
                    payeePaymentViewModel.checkExist(

                        filerContactList[pos].phoneNumber[0].replace(
                            "[\\s()]+",
                            ""
                        ).replace(" ", "")/*.replace("+91", "")*/
                    )
                } else {
                    showToast("number format error")
                }


            }

        binding.rvContact.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvContact.adapter = contactAdapter
    }


    @SuppressLint("MissingPermission")
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d(TAG, "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                Log.d(TAG, "Permission granted")
                getContact()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                checkPermissions()
            } else {
                val snackbar = Snackbar.make(
                    binding.lytMain,
                    resources.getString(R.string.message_no_contact_required),
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction(resources.getString(R.string.settings), View.OnClickListener {
                    if (activity == null) {
                        return@OnClickListener
                    }
                    requireActivity().finish()
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    requireActivity().startActivity(intent)
                })
                snackbar.show()
            }
        }

    @DelicateCoroutinesApi
    @SuppressLint("MissingPermission")
    private fun getContact() {

        // showLoading()
        GlobalScope.launch {
            val contactListDb = db.getContactDao().getAllContact()
            contactList.clear()
            filerContactList.clear()

            if (contactListDb.isNotEmpty()) {
                Log.d(TAG, "DB===" + contactListDb.toString())

                for (i in contactListDb.indices) {
                    val phoneList = mutableListOf<String>()
                    phoneList.add(contactListDb[i].phoneNumber)
                    val contactData = ContactData(
                        contactListDb[i].contactId,
                        contactListDb[i].name,
                        phoneList,
                        null
                    )
                    contactList.add(contactData)
                    filerContactList.add(contactData)
                }

            } else {
                contactList = requireContext().retrieveAllContacts(
                    "", true
                ) as MutableList<ContactData>
                filerContactList.addAll(contactList)

                val contactDbList = mutableListOf<Contact>()
                for (i in contactList.indices) {

                    val contact = Contact(
                        contactList[i].contactId,
                        contactList[i].name,
                        if (contactList[i].phoneNumber.isNotEmpty()) {
                            contactList[i].phoneNumber[0]
                        } else ""
                    )
                    contactDbList.add(contact)

                }
                db.getContactDao().insertAllContact(contactDbList)
            }
            withContext(Dispatchers.Main) {
                //   hideLoading()
                contactAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun checkPermissions() {
        /*  if (requireContext().let {
                  ContextCompat.checkSelfPermission(
                      it,
                      Manifest.permission.READ_CONTACTS
                  )
              } != PackageManager.PERMISSION_GRANTED) {
              Log.d(TAG, "Request Permissions")
              requestMultiplePermissions.launch(
                  arrayOf(
                      Manifest.permission.READ_CONTACTS
                  )
              )
          } else {*/
        if (requireContext().let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CONTACTS
                )
            } != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS
                )
            )
        } else {
            getContact()

            Log.d(TAG, "Permission Already Granted")
        }

    }


    private fun filter() {
        filerContactList.clear()
        for (i in contactList.indices) {
            if (contactList[i].toString()
                    .contains(binding.edtSearch.text.toString(), true)
            ) {
                filerContactList.add(contactList[i])
            }
        }

        contactAdapter.notifyDataSetChanged()
        //  hideSoftKeyBoard()
    }
    private fun happyThemeChanges() {
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cardMainContact.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cardMain2.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.cardSearch.setCardBackgroundColor(resources.getColor(R.color.black))
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardMainContact.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardMain2.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardSearch.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.cardMain.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardMainContact.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardMain2.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.cardSearch.setCardBackgroundColor(resources.getColor(R.color.white))
                binding.lytMain.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }


    companion object {
        fun newInstance(): PayeePaymentFragment {
            val frg = PayeePaymentFragment()
            return frg
        }

    }

    override fun getLayoutId(): Int = R.layout.payee_payment_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): PayeePaymentViewModel = payeePaymentViewModel

}