package com.nbt.blytics.modules.menu

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.nbt.blytics.R
import com.nbt.blytics.activity.bconfig.BconfigActivity
import com.nbt.blytics.activity.main.MainActivity
import com.nbt.blytics.activity.main.MainActivity.Companion.appsMenuItems
import com.nbt.blytics.activity.main.MainActivity.Companion.menuItems
import com.nbt.blytics.activity.main.models.MenuItem
import com.nbt.blytics.base.BaseFragment
import com.nbt.blytics.common.ComingFor
import com.nbt.blytics.databinding.MenuFragmentBinding
import com.nbt.blytics.modules.acinfo.AllAcInfoFragment
import com.nbt.blytics.modules.menu.adapter.MenuAdapter
import com.nbt.blytics.modules.payee.payment.PaymentHistoryAdapter
import com.nbt.blytics.utils.Constants
import com.nbt.blytics.utils.RecyclerTouchListener

class MenuFragment : BaseFragment<MenuFragmentBinding, MenuViewModel>() {
    private val menuViewModel: MenuViewModel by viewModels()
    private lateinit var binding: MenuFragmentBinding
    private lateinit var adapter: MenuAdapter
    var touchHelper: ItemTouchHelper? = null
    /* val menuList = mutableListOf<MenuItem>()*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = getViewDataBinding()
        happyThemeChanges()
        setAdapter()
        binding.product.setOnClickListener {
            binding.menuRecycler.visibility = View.VISIBLE
            binding.comingSoonItem.visibility = View.GONE
        }
        binding.comingSoon.setOnClickListener {
            binding.menuRecycler.visibility = View.GONE
            binding.comingSoonItem.visibility = View.VISIBLE
        }
        binding.imageView5.setOnClickListener {
            Toast.makeText(requireContext(), "Coming Soon 😁", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAdapter() {
        adapter = MenuAdapter(requireActivity(), appsMenuItems, { pos, view -> }) {
            (requireActivity() as MainActivity).setMenuItems()
        }
        val layoutManager = GridLayoutManager(requireContext(), 4)
        binding.menuRecycler.layoutManager = layoutManager
        binding.menuRecycler.adapter = adapter
        binding.menuRecycler.addOnItemTouchListener(
            RecyclerTouchListener(requireContext(),
                binding.menuRecycler,
                object : PaymentHistoryAdapter.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        when (appsMenuItems[position].name) {
                            MenuItem.Name.ACCOUNT -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.ACCOUNT_DETAILS.name)
                                intent.putExtra(AllAcInfoFragment.IS_SAHRE_VISILBE, true)
                                startActivity(intent)
                            }
                            MenuItem.Name.LONE -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.LONE_HOME.name)
                                startActivity(intent)
                            }
                            MenuItem.Name.PAYEE -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.PAYEE_HOME.name)
                                startActivity(intent)
                            }
                            MenuItem.Name.TRANSACTION -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.TRANSACTION_HOME.name)
                                startActivity(intent)
                            }
                            MenuItem.Name.CARD -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.CARD_HOME.name)
                                startActivity(intent)
                            }
                            MenuItem.Name.APPS -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.APPS_HOME.name)
                                startActivity(intent)
                            }
                            MenuItem.Name.SETTING -> {
                                val intent = Intent(requireActivity(), BconfigActivity::class.java)
                                intent.putExtra(Constants.COMING_FOR, ComingFor.SETTING_HOME.name)
                                startActivity(intent)
                            }
                            else -> {
                                Log.e("MenuFragment", "Unknown menu item: ${appsMenuItems[position].name}")
                            }
                        }
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onLongClick(view: View?, position: Int) {
                        view?.let {
                            popMenu(it, position)
                        }
                    }
                })
        )
        /*  val callback: ItemTouchHelper.Callback = ItemMoveCallback(adapter)
          touchHelper = ItemTouchHelper(callback)
          touchHelper!!.attachToRecyclerView(binding.menuRecycler)*/
    }

    private fun popMenu(view: View, index: Int) {
        val popupMenu: PopupMenu = PopupMenu(requireContext(), view)
        popupMenu.menu.add("Replace With").setEnabled(false)
        popupMenu.menu.add(menuItems[0].label)
        popupMenu.menu.add(menuItems[0].label)
        popupMenu.menu.add(menuItems[0].label)
        happyThemeChanges(popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            for (i in 0 until popupMenu.menu.size()) {
                if (item == popupMenu.menu.getItem(i)) {
                    for (n in menuItems.indices) {
                        if (item.title == menuItems[n].label) {
                            val maintemp = menuItems[n]
                            val listTemp = appsMenuItems[index]
                            appsMenuItems.removeAt(index)
                            menuItems.removeAt(n)
                            menuItems.add(n, listTemp)
                            appsMenuItems.add(index, maintemp)
                            adapter.notifyDataSetChanged()
                            (requireActivity() as MainActivity).setMenuItems()
                            break
                        }
                    }
                }
            }
            true
        })
        popupMenu.show()
    }

    private fun happyThemeChanges(menu: Menu) {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                val s = SpannableString(menu.getItem(0).getTitle().toString())
                s.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.orange_dark)), 0, s.length, 0)
                menu.getItem(0).setTitle(s)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                val s = SpannableString(menu.getItem(0).getTitle().toString())
                s.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue_700)), 0, s.length, 0)
                menu.getItem(0).setTitle(s)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                val s = SpannableString(menu.getItem(0).getTitle().toString())
                s.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue_700)), 0, s.length, 0)
                menu.getItem(0).setTitle(s)
            }
        }
    }

    private fun happyThemeChanges() {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                binding.mainLayout.setBackgroundResource(R.color.b_bg_color_light)
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.menu_fragment
    override fun getBindingVariable(): Int = 0
    override fun getViewModel(): MenuViewModel = menuViewModel
}