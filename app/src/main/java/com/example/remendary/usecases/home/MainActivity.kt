package com.example.remendary.usecases.home

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.remendary.EventsFragment
import com.example.remendary.R
import com.example.remendary.databinding.ActivityMainBinding

import com.example.remendary.usecases.home.routine.HomeFragment
import com.example.remendary.usecases.home.task.TasksFragment
import com.example.remendary.usecases.login.CreateAccountActivity
import com.example.remendary.util.Extensions.toast
import com.example.remendary.util.FirebaseUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnLogOut: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replacefragment(HomeFragment())
        binding.navView.selectedItemId = R.id.homeMenuItem

        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tasksMenuItem -> replacefragment(TasksFragment())
                R.id.homeMenuItem -> replacefragment(HomeFragment())
                R.id.eventsMenuItem -> replacefragment(EventsFragment())
                else -> {}
            }
            true
        }

        btnLogOut = findViewById(R.id.btnLogOut)
        btnLogOut.setOnClickListener {
            FirebaseUtils.firebaseAuth.signOut()
            startActivity(Intent(this, CreateAccountActivity::class.java))
            toast("signed out")
            finish()
        }
    }

    private fun replacefragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val framentTransaction = fragmentManager.beginTransaction()
        framentTransaction.replace(R.id.frameLyt, fragment)
        framentTransaction.commit()
    }
}