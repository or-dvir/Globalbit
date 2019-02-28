package com.example.globalbit.other

import android.annotation.SuppressLint
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.GenericRequestBuilder
import com.bumptech.glide.Glide
import com.caverock.androidsvg.SVG
import com.example.globalbit.R
import com.example.globalbit.model.Country
import com.example.globalbit.vvm.ActivityBorders
import kotlinx.android.synthetic.main.list_item_country.view.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.startActivity
import java.io.InputStream

class AdapterCountries(var mCountries: List<Country>)
    : RecyclerView.Adapter<AdapterCountries.ViewHolder>()
{
    companion object
    {
        const val EXTRA_COUNTRY = "EXTRA_COUNTRY"
    }

    private var mRequestBuilder: GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable>? = null

    override fun getItemCount(): Int = mCountries.size

    @SuppressLint("SetJavaScriptEnabled")
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val context = holder.itemView.context
        val country = mCountries[position]

        //note:
        //normally i would use Picasso, but it cannot load SVG images.
        //even with glide it's a little complicated and you need a few extra calssses
        //(which i've copied from the sample code on github
        //at https://github.com/bumptech/glide/tree/master/samples/svg).
        //even still there seems to be a problem with the "override" method where the images would not
        //resize to the given dimensions, and i could not find a working solution online.
        //as this is just an exercise and for the sake of saving time,
        //i have decided to leave it as-is and move on.

        if(mRequestBuilder == null)
        {
            val flagSize = context.dimen(R.dimen.flag)
            mRequestBuilder = context.createGlideSvgRequestBuilder(flagSize, flagSize)
        }

        holder.apply {
            val uri = Uri.parse(country._flag)
            mRequestBuilder!!.load(uri)
                .into(iv)

            val name = "${country._name} (${country._nativeName})"
            tv_name.text = name

            val capital = "${context.getString(R.string.capital)} ${country._capital}"
            tv_capital.text = capital
        }
    }

    override fun onViewRecycled(holder: ViewHolder)
    {
        super.onViewRecycled(holder)

        holder.apply {
            Glide.clear(iv)
            tv_name.text = ""
            tv_capital.text = ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val context = parent.context
        val itemView = LayoutInflater
            .from(context)
            .inflate(R.layout.list_item_country, parent, false)

        val holder = ViewHolder(itemView)

        itemView.setOnClickListener {
            context.startActivity<ActivityBorders>(
                EXTRA_COUNTRY to mCountries[holder.adapterPosition])
        }

        return holder
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val iv = itemView.iv_countryFlag
        val tv_name = itemView.tv_countryName
        val tv_capital = itemView.tv_countryCapital
    }
}