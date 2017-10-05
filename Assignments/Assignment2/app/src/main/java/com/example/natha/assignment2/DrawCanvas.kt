package com.example.natha.assignment2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Natha on 10/4/2017.
 */
class DrawCanvas : View {

    lateinit var paint : Paint

    constructor(context: Context?) : super(context){paint = Paint()}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){paint = Paint()}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){paint = Paint()}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes){paint = Paint()}

    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)
    }

    fun setColor(argb : IntArray) { paint.setARGB(255, argb[0],argb[1],argb[2]) }
    fun getColor() : IntArray {return intArrayOf(Color.red(paint.color), Color.green(paint.color), Color.blue(paint.color))}

    fun setCap(capValue : String) {paint.strokeCap = Paint.Cap.valueOf(capValue)}
    fun getCap() : String {return paint.strokeCap.name}

    fun setWidth(wValue : Float) {paint.strokeWidth = wValue}
    fun getPaintWidth() : Float {return paint.strokeWidth}

    fun setJoin(joinValue : String) {paint.strokeJoin = Paint.Join.valueOf(joinValue)}
    fun getJoin() : String {return paint.strokeJoin.name}
}