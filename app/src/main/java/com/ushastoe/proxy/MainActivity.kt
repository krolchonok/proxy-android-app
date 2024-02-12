package com.ushastoe.proxy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButton
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {
    private val name_setting = "prefs_proxy"

    @SuppressLint("ResourceType", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        registercallback()

        val textView = findViewById<TextView>(R.id.infotext)
        textView.text = checkproxy()
        textView.gravity = Gravity.CENTER
        val list = getListSaveProxy()?.toMutableList()
        generate_button(list ?: return)
        val savebutton = findViewById<Button>(R.id.save)
        savebutton.setOnClickListener {
            if (findViewById<EditText>(R.id.proxytext).text.toString() in list) {
                Toast.makeText(this, "IP уже был сохранен", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                Toast.makeText(this, "IP сохранен", Toast.LENGTH_SHORT).show()
                list += findViewById<EditText>(R.id.proxytext).text.toString()
                generate_button(list)
                saveListProxy(list)
            }
        }
        val clearbutton = findViewById<Button>(R.id.clear)
        clearbutton.setOnClickListener {
            list.clear()
            list += "10.0.0.10"
            generate_button(list)
            Toast.makeText(this, "Список очищен", Toast.LENGTH_SHORT).show()
            saveListProxy(list)
        }
    }


    private fun generate_button(list: List<String>) {
        val existingLinearLayout = findViewById<LinearLayout>(R.id.ip_layout)
        existingLinearLayout.removeAllViews()

        val themeWrapper = ContextThemeWrapper(
            this,
            R.style.MyButton
        )
        for (i in list) {


            val button = MaterialButton(themeWrapper)
            button.text = i
            button.setOnClickListener {
                findViewById<EditText>(R.id.proxytext).setText(i)
                findViewById<EditText>(R.id.proxytext).setSelection(findViewById<EditText>(R.id.proxytext).length())
            }
            existingLinearLayout.addView(button)
        }
    }
    private fun saveListProxy(list: List<String>) {
        val preferences = getSharedPreferences(name_setting, MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("proxy_last", list.joinToString(";"))
        editor.apply()

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

        buttonsend.setOnLongClickListener {
            finishAffinity()

            true
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

    private fun checkproxy(): String {
        var cmdresult: String? = runCommandWithAnswer("su -c settings get global http_proxy")

        cmdresult = if (cmdresult == ":0") {
            "Прокси выключен"
        } else {
            "Текущее прокси: \n$cmdresult"
        }
        return cmdresult
    }

    private fun runCommand(cmd: String?) {
            Runtime.getRuntime().exec(cmd)
    }

    private fun getListSaveProxy(): List<String>? {
        val preferences = getSharedPreferences(name_setting, MODE_PRIVATE)
        val savedText = preferences.getString("proxy_last", "10.0.0.10")
        if (savedText != null) {
            return savedText.split(";")
        }
        return null
    }

    private fun runCommandWithAnswer(cmd: String): String? {
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
            output.toString().substring(0, output.length - 1)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}