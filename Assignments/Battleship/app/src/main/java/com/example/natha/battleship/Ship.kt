package com.example.natha.battleship

import java.io.Serializable

/**
 * Created by Natha on 10/20/2017.
 *
 * This class holds the positions of each ship segment and its status
 */
class Ship() : Serializable {

    var size = 0
    var pos = ArrayList<Triple<Int,Int,Int>>()

    constructor(size : Int, pos : ArrayList<Triple<Int,Int,Int>>) : this()
    {
        this.size = size
        this.pos = pos
    }
}