package com.example.natha.battleship

import java.io.Serializable

/**
 * Created by Nathan on 10/20/2017.
 */
class Player() : Serializable{
    var ships = ArrayList<Ship>()
    var oppAttacks = ArrayList<Triple<Int,Int,Int>>()
    var myAttacks = ArrayList<Triple<Int,Int,Int>>()

    constructor(ships : ArrayList<Ship>, oppAttacks : ArrayList<Triple<Int,Int,Int>>, myAttacks : ArrayList<Triple<Int,Int,Int>>) : this() {
        this.ships = ships
        this.oppAttacks = oppAttacks
        this.myAttacks = myAttacks
    }
}