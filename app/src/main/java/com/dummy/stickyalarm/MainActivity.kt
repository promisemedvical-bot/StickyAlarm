package com.dummy.stickyalarm

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dummy.stickyalarm.alarm.AlarmScheduler
import com.dummy.stickyalarm.data.NoteRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler = findViewById(R.id.recyclerNotes)
        recycler.layoutManager = GridLayoutManager(this, 3)

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(android.content.Intent(this, NoteEditorActivity::class.java))
        }

        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val notes = NoteRepository.getAll(this)
        recycler.adapter = NoteAdapter(notes,
            onClick = { note ->
                startActivity(android.content.Intent(this, NoteEditorActivity::class.java)
                    .putExtra("noteId", note.id))
            },
            onLongClick = { note ->
                AlertDialog.Builder(this)
                    .setTitle("Delete note?")
                    .setPositiveButton("Delete") { _, _ ->
                        NoteRepository.delete(this, note.id)
                        AlarmScheduler.cancel(this, note.id)
                        loadNotes()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
    }
}
