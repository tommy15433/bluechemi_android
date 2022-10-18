package com.example.myapplication

object ElapseTimer {
    private var mstart: Long = 0
    private var mcur: Long = 0
    fun start(){
        mstart = System.currentTimeMillis()
    }
    fun hasElapsed(ms: Long): Boolean{
        return (System.currentTimeMillis() - mstart) > ms
    }

}