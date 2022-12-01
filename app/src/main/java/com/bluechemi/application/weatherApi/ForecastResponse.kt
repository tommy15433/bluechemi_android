package com.bluechemi.application.weatherApi

import com.google.gson.annotations.SerializedName

// 단기 예보조회, 1시간 단위로 업데이트
// 1시간 지남 && 그리드 위치 변함 시 업데이트

class ForecastResponse {
    @SerializedName("response") var response: Response = Response()

    enum class CATEGORY{
        RAIN_PROC { override fun toString(): String = "POP" },
        RAIN_TYPE { override fun toString(): String = "PTY" },
        RAIN_PERHOUR { override fun toString(): String = "PTY" },
        HUMIDITY { override fun toString(): String = "REH" },
        SNOW_PERHOUR { override fun toString(): String = "SNO" },
        SKY { override fun toString(): String = "SKY" },
        TEMP_LASTHOUR { override fun toString(): String = "TMP" },
        TEMP_MAX { override fun toString(): String = "TMX" },
        TEMP_MIN { override fun toString(): String = "TMN" },
        WIND_EW { override fun toString(): String = "UUU" },
        WIND_SN { override fun toString(): String = "VVV" },
        WAVE_HEIGHT { override fun toString(): String = "WAV" },
        WIND_DIR { override fun toString(): String = "VEC" },
        WIND_SPEED { override fun toString(): String = "WSD" }
    }

    enum class ITEM{
        BASEDATE { override fun toString(): String = "baseDate" },
        BASETIME { override fun toString(): String = "baseTime" },
        CATEGORY { override fun toString(): String = "category" },
        FCSTDATE { override fun toString(): String = "category" },
        FCSTTIME { override fun toString(): String = "fcstTime" },
        FCSTVALUE { override fun toString(): String = "fcstValue" },
        NX { override fun toString(): String = "nx" },
        NY { override fun toString(): String = "ny" }

    }

    fun parseCategory(cat: CATEGORY): Item?{
        return response.body.items.item.firstOrNull {
            it.category == cat.toString()
        }
            ?: null
    }
}

class Response{
    @SerializedName("header")   var header: Header = Header()
    @SerializedName("body")     var body: Body = Body()
}
class Body{
    @SerializedName("dataType")     var dataType: String = "null"
    @SerializedName("items")        var items: Items = Items()
    @SerializedName("pageNo")       var pageNo: String = "null"
    @SerializedName("numOfRows")    var numOfRows: String = "null"
    @SerializedName("totalCount")   var totalCount: String = "null"
}

class Header{
    @SerializedName("resultCode") var resultCode: String = "null"
    @SerializedName("resultMsg") var resultMsg: String = "null"
}


class Items{
    @SerializedName("item") var item: List<Item> = listOf()

}

class Item{
    @SerializedName("baseDate")     var baseDate: String = "null"

    @SerializedName("baseTime")     var baseTime: String = "null"
    @SerializedName("category")     var category: String = "null"
    @SerializedName("fcstDate")     var fcstDate: String = "null"
    @SerializedName("fcstTime")     var fcstTime: String = "null"

    @SerializedName("fcstValue")    var fcstValue: String = "null"

    @SerializedName("nx")           var wd1: Int = 0
    @SerializedName("ny")           var wdTnd: Int = 0


    fun getInfo(): String{

        val header: String = toCatagort(category)
        var value: String = ""

        when (category){
            "POP",
            "REH" -> value = "${fcstValue} [%]"
            "PTY" -> value = toPTY(fcstValue)
            "SKY" -> value = toSKY(fcstValue)
            "TMP",
            "TMN",
            "TMX" -> value = fcstValue + " ['c]"
            "UUU",
            "VVV",
            "WSD" -> value = fcstValue + " [m/s]"
            "VEC" -> value = fcstValue + " [deg]"
            "WAV" -> value = fcstValue + " [M]"
            "PCP",
            "SNO" -> value = fcstValue
        }

        return "${header} ${value}"
    }
}

fun toCatagort(category: String): String{
    when (category){
        "POP" -> return "강수확률"
        "PTY" -> return "강수형태"
        "PCP" -> return "1시간 강수량"
        "REH" -> return "습도"
        "SNO" -> return "1시간 신적설"
        "SKY" -> return "하늘상태"
        "TMP" -> return "1시간 기온"
        "TMN" -> return "일 최저기온"
        "TMX" -> return "일 최고기온"
        "UUU" -> return "풍속(동서성분)"
        "VVV" -> return "풍속(남북성분)"
        "WAV" -> return "파고"
        "VEC" -> return "풍향"
        "WSD" -> return "풍속"
        else -> return  "알수없음"
    }
}
fun toPTY(fcstValue: String): String{
    val mtag = "convertPTY"
    when(fcstValue){
        "0" -> return "없음(${mtag})"
        "1" -> return "비(${mtag})"
        "2" -> return "비/눈(${mtag})"
        "3" -> return "눈(${mtag})"
        "4" -> return "소나기(${mtag})"
        else -> return fcstValue
    }
}

fun toSKY(fcstValue: String): String{
    val mtag = "convertSKY"
    when(fcstValue){
        "1" -> return "맑음(${mtag})"
        "3" -> return "구름많음(${mtag})"
        "4" -> return "흐림(${mtag})"
        else -> return fcstValue
    }
}

fun toPCP(fcstValue: String): String{
    val mtag = "convertSKY"
    when(fcstValue){
        "1" -> return "맑음(${mtag})"
        "3" -> return "구름많음(${mtag})"
        "4" -> return "흐림(${mtag})"
        else -> return fcstValue
    }
}