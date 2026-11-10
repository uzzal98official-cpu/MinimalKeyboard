package com.minimalkbd

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(0xFF101114.toInt())
        }
        val title = TextView(this).apply {
            text = "Minimal Keyboard"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val description = TextView(this).apply {
            text = "A simple English keyboard. No suggestions, no predictions, no network access and no analytics. Swipe across the spacebar to move the cursor."
            textSize = 15f
            setTextColor(0xFFB8BBC4.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 26)
        }
        val enable = Button(this).apply {
            text = "1. Enable Minimal Keyboard"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val choose = Button(this).apply {
            text = "2. Select Minimal Keyboard"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        val privacy = TextView(this).apply {
            text = "Privacy: this prototype requests no internet permission and does not collect or transmit typing data."
            textSize = 13f
            setTextColor(0xFF8E929C.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 0)
        }
        root.addView(title)
        root.addView(description)
        root.addView(enable)
        root.addView(choose)
        root.addView(privacy)
        setContentView(root)
    }
}
