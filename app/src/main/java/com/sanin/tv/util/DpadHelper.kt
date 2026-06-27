package com.sanin.tv.util

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object DpadHelper {

    /**
     * Enables full D-pad / remote-control navigation on any RecyclerView.
     *
     * - Sets FOCUS_AFTER_DESCENDANTS so the RV passes focus to its children.
     * - Makes every child view focusable when it attaches to the window.
     * - Adds a key listener that handles:
     *     UP/DOWN on vertical lists, LEFT/RIGHT on horizontal lists (smooth scroll + focus move)
     *     DPAD_CENTER / ENTER → performClick on the focused child
     *
     * Works with both LinearLayoutManager (horizontal & vertical) and GridLayoutManager.
     */
    fun RecyclerView.enableDpadNavigation() {
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        isFocusableInTouchMode = false   // touch should not steal focus from soft keyboard etc.

        // Make each child focusable as soon as it attaches
        addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.isFocusable = true
                view.isFocusableInTouchMode = false
            }
            override fun onChildViewDetachedFromWindow(view: View) {}
        })

        setOnKeyListener { _, keyCode, event ->
            if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false

            val lm = layoutManager ?: return@setOnKeyListener false

            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (lm is LinearLayoutManager && lm.orientation == LinearLayoutManager.VERTICAL) {
                        val first = lm.findFirstVisibleItemPosition()
                        if (first > 0) {
                            smoothScrollToPosition(first - 1)
                            postDelayed({ getChildAt(0)?.requestFocus() }, 80)
                        }
                        true
                    } else false
                }

                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (lm is LinearLayoutManager && lm.orientation == LinearLayoutManager.VERTICAL) {
                        val last = lm.findLastVisibleItemPosition()
                        val total = adapter?.itemCount ?: 0
                        if (last < total - 1) {
                            smoothScrollToPosition(last + 1)
                        }
                        true
                    } else false
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    val isHorizontal = (lm is LinearLayoutManager &&
                            lm.orientation == LinearLayoutManager.HORIZONTAL)
                    if (isHorizontal) {
                        val first = (lm as LinearLayoutManager).findFirstVisibleItemPosition()
                        if (first > 0) {
                            smoothScrollToPosition(first - 1)
                            postDelayed({
                                val child = findViewHolderForAdapterPosition(first - 1)?.itemView
                                child?.requestFocus()
                            }, 80)
                        }
                        true
                    } else false
                }

                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    val isHorizontal = (lm is LinearLayoutManager &&
                            lm.orientation == LinearLayoutManager.HORIZONTAL)
                    if (isHorizontal) {
                        val last = (lm as LinearLayoutManager).findLastVisibleItemPosition()
                        val total = adapter?.itemCount ?: 0
                        if (last < total - 1) {
                            smoothScrollToPosition(last + 1)
                            postDelayed({
                                val child = findViewHolderForAdapterPosition(last + 1)?.itemView
                                child?.requestFocus()
                            }, 80)
                        }
                        true
                    } else false
                }

                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    val focused = focusedChild
                    focused?.performClick()
                    focused != null
                }

                else -> false
            }
        }
    }

    /** Make one or more arbitrary views focusable via D-pad (not touch-mode focus). */
    fun makeFocusable(vararg views: View) {
        for (v in views) {
            v.isFocusable = true
            v.isFocusableInTouchMode = false
        }
    }

    /** Returns true if [keyCode] is any D-pad directional or centre key. */
    fun isDpadKey(keyCode: Int): Boolean = keyCode in listOf(
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_CENTER
    )

    /**
     * Handles left/right D-pad events to cycle between tabs.
     * Typically called from an Activity's [dispatchKeyEvent].
     */
    fun handleTabDpad(
        keyCode: Int,
        event: KeyEvent,
        currentTab: Int,
        tabCount: Int,
        selectTab: (Int) -> Unit
    ): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (currentTab > 0) { selectTab(currentTab - 1); true } else false
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (currentTab < tabCount - 1) { selectTab(currentTab + 1); true } else false
            }
            else -> false
        }
    }
}
