package com.example.globalbit.vvm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.example.globalbit.model.Country
import com.example.globalbit.other.CountriesCallback
import com.example.globalbit.other.countriesRequestAsync
import com.example.globalbit.other.getCountriesClient

class ActivityMainViewModel(mApp: Application): AndroidViewModel(mApp)
{
    companion object
    {
        private const val TAG = "ActivityMainViewModel"
    }

    val mCountriesList = MutableLiveData<List<Country>>()

    init { getAllCountries() }

    private fun getAllCountries()
    {
        countriesRequestAsync(TAG,
                              getCountriesClient().getAllCountries(),
                              CountriesCallback<List<Country>>().apply {
                                  onSuccess = { originalCall, result, requestCode ->
                                      mCountriesList.value = result
                                  }

                                  onErrorOrExceptionOrNullBody = { originalCall,
                                                                   serverErrorCode,
                                                                   exception,
                                                                   requestCode ->
                                      //trigger the observer
                                      mCountriesList.value = null
                                  }
                              })
    }
}