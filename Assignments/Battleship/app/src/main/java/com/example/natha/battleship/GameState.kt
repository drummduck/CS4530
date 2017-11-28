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
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


/**
 * Created by Natha on 10/20/2017.
 */
class GameState() : AppCompatActivity() {

    enum class gameState
    {
        STARTED, PLAYER_ONE_TURN, PLAYER_TWO_TURN, GAME_OVER_PLAYER_ONE, GAME_OVER_PLAYER_TWO
    }

    lateinit var playerOne : Player
    lateinit var playerTwo : Player
    lateinit var myNameDisplay : TextView
    lateinit var enemyNameDisplay : TextView
    lateinit var mDbRoot : FirebaseDatabase
    lateinit var mDbRootRef : DatabaseReference
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    var state = gameState.STARTED
    var gameId = ""
    var spectating = false
    var isPlayerOne = false
    var joining = false


    val clickListener = View.OnClickListener { view ->
        if(view.id == R.id.Okay)
        {
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
                    setupCoordinateButtons()
                    state = gameState.PLAYER_ONE_TURN
                    updateDatabase(false)
                }
            }
        }

        else if(view is Button)
        {
            var xVal = view.resources.getResourceName(view.id).substringAfter("/")[0].toInt() - 64
            var yVal = Integer.parseInt(view.resources.getResourceName(view.id).substringAfter("/").substringBefore("_2").substring(1))
            var hit = false
            var sunk = false
            if(state == gameState.PLAYER_ONE_TURN || state == gameState.PLAYER_TWO_TURN) {
                var currentPlayer: Player
                var opponentPlayer: Player
                if (state == gameState.PLAYER_ONE_TURN) {
                    currentPlayer = playerOne
                    opponentPlayer = playerTwo
                } else {
                    currentPlayer = playerTwo
                    opponentPlayer = playerOne
                }
                for (i in opponentPlayer.ships) {
                    var hitCount = 0
                    var pos = 0
                    for (j in i.pos) {
                        if (xVal == j.first && yVal == j.second) {
                            i.pos[pos] = Triple(xVal, yVal, 2)
                            view.text = "Hit"
                            opponentPlayer.oppAttacks.add(Triple(xVal, yVal, 2))
                            currentPlayer.myAttacks.add(Triple(xVal, yVal, 2))
                            view.setBackgroundColor(Color.YELLOW)
                            view.isClickable = false
                            hitCount++
                            hit = true
                        }
                        if (j.third == 2) hitCount++
                        if (hitCount == i.size) {
                            sunk = true
                            opponentPlayer.shipCount--
                        }
                        if (sunk) {
                            pos = 0
                            for (k in i.pos) {
                                var pos2 = 0
                                for (l in currentPlayer.myAttacks) {

                                    if (l.first == k.first && l.second == k.second) currentPlayer.myAttacks[pos2] = Triple(l.first, l.second, 3)
                                    pos2++
                                }
                                pos2 = 0
                                for (l in opponentPlayer.oppAttacks) {
                                    if (l.first == k.first && l.second == k.second) opponentPlayer.oppAttacks[pos2] = Triple(l.first, l.second, 3)
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

                            if (opponentPlayer.shipCount == 0) {
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
                if (!hit && !sunk) {
                    view.setBackgroundColor(Color.WHITE)
                    opponentPlayer.oppAttacks.add(Triple(xVal, yVal, 1))
                    currentPlayer.myAttacks.add(Triple(xVal, yVal, 1))
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
            }
            updateDatabase(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)
        mDbRoot = FirebaseDatabase.getInstance()
        mDbRootRef = mDbRoot.getReference()
        myNameDisplay = findViewById(R.id.myName)
        enemyNameDisplay = findViewById(R.id.enemyName)
        auth = FirebaseAuth.getInstance()
        if(auth != null && auth.currentUser != null) currentUser = auth.currentUser!!
        findViewById<Button>(R.id.Okay).setOnClickListener(clickListener)
        mDbRootRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
                if(loadFromDatabase(snapshot))
                    loadFromDatabase(gameId, joining, spectating, isPlayerOne)
                Log.e("ONCHILDADDED", "CHILD ADDED")
            }

            override fun onChildChanged(snapshot: DataSnapshot?, p1: String?) {
                if(loadFromDatabase(snapshot))
                loadFromDatabase(gameId, joining, spectating, isPlayerOne)
                Log.e("ONCHILDCHANGED", "CHILD CHANGED")
            }

            override fun onChildRemoved(snapshot: DataSnapshot?){
                if(loadFromDatabase(snapshot))
                loadFromDatabase(gameId, joining, spectating, isPlayerOne)
                Log.e("ONCHILDREMOVED", "CHILD REMOVED")
            }

            override fun onChildMoved(snapshot: DataSnapshot?, p1: String?) {
                if(loadFromDatabase(snapshot))
                loadFromDatabase(gameId, joining, spectating, isPlayerOne)
                Log.e("ONCHILDMOVED", "CHILD MOVED")
            }

            override fun onCancelled(snapshot: DatabaseError?) {
                Log.e("ONCHILDCANCELLED", "CHILD CANCELLED")
            }
        })
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
        else if(intent != null && intent.extras != null && !intent.extras.isEmpty)
        {
            gameId = ""
            for(i in intent.extras.keySet())
            {
                when(i) {

                    "joining" -> joining = true

                    "isPlayerOne" -> isPlayerOne = true

                    "isSpectating" -> spectating = true

                    "gameId" ->
                    {

                        gameId = intent.getStringExtra("gameId")
                        loadFromDatabase(gameId, joining, spectating, isPlayerOne)
                    }

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
                    }
                }
            }
        }
    }

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
            else if(up) pos.add(Triple(pos.last().first, pos.last().second - 1, 0))
            else if(down) pos.add(Triple(pos.last().first, pos.last().second + 1, 0))
            else if(left) pos.add(Triple(pos.last().first - 1, pos.last().second, 0))
            else if(right) pos.add(Triple(pos.last().first +1, pos.last().second, 0))
            else Log.e("GAMESTATE", "PROBLEM SELECTING SHIP DIRECTION")
        }
        return pos
    }

    fun setupPlayer(player : Player)
    {
        for(i in player.ships)
        {
            var size = i.size
            var count = 1
            for(j in i.pos)
            {
                var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(j.first+12)
                if(view is LinearLayout)
                {
                    var button = view.getChildAt(j.second-1)
                    if(button is Button)
                    {
                        if(j.third == 3)
                        {
                            if(count == 1 || count == size) button.setText("**Sunk**")
                            else button.setText("Sunk")
                            button.setBackgroundColor(Color.RED)
                        }
                        else if(j.third == 2)
                        {
                            if(count == 1 || count == size) button.setText("**Hit**")
                            else button.setText("Hit")
                            button.setBackgroundColor(Color.YELLOW)
                        }
                        else if(j.third == 0)
                        {
                            if(count == 1 || count == size) button.setText("**")
                            button.setBackgroundColor(Color.GRAY)
                        }
                    }
                }
                count++
            }
        }


        for(i in player.myAttacks)
        {
            var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(i.first-1)
            if(view is LinearLayout)
            {
                var button = view.getChildAt(i.second-1)
                if(button is Button)
                {
                    var oppPlayer : Player
                    var  endOfShip = false
                    if(state == gameState.PLAYER_ONE_TURN) oppPlayer = playerTwo
                    else oppPlayer = playerOne
                    for(j in oppPlayer.ships)
                    {
                        if((j.pos.first().first == i.first && j.pos.first().second == i.second) || (j.pos.last().first == i.first && j.pos.last().second == i.second)) endOfShip = true
                    }
                    if(i.third == 3)
                    {
                        if(endOfShip) button.setText("**Sunk**")
                        else button.setText("Sunk")
                        button.setBackgroundColor(Color.RED)
                        button.isClickable = false
                    }
                    else if(i.third == 2)
                    {
                        button.setText("Hit")
                        button.setBackgroundColor(Color.YELLOW)
                        button.isClickable = false
                    }
                    else if(i.third == 1)
                    {
                        button.setText("Miss")
                        button.setBackgroundColor(Color.WHITE)
                        button.isClickable = false
                    }
                }
            }
        }


        for(i in player.oppAttacks)
        {
            var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(i.first+12)
            if(view is LinearLayout)
            {
                var button = view.getChildAt(i.second-1)
                if(button is Button)
                {
                    if(i.third == 3)
                    {
                        if(button.text.contains("*"))button.setText("**Sunk**")
                        else button.setText("Sunk")
                        button.setBackgroundColor(Color.RED)
                    }
                    else if(i.third == 2)
                    {
                        if(button.text.contains("*"))button.setText("**Hit**")
                        else button.setText("Hit")
                        button.setBackgroundColor(Color.YELLOW)
                    }

                    else if(i.third == 1)
                    {
                        button.setText("Miss")
                        button.setBackgroundColor(Color.WHITE)
                    }
                }
            }
        }

        for(i in 0..10)
        {
            var child = findViewById<ViewGroup>(R.id.buttons).getChildAt(i)
            if(child is LinearLayout)
            {
                for(i in 0..child.childCount-1)
                {
                    var child2 = child.getChildAt(i)
                    val buttonColor = child2.background as ColorDrawable
                    if(child2 is Button && buttonColor.color == resources.getColor(android.R.color.holo_blue_light))
                    {
                        if(!spectating)child2.isClickable = true
                        child2.setOnClickListener(clickListener)
                    }
                }
            }
        }
    }

    fun setupCoordinateButtons()
    {
        //Setup click listener for each button
        for(i in 0..10)
        {
            var child = findViewById<ViewGroup>(R.id.buttons).getChildAt(i)
            if(child is LinearLayout)
            {
                for(i in 0..child.childCount-1)
                {
                    var child2 = child.getChildAt(i)
                    if(child2 is Button) child2.setOnClickListener(clickListener)
                }
            }
        }
    }


    override fun onBackPressed() {
        var intent = Intent(this, Game::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        if(savedInstanceState !is Bundle) return
        Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE BEFORE")
        savedInstanceState.putString("gameId", gameId)
        savedInstanceState.putBoolean("joining", joining)
        savedInstanceState.putBoolean("spectating", spectating)
        savedInstanceState.putBoolean ("isPlayerOne", isPlayerOne)
        super.onSaveInstanceState(savedInstanceState)
    }

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
            gamesRef.child(gameId).removeValue()
            gamesRef.child(gameId).child("Game State").setValue(state)
            if(isPlayerOne) gamesRef.child(gameId).child("Player One").setValue(playerOne)
            else if(!isPlayerOne) gamesRef.child(gameId).child("Player Two").setValue(playerTwo)
        }

    }

    fun loadFromDatabase(gameId : String, joining : Boolean, spectating : Boolean, isPlayerOne : Boolean)
    {
        FirebaseDatabase.getInstance().reference.child("Games").addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var game : DataSnapshot

                if(!dataSnapshot.hasChild(gameId))
                {
                    Log.e("GAMSESTATE", "Game: " + gameId + " does not exist!")
                    return
                }

                game = dataSnapshot.child(gameId)

                Log.e("GAME READ", "Game key is: " + game.key + ", Game value is: " + game.value)

                var player : DataSnapshot? = null
                var ships : DataSnapshot? = null
                var myAttacks : DataSnapshot? = null
                var oppAttacks : DataSnapshot? = null

                var shipsData = ArrayList<Ship>()
                var myAttacksData = ArrayList<Triple<Int,Int,Int>>()
                var oppAttacksData = ArrayList<Triple<Int,Int,Int>>()
                var playerShipCount = -1
                var playerName = ""
                var stateOfGame = ""
                var whichPlayer = ""

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

                if(joining) whichPlayer = "Player Two"
                else if(spectating)
                {
                    if(state == gameState.PLAYER_ONE_TURN) whichPlayer = "Player One"
                    else if(state == gameState.PLAYER_TWO_TURN) whichPlayer = "Player Two"
                }
                else if(!isPlayerOne) whichPlayer = "Player Two"
                else if (isPlayerOne) whichPlayer = "Player One"


                if(game.hasChild(whichPlayer)) player = game.child(whichPlayer)

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
                    playerName = player.child("name").value.toString()
                    myNameDisplay.setText(playerName)
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
                        shipsData.add(Ship(shipSize, shipArray))
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
                if(player != null && player.hasChild("oppAttacks")) oppAttacks = player.child("oppAttacks")

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

                playerOne = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)

                shipsData = ArrayList<Ship>()
                oppAttacksData = ArrayList<Triple<Int,Int,Int>>()
                myAttacksData = ArrayList<Triple<Int,Int,Int>>()

                if(!isPlayerOne) whichPlayer = "Player One"
                else whichPlayer = "Player Two"

                if(game.hasChild(whichPlayer)) player = game.child(whichPlayer)

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
                    else enemyNameDisplay.setText(playerName)
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
                        shipsData.add(Ship(shipSize, shipArray))
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
                        Log.e("PLAYER ONE ATTACKS DATA", "first: " + Integer.parseInt(attacks.child("first").value.toString()) + ", second: " + Integer.parseInt(attacks.child("second").value.toString()) + ", third: " + Integer.parseInt(attacks.child("third").value.toString()))
                    }
                }

                //SECOND PLAYER OPPONENT ATTACKS
                if(player != null && player.hasChild("oppAttacks")) oppAttacks = player.child("oppAttacks")

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

                playerTwo = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)


                setupCoordinateButtons()
                setupPlayer(playerOne)
                when(stateOfGame)
                {
                    gameState.PLAYER_ONE_TURN.name, gameState.PLAYER_TWO_TURN.name ->
                    {
                        findViewById<Button>(R.id.Okay).isClickable = false
                        if(stateOfGame == gameState.PLAYER_ONE_TURN.name) findViewById<Button>(R.id.Okay).setText("Player One's Turn!")
                        else if(stateOfGame == gameState.PLAYER_TWO_TURN.name) findViewById<Button>(R.id.Okay).setText("Player Two's Turn!")
                    }
                    gameState.GAME_OVER_PLAYER_ONE.name, gameState.GAME_OVER_PLAYER_TWO.name ->
                    {
                        var views = findViewById<ViewGroup>(R.id.buttons)
                        for (j in 0..views.childCount - 1) {
                            var view = views.getChildAt(j)
                            if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        button.isClickable = false
                                    }
                                }
                            }
                        }
                        if(stateOfGame == gameState.GAME_OVER_PLAYER_ONE.name) findViewById<Button>(R.id.Okay).setText("Player One Wins!")
                        else if(stateOfGame == gameState.GAME_OVER_PLAYER_TWO.name) findViewById<Button>(R.id.Okay).setText("Player Two Wins!")
                    }
                    gameState.GAME_OVER_PLAYER_TWO.name ->
                    {
                        var views = findViewById<ViewGroup>(R.id.buttons)
                        for (j in 0..views.childCount - 1) {
                            var view = views.getChildAt(j)
                            if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        button.isClickable = false
                                    }
                                }
                            }
                        }

                    }
                    gameState.STARTED.name ->
                    {
                        var views = findViewById<ViewGroup>(R.id.buttons)
                        for (j in 0..views.childCount - 1) {
                            var view = views.getChildAt(j)
                            if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        button.isClickable = false
                                        if (button.id != R.id.Okay) {
                                            button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                                            button.setText("")
                                        }
                                    }
                                }
                            }
                        }
                        findViewById<Button>(R.id.Okay).setText("Start")
                        if(isPlayerOne)findViewById<Button>(R.id.Okay).isClickable = true
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun loadFromDatabase(snapshot : DataSnapshot?) : Boolean{

        if(snapshot !is DataSnapshot) return false

        if(!snapshot.hasChild(gameId))
        {
            Log.e("GAMSESTATE", "Game: " + gameId + " does not exist!")
            return false
        }

        var game = snapshot.child(gameId)

        var stateOfGame = ""

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
            return false
        }

        if(spectating || (!isPlayerOne && state == gameState.PLAYER_TWO_TURN) || (isPlayerOne && state == gameState.PLAYER_ONE_TURN) ||
                (!isPlayerOne && state == gameState.GAME_OVER_PLAYER_ONE) || (isPlayerOne && state == gameState.GAME_OVER_PLAYER_TWO))
            return true

        return false
    }
}