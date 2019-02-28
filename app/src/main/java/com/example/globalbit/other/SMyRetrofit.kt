package com.example.globalbit.other

import com.example.globalbit.other.interfaces_and_enums.ICountriesClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

object SMyRetrofit
{
    private const val TIMEOUT_SECONDS: Long = 15
    val countriesClient: ICountriesClient

    init
    {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://restcountries.eu/rest/v2/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(OkHttpClient.Builder()
                                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                                .build())
                .build()

        countriesClient = retrofit.create<ICountriesClient>(ICountriesClient::class.java)
    }
}