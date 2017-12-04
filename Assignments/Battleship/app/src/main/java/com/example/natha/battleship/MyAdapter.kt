package com.example.natha.battleship

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.menu.MenuView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.view_titled_image.view.*
import java.io.File
import java.util.regex.Pattern


/**
 * Created by Natha on 10/1/2017.
 */
class MyAdapter(private val dataset: Array<MyAdapterItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    var auth = FirebaseAuth.getInstance()
    var currentUser = auth.currentUser
    val KILL = "com.example.natha.battleship.KILL"

    override fun getItemCount(): Int = dataset.size

    override fun getItemViewType(position: Int): Int = dataset[position].adapterItemType.ordinal

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            MyAdadpterItemType.TITLED_IMAGE.ordinal -> {
                val dataSetItem: MyAdapterItem = dataset[position]
                if (holder !is TitledImageViewHolder || dataSetItem !is ImageWithTitle) throw AssertionError("Invalid ViewHolder was supplied for binding, or the dataset contained an unexpected value.")
                holder.titledImageView.button = dataSetItem.button
                holder.titledImageView.title = dataSetItem.title
                holder.titledImageView.buttonView.setTag(dataSetItem.gameId)
                holder.titledImageView.buttonView.setOnClickListener(clickListener)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            MyAdadpterItemType.TITLED_IMAGE.ordinal -> {
                TitledImageViewHolder(layoutInflater.inflate(R.layout. view_titled_image, parent, false) as TitledImageView).apply {
                    titledImageView.setOnClickListener { clickedView: View ->
                        Log.e("MyAdapter","Selected ${clickedView.javaClass.canonicalName} at position $adapterPosition. Notifying listener.")
                        onMyAdapterItemSelectedListener?.myAdapterItemSelected(dataset[adapterPosition])
                    }
                }
            }

            else -> {
                throw AssertionError("Invalid viewType supplied.")
            }
        }
    }

    enum class MyAdadpterItemType {
        TITLED_IMAGE
    }

    interface OnMyAdapterItemSelectedListener {
        fun myAdapterItemSelected(myAdapterItem: MyAdapterItem)
    }

    interface MyAdapterItem {
        val adapterItemType : MyAdadpterItemType
    }

    data class ImageWithTitle(val button: Int, val title: String, val gameId : String) : MyAdapterItem {
        override val adapterItemType: MyAdadpterItemType = MyAdadpterItemType.TITLED_IMAGE
    }
    class TitledImageViewHolder(val titledImageView: TitledImageView) : RecyclerView.ViewHolder(titledImageView)

    private var onMyAdapterItemSelectedListener : OnMyAdapterItemSelectedListener? = null

    fun setOnMyAdapterItemSelectedListener(onMyAdapterItemSelectedListener: OnMyAdapterItemSelectedListener) {
        this.onMyAdapterItemSelectedListener = onMyAdapterItemSelectedListener
    }

    fun setOnMyAdapterItemSelectedListener(onMyAdapterItemSelectedListener: ((myAdapterItem : MyAdapterItem) -> Unit)) {
        this.onMyAdapterItemSelectedListener = object : OnMyAdapterItemSelectedListener {
            override fun myAdapterItemSelected(myAdapterItem: MyAdapterItem) {
                onMyAdapterItemSelectedListener(myAdapterItem)
            }
        }
    }

    val clickListener = View.OnClickListener { view ->
        var parent = view.parent
        var intent = Intent(view.context,GameState::class.java)
        var killIntent = Intent()
        killIntent.setAction(KILL)
        if(parent is LinearLayout)
        {
            var child = parent.getChildAt(1)
            if(child is TextView)
            {
                if(child.text.contains("waiting for player"))
                {
                    var matched = false
                    var pattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                    var matcher = pattern.matcher(child.text)
                    while(matcher.find())
                    {
                        if(matcher.group().equals(currentUser!!.email)) {
                            intent.putExtra("isPlayerOne", true)
                            intent.putExtra("gameId", parent.getChildAt(0).getTag().toString())
                            matched = true
                            view.context.startActivity(intent)
                            view.context.sendBroadcast(killIntent)
                        }
                    }

                    if(!matched) {
                        Log.e("JOINING GAME", "Joining game!")
                        intent.putExtra("joining", true)
                        intent.putExtra("gameId", parent.getChildAt(0).getTag().toString())
                        view.context.startActivity(intent)
                        view.context.sendBroadcast(killIntent)
                    }
                }

                else if(child.text.contains("Game Started") || child.text.contains("Player One's Turn")
                        || child.text.contains("Player Two's Turn"))
                {
                    var matched = false
                    var pattern = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                    var matcher = pattern.matcher(child.text)
                    var count = 1
                    while(matcher.find())
                    {
                        if(matcher.group().equals(currentUser!!.email)) {
                            if(count == 1)intent.putExtra("isPlayerOne", true)
                            intent.putExtra("gameId", parent.getChildAt(0).getTag().toString())
                            matched = true
                            view.context.startActivity(intent)
                            view.context.sendBroadcast(killIntent)
                        }
                        count++
                    }

                    if(!matched) {
                        Log.e("SPECTATING", "YOU ARE CURRENTLY SPECTATING!")
                        intent.putExtra("isSpectating", true)
                        intent.putExtra("gameId", parent.getChildAt(0).getTag().toString())
                        view.context.startActivity(intent)
                        view.context.sendBroadcast(killIntent)
                    }
                }

                else if(child.text.contains("Wins!"))
                {
                    var rootRef = FirebaseDatabase.getInstance().reference
                    FirebaseDatabase.getInstance().reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            if(dataSnapshot!= null && dataSnapshot.hasChild(currentUser!!.uid))
                            {
                                if(dataSnapshot.child(currentUser!!.uid).hasChild("GamesToIgnore"))
                                {
                                    var games = dataSnapshot.child(currentUser!!.uid).child("GamesToIgnore").getValue() as ArrayList<String>
                                    if(games != null) {
                                        games.add(parent.getChildAt(0).getTag().toString())
                                        var gamesRef = dataSnapshot.child(currentUser!!.uid).child("GamesToIgnore").ref
                                        gamesRef.setValue(games)
                                    }
                                }
                                else
                                {
                                    var userRef = dataSnapshot.child(currentUser!!.uid).ref
                                    var gamesToIgnore = ArrayList<String>()
                                    gamesToIgnore.add(parent.getChildAt(0).getTag().toString())
                                    userRef.child("GamesToIgnore").setValue(gamesToIgnore)
                                }
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }
                    })

                }

                else if (child.text.contains("New Game"))
                {
                    Log.e("NEW GAME", "Starting new game!")
                    intent.putExtra("isPlayerOne", true)
                    intent.putExtra("New Game", "")
                    view.context.startActivity(intent)
                    view.context.sendBroadcast(killIntent)
                }
            }
        }
    }
}