package com.nbt.blytics.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.nbt.blytics.utils.SharePreferences

/**
 * Created bynbton 11-06-2021.
 */
abstract class BaseFragment<T : ViewDataBinding, V : BaseViewModel> :
    androidx.fragment.app.Fragment() {

    private lateinit var mViewDataBinding: T
    private lateinit var mViewModel: V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        mViewModel = getViewModel()
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel)
        mViewDataBinding.executePendingBindings()
        setFragmentTitle()
        return mViewDataBinding.root
    }


    /**
     * @return layout resource id
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract fun getBindingVariable(): Int


    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract fun getViewModel(): V

    /**
     * Override for set title in fragment
     */
    open fun setFragmentTitle() {
        //Empty override method
    }


    /**
     * Method for get View data binding
     */
    fun getViewDataBinding(): T {
        return mViewDataBinding
    }

    fun getToolbar(): ViewDataBinding? {
        return when (activity) {

            //     is MainActivity -> (activity as MainActivity).getToolbar()
            else -> null
        }
    }



    fun showToast(msg: String) {
        (activity as BaseActivity).showToast(msg)
    }

    fun showLoading() {
        (activity as BaseActivity).showLoading()
    }

    fun hideLoading() {
        (activity as BaseActivity).hideLoading()
    }

    fun showSnack(view: View, msg: String) {
        (activity as BaseActivity).showSnack(view, msg)
    }

    fun pref():SharePreferences =(activity as BaseActivity).pref


    fun hideSoftKeyBoard(){
        val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun showSoftKeyBoard(view:View){
        val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(view.windowToken, 0, 0)
        view.clearFocus()
    }

}
