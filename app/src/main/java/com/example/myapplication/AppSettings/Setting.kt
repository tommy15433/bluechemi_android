package com.example.myapplication.AppSettings

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.slider.Slider
object Settings{

    open class SliderSetting(
        val min: Int,
        val max: Int,
        val points: Int){

        fun getValues(): Array<Int>{

            return Array(points){
                it * (max - min) / (points - 1)
            }
        }

        fun getStepSize(): Int{
            return (max - min) / (points)
        }

        fun point2int(_p: Int): Int{
            return getStepSize() * _p
        }

        fun int2point(_i: Int): Int{
            return _i / getStepSize()
        }
    }

    object LedBrightness : SliderSetting(0,255,10){

    }

    object Sensitivity : SliderSetting(8, 255, 3){

    }
}
