package com.minimalkbd

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class MinimalImeService : InputMethodService() {
    private val bg = Color.rgb(21, 23, 28)
    private val key = Color.rgb(51, 56, 66)
    private val special = Color.rgb(68, 75, 88)
    private val keyTextColor = Color.WHITE
    private var shifted = false
    private var symbols = false
    private var emojiMode = false
    private var spaceDownX = 0f
    private var cursorRemainder = 0f
    private var spaceGestureMoved = false

    override fun onCreateInputView(): View = buildKeyboard()

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        emojiMode = false
        shifted = attribute?.inputType?.let { it and android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0 } ?: false
    }

    private fun buildKeyboard(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(7), dp(4), dp(7))
            setBackgroundColor(bg)
        }
        if (emojiMode) {
            root.addView(buildEmojiPanel(), LinearLayout.LayoutParams(-1, dp(230)))
            root.addView(bottomRow())
            return root
        }
        val rows = if (!symbols) listOf("qwertyuiop", "asdfghjkl", "⇧zxcvbnm⌫") else listOf("1234567890", "@#$%&-*+(", "=/:;!?')⌫")
        rows.forEachIndexed { index, row ->
            val rowLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { char ->
                val label = when (char) {
                    '⇧' -> "⇧"
                    '⌫' -> "⌫"
                    else -> if (shifted && !symbols) char.uppercaseChar().toString() else char.toString()
                }
                val v = makeKey(label, if (char == '⇧' || char == '⌫') special else key)
                val weight = if (char == '⇧' || char == '⌫') 1.35f else 1f
                rowLayout.addView(v, LinearLayout.LayoutParams(0, dp(48), weight).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                })
                v.setOnClickListener { handleKey(char) }
            }
            root.addView(rowLayout)
        }
        root.addView(bottomRow())
        return root
    }

    private fun bottomRow(): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun add(label: String, weight: Float, color: Int = special, action: () -> Unit): TextView {
            val v = makeKey(label, color)
            row.addView(v, LinearLayout.LayoutParams(0, dp(48), weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            })
            v.setOnClickListener { action() }
            return v
        }
        add(if (symbols) "ABC" else "?123", 1.25f) {
            symbols = !symbols; emojiMode = false; setInputView(buildKeyboard())
        }
        add("☺", 1f) {
            emojiMode = !emojiMode; setInputView(buildKeyboard())
        }
        val space = add("English (US)", 4.6f, key) { currentInputConnection?.commitText(" ", 1) }
        space.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { spaceDownX = event.rawX; cursorRemainder = 0f; spaceGestureMoved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - spaceDownX
                    val total = cursorRemainder + dx
                    val steps = (total / dp(18).toFloat()).toInt()
                    if (steps != 0) {
                        spaceGestureMoved = true
                        moveCursor(steps)
                        cursorRemainder = total - steps * dp(18)
                    }
                    spaceDownX = event.rawX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!spaceGestureMoved) currentInputConnection?.commitText(" ", 1)
                    true
                }
                else -> true
            }
        }
        add("↵", 1.25f) { sendEnter() }
        return row
    }

    private fun buildEmojiPanel(): View {
        val emojis = listOf("😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😍","🥰","😘","😎","🤔","😭","😡","👍","👎","👏","🙏","❤️","🔥","🎉","💯","✨","🌟","☀️","🌙","🍎","🍕","⚽","🚗","🐱","🐶","🌹","☕")
        val grid = GridLayout(this).apply { columnCount = 8; setBackgroundColor(bg) }
        emojis.forEach { emoji ->
            val v = TextView(this).apply {
                text = emoji; textSize = 25f; gravity = Gravity.CENTER
                setOnClickListener { currentInputConnection?.commitText(emoji, 1) }
            }
            grid.addView(v, ViewGroup.LayoutParams(dp(44), dp(45)))
        }
        return grid
    }

    private fun handleKey(c: Char) {
        val ic = currentInputConnection ?: return
        when (c) {
            '⇧' -> { shifted = !shifted; setInputView(buildKeyboard()) }
            '⌫' -> {
                val selected = ic.getSelectedText(0)
                if (!selected.isNullOrEmpty()) ic.commitText("", 1) else ic.deleteSurroundingText(1, 0)
            }
            else -> {
                val out = if (shifted && !symbols) c.uppercaseChar().toString() else c.toString()
                ic.commitText(out, 1)
                if (shifted) { shifted = false; setInputView(buildKeyboard()) }
            }
        }
    }

    private fun moveCursor(steps: Int) {
        val ic = currentInputConnection ?: return
        val code = if (steps < 0) android.view.KeyEvent.KEYCODE_DPAD_LEFT else android.view.KeyEvent.KEYCODE_DPAD_RIGHT
        repeat(abs(steps).coerceAtMost(8)) {
            ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, code))
            ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, code))
        }
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) ic.performEditorAction(action)
        else ic.commitText("\n", 1)
    }

    private fun makeKey(label: String, color: Int): TextView = TextView(this).apply {
        text = label; textSize = if (label.length > 4) 13f else 20f
        setTextColor(keyTextColor); gravity = Gravity.CENTER; typeface = Typeface.DEFAULT
        background = GradientDrawable().apply { setColor(color); cornerRadius = dp(6).toFloat() }
        isClickable = true; isFocusable = true
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
