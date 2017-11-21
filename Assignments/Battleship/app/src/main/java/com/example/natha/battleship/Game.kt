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
import android.util.Log.i
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.example.natha.battleship.R.id.my_recycler_view
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonStreamParser
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.game_selection.*
import java.io.*



class Game : AppCompatActivity() {

    lateinit var logout : Button
    lateinit var uid : String
    lateinit var email : String
    lateinit var auth : FirebaseAuth
    lateinit var mDbRoot : FirebaseDatabase
    lateinit var mDbRootRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_selection)
        mDbRoot = FirebaseDatabase.getInstance()
        mDbRootRef = mDbRoot.getReference()
        mDbRootRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot?) {

            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
        readDatabase()
        auth = FirebaseAuth.getInstance()
        logout = findViewById(R.id.logout)
        logout.setOnClickListener(View.OnClickListener {
            auth.signOut()
            startActivity(Intent(applicationContext, Login::class.java))
            finish()
        })
        if(intent != null && intent.extras != null)
        {
            for(i in intent.extras.keySet())
            {
                when(i)
                {
                    "userEmail" -> email = intent.getStringExtra(i)
                    "userUID" -> uid = intent.getStringExtra(i)
                }
            }
        }

        setupFiles()
    }

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager
    var numOfFiles = 0

    fun setupFiles() {
        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.plus, "New Game"))

            recyclerViewDataset.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection", "Listener notified of the item selection")

                when (myAdapterItem) {
                    is MyAdapter.ImageWithTitle -> {
                        Log.e("FileSelection", "Selected item contained image of Id (${myAdapterItem.button}")
                        Log.e("FileSelection", "myAdapterTitle: " + myAdapterItem.title)
                            intent = Intent(applicationContext, GameState::class.java)
                        if(myAdapterItem.title.equals("New Game")) intent.putExtra("New Game", "")
                        else intent.putExtra("gameId", myAdapterItem.title.split("\\s+".toRegex())[0])
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    fun readDatabase()
    {
//        var gamesRef = mDbRoot.getReference("Games")
//        var messageId = gamesRef.push().key
//        gamesRef.child(messageId).setValue("Game")
    }
}