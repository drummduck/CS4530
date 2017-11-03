package com.example.natha.battleship

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.example.natha.battleship.R.id.buttons
import java.util.*
import kotlin.collections.ArrayList
import android.R.attr.button
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import java.io.*


/**
 * Created by Natha on 10/20/2017.
 */
class GameState() : AppCompatActivity() {

    enum class gameState
    {
        STARTED, SWITCH_TO_PLAYER_ONE,SWITCH_TO_PLAYER_TWO, PLAYER_ONE_TURN, PLAYER_TWO_TURN, GAME_OVER_PLAYER_ONE, GAME_OVER_PLAYER_TWO
    }

    lateinit var playerOne : Player
    lateinit var playerTwo : Player
    var state = gameState.STARTED
    var fileName = ""


    val clickListener = View.OnClickListener { view ->
        if(view.id == R.id.Okay)
        {
            if(state == gameState.STARTED)
            {
                Log.e("STARTING", "Starting game!")
                findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                for(i in playerOne.ships)
                {
                    var size = i.size
                    var count = 1
                    for(j in i.pos)
                    {
                        var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(j.first+10)
                        if(view is LinearLayout)
                        {
                            var button = view.getChildAt(j.second-1)
                            if(button is Button)
                            {
                                 button.setBackgroundColor(Color.GRAY)
                                if(count == 1) button.setText("**" + button.text.toString())
                                if(count == size) button.setText(button.text.toString() + "**")
                            }
                        }
                        count++
                    }
                }
                setupCoordinateButtons()
                state = gameState.PLAYER_ONE_TURN
                saveGame()
            }

            else if(state == gameState.SWITCH_TO_PLAYER_TWO)
            {
                state = gameState.PLAYER_TWO_TURN
                setupPlayer(playerTwo)
                findViewById<Button>(R.id.Okay).setText("Player Two's Turn")
                saveGame()
            }

            else if(state == gameState.SWITCH_TO_PLAYER_ONE)
            {
                state = gameState.PLAYER_ONE_TURN
                setupPlayer(playerOne)
                findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                saveGame()
            }
        }

        else if(view is Button)
        {
            var xVal = view.resources.getResourceName(view.id).substringAfter("/")[0].toInt() - 64
            var yVal = Integer.parseInt(view.resources.getResourceName(view.id).substringAfter("/").substringBefore("_2").substring(1))
            var hit = false
            var sunk = false
            if(state == gameState.PLAYER_ONE_TURN || state == gameState.PLAYER_TWO_TURN)
            {
                var currentPlayer : Player
                var opponentPlayer : Player
                if(state == gameState.PLAYER_ONE_TURN)
                {
                    currentPlayer = playerOne
                    opponentPlayer = playerTwo
                }
                else
                {
                    currentPlayer = playerTwo
                    opponentPlayer = playerOne
                }
                for(i in opponentPlayer.ships)
                {
                    var hitCount = 0
                    var pos = 0
                    for(j in i.pos)
                    {
                        if(xVal == j.first && yVal == j.second)
                        {
                            i.pos[pos] = Triple(xVal, yVal, 2)
                            view.text = "Hit"
                            opponentPlayer.oppAttacks.add(Triple(xVal,yVal,2))
                            currentPlayer.myAttacks.add(Triple(xVal,yVal,2))
                            view.setBackgroundColor(Color.YELLOW)
                            view.isClickable = false
                            hitCount++
                            hit = true
                        }
                        if(j.third == 2) hitCount++
                        if(hitCount == i.size) sunk = true
                        if(sunk)
                        {
                            pos = 0
                            for(k in i.pos)
                            {
                                var pos2 = 0
                                for(l in currentPlayer.myAttacks)
                                {

                                    if(l.first == k.first && l.second == k.second) currentPlayer.myAttacks[pos2] = Triple(l.first, l.second, 3)
                                    pos2++
                                }
                                pos2 = 0
                                for(l in opponentPlayer.oppAttacks)
                                {
                                    if(l.first == k.first && l.second == k.second) opponentPlayer.oppAttacks[pos2] = Triple(l.first, l.second, 3)
                                    pos2++
                                }
                                i.pos[pos] = Triple(k.first, k.second, 3)
                                var buttons = findViewById<ViewGroup>(R.id.buttons)
                                var row = buttons.getChildAt(k.first-1)
                                if(row is LinearLayout)
                                {
                                    var button = row.getChildAt(k.second-1)
                                    if(button is Button)
                                    {
                                        button.setBackgroundColor(Color.RED)
                                        if(pos == 0 || pos == i.size-1) button.text = "**Sunk**"
                                        else button.text = "Sunk"
                                    }
                                }
                                pos++
                            }
                            var sinkCount = 0
                            for(k in opponentPlayer.ships)
                            {
                                if(k.pos[0].third == 3) sinkCount++

                                if(sinkCount == opponentPlayer.ships.size)
                                {
                                    if(state == gameState.PLAYER_ONE_TURN)
                                    {
                                        state = gameState.GAME_OVER_PLAYER_ONE
                                        findViewById<Button>(R.id.Okay).setText("Player One Wins!")
                                    }
                                    else
                                    {
                                        state = gameState.GAME_OVER_PLAYER_TWO
                                        findViewById<Button>(R.id.Okay).setText("Player Two Wins!")
                                    }
                                    var views = findViewById<ViewGroup>(R.id.buttons)
                                    for(l in 0..views.childCount-1)
                                    {
                                        var view = views.getChildAt(l)
                                        if(view is LinearLayout)
                                        {
                                            for(m in 0..view.childCount-1)
                                            {
                                                var button = view.getChildAt(m)
                                                if(button is Button) button.isClickable = false
                                            }
                                        }
                                    }
                                }
                            }
                            break
                        }
                        pos++
                        if(sunk) break
                    }
                    if(sunk || hit) break
                }
                if(!hit && !sunk)
                {
                    view.setBackgroundColor(Color.WHITE)
                    opponentPlayer.oppAttacks.add(Triple(xVal, yVal, 1))
                    currentPlayer.myAttacks.add(Triple(xVal,yVal, 1))
                    view.setText("Miss")
                    view.isClickable = false
                }

                var timer = 0
                for(i in  1..3) {
                    Handler().postDelayed(Runnable {
                        var views = findViewById<ViewGroup>(R.id.buttons)
                        for (j in 0..views.childCount - 1) {
                            var view = views.getChildAt(j)
                              if (view is LinearLayout) {
                                for (k in 0..view.childCount - 1) {
                                    var button = view.getChildAt(k)
                                    if (button is Button) {
                                        if(i == 1) button.isClickable = false
                                        else if(i > 2 && button.id != R.id.Okay && state != gameState.GAME_OVER_PLAYER_TWO && state != gameState.GAME_OVER_PLAYER_ONE)
                                        {
                                            button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                                            button.setText("")
                                        }
                                    }
                                }
                            }
                        }
                        if(state == gameState.PLAYER_ONE_TURN && i > 2)
                        {
                            findViewById<Button>(R.id.Okay).setText("Switch to Player Two")
                            state = gameState.SWITCH_TO_PLAYER_TWO
                            findViewById<Button>(R.id.Okay).isClickable = true
                            saveGame()
                        }
                        else if(state == gameState.PLAYER_TWO_TURN && i > 2)
                        {
                            findViewById<Button>(R.id.Okay).setText("Switch to Player One")
                            state = gameState.SWITCH_TO_PLAYER_ONE
                            findViewById<Button>(R.id.Okay).isClickable = true
                            saveGame()
                        }
                    }, timer.toLong() * 1000)

                    timer = 3
                }
            }
            saveGame()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)

        findViewById<Button>(R.id.Okay).setOnClickListener(clickListener)
        if(savedInstanceState != null) {

            Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE AFTER")

            for (i in savedInstanceState.keySet()) {
                when (i) {
                    "fileName" -> fileName = savedInstanceState.getString("fileName")
                }
            }
            loadGame()
        }
        else if(intent != null && intent.extras != null && !intent.extras.isEmpty)
        {
            var newGame = false
            fileName = ""
            var fileCount = 0
            for(i in intent.extras.keySet())
            {
                when(i)
                {
                    "New Game" ->
                    {
                        fileCount = intent.getIntExtra("New Game",0)
                        while(!newGame)
                        {
                            if(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/Game" + fileCount.toString()).exists())
                                fileCount++
                            else {
                                fileName = "Game" + fileCount.toString()
                                newGame = true
                            }
                        }
                    }
                    "fileName" -> fileName = intent.getStringExtra(i)
                }
            }

            if(!newGame) loadGame()

            else {
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
                        playerOne = Player(ships, ArrayList<Triple<Int, Int, Int>>(), ArrayList<Triple<Int, Int, Int>>())
                        playerOneSetup = true
                    } else playerTwo = Player(ships, ArrayList<Triple<Int, Int, Int>>(), ArrayList<Triple<Int, Int, Int>>())
                }
                saveGame()
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
                var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(j.first+10)
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
            var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(i.first+10)
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

                    if(child2 is Button && buttonColor.color == resources.getColor(android.R.color.holo_blue_light)) child2.setOnClickListener(clickListener)
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

    fun saveGame()
    {
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/" + fileName)

        if(file.exists())
        {
            file.delete()
            file.createNewFile()
        }
        else file.createNewFile()
        val outputWriter = FileOutputStream(file)
        val outputStream = DataOutputStream(outputWriter)

        //gameState
        Log.e("GAMESTATE", "Gamestate integer value is: " + state.ordinal.toString())
        outputStream.writeInt(state.ordinal)


        Log.e("SHIPS", "Player one ship count is: " + playerOne.ships.size + ", Player two ship count is: " + playerTwo.ships.size)
        for(i in playerOne.ships)
        {
            for(j in i.pos)
            {
                Log.e("POSITIONS", "Xpos is: " + j.first + ",Ypos is: " + j.second + ", Hit/Miss" + j.third)
            }
        }
        //PlayerOne
        //Ships
        var shipsLeft = 5
        for(i in playerOne.ships)
        {
            if(i.pos.first().third == 3) shipsLeft--
            //ShipSize
            outputStream.writeInt(i.size)
            for(j in i.pos)
            {
                //Positions and Hits
                outputStream.writeInt(j.first)
                outputStream.writeInt(j.second)
                outputStream.writeInt(j.third)
            }
        }
        //Ships Left
        outputStream.writeInt(shipsLeft)

        outputStream.writeInt(playerOne.myAttacks.size)
        //My Attacks
        for(i in playerOne.myAttacks)
        {
            outputStream.writeInt(i.first)
            outputStream.writeInt(i.second)
            outputStream.writeInt(i.third)
        }

        outputStream.writeInt(playerOne.oppAttacks.size)
        //Opp Attacks
        for(i in playerOne.oppAttacks)
        {
            outputStream.writeInt(i.first)
            outputStream.writeInt(i.second)
            outputStream.writeInt(i.third)
        }



        //PlayerTwo
        //Ships
        shipsLeft = 5
        for(i in playerTwo.ships)
        {
            if(i.pos.first().third == 3) shipsLeft--
            //ShipSize
            outputStream.writeInt(i.size)
            for(j in i.pos)
            {
                //Positions and Hits
                outputStream.writeInt(j.first)
                outputStream.writeInt(j.second)
                outputStream.writeInt(j.third)
            }
        }
        //Ships Left
        outputStream.writeInt(shipsLeft)

        outputStream.writeInt(playerTwo.myAttacks.size)
        //My Attacks
        for(i in playerTwo.myAttacks)
        {
            outputStream.writeInt(i.first)
            outputStream.writeInt(i.second)
            outputStream.writeInt(i.third)
        }

        outputStream.writeInt(playerTwo.oppAttacks.size)
        //Opp Attacks
        for(i in playerTwo.oppAttacks)
        {
            outputStream.writeInt(i.first)
            outputStream.writeInt(i.second)
            outputStream.writeInt(i.third)
        }

        outputStream.flush()
        outputStream.close()
        outputWriter.close()
    }

    fun loadGame()
    {
        Log.e("LOAD", "Loading Game")
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Battleship/" + fileName)
        val inputFile = FileInputStream(file)
        val inputReader = DataInputStream(inputFile)
        playerOne = Player()
        playerTwo = Player()
        var ships = ArrayList<Ship>()

        when(inputReader.readInt())
        {
            0 -> state = gameState.STARTED
            1 -> state = gameState.SWITCH_TO_PLAYER_ONE
            2 -> state = gameState.SWITCH_TO_PLAYER_TWO
            3 -> state = gameState.PLAYER_ONE_TURN
            4 -> state = gameState.PLAYER_TWO_TURN
            5 -> state = gameState.GAME_OVER_PLAYER_ONE
            6 -> state = gameState.GAME_OVER_PLAYER_TWO
        }

        Log.e("GAMESTATE", "Gamestate integer value is: " + state.ordinal.toString())


        for(i in 0..4)
        {
            var shipSize = inputReader.readInt()
            var positions = ArrayList<Triple<Int,Int,Int>>()
            Log.e("SHIP SIZE", "Ship size is: " + shipSize)
            for(i in 0..shipSize-1)
            {
                positions.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))
                Log.e("SHIP POSITION", "X pos is: " + positions.last().first + ", Y pos is: " + positions.last().second + ", Hit or Sink: " + positions.last().third)
            }

            playerOne.ships.add(Ship(shipSize, positions))
        }

        //throwing away how many ships left, just for first screen
        inputReader.readInt()

        var myAttackSize = inputReader.readInt()
        //myattacks
        if(myAttackSize != 0) for(i in 0..myAttackSize-1) playerOne.myAttacks.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))

        //oppAttacks
        var oppAttackSize = inputReader.readInt()
        if(oppAttackSize != 0) for(i in 0..oppAttackSize-1) playerOne.oppAttacks.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))

        for(i in 0..4)
        {
            var shipSize = inputReader.readInt()
            var positions = ArrayList<Triple<Int,Int,Int>>()
            Log.e("SHIP SIZE", "Ship size is: " + shipSize)
            for(i in 0..shipSize-1)
            {
                positions.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))
                Log.e("SHIP POSITION", "X pos is: " + positions.last().first + ", Y pos is: " + positions.last().second + ", Hit or Sink: " + positions.last().third)
            }
            playerTwo.ships.add(Ship(shipSize, positions))
        }

        //throwing away how many ships left, just for first screen
        inputReader.readInt()

        myAttackSize = inputReader.readInt()
        //myattacks
        for(i in 0..myAttackSize-1) playerTwo.myAttacks.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))

        oppAttackSize = inputReader.readInt()
        //oppAttacks
        for(i in 0..oppAttackSize-1) playerTwo.oppAttacks.add(Triple(inputReader.readInt(), inputReader.readInt(), inputReader.readInt()))

        Log.e("SHIPS", "Player one ship count is: " + playerOne.ships.size + ", Player two ship count is: " + playerTwo.ships.size)
        for(i in playerOne.ships)
        {
            for(j in i.pos)
            {
                Log.e("POSITIONS", "Xpos is: " + j.first + ",Ypos is: " + j.second + ", Hit/Miss" + j.third)
            }
        }
        for(i in playerTwo.ships)
        {
            for(j in i.pos)
            {
                Log.e("POSITIONS", "Xpos is: " + j.first + ",Ypos is: " + j.second + ", Hit/Miss" + j.third)
            }
        }
        setupCoordinateButtons()
        when(state)
        {
            gameState.PLAYER_ONE_TURN ->
            {
                setupPlayer(playerOne)
                findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                findViewById<Button>(R.id.Okay).isClickable = true
            }
            gameState.PLAYER_TWO_TURN ->
            {
                setupPlayer(playerTwo)
                findViewById<Button>(R.id.Okay).setText("Player Two's Turn")
                findViewById<Button>(R.id.Okay).isClickable = true
            }
            gameState.SWITCH_TO_PLAYER_ONE -> {
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
                findViewById<Button>(R.id.Okay).setText("Switch to Player One")
                findViewById<Button>(R.id.Okay).isClickable = true
            }
            gameState.SWITCH_TO_PLAYER_TWO ->
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
                findViewById<Button>(R.id.Okay).setText("Switch to Player Two")
                findViewById<Button>(R.id.Okay).isClickable = true
            }
            gameState.GAME_OVER_PLAYER_ONE ->
            {
                setupPlayer(playerOne)
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
                findViewById<Button>(R.id.Okay).setText("Player One Wins!")
            }
            gameState.GAME_OVER_PLAYER_TWO ->
            {
                setupPlayer(playerTwo)
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
                findViewById<Button>(R.id.Okay).setText("Player Two Wins!")
            }
            gameState.STARTED ->
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
                findViewById<Button>(R.id.Okay).isClickable = true
            }
        }

        inputReader.close()
        inputFile.close()
    }

    override fun onBackPressed() {
        var intent = Intent(this, Game::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        if(savedInstanceState !is Bundle) return
        Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE BEFORE")
        savedInstanceState.putString("fileName", fileName)
        super.onSaveInstanceState(savedInstanceState)
    }

}