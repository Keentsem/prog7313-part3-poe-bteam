package com.example.pocketsafe.ui

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.example.pocketsafe.R

class ProgressBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var animationDrawable: AnimationDrawable? = null

    init {
        setBackgroundResource(R.drawable.progress_animation)
        post {
            animationDrawable = background as? AnimationDrawable
            animationDrawable?.start()
        }
    }

    fun setProgressLevel(level: Int) {
        animationDrawable?.selectDrawable(level)
    }
} 