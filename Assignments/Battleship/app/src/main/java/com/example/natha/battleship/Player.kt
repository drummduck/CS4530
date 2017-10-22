package com.example.natha.battleship

/**
 * Created by Natha on 10/20/2017.
 */
class Player() {
    lateinit var ships : ArrayList<Ship>
    lateinit var oppAttacks : ArrayList<Pair<Int,Int>>
    lateinit var myAttacks : ArrayList<Pair<Int,Int>>

    constructor(ships : ArrayList<Ship>, oppAttacks : ArrayList<Pair<Int,Int>>, myAttacks : ArrayList<Pair<Int,Int>>) : this() {
        this.ships = ships
        this.oppAttacks = oppAttacks
        this.myAttacks = myAttacks
    }
}