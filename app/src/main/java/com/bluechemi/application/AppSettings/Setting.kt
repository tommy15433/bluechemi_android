package com.bluechemi.application.AppSettings

import kotlin.math.abs

object Settings{

    var APP_UUID = ""
    val BITE_DETECTION_INTERVAL_MAX_MS: Long = 2000

    var deviceHashMap:HashMap<Any, HashMap<String, Any>> = hashMapOf()
    var diaryHashMap: HashMap<Any, HashMap<String, Any>> = hashMapOf()


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

    object Sensitivity : SliderSetting(0, 100 ,50, 100){

    }
}
