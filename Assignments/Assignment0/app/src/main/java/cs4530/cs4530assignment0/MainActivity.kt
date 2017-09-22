/*
Nathan Donaldson
U0632803
CS4530
*/


package cs4530.cs4530assignment0

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    var passed: Boolean = false
    //Strings are used throughout this class

    var inputString: String = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var theWordOfAllWords: inputWord = inputWord("#",-1)
        var index: Int = 0

        inputString = String.fromAsset(this, "Assignment0input.txt") ?: ""
       //  val filepath: String = filesDir.absolutePath + File.separator + "Output.txt"


        //Object
        val Object = object
        {
            fun containsUppercaseLetter(inWord: inputWord): Boolean
            {
                for(i in inWord.word.toCharArray()) {
                    Log.i("DEBUG ", i.toString())

                    if (i.isUpperCase()) {
                        //Log.i("DEBUG", "CONTAINS AN UPPERCASELETTER PASSED: " + inWord.word)
                        return true
                    }
                }
                return false

            }
        }


        //Loop through words and call all functions through big function testForValidity
        for(i in inputString.split("\\s+".toRegex()))
        {
            var currentWord = inputWord(i, index)
            //Log.i("DEBUG", currentWord.word)

            if( Object.containsUppercaseLetter(currentWord) && testForValidity(currentWord) == answers.YES && theWordOfAllWords.word.equals('#')) theWordOfAllWords = currentWord
            else if(Object.containsUppercaseLetter(currentWord) && passed)
            {
                if(isBigger(currentWord, theWordOfAllWords))
                {
                    //Log.i("DEBUG", "CURRENTWORD: " + currentWord.word + " IS BIGGER THAN THEWORDOFALLWORDS: " + theWordOfAllWords.word)
                    theWordOfAllWords = currentWord
                }
                else if(!currentWord.firstLetter.equals('#') && !theWordOfAllWords.firstLetter.equals('#') && currentWord.firstLetter < theWordOfAllWords.firstLetter)
                {
                    //Log.i("DEBUG", "CURRENTWORD: " + currentWord.word + "'s FIRST LETTER APPEARS BEFORE THEWORDOFALLWORDS: " + theWordOfAllWords.word + "'s FIRST LETTER IN THE ALPHABET")
                    theWordOfAllWords = currentWord
                }
                else if(currentWord.index < theWordOfAllWords.index)
                {
                    //Log.i("DEBUG", "CURRENTWORD: " + currentWord.word + " APPEARS EARLIER IN THE INPUT STRING THAN THEWORDOFALLWORDS " + theWordOfAllWords.word)
                    theWordOfAllWords = currentWord
                }
            }

            index++
            passed = false
        }

        Log.i("DEBUG", theWordOfAllWords.word)
        val textview: TextView = findViewById<TextView>(R.id.newString)
        textview.text = theWordOfAllWords.word
    }

    //Lambda
    val isBigger = {firstWord: inputWord, secondWord: inputWord -> firstWord.size > secondWord.size}

    //Functions
    fun testForValidity(word: inputWord): answers
    {
        if(word.word.toCharArray().filter {it.isLetter()}.size == word.size && hasEvenCharacterCount(word) && appearsMoreThanOnce(word))
        {
            passed = true
            return answers.YES
        }
        else return answers.NO
    }

//    fun containsOnlyLetters(inWord: inputWord): Boolean{
//
//        for(i in inWord.word.toCharArray())
//        {
//            if(!i.isLetter()) return false
//        }
//        //Log.i("DEBUG", "CONTAINS ONLY LETTERS PASSED: " + inWord.word)
//        return true
//    }

    fun hasEvenCharacterCount(inWord: inputWord): Boolean {
        if(inWord.word.length % 2 == 0)
        {
            //Log.i("DEBUG", "HAS EVEN CHARACTER COUNT PASSED: " + inWord.word)
            return true
        }
        return false
    }

    fun appearsMoreThanOnce(inWord: inputWord): Boolean{
        var count: Int = 0
        for(i in inputString.split("\\s+".toRegex()))
        {
            if(inWord.word.equals(i)) count++
            if(count > 1)
            {
                //Log.i("DEBUG", "WORD APPEARS MORE THAN ONCE PASSED: " + inWord.word)
                return true
            }
        }
        return false
    }
}

//enum
enum class answers
{
    YES,NO
}

//class
class inputWord(val word: String, val index: Int)
{
    var size: Int = word.length
    var firstLetter: Char = word[0]
}



