package com.nbt.blytics.activity

import android.os.Bundle
import com.nbt.blytics.R
import com.nbt.blytics.base.BaseActivity

class PhoneLoginActivity : BaseActivity() {
    // lateinit var mBinding: PhoneLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)
        // mBinding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login)
    }
}