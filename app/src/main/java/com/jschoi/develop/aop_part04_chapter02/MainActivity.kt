package com.jschoi.develop.aop_part04_chapter02

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple Music Player App
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PlayerFragment.newInstance())
                .commit()
    }
}