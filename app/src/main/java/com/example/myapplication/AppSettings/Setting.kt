package com.example.myapplication.AppSettings

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.slider.Slider
import kotlin.math.abs

object Settings{

    var APP_UUID = ""

    open class SliderSetting(
        val min: Int,
        val max: Int,
        val def: Int,
        val points: Int){

        fun getValues(): Array<Int>{

            return Array(points){
                it * (max - min) / (points - 1)
            }
        }

        open fun getStepSize(): Int{
            return (max - min) / (points)
        }

        open fun point2int(_p: Int): Int{
            return getStepSize() * _p + min
        }

        open fun int2point(_i: Int): Int{
            return (_i - min) / getStepSize()
        }
    }

    object LedBrightness : SliderSetting(0,255, 128,255){
        override fun point2int(_p: Int): Int {
            return abs(max - super.point2int(_p))
        }

        override fun int2point(_i: Int): Int {
            return abs(points - super.int2point(_i))
        }
    }

    object Sensitivity : SliderSetting(8, 255 - 8,128, 255-16){

    }
}
