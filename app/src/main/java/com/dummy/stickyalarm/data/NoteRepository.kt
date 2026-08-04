package com.dummy.stickyalarm.data

import android.content.Context
import com.dummy.stickyalarm.model.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NoteRepository {
    private const val PREFS = "sticky_prefs"
    private const val KEY = "notes_json"
    private val gson = Gson()

    fun getAll(context: Context): MutableList<Note> {
        val json = prefs(context).getString(KEY, "[]")
        val type = object : TypeToken<MutableList<Note>>() {}.type
        return gson.fromJson(json, type)
    }

    fun getById(context: Context, id: Long): Note? =
        getAll(context).find { it.id == id }

    fun save(context: Context, note: Note) {
        val list = getAll(context)
        val idx = list.indexOfFirst { it.id == note.id }
        if (idx >= 0) list[idx] = note else list.add(note)
        prefs(context).edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun delete(context: Context, id: Long) {
        val list = getAll(context)
        list.removeAll { it.id == id }
        prefs(context).edit().putString(KEY, gson.toJson(list)).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
