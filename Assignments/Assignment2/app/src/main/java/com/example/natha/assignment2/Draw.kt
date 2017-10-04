package com.example.natha.assignment2

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_file_selection.*


class Draw : AppCompatActivity() {

    lateinit var imageButton: ImageButton

    val clickListener = View.OnClickListener { view ->
        when (view.getId()) {
            R.id.brushButton -> {
                intent = Intent(applicationContext, ColorPicker::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        imageButton = findViewById(R.id.brushButton)
        imageButton.setOnClickListener(clickListener)
    }
}