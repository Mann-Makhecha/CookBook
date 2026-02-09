package com.example.cookbook

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class CookBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        Firebase.initialize(this)
    }
}
