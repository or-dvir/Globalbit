package com.example.globalbit.vvm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.example.globalbit.model.Country
import com.example.globalbit.other.CountriesCallback
import com.example.globalbit.other.countriesRequestAsync
import com.example.globalbit.other.getCountriesClient

class ActivityBordersViewModel(mApp: Application): AndroidViewModel(mApp)
{
    companion object
    {
        private const val TAG = "ActivityBordersViewModel"
    }

    val mCountriesList = MutableLiveData<MutableList<Country>>()

    fun getCountryByCode(code: String)
    {
        countriesRequestAsync(TAG,
                              getCountriesClient().getCountryByCode(code),
                              CountriesCallback<Country>().apply {
                                  onSuccess = { originalCall, result, requestCode ->

                                      var list = mCountriesList.value
                                      if(list == null)
                                          list = mutableListOf()

                                      list.add(result)

                                      //trigger the observer
                                      mCountriesList.value = list
                                  }
                              })
    }
}