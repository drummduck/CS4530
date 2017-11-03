package com.example.natha.battleship

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.MenuView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.view_titled_image.view.*
import java.io.File


/**
 * Created by Natha on 10/1/2017.
 */
class MyAdapter(private val dataset: Array<MyAdapterItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    override fun getItemCount(): Int = dataset.size

    override fun getItemViewType(position: Int): Int = dataset[position].adapterItemType.ordinal

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            MyAdadpterItemType.TITLED_IMAGE.ordinal -> {
                val dataSetItem: MyAdapterItem = dataset[position]
                if (holder !is TitledImageViewHolder || dataSetItem !is ImageWithTitle) throw AssertionError("Invalid ViewHolder was supplied for binding, or the dataset contained an unexpected value.")
                holder.titledImageView.button = dataSetItem.button
                holder.titledImageView.title = dataSetItem.title
                holder.titledImageView.buttonView.setOnClickListener(clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            MyAdadpterItemType.TITLED_IMAGE.ordinal -> {
                TitledImageViewHolder(layoutInflater.inflate(R.layout. view_titled_image, parent, false) as TitledImageView).apply {
                    titledImageView.setOnClickListener { clickedView: View ->
                        Log.e("MyAdapter","Selected ${clickedView.javaClass.canonicalName} at position $adapterPosition. Notifying listener.")
                        onMyAdapterItemSelectedListener?.myAdapterItemSelected(dataset[adapterPosition])
                    }
                }
            }

            else -> {
                throw AssertionError("Invalid viewType supplied.")
            }
        }
    }

    enum class MyAdadpterItemType {
        TITLED_IMAGE
    }

    interface OnMyAdapterItemSelectedListener {
        fun myAdapterItemSelected(myAdapterItem: MyAdapterItem)
    }

    interface MyAdapterItem {
        val adapterItemType : MyAdadpterItemType
    }

    data class ImageWithTitle(val button: Int, val title: String ) : MyAdapterItem {
        override val adapterItemType: MyAdadpterItemType = MyAdadpterItemType.TITLED_IMAGE
    }
    class TitledImageViewHolder(val titledImageView: TitledImageView) : RecyclerView.ViewHolder(titledImageView)

    private var onMyAdapterItemSelectedListener : OnMyAdapterItemSelectedListener? = null

    fun setOnMyAdapterItemSelectedListener(onMyAdapterItemSelectedListener: OnMyAdapterItemSelectedListener) {
        this.onMyAdapterItemSelectedListener = onMyAdapterItemSelectedListener
    }

    fun setOnMyAdapterItemSelectedListener(onMyAdapterItemSelectedListener: ((myAdapterItem : MyAdapterItem) -> Unit)) {
        this.onMyAdapterItemSelectedListener = object : OnMyAdapterItemSelectedListener {
            override fun myAdapterItemSelected(myAdapterItem: MyAdapterItem) {
                onMyAdapterItemSelectedListener(myAdapterItem)
            }
        }
    }

    val clickListener = View.OnClickListener { view ->
        Log.e("BITCH", "BITCH")
        var parent = view.parent
        if(parent is LinearLayout)
        {
            Log.e("YOU", "YOU")
            var child = parent.getChildAt(1)
            if(child is TextView)
            {
                Log.e("SUCK", "SUCK")
                var file: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/" + child.text.split("\\s".toRegex())[0])
                file.delete()
                var intent = Intent(view.context, Game::class.java)
                view.context.startActivity(intent)
            }
        }
    }
}