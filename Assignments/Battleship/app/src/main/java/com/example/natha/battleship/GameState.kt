package com.example.natha.battleship

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.provider.ContactsContract
import android.support.v4.widget.TextViewCompat
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.game_board.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.toast
import android.view.Gravity
import android.widget.TextView




/**
 * Created by Natha on 10/20/2017.
 *
 * This class holds all the logic for the game board.
 * It sets up the players, writes to the databases, and reads from the database.
 * It also calculates each attack and performs the replay feature of the game.
 */
class GameState() : AppCompatActivity() {

    //Representation of what state the game is in
    enum class gameState
    {
        STARTED, PLAYER_ONE_TURN, PLAYER_TWO_TURN, GAME_OVER_PLAYER_ONE, GAME_OVER_PLAYER_TWO
    }

    var playerOne = Player()
    var playerTwo = Player()
    lateinit var myNameDisplay : TextView
    lateinit var enemyNameDisplay : TextView
    lateinit var mDbRoot : FirebaseDatabase
    lateinit var mDbRootRef : DatabaseReference
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var buttons : LinearLayout
    var handler = Handler()
    var state = gameState.STARTED
    var gameId = ""
    var joinGameOver = false
    var spectating = false
    var isPlayerOne = false
    var replayMiss = false
    var replaySunk = false
    var joining = false
    var replay = false

    //Chile listener that updates the game board anytime there is a change.
    val childListener = (object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
            Log.e("ONCHILDADDED", "CHILD ADDED")
        }

        override fun onChildChanged(snapshot: DataSnapshot?, p1: String?) {
            loadFromDatabase()
            Log.e("ONCHILDCHANGED", "CHILD CHANGED")
        }

        override fun onChildRemoved(snapshot: DataSnapshot?){
            Log.e("ONCHILDREMOVED", "CHILD REMOVED")
        }

        override fun onChildMoved(snapshot: DataSnapshot?, p1: String?) {
            Log.e("ONCHILDMOVED", "CHILD MOVED")
        }

        override fun onCancelled(snapshot: DatabaseError?) {
            Log.e("ONCHILDCANCELLED", "CHILD CANCELLED")
        }
    })


    //Click listener for start button or turn attack
    val clickListener = View.OnClickListener { view ->
        if(view.id == R.id.Okay)
        {
            //If the game is started and there is a player, update the state of the game and setup the player
            if(state == gameState.STARTED) {
                if (!enemyNameDisplay.text.equals("Waiting for player...") && isPlayerOne) {
                    Log.e("STARTING", "Starting game!")
                    findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                    for (i in playerOne.ships) {
                        var size = i.size
                        var count = 1
                        for (j in i.pos) {
                            var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(j.first + 12)
                            if (view is LinearLayout) {
                                var button = view.getChildAt(j.second - 1)
                                if (button is Button) {
                                    button.setBackgroundColor(Color.GRAY)
                                    if (count == 1) button.setText("**" + button.text.toString())
                                    if (count == size) button.setText(button.text.toString() + "**")
                                }
                            }
                            count++
                        }
                    }
                    state = gameState.PLAYER_ONE_TURN
                    updateDatabase(false)
                }
            }
        }

        //Pick which player is which depending on the turn and whether or not this is a replay
        //and call the method that performs the attack.
        else if(view is Button)
        {
            if(state == gameState.PLAYER_ONE_TURN) {
                if (replay) {
                    if (isPlayerOne) doAttack(view, playerOne, playerTwo)
                    else if(!isPlayerOne) doAttack(view, playerTwo, playerOne)
                }
                else if(isPlayerOne) doAttack(view, playerOne, playerTwo)
            }
            else if(state == gameState.PLAYER_TWO_TURN)
            {
                if (replay) {
                    if (isPlayerOne) doAttack(view, playerTwo, playerOne)
                    else if(!isPlayerOne) doAttack(view, playerOne, playerTwo)
                }
                else if(!isPlayerOne) doAttack(view, playerOne, playerTwo)
            }
        }
    }

    /**
     * Setup button references and member variables
     * Iterate through
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)
        mDbRoot = FirebaseDatabase.getInstance()
        mDbRootRef = mDbRoot.getReference()
        buttons = findViewById(R.id.buttons)
        myNameDisplay = findViewById(R.id.myName)
        enemyNameDisplay = findViewById(R.id.enemyName)
        auth = FirebaseAuth.getInstance()
        if(auth != null && auth.currentUser != null) currentUser = auth.currentUser!!
        findViewById<Button>(R.id.Okay).setOnClickListener(clickListener)
        mDbRootRef.addChildEventListener(childListener)

        //Saved instance state variables
        if(savedInstanceState != null) {

            Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE AFTER")

            for (i in savedInstanceState.keySet()) {
                when (i) {
                    "gameId" -> gameId = savedInstanceState.getString("gameId")
                    "joining" -> joining = savedInstanceState.getBoolean("joining")
                    "spectating" -> spectating = savedInstanceState.getBoolean("spectating")
                    "isPlayerOne" -> isPlayerOne = savedInstanceState.getBoolean("isPlayerOne")
                }
            }
        }

        //Decide what to do on activity open.
        //Player may be joining or starting a new game, or be opening a game player is currently in or finished.
        //Decide which player it is and whether or not they are spectating or not.
        else if(intent != null && intent.extras != null && !intent.extras.isEmpty)
        {
            gameId = ""
            for(i in intent.extras.keySet())
            {
                when(i) {

                    "joining" -> joining = true

                    "isPlayerOne" -> isPlayerOne = true

                    "isSpectating" -> spectating = true

                    "gameOver" -> joinGameOver = true

                    "gameId" ->
                    {
                        gameId = intent.getStringExtra("gameId")
                        loadFromDatabase()
                    }


                    //If a new game, setup both players information and update the database
                    "New Game" ->
                    {
                        var playerOneSetup = false
                        for (i in 1..2) {
                            var shipSize = 2
                            var ships = ArrayList<Ship>()
                            for (i in 1..5) {
                                var placementOkay = false
                                while (!placementOkay) {
                                    var placement = shipPlacement(shipSize)
                                    placementOkay = true

                                    for (i in ships) {
                                        for (j in i.pos) {
                                            for (k in placement) {
                                                if (k.first == j.first && k.second == j.second) placementOkay = false
                                            }
                                        }
                                    }
                                    if (placementOkay) {
                                        ships.add(Ship(shipSize, placement))
                                        if (i != 2) shipSize++
                                    }
                                }
                            }

                            if (!playerOneSetup) {
                                playerOne = Player(ships, ArrayList<Triple<Int, Int, Int>>(), ArrayList<Triple<Int, Int, Int>>(), currentUser.email!!, 5)
                                myNameDisplay.setText(currentUser.email + "//Ships Left: " + playerOne.shipCount)
                                playerOneSetup = true
                            } else
                            {
                                playerTwo = Player(ships, ArrayList<Triple<Int, Int, Int>>(), ArrayList<Triple<Int, Int, Int>>(), "", 5)
                                enemyNameDisplay.setText("Waiting for player...")
                            }
                        }
                        if(!isPlayerOne || spectating) findViewById<Button>(R.id.Okay).isClickable = false
                        updateDatabase(true)
                        setupPlayer(playerOne)
                    }
                }
            }
        }
    }


    /**
     * Do attack calculations and update the player information
     */
    fun doAttack(view : Button, player : Player, enemy : Player)
    {
        //Get X and Y value based off button label
        var xVal = view.resources.getResourceName(view.id).substringAfter("/")[0].toInt() - 64
        var yVal = Integer.parseInt(view.resources.getResourceName(view.id).substringAfter("/").substringBefore("_2").substring(1))
        var hit = false
        var sunk = false

        //Iterate through enemy ships and determine a hit or a miss
        //Keep track of shipcount, posititions, and values.
        //Disable buton on attack.
        for (i in enemy.ships) {
            var hitCount = 0
            var pos = 0
            for (j in i.pos) {
                //Ship was hit
                if(xVal == j.first && yVal == j.second) {
                    i.pos[pos] = Triple(xVal, yVal, 2)
                    view.text = "Hit"
                    enemy.oppAttacks.add(Triple(xVal, yVal, 2))
                    player.myAttacks.add(Triple(xVal, yVal, 2))
                    view.setBackgroundColor(Color.YELLOW)
                    view.isClickable = false
                    hitCount++
                    hit = true
                }
                if (j.third == 2) hitCount++
                //Ship was sunk
                if (hitCount == i.size) {
                    sunk = true
                    replaySunk = true
                    enemy.shipCount--
                }
                //Ship was sunk, go back through all buttons/data and set them all to sunk
                if (sunk) {
                    pos = 0
                    for (k in i.pos) {
                        var pos2 = 0
                        for (l in player.myAttacks) {
                            if (l.first == k.first && l.second == k.second) player.myAttacks[pos2] = Triple(l.first, l.second, 3)
                            pos2++
                        }
                        pos2 = 0
                        for (l in enemy.oppAttacks) {
                            if (l.first == k.first - 12 && l.second == k.second) enemy.oppAttacks[pos2] = Triple(l.first, l.second, 3)
                            pos2++
                        }
                        i.pos[pos] = Triple(k.first, k.second, 3)
                        var buttons = findViewById<ViewGroup>(R.id.buttons)
                        var row = buttons.getChildAt(k.first - 1)
                        if (row is LinearLayout) {
                            var button = row.getChildAt(k.second - 1)
                            if (button is Button) {
                                button.setBackgroundColor(Color.RED)
                                if (pos == 0 || pos == i.size - 1) button.text = "**Sunk**"
                                else button.text = "Sunk"
                            }
                        }
                        pos++
                    }

                    //If all ships were sunk, update game to OVER and allow no buttons to be clicked
                    if (enemy.shipCount == 0) {
                        if (state == gameState.PLAYER_ONE_TURN) {
                            state = gameState.GAME_OVER_PLAYER_ONE
                            findViewById<Button>(R.id.Okay).setText("Player One Wins!")
                        } else {
                            state = gameState.GAME_OVER_PLAYER_TWO
                            findViewById<Button>(R.id.Okay).setText("Player Two Wins!")
                        }
                        var views = findViewById<ViewGroup>(R.id.buttons)
                        for (l in 0..views.childCount - 1) {
                            var view = views.getChildAt(l)
                            if (view is LinearLayout) {
                                for (m in 0..view.childCount - 1) {
                                    var button = view.getChildAt(m)
                                    if (button is Button) button.isClickable = false
                                }
                            }
                        }
                    }
                    break
                }
                pos++
                if (sunk) break
            }
            if (sunk || hit) break
        }

        //If it is a miss, change states, data structure,  and update button to be unclickable
        if (!hit && !sunk) {
            if(replay) replayMiss = true
            view.setBackgroundColor(Color.WHITE)
            enemy.oppAttacks.add(Triple(xVal, yVal, 1))
            player.myAttacks.add(Triple(xVal, yVal, 1))
            view.setText("Miss")
            view.isClickable = false
            if (state == gameState.PLAYER_ONE_TURN) {
                findViewById<Button>(R.id.Okay).setText("Player Two's Turn")
                state = gameState.PLAYER_TWO_TURN
            } else if (state == gameState.PLAYER_TWO_TURN) {
                findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                state = gameState.PLAYER_ONE_TURN
            }
            var views = findViewById<ViewGroup>(R.id.buttons)
            for (i in 0..views.childCount - 1) {
                var view = views.getChildAt(i)
                if (view is LinearLayout) {
                    for (j in 0..view.childCount - 1) {
                        var button = view.getChildAt(j)
                        if (button is Button) {
                            button.isClickable = false
                        }
                    }
                }
            }
        }
        //update databases if not a replay
        if(!replay)updateDatabase(false)
    }

    /**
     * Calculates ship placement on board
     */
    fun shipPlacement(size : Int) : ArrayList<Triple<Int,Int,Int>>
    {
        var up = false
        var down = false
        var left = false
        var right = false
        var directionChosen = false
        var pos = ArrayList<Triple<Int,Int,Int>>()
        var random = Random()
        var firstX = random.nextInt(10 - 1) + 1
        var firstY = random.nextInt(10 - 1) + 1
        pos.add(Triple(firstX, firstY, 0))

        //Choose a random direction to go and make sure there is enough room for ship length
        for(i in 1..size)
        {
            if(!directionChosen)
            {
                while(!directionChosen)
                {
                    var nextPos = random.nextInt(4 - 1) + 1
                    when (nextPos) {
                        1 -> if (pos.last().second - size > 0) {
                            up = true
                            directionChosen = true
                        }
                        2 -> if (pos.last().second + size < 11) {
                            down = true
                            directionChosen = true
                        }
                        3 -> if (pos.last().first - size > 0) {
                            left = true
                            directionChosen = true
                        }
                        4 -> if (pos.last().first + size < 11) {
                            right = true
                            directionChosen = true
                        }
                        else Log.e("GAMESTATE", "SELECTING SHIP DIRECTION")
                    }
                }
            }

            //Increase direction for ship position data
            else if(up) pos.add(Triple(pos.last().first, pos.last().second - 1, 0))
            else if(down) pos.add(Triple(pos.last().first, pos.last().second + 1, 0))
            else if(left) pos.add(Triple(pos.last().first - 1, pos.last().second, 0))
            else if(right) pos.add(Triple(pos.last().first +1, pos.last().second, 0))
            else Log.e("GAMESTATE", "PROBLEM SELECTING SHIP DIRECTION")
        }
        return pos
    }


    /**
     * Visually setup all "current" player information
     */
    fun setupPlayer(player : Player) {

        var doneWithButton = false

        //Iterate through all buttons
        for (i in 0..buttons.childCount) {
            if (buttons.getChildAt(i) is LinearLayout) {
                for (j in 0..(buttons.getChildAt(i) as LinearLayout).childCount) {

                    //Display player ships based on data
                    for (k in player.ships) {
                        var size = k.size
                        var count = 1
                        for (l in k.pos) {
                            if (l.first + 12 == i && l.second - 1 == j) {
                                var linLay = buttons.getChildAt(i)
                                if (linLay is LinearLayout) {
                                    var button = linLay.getChildAt(j)
                                    if (button is Button) {
                                        if (l.third == 3) {
                                            if((!button.text.equals("**Sunk**") || !button.text.equals("Sunk")) && (button.background as ColorDrawable).color != Color.RED)
                                                if (count == 1 || count == size) button.setText("**Sunk**")
                                                else button.setText("Sunk")
                                            button.setBackgroundColor(Color.RED)
                                        } else if (l.third == 2) {
                                            if((!button.text.equals("**Hit**") || !button.text.equals("Hit")) && (button.background as ColorDrawable).color != Color.YELLOW)
                                                if (count == 1 || count == size) button.setText("**Hit**")
                                                else button.setText("Hit")
                                            button.setBackgroundColor(Color.YELLOW)
                                        } else if (l.third == 0) {
                                            if(!button.text.equals("**") || (button.background as ColorDrawable).color != Color.GRAY) {
                                                if (count == 1 || count == size) button.setText("**")
                                                else button.setText("")
                                                button.setBackgroundColor(Color.GRAY)
                                            }
                                        }
                                    }
                                }
                                doneWithButton = true
                                break
                            }
                            count++
                        }
                    }

                    if (!doneWithButton) {

                        //Display player attacks against opponent based on data
                        for (k in player.myAttacks) {
                            if (k.first - 1 == i && k.second - 1 == j) {
                                var linLay = buttons.getChildAt(i)
                                if (linLay is LinearLayout) {
                                    var button = linLay.getChildAt(j)
                                    if (button is Button) {
                                        var endOfShip = false
                                        for (l in playerTwo.ships) {
                                            if ((l.pos.first().first == k.first && l.pos.first().second == k.second) || (l.pos.last().first == k.first && l.pos.last().second == k.second)) endOfShip = true
                                        }
                                        if (k.third == 3) {
                                            if((!button.text.equals("Sunk") || !button.text.equals("**Sunk**")) && (button.background as ColorDrawable).color != Color.RED) {
                                                if (endOfShip) button.setText("**Sunk**")
                                                else button.setText("Sunk")
                                                button.setBackgroundColor(Color.RED)
                                            }
                                        } else if (k.third == 2) {
                                            if(!button.text.equals("Hit") && (button.background as ColorDrawable).color != Color.YELLOW) {
                                                button.setText("Hit")
                                                button.setBackgroundColor(Color.YELLOW)
                                            }
                                        } else if (k.third == 1) {
                                            if(!button.text.equals("Miss") && (button.background as ColorDrawable).color != Color.WHITE) {
                                                button.setText("Miss")
                                                button.setBackgroundColor(Color.WHITE)
                                            }
                                        }
                                    }
                                }
                                doneWithButton = true
                                break
                            }
                        }
                    }

                    if (!doneWithButton) {

                        //Display opponent attacks against player based on data
                        for (k in player.oppAttacks) {
                            if (k.first + 12 == i && k.second - 1 == j) {
                                var linLay = buttons.getChildAt(i)
                                if (linLay is LinearLayout) {
                                    var button = linLay.getChildAt(j)
                                    if (button is Button) {
                                        if (k.third == 3) {
                                            if((!button.text.equals("Sunk") || !button.text.equals("**Sunk**")) && (button.background as ColorDrawable).color != Color.RED) {
                                                if (button.text.contains("*")) button.setText("**Sunk**")
                                                else button.setText("Sunk")
                                                button.setBackgroundColor(Color.RED)
                                            }
                                        } else if (k.third == 2) {
                                            if((!button.text.equals("Hit") || !button.text.equals("**Hit**")) && (button.background as ColorDrawable).color != Color.YELLOW) {
                                                if (button.text.contains("*")) button.setText("**Hit**")
                                                else button.setText("Hit")
                                                button.setBackgroundColor(Color.YELLOW)
                                            }
                                        } else if (k.third == 1) {
                                            if(!button.text.equals("Miss") && (button.background as ColorDrawable).color != Color.WHITE) {
                                                button.setText("Miss")
                                                button.setBackgroundColor(Color.WHITE)
                                            }
                                        }
                                    }
                                }
                                doneWithButton = true
                                break
                            }
                        }
                    }

                    //Default button to blue because nothing has happened yet
                    if (((isPlayerOne && state == GameState.gameState.PLAYER_ONE_TURN) || (!isPlayerOne && state == GameState.gameState.PLAYER_TWO_TURN) || spectating || replay) && !doneWithButton)
                    {
                        var linLay = buttons.getChildAt(i)
                        if(linLay is LinearLayout)
                        {
                            var button = linLay.getChildAt(j)
                            if(button is Button)
                            {
                                if(button.id != R.id.Okay)
                                {
                                    button.setOnClickListener(clickListener)
                                    //Buttons are not clickable during spectaion or replay
                                    if(spectating || replay) button.isClickable = false
                                    button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                                    button.setText("")
                                    doneWithButton = true
                                }
                            }
                        }
                    }

                    doneWithButton = false
                }
            }
        }
    }

    /**
     * Remove event listener on activity finish
     */
    override fun onBackPressed() {
        var intent = Intent(this, Game::class.java)
        mDbRootRef.removeEventListener(childListener)
        startActivity(intent)
        finish()
    }

    /**
     * Reload state of game on close/open of app
     */
    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        if(savedInstanceState !is Bundle) return
        Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE BEFORE")
        savedInstanceState.putString("gameId", gameId)
        savedInstanceState.putBoolean("joining", joining)
        savedInstanceState.putBoolean("spectating", spectating)
        savedInstanceState.putBoolean ("isPlayerOne", isPlayerOne)
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Update Firebase database with all player info and state of game
     */
    fun updateDatabase(newGame : Boolean)
    {
        if(newGame)
        {
            Log.e("UPDATE", "Updating database with new game")
            var gamesRef = mDbRoot.getReference("Games")
            gameId = gamesRef.push().key
            gamesRef.child(gameId).setValue("Game")
            gamesRef.child(gameId).child("Game State").setValue(state)
            gamesRef.child(gameId).child("Player One").setValue(playerOne)
            gamesRef.child(gameId).child("Player Two").setValue(playerTwo)
        }

        else
        {
            Log.e("UPDATE", "Updating database with current game")
            var gamesRef = mDbRoot.getReference("Games")
            gamesRef.child(gameId).child("Game State").setValue(state)
            if(isPlayerOne)
            {
                gamesRef.child(gameId).child("Player One").setValue(playerOne)
                gamesRef.child(gameId).child("Player Two").setValue(playerTwo)
            }
            else if(!isPlayerOne)
            {
                gamesRef.child(gameId).child("Player One").setValue(playerTwo)
                gamesRef.child(gameId).child("Player Two").setValue(playerOne)
            }
        }

    }

    /**
     * Load all information from firebase on game change or entering a game
     */
    fun loadFromDatabase()
    {

        //Iterate through data on specific game given an ID from the recycler view click
        FirebaseDatabase.getInstance().reference.child("Games").addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(isPlayerOne) Log.e("IM PLAYER ONE!!!!", "IM PLAYER ONE!!!!")
                if(!isPlayerOne) Log.e("IM PLAYER TWO!!!!", "IM PLAYER TWO!!!!")

                var game : DataSnapshot

                if(!dataSnapshot.hasChild(gameId))
                {
                    Log.e("GAMSESTATE", "Game: " + gameId + " does not exist!")
                    return
                }

                game = dataSnapshot.child(gameId)

                Log.e("GAME READ", "Game key is: " + game.key + ", Game value is: " + game.value)

                var player : DataSnapshot? = null
                var enemy : DataSnapshot? = null
                var ships : DataSnapshot? = null
                var myAttacks : DataSnapshot? = null
                var oppAttacks : DataSnapshot? = null

                var shipsData = ArrayList<Ship>()
                var myAttacksData = ArrayList<Triple<Int,Int,Int>>()
                var oppAttacksData = ArrayList<Triple<Int,Int,Int>>()
                var playerShipCount = -1
                var playerName = ""
                var stateOfGame = ""
                var currentPlayer = ""
                var enemyPlayer = ""

                //STATE OF GAME
                if(game.hasChild("Game State"))
                {
                    stateOfGame = game.child("Game State").value as String
                    state = gameState.valueOf(stateOfGame)
                    Log.e("GAME STATE", stateOfGame)
                }

                else
                {
                    Log.e("ERROR", "ERROR GETTING GAME INFO")
                    return
                }

                //Display enemy name if there is one
                if(state == gameState.STARTED && isPlayerOne)
                {
                    if(game.hasChild("Player Two") && game.child("Player Two").hasChild("name"))
                    {
                        if(!game.child("Player Two").child("name").value.toString().isEmpty()) {
                            playerTwo.name = game.child("Player Two").child("name").value.toString()
                            enemyNameDisplay.setText(game.child("Player Two").child("name").value.toString() + "//Ships Left: " + playerTwo.shipCount)
                        }
                    }

                    else
                    {
                        Log.e("ERROR", "ERROR GETTING GAME INFO")
                        return
                    }
                }

                //Select which player we are loading for the local data
                if(joining)
                {
                    currentPlayer = "Player Two"
                    enemyPlayer = "Player One"
                }
                else if(spectating)
                {
                    if(state == gameState.PLAYER_ONE_TURN || state == gameState.STARTED || state == gameState.GAME_OVER_PLAYER_ONE)
                    {
                        currentPlayer = "Player One"
                        enemyPlayer = "Player Two"
                    }
                    else if(state == gameState.PLAYER_TWO_TURN || state == gameState.GAME_OVER_PLAYER_TWO)
                    {
                        currentPlayer = "Player Two"
                        enemyPlayer = "Player One"
                    }
                }
                else if(!isPlayerOne)
                {
                    currentPlayer = "Player Two"
                    enemyPlayer = "Player One"
                }
                else if (isPlayerOne)
                {
                    currentPlayer = "Player One"
                    enemyPlayer = "Player Two"
                }

                //Select which player from database based on which player we are
                if(game.hasChild(currentPlayer)) player = game.child(currentPlayer)
                if(game.hasChild(enemyPlayer)) enemy = game.child(enemyPlayer)

                //FIRST PLAYER SHIPCOUNT
                if(player != null && player.hasChild("shipCount"))
                {
                    Log.e("PLAYER ONE SHIP COUNT", player.child("shipCount").value.toString())
                    playerShipCount = Integer.parseInt(player.child("shipCount").value.toString())
                }

                //FIRST PLAYER NAME
                if(player != null && player.hasChild("name"))
                {
                    Log.e("PLAYER ONE NAME", player.child("name").value.toString())
                    if(joining && currentUser != null && currentUser is FirebaseUser) playerName = currentUser.email!!
                    else playerName = player.child("name").value.toString()
                    myNameDisplay.setText(playerName + "//Ships Left: " + playerShipCount)
                }


                //FIRST PLAYER SHIPS
                if(player != null && player.hasChild("ships")) ships = player.child("ships")

                if(ships != null) for(ship in ships.children)
                {
                    var shipSize = 0
                    var shipArray = ArrayList<Triple<Int,Int,Int>>()
                    if(ship.hasChild("size")) shipSize = Integer.parseInt(ship.child("size").value.toString())
                    Log.e("PLAYER ONE SHIP", "SHIP SIZE: " + shipSize)
                    for(shipData in ship.children)
                    {

                        for(posData in shipData.children)
                        {
                            if(posData.hasChild("first") && posData.hasChild("second") && posData.hasChild("third"))
                            {
                                shipArray.add(Triple(Integer.parseInt(posData.child("first").value.toString()),
                                        Integer.parseInt(posData.child("second").value.toString()),
                                        Integer.parseInt(posData.child("third").value.toString())))
                                Log.e("PLAYER ONE SHIP POSITION DATA", "first: " + Integer.parseInt(posData.child("first").value.toString()) + ", second: " + Integer.parseInt(posData.child("second").value.toString()) + ", third: " + Integer.parseInt(posData.child("third").value.toString()))
                            }
                        }
                        if(!shipData.key.equals("size")) shipsData.add(Ship(shipSize, shipArray))
                    }
                }

                //FIRST PLAYER MYATTACKS
                if(player != null && player.hasChild("myAttacks")) myAttacks = player.child("myAttacks")

                if(myAttacks != null) for(attacks in myAttacks.children)
                {
                    if(attacks.hasChild("first") && attacks.hasChild("second") && attacks.hasChild("third"))
                    {
                        myAttacksData.add(Triple(Integer.parseInt(attacks.child("first").value.toString()),
                                Integer.parseInt(attacks.child("second").value.toString()),
                                Integer.parseInt(attacks.child("third").value.toString())))
                        Log.e("PLAYER ONE ATTACKS DATA", "first: " + Integer.parseInt(attacks.child("first").value.toString()) + ", second: " + Integer.parseInt(attacks.child("second").value.toString()) + ", third: " + Integer.parseInt(attacks.child("third").value.toString()))

                    }
                }

                //FIRST PLAYER OPPONENT ATTACKS
                if(enemy != null && enemy.hasChild("myAttacks")) oppAttacks = enemy.child("myAttacks")

                if(oppAttacks != null) for(attacks in oppAttacks.children)
                {
                    if(attacks.hasChild("first") && attacks.hasChild("second") && attacks.hasChild("third"))
                    {
                        oppAttacksData.add(Triple(Integer.parseInt(attacks.child("first").value.toString()),
                                Integer.parseInt(attacks.child("second").value.toString()),
                                Integer.parseInt(attacks.child("third").value.toString())))
                        Log.e("PLAYER ONE OPP ATTACKS DATA", "first: " + Integer.parseInt(attacks.child("first").value.toString()) + ", second: " + Integer.parseInt(attacks.child("second").value.toString()) + ", third: " + Integer.parseInt(attacks.child("third").value.toString()))

                    }
                }

                //Create the player
                playerOne = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)


                //Reset data for loading other player data
                shipsData = ArrayList<Ship>()
                oppAttacksData = ArrayList<Triple<Int,Int,Int>>()
                myAttacksData = ArrayList<Triple<Int,Int,Int>>()
                player = null
                enemy  = null
                ships  = null
                myAttacks = null
                oppAttacks = null

                //Determine other player to use from database
                if(currentPlayer.equals("Player One"))
                {
                    currentPlayer = "Player Two"
                    enemyPlayer = "Player One"
                }

                else
                {
                    currentPlayer = "Player One"
                    enemyPlayer = "Player Two"
                }

                if(game.hasChild(currentPlayer)) player = game.child(currentPlayer)
                if(game.hasChild(enemyPlayer)) enemy = game.child(enemyPlayer)

                //SECOND PLAYER SHIPCOUNT
                if(player != null && player.hasChild("shipCount"))
                {
                    Log.e("PLAYER TWO SHIP COUNT", player.child("shipCount").value.toString())
                    playerShipCount = Integer.parseInt(player.child("shipCount").value.toString())
                }

                //SECOND PLAYER NAME
                if(player != null && player.hasChild("name"))
                {
                    Log.e("PLAYER TWO NAME", player.child("name").value.toString())
                    playerName = player.child("name").value.toString()
                    if(playerName.isEmpty()) enemyNameDisplay.setText("Waiting for player...")
                    else if(spectating) enemyNameDisplay.setText("You are spectating...")
                    else enemyNameDisplay.setText(playerName + "//Ships Left: " + playerShipCount)

                }

                //SECOND PLAYER SHIPS
                if(player != null && player.hasChild("ships")) ships = player.child("ships")

                if(ships != null) for(ship in ships.children)
                {
                    var shipSize = 0
                    var shipArray = ArrayList<Triple<Int,Int,Int>>()
                    if(ship.hasChild("size")) shipSize = Integer.parseInt(ship.child("size").value.toString())
                    Log.e("PLAYER TWO SHIP", "SHIP SIZE: " + shipSize)
                    for(shipData in ship.children)
                    {
                        for(posData in shipData.children)
                        {
                            if(posData.hasChild("first") && posData.hasChild("second") && posData.hasChild("third"))
                            {
                                shipArray.add(Triple(Integer.parseInt(posData.child("first").value.toString()),
                                        Integer.parseInt(posData.child("second").value.toString()),
                                        Integer.parseInt(posData.child("third").value.toString())))
                                Log.e("PLAYER TWO SHIP POSITION DATA", "first: " + Integer.parseInt(posData.child("first").value.toString()) + ", second: " + Integer.parseInt(posData.child("second").value.toString()) + ", third: " + Integer.parseInt(posData.child("third").value.toString()))

                            }
                        }
                        if(!shipData.key.equals("size")) shipsData.add(Ship(shipSize, shipArray))
                    }
                }

                //SECOND PLAYER MYATTACKS
                if(player != null && player.hasChild("myAttacks")) myAttacks = player.child("myAttacks")

                if(myAttacks != null) for(attacks in myAttacks.children)
                {
                    if(attacks.hasChild("first") && attacks.hasChild("second") && attacks.hasChild("third"))
                    {
                        myAttacksData.add(Triple(Integer.parseInt(attacks.child("first").value.toString()),
                                Integer.parseInt(attacks.child("second").value.toString()),
                                Integer.parseInt(attacks.child("third").value.toString())))
                        Log.e("PLAYER TWO ATTACKS DATA", "first: " + Integer.parseInt(attacks.child("first").value.toString()) + ", second: " + Integer.parseInt(attacks.child("second").value.toString()) + ", third: " + Integer.parseInt(attacks.child("third").value.toString()))
                    }
                }

                //SECOND PLAYER OPPONENT ATTACKS
                if(enemy != null && enemy.hasChild("myAttacks")) oppAttacks = enemy.child("myAttacks")

                if(oppAttacks != null) for(attacks in oppAttacks.children)
                {
                    if(attacks.hasChild("first") && attacks.hasChild("second") && attacks.hasChild("third"))
                    {
                        oppAttacksData.add(Triple(Integer.parseInt(attacks.child("first").value.toString()),
                                Integer.parseInt(attacks.child("second").value.toString()),
                                Integer.parseInt(attacks.child("third").value.toString())))
                        Log.e("PLAYER TWO OPP ATTACKS DATA", "first: " + Integer.parseInt(attacks.child("first").value.toString()) + ", second: " + Integer.parseInt(attacks.child("second").value.toString()) + ", third: " + Integer.parseInt(attacks.child("third").value.toString()))

                    }
                }

                //Create other player
                playerTwo = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)

                //Setup player visually based on state of game
                setupPlayer(playerOne)
                when(stateOfGame)
                {
                    gameState.PLAYER_ONE_TURN.name, gameState.PLAYER_TWO_TURN.name ->
                    {
                        findViewById<Button>(R.id.Okay).isClickable = false
                        if(stateOfGame == gameState.PLAYER_ONE_TURN.name)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player One's Turn")

                            if(spectating)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else if(isPlayerOne)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else
                            {
                                enemyNameDisplay.setTextColor(Color.YELLOW)
                                myNameDisplay.setTextColor(Color.WHITE)
                                for (i in 0..buttons.childCount - 1) {
                                    var view = buttons.getChildAt(i)
                                    if (view is LinearLayout) {
                                        for (j in 0..view.childCount - 1) {
                                            var button = view.getChildAt(j)
                                            if (button is Button) {
                                                button.isClickable = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else if(stateOfGame == gameState.PLAYER_TWO_TURN.name)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player Two's Turn")

                            if(spectating)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else if(!isPlayerOne)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else
                            {
                                enemyNameDisplay.setTextColor(Color.YELLOW)
                                myNameDisplay.setTextColor(Color.WHITE)
                                for (i in 0..buttons.childCount - 1) {
                                    var view = buttons.getChildAt(i)
                                    if (view is LinearLayout) {
                                        for (j in 0..view.childCount - 1) {
                                            var button = view.getChildAt(j)
                                            if (button is Button) {
                                                button.isClickable = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    gameState.GAME_OVER_PLAYER_ONE.name, gameState.GAME_OVER_PLAYER_TWO.name ->
                    {
                        for (j in 0..buttons.childCount - 1) {
                            var view = buttons.getChildAt(j)
                            if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        button.isClickable = false
                                    }
                                }
                            }
                        }
                        if(stateOfGame == gameState.GAME_OVER_PLAYER_ONE.name)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player One Wins!!")

                            if(spectating)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else if(isPlayerOne)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else
                            {
                                enemyNameDisplay.setTextColor(Color.YELLOW)
                                myNameDisplay.setTextColor(Color.WHITE)
                            }
                        }
                        else if(stateOfGame == gameState.GAME_OVER_PLAYER_TWO.name)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player Two Wins!!")
                            if(spectating)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else if(!isPlayerOne)
                            {
                                myNameDisplay.setTextColor(Color.YELLOW)
                                enemyNameDisplay.setTextColor(Color.WHITE)
                            }
                            else
                            {
                                enemyNameDisplay.setTextColor(Color.YELLOW)
                                myNameDisplay.setTextColor(Color.WHITE)
                            }
                        }

                        //If the game is over and you are joining it, prompt player for a replay of the game
                        if(joinGameOver) {
                            var alert = alert("") {
                                neutralPressed("Yes") {
                                    replay = true
                                    replayGame()
                                }
                                negativeButton("No") {

                                }
                            }
                            val myMsg = TextView(applicationContext)
                            myMsg.text = "Would you like to see a replay of this game?"
                            myMsg.setTextColor(Color.WHITE)
                            myMsg.gravity = Gravity.CENTER
                            alert.customView = myMsg
                            alert.show().setCanceledOnTouchOutside(false)
                            joinGameOver = false
                        }
                    }
                    gameState.STARTED.name ->
                    {
                        for (j in 0..buttons.childCount - 1) {
                            var view = buttons.getChildAt(j)
                            if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        button.isClickable = false
                                    }
                                }
                            }
                        }
                        findViewById<Button>(R.id.Okay).setText("Start")
                        if(spectating)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                            myNameDisplay.setTextColor(Color.YELLOW)
                        }
                        else if(isPlayerOne)findViewById<Button>(R.id.Okay).isClickable = true
                        else if(!isPlayerOne)
                        {
                            findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                            enemyNameDisplay.setTextColor(Color.YELLOW)
                        }
                        if(joining)
                        {
                            joining = false
                            updateDatabase(false)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    /**
     * Replay the game move by move with a slight delay in between each turn and from each player's view
     */
    fun replayGame()
    {
        //Create data structures for attack positions and set state to first player turn
        var playerOneAttacks = ArrayList<Pair<Int,Int>>()
        var playerTwoAttacks = ArrayList<Pair<Int,Int>>()
        state = gameState.PLAYER_ONE_TURN


        //Add attack positions from player data into our data structure
        for(i in playerOne.myAttacks) playerOneAttacks.add(Pair(i.first, i.second))

        for(i in playerTwo.myAttacks) playerTwoAttacks.add(Pair(i.first,  i.second))

        //Reset ship status on each player
        for(i in playerOne.ships)
        {
            var count = 0
            for(j in i.pos)
            {
                i.pos[count] = Triple(j.first, j.second, 0)
                count++
            }
        }

        for(i in playerTwo.ships)
        {
            var count = 0
            for(j in i.pos)
            {
                i.pos[count] = Triple(j.first, j.second, 0)
                count++
            }
        }

        //Reset player attack information and shipcount
        playerOne.myAttacks.clear()
        playerOne.oppAttacks.clear()
        playerTwo.myAttacks.clear()
        playerTwo.oppAttacks.clear()

        playerOne.shipCount = 5
        playerTwo.shipCount = 5

        //Replay variables
        var showBoard = true
        var playerOneAttackCount = 0
        var playerTwoAttackCount = 0
        var maxTurns : Int
        maxTurns = (playerTwoAttacks.size + playerOneAttacks.size) * 3

        //Iterate over a MAX value of turns until end of game, updating everything each second
        for(i in 1..maxTurns) {
            handler.postDelayed(Runnable {
                Log.e("REPLAY RUNNING", "REPLAY RUNNING")

                //Game must be in state of playing
                if(state != gameState.GAME_OVER_PLAYER_TWO || state != gameState.GAME_OVER_PLAYER_ONE) {

                    //If player one, read from correct attack list and perform the attack, creating manual click event
                    //Depends on which state it is as well
                    if (isPlayerOne) {
                        if (state == gameState.PLAYER_ONE_TURN) {
                            myNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                            enemyNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                            myNameDisplay.setTextColor(Color.YELLOW)
                            enemyNameDisplay.setTextColor(Color.WHITE)
                            findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                            if (showBoard) {
                                setupPlayer(playerOne)
                                showBoard = false
                            } else if (!showBoard) {
                                var linLay = buttons.getChildAt(playerOneAttacks[playerOneAttackCount].first-1) as LinearLayout
                                var button = linLay.getChildAt(playerOneAttacks[playerOneAttackCount].second-1) as Button
                                button.performClick()
                                if (replayMiss) {
                                    showBoard = true
                                    replayMiss = false
                                }
                                else if(replaySunk)
                                {
                                    enemyNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                                    replaySunk = false
                                }
                                playerOneAttackCount++
                            }
                        }

                        else if (state == gameState.PLAYER_TWO_TURN) {
                            myNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                            enemyNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                            myNameDisplay.setTextColor(Color.YELLOW)
                            enemyNameDisplay.setTextColor(Color.WHITE)
                            findViewById<Button>(R.id.Okay).setText("Player Two's Turn")
                            if (showBoard) {
                                setupPlayer(playerTwo)
                                showBoard = false
                            } else if (!showBoard) {
                                var linLay = buttons.getChildAt(playerTwoAttacks[playerTwoAttackCount].first-1) as LinearLayout
                                var button = linLay.getChildAt(playerTwoAttacks[playerTwoAttackCount].second-1) as Button
                                button.performClick()
                                if (replayMiss) {
                                    showBoard = true
                                    replayMiss = false
                                }
                                else if(replaySunk)
                                {
                                    enemyNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                                    replaySunk = false
                                }
                                playerTwoAttackCount++
                            }
                        }
                    }
                    //If player two, read from correct attack list and perform the attack, creating manual click event
                    //Depends on which state as well
                    else {
                        if (state == gameState.PLAYER_ONE_TURN) {
                            myNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                            enemyNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                            myNameDisplay.setTextColor(Color.YELLOW)
                            enemyNameDisplay.setTextColor(Color.WHITE)
                            findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                            if (showBoard) {
                                setupPlayer(playerTwo)
                                showBoard = false
                            } else if (!showBoard) {
                                var linLay = buttons.getChildAt(playerTwoAttacks[playerTwoAttackCount].first-1) as LinearLayout
                                var button = linLay.getChildAt(playerTwoAttacks[playerTwoAttackCount].second-1) as Button
                                button.performClick()
                                if (replayMiss) {
                                    showBoard = true
                                    replayMiss = false
                                }
                                else if(replaySunk)
                                {
                                    enemyNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                                    replaySunk = false
                                }
                                playerTwoAttackCount++
                            }
                        } else if (state == gameState.PLAYER_TWO_TURN) {
                            myNameDisplay.text = playerOne.name + "//Ships Left " + playerOne.shipCount
                            enemyNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                            myNameDisplay.setTextColor(Color.YELLOW)
                            enemyNameDisplay.setTextColor(Color.WHITE)
                            findViewById<Button>(R.id.Okay).setText("Player Two's Turn")
                            if (showBoard) {
                                setupPlayer(playerOne)
                                showBoard = false
                            } else if (!showBoard) {
                                var linLay = buttons.getChildAt(playerOneAttacks[playerOneAttackCount].first-1) as LinearLayout
                                var button = linLay.getChildAt(playerOneAttacks[playerOneAttackCount].second-1)
                                button.performClick()
                                if (replayMiss) {
                                    showBoard = true
                                    replayMiss = false
                                }
                                else if(replaySunk)
                                {
                                    enemyNameDisplay.text = playerTwo.name + "//Ships Left " + playerTwo.shipCount
                                    replaySunk = false
                                }
                                playerOneAttackCount++
                            }
                        }
                    }
                }

            }, (i*1000).toLong())
        }
    }

    /**
     * Destroy handler on activity destroy
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}