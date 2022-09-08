package com.example.myapplication.weatherApi

import android.telephony.CarrierConfigManager


class GPS(var lat: Double = 0.0, var lon: Double = 0.0)
class GRID(var x: Double = 0.0, var y: Double = 0.0){

    override fun equals(other: Any?): Boolean {
        if (other is GRID){
            return (other.x == this.x)
                    && (other.y == this.y)
        }else{
            return false
        }
    }
}

object GpsGridConverter {


    fun toGps(grid: GRID): GPS{

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var gps = GPS()

        val xn = grid.x - XO
        val yn = ro - grid.y + YO
        var ra = Math.sqrt(xn * xn + yn * yn)
        if (sn < 0.0) {
            ra = -ra
        }
        var alat = Math.pow(re * sf / ra, 1.0 / sn)
        alat = 2.0 * Math.atan(alat) - Math.PI * 0.5
        var theta = 0.0
        if (Math.abs(xn) <= 0.0) {
            theta = 0.0
        } else {
            if (Math.abs(yn) <= 0.0) {
                theta = Math.PI * 0.5
                if (xn < 0.0) {
                    theta = -theta
                }
            } else theta = Math.atan2(xn, yn)
        }
        val alon = theta / sn + olon

        gps.lat = alat * RADDEG
        gps.lon = alon * RADDEG

        return gps
    }

    fun toGrid(gps: GPS): GRID{

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var grid = GRID()

        var ra = Math.tan(Math.PI * 0.25 + gps.lat * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = gps.lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * java.lang.Math.PI
        if (theta < -Math.PI) theta += 2.0 * java.lang.Math.PI
        theta *= sn

        grid.x = Math.floor(ra * Math.sin(theta) + XO + 0.5)
        grid.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5)

        return grid
    }


    private val RE = 6371.00877 // 지구 반경(km)
    private val GRID = 5.0 // 격자 간격(km)
    private val SLAT1 = 30.0 // 투영 위도1(degree)
    private val SLAT2 = 60.0 // 투영 위도2(degree)
    private val OLON = 126.0 // 기준점 경도(degree)
    private val OLAT = 38.0 // 기준점 위도(degree)
    private val XO = 43.0 // 기준점 X좌표(GRID)
    private val YO = 136.0 // 기1준점 Y좌표(GRID)

    //
    // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
    //
    private val DEGRAD = Math.PI / 180.0
    private val RADDEG = 180.0 / Math.PI
    private val re = RE / GRID
    private val slat1 = SLAT1 * DEGRAD
    private val slat2 = SLAT2 * DEGRAD
    private val olon = OLON * DEGRAD
    private val olat = OLAT * DEGRAD
}
