package com.example.pruebafirebase.Presentation

import android.content.Context

class SharedPreferenceManager(context: Context) {
    private val preferences = context.getSharedPreferences(
        context.packageName,
        Context.MODE_PRIVATE
    )

    private val editor = preferences.edit()

    private val keyEmail = "email"
    private val keyPassword = "password"
    private val keyChecked = "checked"

    var email
        get()=preferences.getString(keyEmail,"").toString()
        set(value:String){
            editor.putString(keyEmail,value)
            editor.commit()
        }
    var password
        get()=preferences.getString(keyPassword,"").toString()
        set(value:String){
            editor.putString(keyPassword,value)
            editor.commit()
        }
    var checked
        get()=preferences.getBoolean(keyChecked,false)
        set(value:Boolean){
            editor.putBoolean(keyChecked,value)
            editor.commit()
        }
}