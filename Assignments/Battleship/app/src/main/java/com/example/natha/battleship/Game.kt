package com.example.natha.battleship

import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.game_selection.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern
import kotlin.collections.ArrayList


class Game : AppCompatActivity(){
    lateinit var logout : Button
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var shownUser : TextView
    lateinit var mDbRoot : FirebaseDatabase
    lateinit var mDbRootRef : DatabaseReference
    lateinit var checkInternet : TextView
    lateinit var loadingPanel : ProgressBar
    lateinit var recyclerView : android.support.v7.widget.RecyclerView
    var connected = false
    var handler = Handler()
    val KILL = "com.example.natha.battleship.KILL"

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.e("BROADCAST RECEIVED", "RECEIVED KILL!")

            when (intent?.action) {
                KILL -> finish()
            }
        }
    }

    val childEventListener = object : ChildEventListener {
        override fun onChildAdded(p0: DataSnapshot?, p1: String?) {setupRecyclerView()}

        override fun onChildChanged(p0: DataSnapshot?, p1: String?) {setupRecyclerView()}

        override fun onChildRemoved(p0: DataSnapshot?) {setupRecyclerView()}

        override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        override fun onCancelled(p0: DatabaseError?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_selection)
        checkInternet = findViewById(R.id.checkInternet)
        recyclerView = findViewById(R.id.my_recycler_view)
        loadingPanel = findViewById(R.id.loadingPanel)
        shownUser = findViewById(R.id.loggedInAs)
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

        this.registerReceiver(broadCastReceiver, IntentFilter(KILL))
        mDbRootRef.addChildEventListener(childEventListener)

        shownUser.text = "Logged in as: " + currentUser.email

        setupRecyclerView()

        handler.postDelayed(Runnable {
            if(!connected) {
                checkInternet.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
                loadingPanel.visibility = View.INVISIBLE
            }
            }, 10000)
    }

    fun setupRecyclerView()
    {
        val recyclerViewDataset: MutableList<MyAdapter.MyAdapterItem> = mutableListOf()
        var gamesToIgnore = ArrayList<String>()

        mDbRootRef.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(!dataSnapshot.hasChild("Users")) return
                if(!dataSnapshot.hasChild("Games")) mDbRootRef.child("Games").setValue("")


                var usersSnapshot = dataSnapshot.child("Users")
                if(usersSnapshot.hasChild(currentUser.uid) && usersSnapshot.child(currentUser.uid).hasChild("GamesToIgnore"))
                    gamesToIgnore = usersSnapshot.child(currentUser!!.uid).child("GamesToIgnore").getValue() as ArrayList<String>

                var gamesSnapshot = dataSnapshot.child("Games")
                var gameId = ""

                for (game in gamesSnapshot.children) {
                    Log.e("GAME READ", "Game key is: " + game.key + ", Game value is: " + game.value)

                    gameId = game.key
                    var playerOneShipCount = -1
                    var playerTwoShipCount = -1
                    var playerOneName = ""
                    var playerTwoName = ""
                    var state = ""

                    if(gamesToIgnore.contains(gameId))
                    {
                        Log.e("PLAYER DELETED GAME", "Not going to show game due to player deletion!")
                        continue
                    }

                    if(game.hasChild("Game State"))
                    {
                        state = game.child("Game State").value as String
                        Log.e("RECYCLER VIEW GAME STATE", state)
                    }
                    if(game.hasChild("Player One") && game.child("Player One").hasChild("shipCount"))
                    {
                        Log.e("RECYCLER VIEW PLAYER ONE SHIP COUNT", game.child("Player One").child("shipCount").value.toString())
                        playerOneShipCount = Integer.parseInt(game.child("Player One").child("shipCount").value.toString())
                    }
                    if(game.hasChild("Player Two") && game.child("Player Two").hasChild("shipCount"))
                    {
                        Log.e("RECYCLER VIEW PLAYER TWO SHIP COUNT", game.child("Player Two").child("shipCount").value.toString())
                        playerTwoShipCount = Integer.parseInt(game.child("Player Two").child("shipCount").value.toString())
                    }
                    if(game.hasChild("Player One") && game.child("Player One").hasChild("name"))
                    {
                        Log.e("RECYCLER VIEW PLAYER ONE NAME", game.child("Player One").child("name").value.toString())
                        playerOneName = game.child("Player One").child("name").value.toString()
                    }
                    if(game.hasChild("Player Two") && game.child("Player Two").hasChild("name"))
                    {
                        Log.e("RECYCLER VIEW PLAYER TWO NAME", game.child("Player Two").child("name").value.toString())
                        playerTwoName = game.child("Player Two").child("name").value.toString()
                    }

                    if(gameId.equals("") || playerOneShipCount == -1 || playerTwoShipCount == -1 || playerOneName.equals("") || state.equals(""))
                    {
                        Log.e("RECYCLER VIEW","DATA IS NOT VALID, CANT HAVE EMPTY DATA")
                        continue
                    }

                    if((state == GameState.gameState.GAME_OVER_PLAYER_ONE.name || state == GameState.gameState.GAME_OVER_PLAYER_TWO.name) &&
                            (!playerOneName.equals(currentUser.email) && !playerTwoName.equals(currentUser.email)))
                    {
                        Log.e("RECYCLER VIEW", "PLAYER WAS NOT APART OF GAME")
                        continue
                    }

                    var dataString : String

                    when(state) {
                        GameState.gameState.STARTED.name -> {
                            if(playerTwoName.isEmpty()) dataString = "Game Started!\n\n" + playerOneName + " is waiting for player to join"
                            else dataString = "Game Started!\n\n" + "Player One: " + playerOneName + "\nShips left: " + playerOneShipCount + "\n\nPlayer Two: " + playerTwoName + "\nShips left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.scaledstart, dataString, gameId))
                        }
                        GameState.gameState.PLAYER_ONE_TURN.name -> {
                            dataString = "Player One's Turn!\n\n" + "Player One: " + playerOneName + "\nShips left: " + playerOneShipCount + "\n\nPlayer Two: " + playerTwoName + " \nShips left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.scaledfight, dataString, gameId))
                        }
                        GameState.gameState.PLAYER_TWO_TURN.name -> {
                            dataString = "Player Two's Turn!\n\n" + "Player One: " + playerOneName + "\nShips left: " + playerOneShipCount + "\n\nPlayer Two: " + playerTwoName + " \nShips left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.scaledfight, dataString, gameId))
                        }
                        GameState.gameState.GAME_OVER_PLAYER_ONE.name -> {
                            dataString = "Player One Wins!\n\n" + "Player One: " + playerOneName + "\nShips left: " + playerOneShipCount + "\n\nPlayer Two: " + playerTwoName + " \nShips left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.scaleddelete, dataString, gameId))
                        }
                        GameState.gameState.GAME_OVER_PLAYER_TWO.name -> {
                            dataString = "Player Two Wins!\n\n" + "Player One: " + playerOneName + "\nShips left: " + playerOneShipCount + "\n\nPlayer Two: " + playerTwoName + " \nShips left: " + playerTwoShipCount
                            recyclerViewDataset.add(MyAdapter.ImageWithTitle(R.drawable.scaleddelete, dataString, gameId))
                        }
                    }
                }
                connected = true
                loadingPanel.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                setupFiles(recyclerViewDataset)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("NO INTERNET", "NO INTERNET")
                checkInternet.visibility = View.VISIBLE
                recyclerView.visibility = View.INVISIBLE
                loadingPanel.visibility = View.INVISIBLE
            }
        })
    }

    private lateinit var recyclerViewLayoutManager: LinearLayoutManager

    fun setupFiles(databaseList : MutableList<MyAdapter.MyAdapterItem>) {
        recyclerViewLayoutManager = LinearLayoutManager(this)

        my_recycler_view.setHasFixedSize(true)
        my_recycler_view.layoutManager = recyclerViewLayoutManager

        my_recycler_view.adapter = MyAdapter({
            databaseList.add(0,MyAdapter.ImageWithTitle(R.drawable.scaledplus, "\nNew Game", ""))
            databaseList.toTypedArray()
        }()).apply {
            setOnMyAdapterItemSelectedListener { myAdapterItem: MyAdapter.MyAdapterItem ->
                Log.e("FileSelection", "Listener notified of the item selection")

                when (myAdapterItem) {
                    is MyAdapter.ImageWithTitle -> {
                        Log.e("FileSelection", "Selected item contained image of Id (${myAdapterItem.button}")
                        Log.e("FileSelection", "myAdapterTitle: " + myAdapterItem.title)
                        intent = Intent(applicationContext, GameState::class.java)
                        if(myAdapterItem.title.contains("waiting for player"))
                        {
                            var matched = false
                            var pattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                            var matcher = pattern.matcher(myAdapterItem.title)
                            while(matcher.find())
                            {
                                if(matcher.group().equals(currentUser!!.email)) {
                                    intent.putExtra("isPlayerOne", true)
                                    intent.putExtra("gameId", myAdapterItem.gameId)
                                    matched = true
                                    finish()
                                    startActivity(intent)
                                }
                            }
                            if(!matched) {
                                Log.e("JOINING GAME", "Joining game!")
                                intent.putExtra("joining", true)
                                intent.putExtra("gameId", myAdapterItem.gameId)
                                finish()
                                startActivity(intent)
                            }
                        }

                        else if(myAdapterItem.title.contains("Game Started") || myAdapterItem.title.contains("Player One's Turn")
                                || myAdapterItem.title.contains("Player Two's Turn") || myAdapterItem.title.contains("Wins!"))
                        {
                            var matched = false
                            var pattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                            var matcher = pattern.matcher(myAdapterItem.title)
                            var count = 1
                            while(matcher.find())
                            {
                                if(matcher.group().equals(currentUser!!.email)) {
                                    if(count == 1)intent.putExtra("isPlayerOne", true)
                                    intent.putExtra("gameId", myAdapterItem.gameId)
                                    if(myAdapterItem.title.contains("Wins!")) intent.putExtra("gameOver", true)
                                    matched = true
                                    finish()
                                    startActivity(intent)
                                }
                                count++
                            }
                            if(!matched) {
                                Log.e("SPECTATING", "YOU ARE CURRENTLY SPECTATING!")
                                intent.putExtra("isSpectating", true)
                                intent.putExtra("gameId", myAdapterItem.gameId)
                                finish()
                                startActivity(intent)
                            }
                        }

                        else if (myAdapterItem.title.contains("New Game"))
                        {
                            Log.e("NEW GAME", "Starting new game!")
                            intent.putExtra("isPlayerOne", true)
                            intent.putExtra("New Game", "")
                            finish()
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDbRootRef.removeEventListener(childEventListener)
        unregisterReceiver(broadCastReceiver)
        handler.removeCallbacksAndMessages(null)
    }
}