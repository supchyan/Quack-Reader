package com.example.arifuretareader

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object CoolToast {

    lateinit var toastContainer: LinearLayout

    // generates toast message on call
    fun showToast(context: Context,
                  scope: CoroutineScope,
                  icon: Int,
                  text: String,
                  lifeTime: Long = 3000) {

        val layoutInflater = LayoutInflater.from(toastContainer.context)

        val toastView = layoutInflater.inflate(R.layout.duck_toast, toastContainer, false)
        val toastTextId = toastView.findViewById<TextView>(R.id._toast_text)
        val toastPicId = toastView.findViewById<ImageView>(R.id._toast_pic)

        toastView.animation = AnimationUtils.loadAnimation(context, R.anim.toast_appearing)

        try {
            toastContainer.addView(toastView)
        } catch (e: Exception) {
            toastContainer.removeView(toastView)
            toastContainer.addView(toastView)
        }

        toastPicId.setImageResource(icon)
        try {
            val animatable = toastPicId.drawable as Animatable
            animatable.start()
        } catch (_: Exception) {

        }
        toastTextId.text = text

        val lifeTimeToast = scope.launch {
            delay(lifeTime)
            toastView.animate().alpha(0f).setDuration(220).start()
        }

        val removeToast = scope.launch {
            while (true) {
                if(toastView.alpha == 0f) {
                    toastContainer.removeView(toastView)
                }
                delay(1)
            }
        }
    }
    //
}