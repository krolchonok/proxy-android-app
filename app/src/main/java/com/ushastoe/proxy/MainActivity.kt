package com.ushastoe.proxy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        registercallback()

        val textView = findViewById<TextView>(R.id.infotext)
        textView.text = checkproxy()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun registercallback() {
        val buttonsend = findViewById<Button>(R.id.send)
        val inputtext = findViewById<EditText>(R.id.proxytext)
        val textView = findViewById<TextView>(R.id.infotext)

        inputtext.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2

            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (inputtext.right - inputtext.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    inputtext.text = null
                    return@setOnTouchListener true
                }
            }
            false
        }

        buttonsend.setOnClickListener {
            if (checkportinproxy(inputtext.text.toString())) {
                runCommand("su -c settings put global http_proxy ${inputtext.text}")
                repeat(5) {
                    textView.text = checkproxy()
                    Thread.sleep(50)
                }
            } else {
                Toast.makeText(this, "Прокси без порта", Toast.LENGTH_SHORT).show()
            }
        }

        val button3128 = findViewById<Button>(R.id.button1)

        button3128.setOnClickListener {
            val newip = inputtext.text.toString()
            val modifiedAddress = replacePort(newip, 3128)
            inputtext.setText(modifiedAddress)
            inputtext.setSelection(inputtext.length())
        }

        val button80 = findViewById<Button>(R.id.button2)

        button80.setOnClickListener {
            val newip = inputtext.text.toString()
            val modifiedAddress = replacePort(newip, 80)
            inputtext.setText(modifiedAddress)
            inputtext.setSelection(inputtext.length())
        }

        val buttonempty = findViewById<Button>(R.id.button3)

        buttonempty.setOnClickListener {
            inputtext.setText(":0")
            inputtext.setSelection(inputtext.length())
        }

        val proxyipbutton = findViewById<Button>(R.id.proxyip10)
        proxyipbutton.setOnClickListener {
            inputtext.setText("10.0.0.10")
            inputtext.setSelection(inputtext.length())
        }
    }

    private fun replacePort(originalAddress: String, newPort: Int): String {
        val parts = originalAddress.split(":")
        return if (parts.size == 2) {
            val ipAddress = parts[0]
            "$ipAddress:$newPort"
        } else {
            "$originalAddress:$newPort"
        }
    }
    private fun checkportinproxy(originalAddress: String): Boolean {
        val parts = originalAddress.split(":")
        return parts.size == 2
    }

    private fun checkproxy(): String? {
        var cmdresult: String? = runCommandWithAnswer("su -c settings get global http_proxy")

        cmdresult = if (cmdresult == ":0") {
            "Прокси выключен"
        } else {
            "Прокси установлен $cmdresult"
        }
        return cmdresult
    }

    private fun runCommand(cmd: String?) {
            Runtime.getRuntime().exec(cmd)
    }

    private fun runCommandWithAnswer(cmd: String?): String? {
        return try {
            val process = Runtime.getRuntime().exec(cmd)

            val reader = BufferedReader(
                InputStreamReader(process.inputStream)
            )
            var read: Int
            val buffer = CharArray(4096)
            val output = StringBuffer()
            while (reader.read(buffer).also { read = it } > 0) {
                output.append(buffer, 0, read)
            }
            reader.close()

            process.waitFor()
            output.toString()?.substring(0, output.length - 1)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

}