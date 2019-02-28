package com.example.globalbit.other

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.StreamEncoder
import com.bumptech.glide.load.resource.file.FileToStreamDecoder
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.UI
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import java.io.InputStream

object Consts
{
    const val HTTP_CODE_200_OK = 200
}

typealias retroSuccess<T> = ((originalCall: Call<T>, result: T, requestCode: Int) -> Unit)?
typealias retroErrorCode<T> = ((originalCall: Call<T>, serverErrorCode: Int, requestCode: Int) -> Unit)?
typealias retroException<T> = ((originalCall: Call<T>, exception: Exception, requestCode: Int) -> Unit)?
typealias retroErrorOrException<T> = ((originalCall: Call<T>, serverErrorCode: Int?, exception: Exception?, requestCode: Int) -> Unit)?


fun AppCompatActivity.setHomeUpEnabled(enabled: Boolean) = supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
fun getCountriesClient() = SMyRetrofit.countriesClient

fun Context.createGlideSvgRequestBuilder(width:Int, height: Int)
        : GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable>
{
    return Glide.with(this)
        .using(Glide.buildStreamModelLoader(Uri::class.java, this), InputStream::class.java)
        .from(Uri::class.java)
        .`as`(SVG::class.java)
        .transcode(SvgDrawableTranscoder(), PictureDrawable::class.java)
        .sourceEncoder(StreamEncoder())
        .cacheDecoder(FileToStreamDecoder(SvgDecoder()))
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .decoder(SvgDecoder())
        .override(width, height)
        .listener(SvgSoftwareLayerSetter<Uri>())
}

fun <T> countriesRequestAsync(logTag: String,
                           call: Call<T>,
                           callback: CountriesCallback<T>,
                           requestCode: Int = -1,
                           shouldPostErrorEvent: Boolean= true)
        : Job
{
    return tvMazeRequestAsync(logTag,
                              call,
                              callback.onSuccess,
                              callback.onErrorCodeOrNullBody,
                              callback.onException,
                              callback.onErrorOrExceptionOrNullBody,
                              requestCode,
                              shouldPostErrorEvent)
}

private fun <T> tvMazeRequestAsync(logTag: String,
                                   call: Call<T>,
                                   onSuccess: retroSuccess<T>,
                                   onErrorCodeOrNullBody: retroErrorCode<T>,
                                   onException: retroException<T>,
                                   onErrorOrExceptionOrNullBody: retroErrorOrException<T>,
                                   requestCode: Int,
                                   shouldPostErrorEvent: Boolean)
        : Job
{
    return launch(UI)
    {
        var event: EventNetworkError? = null

        try
        {
            val response = withContext(CommonPool) { call.execute() }

            val code = response.code()
            val body = response.body()
            var errorMessage = ""

            //NOTE:
            //response.isSuccessful() returns true for all values between 200 and 300!!!
            if(code == Consts.HTTP_CODE_200_OK)
            {
                if(body != null)
                    onSuccess?.let {it(call, body, requestCode) }
                else
                {
                    errorMessage = "server response body was NULL"
                    event = EventNetworkError(requestCode)
                }
            }

            //server error code (NOT 200-ok)
            else
            {
                errorMessage = response.message()
                event = EventNetworkError(requestCode, errorCode = code)
            }

            if(event != null)
            {
                Log.e(logTag, "server error:\n" +
                        "return code: $code\n" +
                        "error message: $errorMessage\n" +
                        "original call was: ${call.request()}")

                onErrorCodeOrNullBody?.let { it(call, code, requestCode) }
                onErrorOrExceptionOrNullBody?.let { it(call, code, null, requestCode) }
            }
        }
        catch (e: Exception)
        {
            Log.e(logTag, "${e.message}\noriginal call was: ${call.request()}", e)
            event = EventNetworkError(requestCode, e)

            onException?.let { it(call, e, requestCode) }
            onErrorOrExceptionOrNullBody?.let { it(call, null, e, requestCode) }
        }
        finally
        {
            //if not null, we had an error or exception
            if (event != null && shouldPostErrorEvent)
                EventBus.getDefault().post(event)
        }
    }
}