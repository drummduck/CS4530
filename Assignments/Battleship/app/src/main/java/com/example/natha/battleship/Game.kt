package com.example.natha.battleship

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.Image
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.util.Log.i
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.example.natha.battleship.R.id.my_recycler_view
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonStreamParser
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.game_selection.*
import java.io.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class Game : AppCompatActivity() {

    lateinit var logout : Button
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var mDbRoot : FirebaseDatabase
    lateinit var mDbRootRef : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_selection)
        mDbRoot = FirebaseDatabase.getInstance()
        mDbRootRef = mDbRoot.getReference()
        auth = FirebaseAuth.getInstance()
        if (auth != null && auth.currentUser != null) currentUser = auth.currentUser!!
        logout = findViewById(R.id.logout)
        logout.setOnClickListener(View.OnClickListener {
            auth.signOut()
            startActivity(Intent(applicationContext, Login::class.java))
            finish()
        })

        setupRecyclerView()

        mDbRootRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {setupRecyclerView()}

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {setupRecyclerView()}

            override fun onChildRemoved(p0: DataSnapshot?) {setupRecyclerView()}

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {setupRecyclerView()}

            override fun onCancelled(p0: DatabaseError?) {setupRecyclerView()}
        })
    }

    fun setupRecyclerView()
    {
        val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
        FirebaseDatabase.getInstance().reference.child("Games").addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var gameId = ""

                for (game in dataSnapshot.children) {
                    Log.e("GAME READ", "Game key is: " + game.key + ", Game value is: " + game.value)

                    gameId = game.key
                    var playerOneShipCount = -1
                    var playerTwoShipCount = -1
                    var playerOneName = ""
                    var playerTwoName = ""
                    var state = ""

                    if(game.hasChild("Game State"))
                    {
                        state = game.child("Game State").value as String
                        Log.e("GAME STATE", state)
                    }
                    if(game.hasChild("Player One") && game.child("Player One").hasChild("shipCount"))
                    {
                        Log.e("PLAYER ONE SHIP COUNT", game.child("Player One").child("shipCount").value.toString())
                        playerOneShipCount = Integer.parseInt(game.child("Player One").child("shipCount").value.toString())
                    }
                    if(game.hasChild("Player Two") && game.child("Player Two").hasChild("shipCount"))
                    {
                        Log.e("PLAYER TWO SHIP COUNT", game.child("Player Two").child("shipCount").value.toString())
                        playerTwoShipCount = Integer.parseInt(game.child("Player Two").child("shipCount").value.toString())
                    }
                    if(game.hasChild("Player One") && game.child("Player One").hasChild("name"))
                    {
                        Log.e("PLAYER ONE NAME", game.child("Player One").child("name").value.toString())
                        playerOneName = game.child("Player One").child("name").value.toString()
                    }
                    if(game.hasChild("Player Two") && game.child("Player Two").hasChild("name"))
                    {
                        Log.e("PLAYER TWO NAME", game.child("Player Two").child("name").value.toString())
                        playerTwoName = game.child("Player Two").child("name").value.toString()
                    }

                    if(gameId.equals("") || playerOneShipCount == -1 || playerTwoShipCount == -1 || playerOneName.equals("") || state.equals(""))
                    {
                        Log.e("RECYCLER VIEW","DATA IS NOT VALID, CANT HAVE EMPTY DATA")
                        return
                    }

                    if(state != GameState.gameState.STARTED.name && playerTwoName.isEmpty())
                    {
                        Log.e("RECYCLER VIEW", "DATA IS NOT VALID, PLAYER 2 CANT BE EMPTY WITH THE GAME STARTED")
                        return
                    }

                    if((state == GameState.gameState.GAME_OVER_PLAYER_ONE.name || state == GameState.gameState.GAME_OVER_PLAYER_TWO.name) &&
                            (!playerOneName.equals(currentUser.email) || !playerTwoName.equals("Player Two")))
                    {
                        Log.e("RECYCLER VIEW", "PLAYER WAS NOT APART OF GAME")
                        return
                    }

                    var dataString : String

                    when(state) {
                        GameState.gameState.STARTED.name -> {
                            if(playerTwoName.isEmpty()) dataString = "Game Started\n" + playerOneName + " is waiting for player to join"
                            else dataString = "Game Started\n" + playerOneName + " turn\n" + playerOneName + " Ships left: " + playerOneShipCount + "\n" + playerTwoName + " Ships left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.start, dataString, gameId))
                        }
                        GameState.gameState.PLAYER_ONE_TURN.name, GameState.gameState.SWITCH_TO_PLAYER_ONE.name -> {
                            dataString = "Game In Progress\n" + playerOneName + " turn\n" + playerOneName + " Ships left: " + playerOneShipCount + "\n" + playerTwoName + " Ships left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.battle, dataString, gameId))
                        }
                        GameState.gameState.PLAYER_TWO_TURN.name, GameState.gameState.SWITCH_TO_PLAYER_TWO.name -> {
                            dataString = "Game In Progress\n" + playerTwoName + " turn\n" + playerOneName + " Ships left: " + playerOneShipCount + "\n" + playerTwoName + " Ships left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.battle, dataString, gameId))
                        }
                        GameState.gameState.GAME_OVER_PLAYER_ONE.name -> {
                            dataString = "Game Over\n" + playerOneName + " wins!\n" + "Ships left: " + playerOneShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.delete, dataString, gameId))
                        }
                        GameState.gameState.GAME_OVER_PLAYER_TWO.name -> {
                            dataString = "Game Over\n" + playerTwoName + " wins!\n" + "Ships left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.delete, dataString, gameId))
                        }
                    }
                }
                findViewById<RelativeLayout>(R.id.loadingPanel).setVisibility(View.GONE)
                setupFiles(recyclerViewDataset)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager
    var numOfFiles = 0

    fun setupFiles(databaseList : MutableList<MyAdapter.MyAdapterItem>) {
        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            databaseList.add(0,MyAdapter.ImageWithTitle(R.drawable.plus, "New Game", ""))
            databaseList.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection", "Listener notified of the item selection")

                when (myAdapterItem) {
                    is MyAdapter.ImageWithTitle -> {
                        Log.e("FileSelection", "Selected item contained image of Id (${myAdapterItem.button}")
                        Log.e("FileSelection", "myAdapterTitle: " + myAdapterItem.title)
                            intent = Intent(applicationContext, GameState::class.java)
                        if(myAdapterItem.title.equals("New Game")) intent.putExtra("New Game", "")
                        else intent.putExtra("gameId", myAdapterItem.gameId)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}