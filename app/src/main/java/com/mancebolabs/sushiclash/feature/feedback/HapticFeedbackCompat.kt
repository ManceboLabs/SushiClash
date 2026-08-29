package com.mancebolabs.sushiclash.feature.feedback

import android.annotation.SuppressLint
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

internal object HapticFeedbackCompat {

    fun performConfirm(view: View) {
        view.performHapticFeedback(confirmConstant())
    }

    fun performReject(view: View) {
        view.performHapticFeedback(rejectConstant())
    }

    @SuppressLint("InlinedApi")
    fun confirmConstant(sdkInt: Int = Build.VERSION.SDK_INT): Int {
        return if (sdkInt >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
    }

    @SuppressLint("InlinedApi")
    fun rejectConstant(sdkInt: Int = Build.VERSION.SDK_INT): Int {
        return if (sdkInt >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
    }
}
