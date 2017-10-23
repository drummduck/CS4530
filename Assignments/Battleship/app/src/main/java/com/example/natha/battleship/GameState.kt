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
    var handler = Handler()


    val clickListener = View.OnClickListener { view ->
        if(view.id == R.id.Okay)
        {
            if(state == gameState.STARTED)
            {
                findViewById<Button>(R.id.Okay).setText("Player One's Turn")
                for(i in playerOne.ships)
                {
                    var size = i.size
                    var count = 1
                    for(j in i.pos)
                    {
                        var view = findViewById<ViewGroup>(R.id.buttons).getChildAt(j.first+11)
                        if(view is LinearLayout)
                        {
                            var button = view.getChildAt(j.second)
                            if(button is Button)
                            {
                                 button.setBackgroundColor(Color.GRAY)
                                if(count == 1) button.setText("-" + button.text.toString())
                                if(count == size) button.setText(button.text.toString() + "-")
                            }
                        }
                        count++
                    }
                }
                setupCoordinateButtons()
                state = gameState.PLAYER_ONE_TURN
            }

            else if(state == gameState.SWITCH_TO_PLAYER_TWO)
            {
                setupPlayer(playerTwo)
                state = gameState.PLAYER_TWO_TURN
            }

            else if(state == gameState.SWITCH_TO_PLAYER_ONE)
            {
                setupPlayer(playerOne)
                state = gameState.PLAYER_ONE_TURN
            }
        }

        else if(view is Button)
        {
            var xVal = view.resources.getResourceName(view.id).substringAfter("/")[0].toInt() - 64
            var yVal = Integer.parseInt(view.resources.getResourceName(view.id).substringAfter("/").substringBefore("_2").substring(1))
            var hit = false
            var sunk = false
            if(state == gameState.PLAYER_ONE_TURN)
            {
                for(i in playerOne.ships)
                {
                    var hitCount = 0
                    var pos = 0
                    for(j in i.pos)
                    {
                        if(xVal == j.first && yVal == j.second && j.third != 2)
                        {
                            i.pos[pos] = Triple(xVal, yVal, 2)
                            if(view.text.isEmpty()) view.text = "Hit"
                            playerTwo.oppAttacks.add(Triple(xVal,yVal,2))
                            playerOne.myAttacks.add(Triple(xVal,yVal,2))
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
                                for(j in playerOne.myAttacks)
                                {
                                    if(j.first == k.first && j.second == k.second) playerOne.myAttacks[pos2] = Triple(j.first, j.second, 3)
                                    pos2++
                                }
                                pos2 = 0
                                for(j in playerTwo.oppAttacks)
                                {
                                    if(j.first == k.first && j.second == k.second) playerTwo.oppAttacks[pos2] = Triple(j.first, j.second, 3)
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
                                        button.text = "Sunk"
                                    }
                                }
                                pos++
                            }
                            for(k in 0..i.size-1)
                            {
                                i.pos[k] = Triple(i.pos[k].first, i.pos[k].second, 3)
                            }

                            for(i in playerTwo.ships)
                            {
                                var sinkCount = 0
                                for(j in i.pos)
                                {
                                    if(j.third == 3) sinkCount++
                                    if(sinkCount == i.size)
                                    {
                                        state = gameState.GAME_OVER_PLAYER_ONE
                                        var views = findViewById<ViewGroup>(R.id.buttons)
                                        for(i in 0..views.childCount-1)
                                        {
                                            var view = views.getChildAt(i)
                                            if(view is LinearLayout)
                                            {
                                                for(j in 0..view.childCount-1)
                                                {
                                                    var button = view.getChildAt(j)
                                                    if(button is Button) button.isClickable = false

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break
                        }
                        pos++
                    }
                    if(sunk || hit) break
                }
                if(!hit && !sunk)
                {
                    view.setBackgroundColor(Color.WHITE)
                    playerOne.oppAttacks.add(Triple(xVal, yVal, 1))
                    playerTwo.myAttacks.add(Triple(xVal,yVal, 1))
                    view.setText("Miss")
                    view.setTextColor(Color.BLACK)
                    view.isClickable = false
                }

                Handler().postDelayed(Runnable {
                    var views = findViewById<ViewGroup>(R.id.buttons)
                    for(i in 0..views.childCount-1)
                    {
                        var view = views.getChildAt(i)
                        if(view is LinearLayout)
                        {
                            for(j in 0..view.childCount-1)
                            {
                                var button = view.getChildAt(j)
                                if(button is Button && button.id != R.id.Okay)
                                {
                                    button.setText("")
                                    button.setBackgroundColor(Color.GRAY)
                                    button.isClickable = false
                                }
                            }
                        }
                    }
                    findViewById<Button>(R.id.Okay).setText("Switch to player two")
                    state = gameState.SWITCH_TO_PLAYER_TWO
                    },5000L)
            }

            if(state == gameState.PLAYER_TWO_TURN)
            {
                for(i in playerOne.ships)
                {
                    var hitCount = 0
                    var pos = 0
                    for(j in i.pos)
                    {
                        if(xVal == j.first && yVal == j.second && j.third != 2)
                        {
                            i.pos[pos] = Triple(xVal, yVal, 2)
                            if(view.text.isEmpty()) view.text = "Hit"
                            playerOne.oppAttacks.add(Triple(xVal,yVal,2))
                            playerTwo.myAttacks.add(Triple(xVal,yVal,2))
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
                                for(j in playerTwo.myAttacks)
                                {
                                    if(j.first == k.first && j.second == k.second) playerTwo.myAttacks[pos2] = Triple(j.first, j.second, 3)
                                    pos2++
                                }
                                pos2 = 0
                                for(j in playerOne.oppAttacks)
                                {
                                    if(j.first == k.first && j.second == k.second) playerTwo.oppAttacks[pos2] = Triple(j.first, j.second, 3)
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
                                        button.text = "Sunk"
                                    }
                                }
                                pos++
                            }
                            for(k in 0..i.size-1)
                            {
                                i.pos[k] = Triple(i.pos[k].first, i.pos[k].second, 3)
                            }
                            for(i in playerTwo.ships)
                            {
                                var sinkCount = 0
                                for(j in i.pos)
                                {
                                    if(j.third == 3) sinkCount++
                                    if(sinkCount == i.size)
                                    {
                                        state = gameState.GAME_OVER_PLAYER_ONE
                                        var views = findViewById<ViewGroup>(R.id.buttons)
                                        for(i in 0..views.childCount-1)
                                        {
                                            var view = views.getChildAt(i)
                                            if(view is LinearLayout)
                                            {
                                                for(j in 0..view.childCount-1)
                                                {
                                                    var button = view.getChildAt(j)
                                                    if(button is Button) button.isClickable = false

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            break
                        }
                        pos++
                    }
                    if(sunk || hit) break
                }
                if(!hit && !sunk)
                {
                    view.setBackgroundColor(Color.WHITE)
                    playerOne.oppAttacks.add(Triple(xVal, yVal, 1))
                    playerTwo.myAttacks.add(Triple(xVal,yVal, 1))
                    view.setText("Miss")
                    view.setTextColor(Color.BLACK)
                    view.isClickable = false
                }
                Handler().postDelayed(Runnable {
                    var views = findViewById<ViewGroup>(R.id.buttons)
                    for(i in 0..views.childCount-1)
                    {
                        var view = views.getChildAt(i)
                        if(view is LinearLayout)
                        {
                            for(j in 0..view.childCount-1)
                            {
                                var button = view.getChildAt(j)
                                if(button is Button && button.id != R.id.Okay)
                                {
                                    button.setText("")
                                    button.setBackgroundColor(Color.GRAY)
                                    button.isClickable = false
                                }
                            }
                        }
                    }
                    findViewById<Button>(R.id.Okay).setText("Switch to player one")
                    state = gameState.SWITCH_TO_PLAYER_ONE
                },5000L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)

        findViewById<Button>(R.id.Okay).setOnClickListener(clickListener)
        if(intent != null && intent.extras != null && !intent.extras.isEmpty)
        {

        }

        else if(savedInstanceState != null && !savedInstanceState.keySet().isEmpty())
        {

        }

        else
        {
            var playerOneSetup = false
            for(i in 1..2)
            {
                var shipSize = 2
                var ships = ArrayList<Ship>()
                for (i in 1..5)
                {
                    var placementOkay = false
                    while(!placementOkay)
                    {
                        var placement = shipPlacement(shipSize)
                        placementOkay = true

                        for (i in ships)
                        {
                            for (j in i.pos)
                            {
                                for (k in placement)
                                {
                                    if (k.first == j.first && k.second == j.second) placementOkay = false
                                }
                            }
                        }
                        if (placementOkay)
                        {
                            ships.add(Ship(shipSize, placement))
                            if (i != 2 ) shipSize++
                        }
                    }
                }

                if(!playerOneSetup)
                {
                    playerOne = Player(ships, ArrayList<Triple<Int,Int,Int>>(), ArrayList<Triple<Int,Int,Int>>())
                    playerOneSetup = true
                }
                else {
                    playerTwo = Player(ships, ArrayList<Triple<Int, Int, Int>>(), ArrayList<Triple<Int, Int, Int>>())

                    for (i in playerTwo.ships) {
                        Log.e("SIZE", "Ship size is: " + i.size)
                        for (j in i.pos) {
                            Log.e("LOCATION", "Xpos: " + j.first + ", Ypos: " + j.second + ", Sunk? " + j.third)
                        }
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
                        1 -> if (pos.last().second - size > 1) {
                            up = true
                            directionChosen = true
                        }
                        2 -> if (pos.last().second + size < 10) {
                            down = true
                            directionChosen = true
                        }
                        3 -> if (pos.last().first - size > 1) {
                            left = true
                            directionChosen = true
                        }
                        4 -> if (pos.last().first + size < 10) {
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

    }

    fun setupCoordinateButtons()
    {
        //Setup click listener for each button
        for(i in 0..11)
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

}