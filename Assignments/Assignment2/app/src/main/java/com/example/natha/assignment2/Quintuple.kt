package com.example.natha.assignment2

/**
 * Created by Natha on 10/10/2017.
 */
class Quintuple<F, S, T, Fo, Fi> {

    private var first: F? = null
    private var second: S? = null
    private var third: T? = null
    private var fourth: Fo? = null
    private var fifth: Fi? = null

    fun Pair(first: F, second: S, third: T, fourth: Fo, fifth: Fi) {
        this.first = first
        this.second = second
        this.third = third
        this.fourth = fourth
        this.fifth = fifth
    }

    fun getFirst(): F? {
        return first
    }

    fun getSecond(): S? {
        return second
    }

    fun getThird(): T?
    {
        return third
    }

    fun getFourth(): Fo?
    {
        return fourth
    }

    fun getFifth(): Fi?
    {
        return fifth
    }
}