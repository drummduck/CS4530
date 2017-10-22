package com.example.natha.battleship

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.game_selection.*
import java.io.File

class Game : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_selection)
        requestPermissions()
    }

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager
    var numOfFiles = 0
    val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

    override fun onRequestPermissionsResult(requestCode : Int,
                                            stringArray : Array<String>, grantResults : IntArray) {
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    fun setupFiles()
    {
        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
            val newItem : Drawable = getDrawable(R.drawable.plus)
            recyclerViewDataset.add(MyAdapter.ImageWithTitle(newItem, "New Game"))
            var dir : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/")
            if(!dir.exists()) dir.mkdirs()
            else {
                numOfFiles = dir.listFiles().size/2
                var count = 1
                for(i in dir.listFiles())
                {
                    recyclerViewDataset.add(MyAdapter.ImageWithTitle(newItem, "Game" + count))
                    count++
                }

            }

            recyclerViewDataset.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                when(myAdapterItem){
                    is MyAdapter.ImageWithTitle -> {
//                        intent = Intent(applicationContext, Board::class.java)
//                        intent.putExtra("fileName", myAdapterItem.title)
//                        intent.putExtra("numOfFiles", numOfFiles)
//                        startActivity(intent)
//                        finish()
                    }
                }
            }
        }
    }

    fun requestPermissions() {
        var state = Environment.getExternalStorageState()
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d("FileSelection", "Error: external storage is unavailable")
            return
        }
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d("FileSelection", "Error: external storage is read only.")
            return
        }
        Log.d("FileSelection", "External storage is not read only or unavailable")

        if (ContextCompat.checkSelfPermission(this, // request permission when it is not granted.
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("FileSelection", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                var stringArray = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this,
                        stringArray,
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else setupFiles()
    }

}
