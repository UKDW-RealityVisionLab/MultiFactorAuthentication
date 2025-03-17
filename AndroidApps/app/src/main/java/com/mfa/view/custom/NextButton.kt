package com.mfa.view.custom

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.mfa.R

class NextButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    init {
        // Setup button appearance
        text = "Lanjut"
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        setTextColor(ContextCompat.getColor(context, R.color.green_primary))
        cornerRadius = resources.getDimensionPixelSize(R.dimen.button_corner_radius)
        setPadding(
            resources.getDimensionPixelSize(R.dimen.button_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.button_padding_vertical),
            resources.getDimensionPixelSize(R.dimen.button_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.button_padding_vertical)
        )
    }
}