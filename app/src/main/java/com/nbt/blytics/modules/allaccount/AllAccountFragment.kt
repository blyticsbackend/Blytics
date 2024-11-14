package com.nbt.blytics.modules.allaccount

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.base.BaseActivity
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.FragmentAllAccountBinding
import com.nbt.blytics.modules.home.HomeFragment
import com.nbt.blytics.modules.signin.model.FailResponse
import com.nbt.blytics.utils.*


class AllAccountFragment : BaseFragment<FragmentAllAccountBinding, AllAccountViewModel>() {
    private lateinit var binding: FragmentAllAccountBinding
    private val linkedAccList: MutableList<LinkedAccResponse.AccList> = mutableListOf()
    private lateinit var lindedAdapter: LinkedAccountAdapter
    private lateinit var adapter: AllAccountAdapter

    companion object {
         var SELECTED_LINKED_AC: LinkedAccResponse.AccList? =null
         var LINKED_AC_ADDRESS: LinkedAccResponse.Address? =null

    }

  /*  private val allAccountViewModel: AllAccountViewModel by lazy {
        ViewModelProvider(this).get(AllAccountViewModel::class.java)
    }*/
    private val allAccountViewModel: AllAccountViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()

        happyThemeChanges()
        hideLoading()
        observer()
        LINKED_AC_ADDRESS = null
        val acType = requireArguments().getString(Constants.ACC_TYPE)
        allAccountRecycler(acType!!)
        setLinkedAccAdapter()
        HomeFragment.IS_SWITCH_SHOWN = true
        if (acType.equals(Constants.CURRENT_ACC)) {
            binding.lblTitle.text = "Current Accounts"
            (requireContext() as BconfigActivity).setToolbarTitle("Current Accounts")
            allAccountViewModel.getAllAccount(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""), "current"
            )
        } else if (acType.equals(Constants.SAVING_ACC)) {
            binding.lblTitle.text = "Saving Accounts"
            (requireContext() as BconfigActivity).setToolbarTitle("Saving Accounts")
            showLoading()
            allAccountViewModel.getAllAccount(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""), "saving"
           )
        } else if (acType.equals(Constants.LINKED_ACC)) {
            binding.lblTitle.text = "Linked Accounts"
            (requireContext() as BconfigActivity).setToolbarTitle("Linked Accounts")

            showLoading()
            allAccountViewModel.getAllAccount(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""), "linked"
            )

        }

        binding.btnCreateAc.setOnClickListener {
            if (acType.equals(Constants.CURRENT_ACC)) {
                val bundle = bundleOf(
                    Constants.COMING_FOR to ComingFor.HOME.name,
                    Constants.ACC_TYPE to Constants.CURRENT_ACC
                )
                findNavController().navigate(R.id.addAccountFragment, bundle)

            } else if (acType.equals(Constants.SAVING_ACC)) {
                val bundle = bundleOf(
                    Constants.COMING_FOR to ComingFor.HOME.name,
                    Constants.ACC_TYPE to Constants.SAVING_ACC
                )
                findNavController().navigate(R.id.addAccountFragment, bundle)
            } else if (acType.equals((Constants.LINKED_ACC))) {


                findNavController().navigate(
                    R.id.phoneRegistrationFragment3,
                    bundleOf(
                        Constants.PHONE_NUMBER to "",
                        Constants.COMING_FOR to ComingFor.LINK_AC_PHONE_VERIFY.name,
                        Constants.USER_ID to "", Constants.USER_WALLET_UUID to ""
                    )
                )
            }

        }
        binding.btnAddImage.setOnClickListener {
            if (acType.equals(Constants.CURRENT_ACC)) {
                val bundle = bundleOf(
                    Constants.COMING_FOR to ComingFor.HOME.name,
                    Constants.ACC_TYPE to Constants.CURRENT_ACC
                )
                findNavController().navigate(R.id.addAccountFragment, bundle)
            } else if (acType.equals(Constants.SAVING_ACC)) {
                val bundle = bundleOf(
                    Constants.COMING_FOR to ComingFor.HOME.name,
                    Constants.ACC_TYPE to Constants.SAVING_ACC
                )
                findNavController().navigate(R.id.addAccountFragment, bundle)
            } else if (acType.equals(Constants.LINKED_ACC)) {
                findNavController().navigate(
                    R.id.phoneRegistrationFragment3,
                    bundleOf(
                        Constants.PHONE_NUMBER to "",
                        Constants.COMING_FOR to ComingFor.LINK_AC_PHONE_VERIFY.name,
                        Constants.USER_ID to "", Constants.USER_WALLET_UUID to ""
                    )
                )
            }

        }


        /* mBinding.card.setOnClickListener {
             SharePreferences.getInstance(requireContext())
                 .setStringValue(SharePreferences.DEFAULT_ACCOUNT, selectedLinkAc.accNo)
             SharePreferences.getInstance(requireContext())
                 .setStringValue(SharePreferences.DEFAULT_PURPOSE, "")
             SharePreferences.setStringValue(
                 SharePreferences.USER_LINKED_UUID,
                 selectedLinkAc.accUuid
             )
             SharePreferences.setStringValue(SharePreferences.DEFAULT_AC_TYPE, SharePreferences.AcType.LINKED.name)
             startActivity(Intent(requireContext(),LinkedActivity::class.java))
         }
         mBinding.cardEnd.setOnClickListener {
             SharePreferences.getInstance(requireContext())
                 .setStringValue(SharePreferences.DEFAULT_ACCOUNT, selectedLinkAc.accNo)
             SharePreferences.getInstance(requireContext())
                 .setStringValue(SharePreferences.DEFAULT_PURPOSE, "")
             SharePreferences.setStringValue(
                 SharePreferences.USER_LINKED_UUID,
                 selectedLinkAc.accUuid
             )
             pref().setStringValue(SharePreferences.DEFAULT_AC_TYPE, SharePreferences.AcType.LINKED.name)
             startActivity(Intent(requireContext(), LinkedActivity::class.java))
         }*/


        /* activity?.onBackPressedDispatcher?.addCallback(
             requireActivity(),
             object : OnBackPressedCallback(true) {
                 override fun handleOnBackPressed() {

                     if (acType.equals(Constants.LINKED_ACC)) {
                         if (binding.viewFlipper.displayedChild > 0) {
                             backView()
                         } else {
                             remove()
                             requireActivity().onBackPressed()
                         }

                     } else {
                         remove()
                         requireActivity().onBackPressed()
                     }
                 }
             })*/


    }


    private fun observer() {
        allAccountViewModel.observerResponse.observe(viewLifecycleOwner, Observer {
            when (it) {

                is LinkedAccResponse -> {
                    hideLoading()
                    allAccountViewModel.observerResponse.value = null
                    LINKED_AC_ADDRESS = it.data.address
                    if (it.data.acc_list.isEmpty()) {
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()
                        binding.imgUserProfile.show()
                        binding.btnCreateAc.show()
                        binding.lblTitleCrateAc.show()
                        binding.lblTitleCrateAc.text = "Hey,'${
                            pref().getStringValue(
                                SharePreferences.USER_FIRST_NAME,
                                ""
                            )
                        }',\nPlease create an account"
                        binding.imgUserProfile.setImage(
                            pref().getStringValue(
                                SharePreferences.USER_PROFILE_IMAGE,
                                ""
                            )
                        )
                    } else {


                        linkedAccList.clear()
                        linkedAccList.addAll(it.data.acc_list)
                        lindedAdapter.notifyDataSetChanged()
                        binding.imgUserProfile.hide()
                        binding.lblTitleCrateAc.hide()
                        binding.btnCreateAc.hide()
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()
                    }
                }
                is CurrentAccountModel -> {
                    allAccountViewModel.observerResponse.value = null
                    hideLoading()
                    adapter.notifyDataSetChanged()
                    if (it.data.isEmpty()) {
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()
                        binding.imgUserProfile.show()
                        binding.btnCreateAc.show()
                        binding.lblTitleCrateAc.show()
                        binding.lblTitleCrateAc.text = "Hey,'${
                            pref().getStringValue(
                                SharePreferences.USER_FIRST_NAME,
                                ""
                            )
                        }',\nPlease create an account"
                        binding.imgUserProfile.setImage(
                            pref().getStringValue(
                                SharePreferences.USER_PROFILE_IMAGE,
                                ""
                            )
                        )
                    } else {
                        binding.imgUserProfile.hide()
                        binding.lblTitleCrateAc.hide()
                        binding.btnCreateAc.hide()
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()
                    }
                }
                is AllAccountModel -> {
                    allAccountViewModel.observerResponse.value = null
                    hideLoading()
                    val acType = requireArguments().getString(Constants.ACC_TYPE)
                    allAccountRecycler(acType!!)
                    if (it.data.isEmpty()) {
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()
                        binding.imgUserProfile.show()
                        binding.btnCreateAc.show()
                        binding.lblTitleCrateAc.show()
                        binding.lblTitleCrateAc.text = "Hey,'${
                            pref().getStringValue(
                                SharePreferences.USER_FIRST_NAME,
                                ""
                            )
                        }',\nPlease create an account"
                        binding.imgUserProfile.setImage(
                            pref().getStringValue(
                                SharePreferences.USER_PROFILE_IMAGE,
                                ""
                            )
                        )
                    } else {

                        binding.imgUserProfile.hide()
                        binding.lblTitleCrateAc.hide()
                        binding.btnCreateAc.hide()
                        binding.lblTitle.hide()
                        binding.btnAddImage.hide()


                    }
                }
                is GetUpdateDefaultAcc -> {
                    hideLoading()
                    allAccountViewModel.observerResponse.value = null
                   // showToast(it.message)
                    requireActivity().finish()
                    startActivity(Intent(requireContext(), MainActivity::class.java))

                }
                /* is GetUpdateDefaultAcc -> {
                     hideLoading()
                     allAccountViewModel.observerResponse.value = null

                     showToast(it.message)
                     findNavController().navigate(R.id.action_allAccountFragment_to_homeFragment)

                 }*/
                is FailResponse -> {
                    hideLoading()
                    showToast(it.message)
                    allAccountViewModel.observerResponse.value = null
                }
                is Throwable -> {
                    allAccountViewModel.observerResponse.value = null
                    showToast(it.message.toString())
                    hideLoading()
                }
            }
        })
    }


    private fun setLinkedAccAdapter() {
        lindedAdapter = LinkedAccountAdapter(requireContext(), linkedAccList) { it, view ->
            HomeFragment.IS_SWITCH_SHOWN = true
            val data = linkedAccList[it]
            SELECTED_LINKED_AC= null
            SELECTED_LINKED_AC = data
            //HomeFragment.SELECTED_AC_TYPE = "Linked Account"
            findNavController().navigate(R.id.action_allAccountFragment_to_linkedAcHomeFragment3)


        }
        binding.allAccountRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allAccountRecycler.adapter = lindedAdapter
    }


    private fun allAccountRecycler(acType: String) {
        adapter =  AllAccountAdapter(
            requireContext(), allAccountViewModel.allAccountList, acType
        ) { acc, purpose, acc_uid ->
            HomeFragment.IS_SWITCH_SHOWN = true

            SharePreferences.getInstance(requireContext())
                .setStringValue(SharePreferences.DEFAULT_ACCOUNT, acc)
            SharePreferences.getInstance(requireContext())
                .setStringValue(SharePreferences.DEFAULT_PURPOSE, purpose)
            SharePreferences.setStringValue(SharePreferences.USER_WALLET_UUID, acc_uid)
            if (acType.equals(Constants.CURRENT_ACC)) {
                HomeFragment.SELECTED_AC_TYPE = "Current Account"
                SharePreferences.setStringValue(
                    SharePreferences.DEFAULT_AC_TYPE,
                    SharePreferences.AcType.CURRENT.name
                )
            } else if (acType.equals(Constants.SAVING_ACC)) {
                HomeFragment.SELECTED_AC_TYPE = "Saving Account"
                SharePreferences.setStringValue(
                    SharePreferences.DEFAULT_AC_TYPE,
                    SharePreferences.AcType.SAVING.name
                )
            }


            requireActivity().finish()
            startActivity(Intent(requireContext(), MainActivity::class.java))
           /* showLoading()
            allAccountViewModel.updateAccount(
                pref().getStringValue(SharePreferences.USER_ID, ""),
                pref().getStringValue(SharePreferences.USER_TOKEN, ""),
                acc_uid,
                acType
            )*/
        }
        binding.allAccountRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.allAccountRecycler.adapter = adapter
    }

    private fun happyThemeChanges() {
        if (BaseActivity.isCustomMode) {

        }
        when (requireContext().resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_orange_btn)
                binding.btnAddImage.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orange_dark
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_dark)



            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnAddImage.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.btnCreateAc.setBackgroundResource(R.drawable.bg_gradient_light_btn)
                binding.btnAddImage.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.topLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
        }


    }




    override fun getLayoutId(): Int = R.layout.fragment_all_account
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): AllAccountViewModel = allAccountViewModel
}