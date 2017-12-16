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
 */
class GameState() : AppCompatActivity() {

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
    var state = gameState.STARTED
    var gameId = ""
    var spectating = false
    var isPlayerOne = false
    var joining = false
    var replay = false
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

                for (i in playerTwo.ships) {
                    var hitCount = 0
                    var pos = 0
                    for (j in i.pos) {
                        if (xVal == j.first && yVal == j.second) {
                            i.pos[pos] = Triple(xVal, yVal, 2)
                            view.text = "Hit"
                            playerTwo.oppAttacks.add(Triple(xVal, yVal, 2))
                            playerOne.myAttacks.add(Triple(xVal, yVal, 2))
                            view.setBackgroundColor(Color.YELLOW)
                            view.isClickable = false
                            hitCount++
                            hit = true
                        }
                        if (j.third == 2) hitCount++
                        if (hitCount == i.size) {
                            sunk = true
                            playerTwo.shipCount--
                        }
                        if (sunk) {
                            pos = 0
                            for (k in i.pos) {
                                var pos2 = 0
                                for (l in playerOne.myAttacks) {

                                    if (l.first == k.first && l.second == k.second) playerOne.myAttacks[pos2] = Triple(l.first, l.second, 3)
                                    pos2++
                                }
                                pos2 = 0
                                for (l in playerTwo.oppAttacks) {
                                    if (l.first == k.first && l.second == k.second) playerTwo.oppAttacks[pos2] = Triple(l.first, l.second, 3)
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

                            if (playerTwo.shipCount == 0) {
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
                    playerTwo.oppAttacks.add(Triple(xVal, yVal, 1))
                    playerOne.myAttacks.add(Triple(xVal, yVal, 1))
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
                if(!replay)updateDatabase(false)
            }
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
        mDbRootRef.addChildEventListener(childListener)
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
                        loadFromDatabase()
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
                        setupPlayer(playerOne)
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

    fun setupPlayer(player : Player) {

        var doneWithButton = false

        for (i in 0..buttons.childCount) {
            if (buttons.getChildAt(i) is LinearLayout) {
                for (j in 0..(buttons.getChildAt(i) as LinearLayout).childCount) {

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
                                    if(!spectating)button.setOnClickListener(clickListener)
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

    override fun onBackPressed() {
        var intent = Intent(this, Game::class.java)
        mDbRootRef.removeEventListener(childListener)
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

    fun loadFromDatabase()
    {
        FirebaseDatabase.getInstance().reference.child("Games").addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(isPlayerOne) Log.e("IM PLAYER ONE!!!!", "IM PLAYER ONEEEE!!!!")
                if(!isPlayerOne) Log.e("IM PLAYER TWO!!!!", "IM PLAYER TWOOOO!!!!")

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

                playerOne = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)

                shipsData = ArrayList<Ship>()
                oppAttacksData = ArrayList<Triple<Int,Int,Int>>()
                myAttacksData = ArrayList<Triple<Int,Int,Int>>()
                player = null
                enemy  = null
                ships  = null
                myAttacks = null
                oppAttacks = null

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
                    else if(spectating) enemyNameDisplay.setText("Spectating")
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

                playerTwo = Player(shipsData, oppAttacksData, myAttacksData, playerName, playerShipCount)

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

    fun replayGame()
    {
        var playerOneAttacks = ArrayList<Pair<Int,Int>>()
        var playerTwoAttacks = ArrayList<Pair<Int,Int>>()


        for(i in playerOne.myAttacks)
        {
            playerOneAttacks.add(Pair(i.first, i.second))
        }

        for(i in playerTwo.myAttacks)
        {
            playerTwoAttacks.add(Pair(i.first,  i.second))
        }

        for(i in playerOne.ships)
        {
            var count = 0
            for(j in i.pos)
            {
                i.pos[count] = Triple(j.first, j.second, 0)
            }
        }

        for(i in playerTwo.ships)
        {
            var count = 0
            for(j in i.pos)
            {
                i.pos[count] = Triple(j.first, j.second, 0)
            }
        }

        var showBoard = true
        var count = 1

        while(state != gameState.GAME_OVER_PLAYER_TWO || state != gameState.GAME_OVER_PLAYER_ONE) {
            Handler().postDelayed(Runnable {

                if(isPlayerOne)
                {
                    if(state == gameState.GAME_OVER_PLAYER_ONE)
                    {
                        if(showBoard)
                        {
                            setupPlayer(playerOne)
                            showBoard = false
                        }

                        else if(!showBoard)
                        {
                            //Hit button with x,y
                        }
                    }
                }

                else
                {
                    if(state == gameState.GAME_OVER_PLAYER_TWO)
                    {
                        if(showBoard)
                        {
                            setupPlayer(playerTwo)
                            showBoard = false
                        }

                        else if(!showBoard)
                        {
                            //Hit button with x,y
                        }
                    }
                }

                count++
            }, (count*1000).toLong())
        }

        replay = false
    }
}
