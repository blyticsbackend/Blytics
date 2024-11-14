package com.nbt.blytics.modules.acinfo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.AllAcInfoFragmentBinding
import com.nbt.blytics.modules.linkedac.manageac.ManageLinkedFragment
import com.nbt.blytics.modules.payment.manageac.ManageFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.SharePreferences
import com.nbt.blytics.utils.hide
import com.nbt.blytics.utils.setImage
import com.nbt.blytics.utils.show

class AllAcInfoFragment : BaseFragment<AllAcInfoFragmentBinding, AllAcInfoViewModel>() {

    private val allAcInfoViewModel: AllAcInfoViewModel by viewModels()
    private lateinit var binding: AllAcInfoFragmentBinding
    private lateinit var adapter: AcInfoAdapter
    private val acList = mutableListOf<AcInfoResponse.Data>()
    private val limit = "12"
    private var requestCode: Int = -1
    private var selectedAc: String = ""
    private var isShareVisible = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        hideLoading()
        observer()
        happyThemeChanges()
        (requireContext() as? BconfigActivity)?.setToolbarTitle("Select account")
        try {
            isShareVisible =
                requireArguments().getBoolean(AllAcInfoFragment.IS_SAHRE_VISILBE, false)
            if (isShareVisible) {
                (requireContext() as BconfigActivity).setToolbarTitle("Accounts")
            }
        } catch (ex: Exception) {
            isShareVisible = false
        }
        setAdapter()
        try {

            requestCode = requireArguments().getInt(Constants.RESULT_CODE, -1)
            selectedAc = requireArguments().getString(Constants.SELECTED_AC, "")
        } catch (ex: Exception) {
            requestCode = -1
        }

        binding.btnCreateAc.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_AC.name)
            startActivity(intent)
        }

        binding.addAccountButton.setOnClickListener {
            val intent = Intent(requireActivity(), BconfigActivity::class.java)
            intent.putExtra(Constants.COMING_FOR, ComingFor.CREATE_AC.name)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        showLoading()
        allAcInfoViewModel.getAllAc(
            AcInfoRequest(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, "")
            ))
    }

    private fun setAdapter() {
        if (isShareVisible) {
            val swipeGusture = object : SwipeGustureAcInfo(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    when (direction) {
                        ItemTouchHelper.RIGHT -> {
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("saving", true)) {
                                AlertDialog.Builder(requireContext())
                                    //.setTitle("")
                                    .setMessage("Yor can't delete saving a/c").setCancelable(false)
                                    .setPositiveButton("OK",
                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                            adapter.notifyDataSetChanged()
                                        }).create().show()

                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("current", true)) {
                                AlertDialog.Builder(requireContext())
                                    //.setTitle("")
                                    .setMessage("Yor can't delete current a/c").setCancelable(false)
                                    .setPositiveButton("OK",
                                        DialogInterface.OnClickListener { dialogInterface, i ->
                                            adapter.notifyDataSetChanged()
                                        }).create().show()
                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("linked", true)) {
                                val intent = Intent(requireContext(), BconfigActivity::class.java)
                                intent.putExtra(ManageFragment.AC_TYPE, SharePreferences.AcType.LINKED.name)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_LINK_AC.name)
                                intent.putExtra(ManageLinkedFragment.AccId, acList[viewHolder.absoluteAdapterPosition].accid)
                                intent.putExtra(ManageLinkedFragment.UUID, acList[viewHolder.absoluteAdapterPosition].acc_uuid)
                                intent.putExtra(ManageLinkedFragment.COMING_FOR_DELETE, true)
                                intent.putExtra(ManageLinkedFragment.FROM, acList[viewHolder.absoluteAdapterPosition].acc_no)
                                intent.putExtra(ManageLinkedFragment.AMOUNT, acList[viewHolder.absoluteAdapterPosition].amount)
                                for (i in acList.indices) {
                                    if (acList[i].acc_type == "wallet") {
                                        intent.putExtra(ManageLinkedFragment.TO, acList[i].acc_no)
                                    }
                                }
                                startActivity(intent)
                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("wallet", true)) {
                                AlertDialog.Builder(requireContext())
                                    //.setTitle("")
                                    .setMessage("Yor can't delete wallet a/c").setCancelable(false)
                                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i -> adapter.notifyDataSetChanged() }).create().show()
                            }
                        }

                        ItemTouchHelper.LEFT -> {
                            val intent = Intent(requireContext(), BconfigActivity::class.java)
                            intent.putExtra(ManageFragment.UUID, acList[viewHolder.absoluteAdapterPosition].acc_uuid)
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("saving", true)) {
                                intent.putExtra(ManageFragment.AC_TYPE, SharePreferences.AcType.SAVING.name)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_AC.name)
                                startActivity(intent)
                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("current", true)) {
                                intent.putExtra(ManageFragment.AC_TYPE, SharePreferences.AcType.CURRENT.name)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_AC.name)
                                startActivity(intent)
                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("linked", true)) {
                                intent.putExtra(ManageFragment.AC_TYPE, SharePreferences.AcType.LINKED.name)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.MANAGE_LINK_AC.name)
                                intent.putExtra(ManageLinkedFragment.AccId, acList[viewHolder.absoluteAdapterPosition].accid)
                                intent.putExtra(ManageLinkedFragment.UUID, acList[viewHolder.absoluteAdapterPosition].acc_uuid)
                                startActivity(intent)
                                /* AllAccountFragment.SELECTED_LINKED_AC = LinkedAccResponse.AccList(
                                     days = acList[0].acc_holder_name
                                 )*/
                            }
                            if (acList[viewHolder.absoluteAdapterPosition].acc_type.equals("wallet", true)) {
                                AlertDialog.Builder(requireContext())
                                    //.setTitle("")
                                    .setMessage("Yor can't edit wallet a/c").setCancelable(false)
                                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i -> adapter.notifyDataSetChanged() }).create().show()
                            }
                            adapter.notifyDataSetChanged()
                        }
                        ItemTouchHelper.RIGHT -> {
                            adapter.deleteItem(viewHolder.absoluteAdapterPosition)
                            showToast("delete")
                        }
                    }
                }
            }
            val touchHelper = ItemTouchHelper(swipeGusture)
            touchHelper.attachToRecyclerView(binding.rvAllAc)
        }

        adapter = AcInfoAdapter(requireContext(), acList, isShareVisible) {
            val intent = Intent()
            intent.putExtra(SELECTED_AC, acList[it].acc_no)
            intent.putExtra(SELECTED_UUID, acList[it].acc_uuid)
            intent.putExtra(AC_HOLDER, acList[it].acc_holder_name)
            if (requestCode != -1) {
                if (requestCode == 123) {
                    requireActivity().setResult(123, intent)
                    requireActivity().finish()
                } else if (requestCode == 1234) {
                    requireActivity().setResult(1234, intent)
                    requireActivity().finish()
                }
            }
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvAllAc.layoutManager = layoutManager
        binding.rvAllAc.adapter = adapter/* val endlessRecyclerView = object : EndlessRecyclerViewScrollListener(layoutManager) {
             override fun onLoadMore(page: Int, totalItemsCount: Int) {
                 *//* chargesDetailsViewModel.userCharge(
                     UserChargeRequest(
                         pref().getStringValue(SharePreferences.USER_ID, ""),
                         pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                         pref().getStringValue(SharePreferences.USER_WALLET_UUID, ""),
                         transactUserId,
                         page = (page + 1).toString(),
                         offset = limit
                     )
                 )*//*
            }
        }
        binding.rvAllAc.addOnScrollListener(endlessRecyclerView)*/
    }

    @SuppressLint("SetTextI18n")
    private fun observer() {
        allAcInfoViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {
                is AcInfoResponse -> {
                    hideLoading()
                    acList.clear()
                    val data = it.list
                    acList.addAll(data)
                    for (i in data.indices) {
                        if (requestCode != -1) {
                            if (data[i].acc_type.equals("linked", true)) {
                                acList.remove(data[i])
                            }
                        }
                        if (data[i].acc_no == selectedAc) {
                            acList.remove(data[i])
                        }
                    }

                    Log.d("${AllAcInfoFragment.javaClass.name} ==", acList.toString())
                    adapter.notifyDataSetChanged()
                    if (acList.isEmpty()) {
                        binding.txtNoTxn.show()
                        binding.lytNoData.hide()
                        binding.lblTitleCrateAc.text = "Hey'${pref().getStringValue(SharePreferences.USER_FIRST_NAME, "")}',\n\nPlease create an account"
                        binding.imgUserProfile.setImage(pref().getStringValue(SharePreferences.USER_PROFILE_IMAGE, ""))
                    } else {
                        binding.txtNoTxn.hide()
                        binding.lytNoData.hide()
                    }
                    allAcInfoViewModel.observerResponse.value = null
                }

                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    allAcInfoViewModel.observerResponse.value = null
                }

                is Throwable -> {
                    showToast(it.message.toString())
                    hideLoading()
                    allAcInfoViewModel.observerResponse.value = null
                }
            }
        })
    }

    companion object {
        val SELECTED_AC = "selected_ac"
        val SELECTED_UUID = "selected_uuid"
        val IS_SAHRE_VISILBE = "is_share_visible"
        val AC_HOLDER = "ac_holder"
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) { }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_dark)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.black))

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
                binding.card1.setCardBackgroundColor(resources.getColor(R.color.white))
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.all_ac_info_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): AllAcInfoViewModel = allAcInfoViewModel
}