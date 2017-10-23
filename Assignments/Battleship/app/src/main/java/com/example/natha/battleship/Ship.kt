package com.example.natha.battleship

/**
 * Created by Natha on 10/20/2017.
 */
class Ship() {

    var size = 0
    lateinit var pos : ArrayList<Triple<Int,Int,Int>>

    constructor(size : Int, pos : ArrayList<Triple<Int,Int,Int>>) : this()
    {
        this.size = size
        this.pos = pos
    }
}