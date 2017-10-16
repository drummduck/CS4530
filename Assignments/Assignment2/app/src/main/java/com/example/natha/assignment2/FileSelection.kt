package com.example.natha.assignment2

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_file_selection.*
import java.io.File
import java.util.jar.Manifest
import android.Manifest.permission
import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import com.example.natha.assignment2.R.drawable.canvas
import kotlinx.android.synthetic.main.view_titled_image.view.*
import java.io.DataInputStream
import java.io.FileInputStream
import java.io.FilenameFilter
import java.security.Permission
import java.security.Permissions


class FileSelection : AppCompatActivity() {

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager
    var numOfFiles = 0
    val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    var drawing = ArrayList<Pair<Quadruple, ArrayList<Pair<Float,Float>>>>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_selection)
        requestPermissions()
    }

    override fun onRequestPermissionsResult(requestCode : Int,
    stringArray : Array<String>, grantResults : IntArray) {
        when(requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupFiles()
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
            val defaultImage : Drawable = getDrawable(R.drawable.folder)
            recyclerViewDataset.add(MyAdapter.ImageWithTitle(newItem, "New Project"))
            var dir : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Assignment2Draw/")
            if(!dir.exists()) dir.mkdirs()
            else {
                numOfFiles = dir.listFiles().size/2
                var count = 1
                for(i in dir.listFiles())
                {
                    if(!i.absolutePath.contains(".png")) {
                        Log.e("FILES", "filePath is: " + i.absolutePath)
                        var jeff = ImageView(this)
                        var file = File(i.absolutePath + "image.png")
                        if (!file.exists()) recyclerViewDataset.add(MyAdapter.ImageWithTitle(defaultImage, "Project" + count))
                        else {
                            var bitMap = BitmapFactory.decodeFile(file.absolutePath)
                            jeff.setImageBitmap(bitMap)
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(jeff.drawable, "Project" + count))
                        }
                        count++
                    }
                }
            }

            recyclerViewDataset.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection","Listener notified of the item selection")

                when(myAdapterItem){
                    is MyAdapter.ImageWithTitle -> {
                        Log.e("FileSelection", "Selected item contained image of size (${myAdapterItem.image.bounds.width()} x ${myAdapterItem.image.bounds.height()}")
                        Log.e("FileSelection", "myAdapterTitle: " + myAdapterItem.title)
                        intent = Intent(applicationContext, Draw::class.java)
                        intent.putExtra("fileName", myAdapterItem.title)
                        intent.putExtra("numOfFiles", numOfFiles)
                        startActivity(intent)
                        finish()
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
                WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("FileSelection", "permission:WRITE_EXTERNAL_STORAGE: NOT granted!");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                var stringArray = arrayOf(WRITE_EXTERNAL_STORAGE)
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

    fun readString(reader : DataInputStream) : String
    {
        var returnString = ""
        while(true)
        {
            var readInChar = reader.readChar()
            if(readInChar == '\t') return returnString
            else returnString = returnString + readInChar
        }
    }
}