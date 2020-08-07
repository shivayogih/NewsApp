package com.springr.newsapplication.util


import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

import com.google.android.material.snackbar.Snackbar
import com.springr.newsapplication.R


object AppAlerts {

    fun showSnackbar(context: Activity, message: String) {
        val snackbar = Snackbar.make(
            context.findViewById(android.R.id.content), message,
            Snackbar.LENGTH_LONG
        )
        val view = snackbar.view
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = Gravity.BOTTOM
        view.layoutParams = layoutParams
        view.setBackgroundColor(context.resources.getColor(R.color.primary_text_color))
        val textView = view.findViewById<TextView>(R.id.snackbar_text)
        textView.setTextColor(context.resources.getColor(R.color.white))
        snackbar.show()
    }
}
