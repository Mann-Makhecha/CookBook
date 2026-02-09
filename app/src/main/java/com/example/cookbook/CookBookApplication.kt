package com.example.cookbook

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.initialize

class CookBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        try {
            Firebase.initialize(this)
            Log.d("CookBookApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("CookBookApp", "Firebase initialization failed", e)
        }
    }
}
