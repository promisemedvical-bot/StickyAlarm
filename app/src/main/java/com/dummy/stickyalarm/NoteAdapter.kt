package com.dummy.stickyalarm

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dummy.stickyalarm.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onClick: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val root: android.view.View = view.findViewById(R.id.noteRoot)
        val title: TextView = view.findViewById(R.id.txtTitle)
        val snippet: TextView = view.findViewById(R.id.txtSnippet)
        val date: TextView = view.findViewById(R.id.txtDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val note = notes[position]
        holder.root.setBackgroundColor(Color.parseColor(note.colorHex))
        holder.title.text = note.title.ifBlank { "Untitled" }
        holder.snippet.text = if (note.body.isNotBlank()) note.body else if (note.drawingBase64 != null) "🎨 Drawing note" else ""
        holder.date.text = note.eventDateTime?.let {
            SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(it))
        } ?: ""
        holder.itemView.rotation = if (position % 2 == 0) -2f else 2f
        holder.itemView.setOnClickListener { onClick(note) }
        holder.itemView.setOnLongClickListener { onLongClick(note); true }
    }

    override fun getItemCount() = notes.size
}
