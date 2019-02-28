package com.example.globalbit.other.interfaces_and_enums

import com.example.globalbit.model.Country
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ICountriesClient
{
    @GET("all")
    fun getAllCountries(): Call<List<Country>>

    @GET("alpha/{code}")
    fun getCountryByCode(@Path("code") code: String): Call<Country>
}
