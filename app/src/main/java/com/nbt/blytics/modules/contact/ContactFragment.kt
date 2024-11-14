package com.nbt.blytics.modules.contact

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.CheckFor
import com.nbt.blytics.database.BlyticsDatabase
import com.nbt.blytics.database.Contact
import com.nbt.blytics.databinding.ContactFragmentBinding
import com.nbt.blytics.modules.contact.adapter.ContactAdapter
import com.nbt.blytics.modules.contact.model.ContactData
import com.nbt.blytics.modules.payamount.PayAmountFragment
import com.nbt.blytics.modules.paynow.PayNowFragment
import com.nbt.blytics.modules.phoneregistation.models.CheckExistPhoneResponse
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.modules.transactionhistory.TransactionResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.retrieveAllContacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactFragment : BaseFragment<ContactFragmentBinding, ContactViewModel>() {

    private val contactViewModel: ContactViewModel by viewModels()
    private lateinit var binding: ContactFragmentBinding
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var db: BlyticsDatabase
    private var payMode: String? = ""
    private var contactList: MutableList<ContactData> = mutableListOf()
    private var filteredContactList: MutableList<ContactData> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        db = (requireActivity() as BaseActivity).db
        hideLoading()
        showSoftKeyBoard(binding.edtSearch)
        payMode = requireArguments().getString(Constants.PAY_MODE)
        binding.edtSearch.doOnTextChanged { text, start, count, after ->
            filterContacts()
        }

        binding.refreshContact.setOnRefreshListener {
            refreshContacts()
        }

        observer()
        setAdapter()
        checkPermissions()
        loadContacts()
    }
    private fun loadContacts() {
        contactList.clear()
        showLoading()
        GlobalScope.launch {
            val contactsFromDb = db.getContactDao().getAllContact()
            contactList.addAll(contactsFromDb.map {
                ContactData(it.contactId, it.name, mutableListOf(it.phoneNumber), null)
            })

            withContext(Dispatchers.Main) {
                hideLoading()
                filterContacts()
            }
        }
        contactAdapter.notifyDataSetChanged()
    }

    @SuppressLint("MissingPermission")
    private fun refreshContacts() {
        showLoading()
        GlobalScope.launch {
            db.getContactDao().clearAllContacts()
            contactList.clear()
            val newContacts = requireContext().retrieveAllContacts("", true).toMutableList()
            contactList.addAll(newContacts)
            val contactDbList = newContacts.map {
                Contact(it.contactId, it.name, it.phoneNumber.firstOrNull() ?: "")
            }.toMutableList()
            db.getContactDao().insertAllContact(contactDbList)
            withContext(Dispatchers.Main) {
                hideLoading()
                filterContacts()
                binding.refreshContact.isRefreshing = false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterContacts() {
        val query = binding.edtSearch.text.toString().trim().lowercase()
        filteredContactList.clear()

        contactList.forEach { contact ->
            if (contact.name.lowercase().contains(query) ||
                contact.phoneNumber.any { it.lowercase().contains(query) }) {
                filteredContactList.add(contact)
            }
        }
        contactAdapter.notifyDataSetChanged()
    }

    @SuppressLint("MissingPermission")
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }
            if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                Log.d("TAG", "Permission granted")
                refreshContacts()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_CONTACTS)) {
                checkPermissions()
            } else {
                val snackbar = Snackbar.make(binding.lytMain, resources.getString(R.string.message_no_contact_required), Snackbar.LENGTH_INDEFINITE)
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

    private fun formatPhoneNumber(phoneNumber: String, countryCode: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val phoneNumberProto = phoneUtil.parse(phoneNumber, countryCode)
            return phoneUtil.format(phoneNumberProto, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: Exception) {
            return phoneNumber
        }
    }

    private fun checkPermissions() {
        if (requireContext().let {
            ContextCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS)
            } != PackageManager.PERMISSION_GRANTED) {
            requestMultiplePermissions.launch(
                arrayOf(Manifest.permission.READ_CONTACTS))
        } else {
            loadContacts()
            Log.d("TAG", "Permission Already Granted")
        }
    }

    private fun setAdapter() {
        contactAdapter = ContactAdapter(requireContext(), filteredContactList) { pos ->
            if (filteredContactList[pos].phoneNumber.isNotEmpty()) {
                showLoading()
                val selectedContact = filteredContactList[pos]
                val formattedNumber = formatPhoneNumber(selectedContact.phoneNumber[0], "IN")
                contactViewModel.checkExist2(formattedNumber.replace("[\\s()]+", "").replace(" ", ""), CheckFor.FULL_DETAILS)
            } else {
                showToast("Number format error")
            }
        }

        binding.rvContact.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContact.adapter = contactAdapter
    }

    private fun observer() {
        contactViewModel.observerResponse.observe(viewLifecycleOwner) {
            when (it) {
                is CheckExistPhoneResponse -> {
                    hideLoading()
                    if (it.status.equals(Constants.Status.SUCCESS.name, true)) {
                        it.data?.apply {
                            binding.edtSearch.setText("")
                            if (payMode.equals(Constants.PayType.SCHEDULE.name, true)) {
                                PayNowFragment.SELECTED_TXN_PAYEES.add(TransactionResponse.Data.Txn("", "", "", "", "", "", "", "", "", "", mobNo, false, "", "", "", ""))
                                val intent = Intent()
                                intent.putExtra("name", userName)
                                intent.putExtra("mobileNo", mobNo)
                                requireActivity().setResult(1234, intent)
                                requireActivity().finish()
                            } else {
                                findNavController().navigate(R.id.action_contactFragment_to_payAmountFragment, bundleOf(PayAmountFragment.IS_MULTI_PAY to "no", PayAmountFragment.RECEIVER_NAME to userName, PayAmountFragment.RECEIVER_ID to userId, PayAmountFragment.RECEIVER_CONTACT to mobNo, PayAmountFragment.RECEIVER_IMG to avatarUrl, Constants.PAY_MODE to payMode))
                            }
                        }

                    } else {
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                        dialog.apply {
                            setTitle("Alert!")
                            setMessage(it.message)
                            setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
                        }
                        dialog.show()
                    }
                    contactViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    contactViewModel.observerResponse.value = null
                }
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.contact_fragment

    override fun getBindingVariable(): Int = 0

    override fun getViewModel(): ContactViewModel = contactViewModel
}