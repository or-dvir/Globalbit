package com.example.globalbit.other

class EventNetworkError(val requestCode: Int,
                        val exception: Exception? = null,
                        val errorCode: Int? = null)