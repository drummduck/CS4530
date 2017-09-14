package com.example.natha.assignment1

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.widget.TextView
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.view.*

/**
 * Created by Nathan on 9/11/2017.
 */
class BrushSelector : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)



    override fun onDraw(canvas : Canvas?)
    {
        super.onDraw(canvas)

        if(canvas !is Canvas) return
        //Set background
        canvas.drawColor(Color.BLACK)
    }
}

