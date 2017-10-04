package com.example.natha.assignment2

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_file_selection.*


class Draw : AppCompatActivity() {

    lateinit var imageButton : ImageButton

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

//        blueValue.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(character: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                try
//                {
//                    var number : Int = Integer.parseInt(character.toString())
//                    if(number > 255)
//                    {
//                        number = 255
//                        blueValue.setText(number.toString())
//                    }
//
//                    if(blueValue.inputType == InputType.TYPE_CLASS_TEXT) blueValue.inputType = InputType.TYPE_CLASS_NUMBER
//
//                    blueSlider.setProgress(number)
//                }
//                catch(e : NumberFormatException){return}
//            }
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//            override fun afterTextChanged(p0: Editable?) {}
//        }
    }
}

