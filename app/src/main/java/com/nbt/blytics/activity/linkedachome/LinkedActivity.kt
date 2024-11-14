package com.nbt.blytics.activity.linkedachome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.nbt.blytics.R
import com.nbt.blytics.databinding.ActivityLinkedBinding

class LinkedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinkedBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_linked)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_linked)

       /* navHostFragment =
            supportFragmentManager.findFragmentById(R.id.linked_fragments_container) as NavHostFragment
        navController = navHostFragment.navController
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.linked_nav)*/

        setSupportActionBar(binding.toolbarTop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return  true
    }
}