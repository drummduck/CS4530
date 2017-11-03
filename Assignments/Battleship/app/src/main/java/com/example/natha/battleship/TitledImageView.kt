package com.example.natha.battleship

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.view_titled_image.view.*
import java.io.File

import kotlinx.android.synthetic.main.view_titled_image.view.buttonView as importedButtonView
import kotlinx.android.synthetic.main.view_titled_image.view.titleTextView as importedTitledTextView

/**
 * Created by Natha on 10/2/2017.
 */
class TitledImageView : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    var button: Int
        get() = buttonView.id
        set(newimage){buttonView.setBackgroundResource(newimage)}

    var title: String
        get() = titleTextView.text.toString()
        set(newTitle) { titleTextView.text = newTitle}

    var buttonView: ImageButton = ImageButton(context)
    var titleTextView = TextView(context)


    override fun onFinishInflate() {
        super.onFinishInflate()
        buttonView = importedButtonView
        titleTextView = importedTitledTextView
    }


}

