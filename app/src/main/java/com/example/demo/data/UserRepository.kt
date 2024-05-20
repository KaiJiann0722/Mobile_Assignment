package com.example.demo.data

import android.content.SharedPreferences

class UserRepository(private val sharedPreferences: SharedPreferences) {

    fun getUserId(): String? {
        return sharedPreferences.getString("userId", null)
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("userId", userId).apply()
    }
}