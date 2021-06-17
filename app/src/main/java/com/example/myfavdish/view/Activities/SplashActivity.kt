package com.example.myfavdish.view.Activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.myfavdish.R
import com.example.myfavdish.databinding.ActivitySplashScreenBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashBinding: ActivitySplashScreenBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(WindowInsets.Type.statusBars())
        }else{
           @Suppress("DEPRECATION")
           window.setFlags(
               WindowManager.LayoutParams.FLAG_FULLSCREEN,
               WindowManager.LayoutParams.FLAG_FULLSCREEN
           )
        }

        val splashAnimation = AnimationUtils.loadAnimation(this,
            R.anim.anim_splash
        )
        splashBinding.tvAppName.animation = splashAnimation
        //splashBinding.tvAppName.text = "hello world"

        splashAnimation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
              Handler(Looper.getMainLooper()).postDelayed({
                  startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                  finish()
              }, 1000)
            }

            override fun onAnimationStart(animation: Animation?) {
               //
            }

        })

    }
}