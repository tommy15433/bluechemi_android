package com.bluechemi.application.weatherApi

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

const  val SERVICE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"
const val ENDPOINT_URL = "getVilageFcst"
const  val SERVICE_KEY = "yJVSmvWIqXF15xKVQwwnDzcvq8sfvAs5tR0MHaRg36vcWL0BJkO%2FUUj2qO%2FRfFwX14Rx9bOHow%2B1fGaotE0S%2FA%3D%3D"

object ForecastParser {

    interface Listener{
        fun onForecastUpdated(result: ForecastResponse)
    }

    private var mListener: Listener? = null
    private var retrofit: Retrofit? = null

    private fun unSafeOkHttpClient() : OkHttpClient.Builder {
        val okHttpClient = OkHttpClient.Builder()
        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts:  Array<TrustManager> = arrayOf(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?){}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate>  = arrayOf()
            })

            // Install the all-trusting trust manager
            val  sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory
            if (trustAllCerts.isNotEmpty() &&  trustAllCerts.first() is X509TrustManager) {
                okHttpClient.sslSocketFactory(sslSocketFactory, trustAllCerts.first() as X509TrustManager)
                okHttpClient.hostnameVerifier { _, _ -> true }
            }

            return okHttpClient
        } catch (e: Exception) {
            return okHttpClient
        }
    }

    val client: Retrofit?
        get() {
            if (retrofit == null){

                val gson: Gson = GsonBuilder()
                    .setLenient()
                    .create()

                retrofit = Retrofit.Builder()
                    .baseUrl(SERVICE_URL)
                    .client(unSafeOkHttpClient().build())
                    //.addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit
        }

    private var mLastResponse: ForecastResponse? = null
    val lastResponse: ForecastResponse
        get() = mLastResponse?: ForecastResponse()


    fun setListener(listener: ForecastParser.Listener){
        mListener = listener
    }

    fun parse(lat: Double, lon: Double) {
        val postApi = client?.create(ApiService::class.java)
        val grid: GRID = GpsGridConverter.toGrid(GPS(lat, lon))
        val timepage: Timepage = curtime2basetime()

        Log.i("retrofit2", "page: ${timepage.pageno} time: ${timepage.basetime}")

        postApi!!.getPost(
            timepage.pageno,
            12,
            "JSON",
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()),
            timepage.basetime,
            grid.x.toInt(),
            grid.y.toInt()
        ).enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful) {

                    response.body()?.let {
                        mListener?.onForecastUpdated(it)
                    }
                    Log.i("retrofit2", "response success")

                    mLastResponse = response.body()
                    mLastResponse?.response?.body?.items?.item?.forEach {
                        //Log.i("response", "${it.getInfo()}")
                    }
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Log.i("retrofitOnFailure", "${t.message}")
            }
        })
    }

    private fun curtime2basetime(): Timepage{

        var time: String = ""
        var page: Int = 1
        val curTime: Long =  SimpleDateFormat("HHmm", Locale.getDefault()).format(Date()).toLong()

        if (curTime in 200..459){
            return Timepage("2300", (curTime/100).toInt()-1)
        }else if (curTime in 500..759){
            return Timepage("0200", (curTime/100).toInt()-4)
        }else if (curTime in 800..1059){
            return Timepage("0500", (curTime/100).toInt()-7)
        }else if (curTime in 1100..1359){
            return Timepage("0800", (curTime/100).toInt()-10)
        }else if (curTime in 1400..1659){
            return Timepage("1100", (curTime/100).toInt()-13)
        }else if (curTime in 1700..1959){
            return Timepage("1400", (curTime/100).toInt()-16)
        }else if (curTime in 2000..2259){
            return Timepage("1700", (curTime/100).toInt()-19)
        }else if (curTime in 2300..2359){
            return Timepage("2000", 1)
        }else if (curTime in 0..59){
            return Timepage("2000", 2)
        }else{
            return Timepage("2000", 3)
        }
    }

    internal class Timepage(val basetime: String, val pageno: Int)
}

interface ApiService{
    @GET("${ENDPOINT_URL}?serviceKey=${SERVICE_KEY}")
    fun getPost(
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") base_date: String,
        @Query("base_time") base_time: String,
        @Query("nx") gridx: Int,
        @Query("ny") gridy: Int

    ): Call<ForecastResponse>

}
