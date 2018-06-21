package com.glassdoor.sample

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.glassdoor.prefextensions.annotations.Preference
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("com.glassdoor.app", Context.MODE_PRIVATE)
        bindText()

        save.setOnClickListener {
            val value = editor.text.toString()
            preferences.edit().putMessage(value).run {
                bindText()
            }
        }
    }

    fun bindText() {
        text.text = preferences.getMessage()
    }
}