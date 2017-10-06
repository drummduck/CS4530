package com.example.natha.assignment2

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_file_selection.*


class Draw : AppCompatActivity() {

    lateinit var brushButton: ImageButton
    lateinit var undoButton : ImageButton
    lateinit var redoButton : ImageButton
    lateinit var drawCanvas : DrawCanvas

    var redoEnabled = false
    var undoEnabled = false

    var undoArray = ArrayList<ArrayList<Pair<Float,Float>>>()
    var redoArray = ArrayList<ArrayList<Pair<Float,Float>>>()
    var currentDraw = ArrayList<Pair<Float,Float>>()

    val clickListener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.brushButton ->
            {
                intent = Intent(applicationContext, ColorPicker::class.java)
                intent.putExtra("rValue", drawCanvas.getColor()[0])
                intent.putExtra("gValue", drawCanvas.getColor()[1])
                intent.putExtra("bValue", drawCanvas.getColor()[2])
                intent.putExtra("wValue", drawCanvas.getPaintWidth())
                intent.putExtra("capValue", drawCanvas.getCap())
                intent.putExtra("joinValue", drawCanvas.getJoin())
                startActivity(intent)
                finish()
            }

            R.id.undoButton ->
            {
                if(!undoArray.isEmpty()) {
                    redoArray.add(undoArray[undoArray.size - 1])
                    undoArray.removeAt(undoArray.size - 1)
                    drawCanvas.setCanvas(undoArray)
                    if (undoArray.isEmpty()) undoButton.setColorFilter(Color.argb(180, 255, 255, 255))
                    redoButton.setColorFilter(Color.argb(0, 255, 255, 255))
                }
            }

            R.id.redoButton ->
            {
                if(!redoArray.isEmpty())
                {
                    undoArray.add(redoArray[redoArray.size - 1])
                    redoArray.removeAt(redoArray.size - 1)
                    drawCanvas.setCanvas(undoArray)
                    if(redoArray.isEmpty()) redoButton.setColorFilter(Color.argb(180,225,225,225))
                    undoButton.setColorFilter(Color.argb(0,255,255,255))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        drawCanvas = findViewById(R.id.drawingSpace)
        var intent = getIntent()

        var red : Int = 0
        var green : Int = 0
        var blue : Int = 0
        drawCanvas.setCap(Paint.Cap.BUTT.name)
        drawCanvas.setJoin(Paint.Join.MITER.name)
        drawCanvas.setWidth(20F)

        if(intent != null && intent.extras != null) {
            for (i in intent.extras.keySet()) {
                when (i) {
                    "rValue" -> red = intent.getIntExtra(i, 0)
                    "gValue" -> green = intent.getIntExtra(i, 0)
                    "bValue" -> blue = intent.getIntExtra(i, 0)
                    "wValue" -> drawCanvas.setWidth(intent.getFloatExtra(i, 20F))
                    "capValue" -> drawCanvas.setCap(intent.getStringExtra("capValue"))
                    "joinValue" -> drawCanvas.setJoin(intent.getStringExtra("joinValue"))
                }
            }
        }

        drawCanvas.setColor(intArrayOf(red, green, blue))
        drawCanvas.setOnTouchDrawListener { _, currentDrawing, release ->
            if(release) {
                undoArray.add(currentDrawing)
                undoButton.setColorFilter(Color.argb(0, 255, 255, 255))
            }
            else
            {
                redoArray.clear()
                redoButton.setColorFilter(Color.argb(180,255,255,255))
            }
        }


        brushButton = findViewById(R.id.brushButton)
        brushButton.setOnClickListener(clickListener)

        undoButton = findViewById(R.id.undoButton)
        undoButton.setColorFilter(Color.argb(180,255,255,255))
        undoButton.setOnClickListener(clickListener)

        redoButton = findViewById(R.id.redoButton)
        redoButton.setColorFilter(Color.argb(180,255,255,255))
        redoButton.setOnClickListener(clickListener)
    }
}