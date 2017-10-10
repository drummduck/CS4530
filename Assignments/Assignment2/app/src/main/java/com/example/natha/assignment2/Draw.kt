package com.example.natha.assignment2

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_file_selection.*
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import android.os.Environment.MEDIA_MOUNTED
import android.provider.ContactsContract
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import java.io.*
import android.app.Activity
import android.graphics.ColorFilter
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Paths


class Draw : AppCompatActivity() {

    lateinit var brushButton: ImageButton
    lateinit var undoButton : ImageButton
    lateinit var redoButton : ImageButton
    lateinit var drawCanvas : DrawCanvas

    var redoEnabled = false
    var undoEnabled = false

    var undoArray = ArrayList<ArrayList<Pair<Float,Float>>>()
    var redoArray = ArrayList<ArrayList<Pair<Float,Float>>>()
    var fileName : String = ""
    var numOfFiles : Int = 0

    var fromColorPicker = false
    var fromFileSelection = false

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
                intent.putExtra("fileName", fileName)
                startActivityForResult(intent,1)
            }

            R.id.undoButton ->
            {
                if(!undoArray.isEmpty()) {
                    redoArray.add(undoArray[undoArray.size - 1])
                    undoArray.removeAt(undoArray.size - 1)
                    drawCanvas.setCanvas(undoArray)
                    if (undoArray.isEmpty()) undoButton.setColorFilter(Color.argb(180, 255, 255, 255))
                    redoButton.setColorFilter(Color.argb(0, 255, 255, 255))
                    writeToFile()
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
                    writeToFile()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        if(!fromColorPicker) {
            drawCanvas = findViewById(R.id.drawingSpace)

            brushButton = findViewById(R.id.brushButton)
            brushButton.setOnClickListener(clickListener)

            undoButton = findViewById(R.id.undoButton)
            undoButton.setOnClickListener(clickListener)

            redoButton = findViewById(R.id.redoButton)
            redoButton.setOnClickListener(clickListener)

            var intent = getIntent()

            if (savedInstanceState != null) {
                var red: Int = 0
                var green: Int = 0
                var blue: Int = 0
                for (i in savedInstanceState.keySet()) {
                    when (i) {
                        "rValue" -> red = savedInstanceState.getInt(i, 0)
                        "gValue" -> green = savedInstanceState.getInt(i, 0)
                        "bValue" -> blue = savedInstanceState.getInt(i, 0)
                        "wValue" -> drawCanvas.setWidth(savedInstanceState.getFloat("wValue", 20F))
                        "capValue" -> drawCanvas.setCap(savedInstanceState.getString("capValue"))
                        "joinValue" -> drawCanvas.setJoin(savedInstanceState.getString("joinValue"))
                        "fileName" -> fileName = savedInstanceState.getString("fileName")
                    }
                }
                drawCanvas.setColor(intArrayOf(red, green, blue))
            }
            else if (intent != null && intent.extras != null) {
                for (i in intent.extras.keySet()) {
                    when (i) {
                        "fileName" -> fileName = intent.getStringExtra("fileName")
                        "numOfFiles" -> numOfFiles = intent.getIntExtra("numOfFiles", 0)
                    }
                }
                drawCanvas.setCap(Paint.Cap.BUTT.name)
                drawCanvas.setJoin(Paint.Join.MITER.name)
                drawCanvas.setWidth(20F)
                drawCanvas.setColor(intArrayOf(0, 0, 0))
                fromFileSelection = true
                if (fileName.equals("New Project")) fileName = "Project" + (numOfFiles + 1)
                else readFromFile()
            }

            drawCanvas.setOnTouchDrawListener { _, currentDrawing, release ->
                if (release) {
                    undoArray.add(currentDrawing)
                    undoButton.setColorFilter(Color.argb(0, 255, 255, 255))
                    writeToFile()
                } else {
                    redoArray.clear()
                    redoButton.setColorFilter(Color.argb(180, 255, 255, 255))
                }
            }
        }
        fromColorPicker = false
        fromFileSelection = false
    }

    fun writeToFile()
    {
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Assignment2Draw/" + fileName)

        if(file.exists())
        {
            file.delete()
            file.createNewFile()
        }

        val outputWriter = FileOutputStream(file)
        val outputStream = DataOutputStream(outputWriter)
        Log.e("WRITE TO FILE", "size of undoArray: " + undoArray.size)
        outputStream.write(undoArray.size)
        for(i in undoArray) outputStream.write(i.size)
        for(i in undoArray)
        {
            for(j in i)
            {
                outputStream.writeFloat(j.first)
                outputStream.writeFloat(j.second)
            }
        }

        outputStream.write(redoArray.size)
        for(i in redoArray) outputStream.write(i.size)
        for(i in redoArray)
        {
            for(j in i)
            {
                outputStream.writeFloat(j.first)
                outputStream.writeFloat(j.second)
            }
        }
        outputStream.flush()
        outputStream.close()
        outputWriter.close()
    }

    fun readFromFile()
    {
        Log.e("FILENAME", "Filename is: " + fileName)
        var file : File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Assignment2Draw/" + fileName)

        val inputFile = FileInputStream(file)
        val inputReader = DataInputStream(inputFile)
        var undoArraySize = inputReader.readInt()
        Log.e("READ FROM FILE", "undoArraySize is: " + undoArraySize)
        var undoPairArraySize = ArrayList<Int>()
        for(i in 1..undoArraySize) undoPairArraySize.add(inputReader.readInt())
        for(i in undoPairArraySize)
        {
            var pairArray = ArrayList<Pair<Float,Float>>()
            for(j in 1..i)
            {
                var pair = Pair(inputReader.readFloat(), inputReader.readFloat())
                pairArray.add(pair)
            }
            undoArray.add(pairArray)
        }
        var redoArraySize = inputReader.readInt()
        var redoPairArraySize = ArrayList<Int>()
        for(i in 1..redoArraySize) redoPairArraySize.add(inputReader.readInt())
        for(i in 1..redoArraySize)
        {
            var pairArray = ArrayList<Pair<Float,Float>>()
            for(j in redoPairArraySize)
            {
                var pair = Pair(inputReader.readFloat(), inputReader.readFloat())
                pairArray.add(pair)
            }
            redoArray.add(pairArray)
        }

        if(redoArray.size > 0) redoButton.setColorFilter(Color.argb(0,255,255,255))
        if(undoArray.size > 0) undoButton.setColorFilter(Color.argb(0,255,255,255))

        drawCanvas.invalidate()
        inputReader.close()
        inputFile.close()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
        super.onSaveInstanceState(savedInstanceState)
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
            savedInstanceState!!.putInt("redValue", drawCanvas.getColor()[0])
            savedInstanceState.putInt("greenValue", drawCanvas.getColor()[1])
            savedInstanceState.putInt("blueValue", drawCanvas.getColor()[2])
            savedInstanceState.putFloat("wValue", drawCanvas.getPaintWidth())
            savedInstanceState.putString("capValue", drawCanvas.getCap())
            savedInstanceState.putString("joinValue", drawCanvas.getJoin())
            savedInstanceState.putString("fileName", fileName)
            super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        if (requestCode == 1) {
                var red = 0
                var green = 0
                var blue = 0
                for(i in data.extras.keySet())
                {
                    Log.e("KEYSET", "KEYSET ACTIVITY RESULT VALUE: " + i)
                    Log.e("VALUESET", "VALUESET ACTIVITY RESULT VALUE: " + data.extras.get(i))
                    when(i)
                    {
                        "rValue" -> red = intent.getIntExtra(i, 0)
                        "gValue" -> green = intent.getIntExtra(i, 0)
                        "bValue" -> blue = intent.getIntExtra(i, 0)
                        "wValue" -> drawCanvas.setWidth(intent.getFloatExtra(i, 20F))
                        "capValue" -> drawCanvas.setCap(intent.getStringExtra("capValue"))
                        "joinValue" -> drawCanvas.setJoin(intent.getStringExtra("joinValue"))
                    }
                }

                drawCanvas.setColor(intArrayOf(red, green, blue))
                fromColorPicker = true
            }
    }

    override fun onBackPressed() {
        var intent = Intent(this, FileSelection::class.java)
        startActivity(intent)
        finish()
    }
}