package com.example.natha.battleship

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import com.example.natha.battleship.R.id.my_recycler_view
import kotlinx.android.synthetic.main.game_selection.*
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

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

    fun setupFiles() {
        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.plus, "New Game"))
            var dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/")
            if (!dir.exists()) dir.mkdirs()
            else {
                numOfFiles = dir.listFiles().size
                var count = 1
                for (i in dir.listFiles()) {
                    recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.delete, setGameSelectionText(i, i.name)))
                    count++
                }

            }

            recyclerViewDataset.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection", "Listener notified of the item selection")

                when (myAdapterItem) {
                    is MyAdapter.ImageWithTitle -> {
                        Log.e("FileSelection", "Selected item contained image of Id (${myAdapterItem.button}")
                        Log.e("FileSelection", "myAdapterTitle: " + myAdapterItem.title)
                        intent = Intent(applicationContext, GameState::class.java)
                        if(myAdapterItem.title.equals("New Game")) intent.putExtra("New Game", itemCount)
                        else intent.putExtra("fileName", myAdapterItem.title.split("\\s+".toRegex())[0])
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

    fun setGameSelectionText(file : File, name : String) : String
    {
        val inputFile = FileInputStream(file)
        val inputReader = DataInputStream(inputFile)
        var state = inputReader.readInt()

        for(i in 0..4)
        {
            var shipSize = inputReader.readInt()
            for(i in 0..shipSize-1)
            {
                inputReader.readInt()
                inputReader.readInt()
                inputReader.readInt()
            }
        }
        var playerOneShipsLeft = inputReader.readInt()
        var myAttackSize = inputReader.readInt()
        //myattacks
        if(myAttackSize != 0) {
            for (i in 0..myAttackSize - 1) {
                inputReader.readInt()
                inputReader.readInt()
                inputReader.readInt()
            }
        }
            //oppAttacks
        var oppAttackSize = inputReader.readInt()
        //myattacks
        if(oppAttackSize != 0) {
            for (i in 0..oppAttackSize - 1) {
                inputReader.readInt()
                inputReader.readInt()
                inputReader.readInt()
            }
        }
        for(i in 0..4)
        {
            var shipSize = inputReader.readInt()
            for(i in 0..shipSize-1)
            {
                inputReader.readInt()
                inputReader.readInt()
                inputReader.readInt()
            }
        }
        var playerTwoShipsLeft = inputReader.readInt()
        //Game# (in progress, started, Player # won) (Player (one/two)'s turn) PlayerOne Ships: # PlayerTwo Ships: #
        var returnString = ""
        when(state)
        {
            0 -> {returnString = name + " Started\n" + "Player One's Turn\n" + "Player One Ships Left: " + playerOneShipsLeft + "\nPlayer Two Ships Left: " + playerTwoShipsLeft}
            1,3 -> {returnString = name + " In Progress\n" + "Player One's Turn\n" + "Player One Ships Left: " + playerOneShipsLeft + "\nPlayer Two Ships Left: " + playerTwoShipsLeft}
            2,4 -> {returnString = name + " In Progress\n" + "Player Two's Turn\n" + "Player One Ships Left: " + playerOneShipsLeft + "\nPlayer Two Ships Left: " + playerTwoShipsLeft}
            5 -> {returnString = name + " Over\n" + "Player One Wins!\n" + "Player Ones Ships Left: " + playerOneShipsLeft}
            6 -> {returnString = name + " Over\n" + "Player Two Wins!\n" + "Player Two's Ships Left: " + playerTwoShipsLeft}
        }

        inputReader.close()
        inputFile.close()

        return returnString
    }
}
