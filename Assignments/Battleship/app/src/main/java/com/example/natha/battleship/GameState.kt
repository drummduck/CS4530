package com.example.natha.battleship

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Natha on 10/20/2017.
 */
class GameState() : AppCompatActivity() {

    enum class gameState
    {
        STARTED, IN_PROGRESS, PLAYER_ONE_TURN, PLAYER_TWO_TURN, GAME_OVER
    }

    lateinit var playerOne : Player
    lateinit var playerOneShips : ArrayList<Ship>
    lateinit var playerTwo : Player
    lateinit var playerTwoShips : ArrayList<Ship>
    var state = gameState.STARTED

    val clickListener = View.OnClickListener { view ->
        when(view.id)
        {
            R.id.Okay ->
            {
                if(state == gameState.STARTED)
                {

                }
                
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        if(intent != null && !intent.extras.isEmpty)
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
                    ships.add(Ship(shipSize, shipPlacement(i), ArrayList<Pair<Int, Int>>()))
                    if (i != 3) shipSize++
                }

                if(!playerOneSetup)
                {
                    playerOne = Player(ships, ArrayList<Pair<Int,Int>>(), ArrayList<Pair<Int,Int>>())
                    playerOneSetup = true
                }
                else playerTwo = Player(ships, ArrayList<Pair<Int,Int>>(), ArrayList<Pair<Int,Int>>())
            }
        }
    }

    fun shipPlacement(size : Int) : ArrayList<Pair<Int,Int>>
    {
        var up = false
        var down = false
        var left = false
        var right = false
        var chosen = false
        var pos = ArrayList<Pair<Int,Int>>()
        var random = Random()
        var firstX = random.nextInt(10 - 1) + 1
        var firstY = random.nextInt(10 - 1) + 1
        pos.add(Pair(firstX, firstY))
        for(i in 1..size)
        {
            if(!chosen)
            {
                while(!chosen)
                {
                    var nextPos = random.nextInt(4 - 1) + 1
                    when (nextPos) {
                        1 -> if (pos.last().second - size > 1) {
                            up = true
                            chosen = true
                        }
                        2 -> if (pos.last().second + size < 10) {
                            down = true
                            chosen = true
                        }
                        3 -> if (pos.last().first - size > 1) {
                            left = true
                            chosen = true
                        }
                        4 -> if (pos.last().first + size < 10) {
                            right = true
                            chosen = true
                        }
                        else Log.e("GAMESTATE", "SELECTING SHIP DIRECTION")
                    }
                }
            }
            else if(up) pos.add(Pair(pos.last().first, pos.last().second - 1))
            else if(down) pos.add(Pair(pos.last().first, pos.last().second + 1))
            else if(left) pos.add(Pair(pos.last().first - 1, pos.last().second))
            else if(right) pos.add(Pair(pos.last().first +1, pos.last().second))
            else Log.e("GAMESTATE", "PROBLEM SELECTING SHIP DIRECTION")
        }
        return pos
    }
}