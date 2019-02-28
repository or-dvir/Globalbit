package com.example.globalbit.vvm

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.example.globalbit.R
import com.example.globalbit.other.AdapterCountries
import com.example.globalbit.other.EventNetworkError
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.toast

class ActivityMain : AppCompatActivity()
{
    private lateinit var mViewModel: ActivityMainViewModel
    private var mAdapter = AdapterCountries(listOf())

    //note:
    //recently co-routines have been moved out of the "experimental" stage,
    //but i have not yet had the time to fully study the new way of using them.
    //so in the interest of saving time i am using the "experimental way".
    //for the purposes of this exercise, this should be good enough.

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mViewModel = ViewModelProviders.of(this)
            .get(ActivityMainViewModel::class.java)

        activityMain_rv.apply {
            addItemDecoration(DividerItemDecoration(this@ActivityMain, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(this@ActivityMain, RecyclerView.VERTICAL, false)
            adapter = mAdapter
        }

        mViewModel.mCountriesList.observe(this, Observer {
            pb.visibility = View.GONE

            if(it == null)
            {
                //request code does not matter in this case.
                //we want default error handling
                onNetworkError(EventNetworkError(0))
                return@Observer
            }

            //remove all shows from previous page, and add all shows of current page
            mAdapter.mCountries = it
            mAdapter.notifyDataSetChanged()
        })
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    @Subscribe
    fun onNetworkError(event: EventNetworkError)
    {
        //for the purposes of this exercise,
        //the specific details of the error event are not important

        pb.visibility = View.GONE
        toast(R.string.someError)
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
}
