package com.example.natha.battleship

/**
 * Created by Natha on 10/20/2017.
 */
class Ship() {

    var size = 0
    lateinit var pos : ArrayList<Pair<Int,Int>>
    lateinit var hitOrMiss : ArrayList<Pair<Int,Int>>

    constructor(size : Int, pos : ArrayList<Pair<Int,Int>>, hitOrMiss : ArrayList<Pair<Int,Int>>) : this()
    {
        this.size = size
        this.pos = pos
        this.hitOrMiss = hitOrMiss
    }
}