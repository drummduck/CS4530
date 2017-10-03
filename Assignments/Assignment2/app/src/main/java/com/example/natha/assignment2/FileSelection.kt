package com.example.natha.assignment2

import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_file_selection.*


class FileSelection : AppCompatActivity() {

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selection)

        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
            val imageForTitledItems: Drawable = getDrawable(R.drawable.folder)

            for(index: Int in 0..1000 step 2)
            {
                recyclerViewDataset.add(MyAdapter.ImageWithTitle(imageForTitledItems, "Image Title ${ index + 1 }"))
            }
            recyclerViewDataset.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection","Listener notified of the item selection")

                when(myAdapterItem){
                    is MyAdapter.ImageWithTitle -> Log.e("FileSelection", "Selected item contained image of size (${myAdapterItem.image.bounds.width()} x ${myAdapterItem.image.bounds.height()}")
                    else -> Log.wtf("Error", "Selected item was invalid")
                }
            }
        }


    }
}