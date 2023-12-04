package com.example.remendary.usecases.home

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.remendary.EventsFragment
import com.example.remendary.R
import com.example.remendary.databinding.ActivityMainBinding

import com.example.remendary.usecases.home.routine.HomeFragment
import com.example.remendary.usecases.home.task.TasksFragment

class MainActivity : AppCompatActivity() {
    private  lateinit var binding: ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replacefragment(HomeFragment())
        binding.navView.selectedItemId = R.id.homeMenuItem

        binding.navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.tasksMenuItem -> replacefragment(TasksFragment())
                R.id.homeMenuItem -> replacefragment(HomeFragment())
                R.id.eventsMenuItem -> replacefragment(EventsFragment())
                else ->{}
            }
            true
        }
    }

    private fun replacefragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val framentTransaction = fragmentManager.beginTransaction()
        framentTransaction.replace(R.id.frameLyt, fragment)
        framentTransaction.commit()
    }
}