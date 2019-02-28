package com.example.globalbit.vvm

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ProgressBar
import com.bumptech.glide.GenericRequestBuilder
import com.caverock.androidsvg.SVG
import com.example.globalbit.R
import com.example.globalbit.model.Country
import com.example.globalbit.other.AdapterCountries
import com.example.globalbit.other.EventNetworkError
import com.example.globalbit.other.createGlideSvgRequestBuilder
import com.example.globalbit.other.setHomeUpEnabled
import icepick.Icepick
import icepick.State
import kotlinx.android.synthetic.main.activity_borders.*
import kotlinx.android.synthetic.main.list_item_country.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.dimen
import org.jetbrains.anko.toast
import java.io.InputStream

class ActivityBorders : AppCompatActivity()
{
    private var mProgBar: ProgressBar? = null
    private lateinit var mRequestBuilder: GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable>
    private lateinit var mViewModel: ActivityBordersViewModel

    @JvmField @State
    var mBorderCounter = 0
    //no need to save state because it will always be set to the correct number in onCreate()
    private var mNumBorders = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borders)

        if(savedInstanceState == null)
            mBorderCounter = 0
        else
            Icepick.restoreInstanceState(this, savedInstanceState)

        setHomeUpEnabled(true)

        mViewModel = ViewModelProviders.of(this)
            .get(ActivityBordersViewModel::class.java)
        mProgBar = ProgressBar(this)
        val flagSize = dimen(R.dimen.flag)
        mRequestBuilder = createGlideSvgRequestBuilder(flagSize, flagSize)

        mViewModel.mCountriesList.observe(this, Observer {

            if(it == null)
            {
                //request code does not matter in this case.
                //we want default error handling
                onNetworkError(EventNetworkError(0))
                return@Observer
            }

            //add the new border
            addBorder(it.last())
        })

        //note:
        //i am using linear layout here and not recycler view because
        //the effect i want to achieve is that that borders would be loaded one at a time,
        //without blocking the user. what i want to achieve is showing a progress bar
        //when loading the next border (always at the bottom of the layout)
        //until the last border has been loaded, and this is easy to achieve with linear layout.
        //also, according to google the countries with the most borders are China and Russia
        //having 14 borders each. 14 is a very small number which will not use a lot of memory,
        //so it does not justify using a recycler view

        activityBorders_ll.addView(mProgBar)

        val country = intent.getSerializableExtra(AdapterCountries.EXTRA_COUNTRY) as Country
        title = "${country._name} ${getString(R.string.boarders)}"
        mNumBorders = country._borders.size

        country._borders.apply {
            if(size == 0)
            {
                toast(R.string.noBorders)
                doneLoadingBorders()
            }

            else
                forEach { mViewModel.getCountryByCode(it) }
        }
    }

    private fun addBorder(country: Country)
    {
        //inflation is an expensive and (relatively) long operation.
        //do not attach to root so that we keep displaying the progress bar
        //until the view has finished inflating
        val view =
            layoutInflater.inflate(R.layout.list_item_country, activityBorders_ll, false)
                .apply {
                    val uri = Uri.parse(country._flag)
                    mRequestBuilder.load(uri)
                        .into(iv_countryFlag)

                    val name = "${country._name} (${country._nativeName})"
                    tv_countryName.text = name

                    val capital = "${context.getString(R.string.capital)} ${country._capital}"
                    tv_countryCapital.text = capital
                }

        val divider =
            layoutInflater.inflate(R.layout.divider, activityBorders_ll, false)

        activityBorders_ll.apply {
            removeView(mProgBar)
            //add the country and divider
            addView(view)
            addView(divider)
            //add progress bar beneath the new country
            addView(mProgBar)
        }

        borderAdded()
    }

    private fun borderAdded()
    {
        mBorderCounter++

        if(mBorderCounter >= mNumBorders)
                doneLoadingBorders()
    }

    private fun doneLoadingBorders()
    {
        if(mProgBar != null)
        {
            activityBorders_ll.apply {
                removeView(mProgBar)

                //remove last divider (if exists)
                val v = getChildAt(childCount - 1)
                if(v != null)
                    removeView(v)
            }
        }

        mProgBar = null
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    @Subscribe
    fun onNetworkError(event: EventNetworkError)
    {
        //for the purposes of this exercise,
        //the specific details of the error event are not important

        toast(R.string.errorLoadingBorder)
        //count as if a border was added, so we remove the progress bar
        borderAdded()
    }

    public override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    override fun onStart()
    {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop()
    {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if(item.itemId == android.R.id.home)
            onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
