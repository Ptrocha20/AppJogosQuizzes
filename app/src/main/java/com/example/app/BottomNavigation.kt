package com.example.app

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavigation(private val activity: Activity) {


    fun setupBottomNavigation() {
        val bottomNavigationView = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (activity !is PaginaInicialActivity) {
                        val intent = Intent(activity, PaginaInicialActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (activity !is ContaActivity) {
                        val intent = Intent(activity, ContaActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_settings -> {
                    if (activity !is DefinicoesActivity) {
                        val intent = Intent(activity, DefinicoesActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        activity.startActivity(intent)
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }
}


