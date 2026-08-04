package com.dummy.stickyalarm

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dummy.stickyalarm.alarm.AlarmScheduler
import com.dummy.stickyalarm.calendar.CalendarHelper
import com.dummy.stickyalarm.data.NoteRepository
import com.dummy.stickyalarm.model.Note
import java.text.SimpleDateFormat
import java.util.*

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var edtTitle: EditText
    private lateinit var edtBody: EditText
    private lateinit var drawingView: DrawingView
    private lateinit var txtReminderPreview: TextView

    private var note = Note()
    private var selectedDateTime: Long? = null
    private var isDrawMode = false

    private val emojis = listOf("😀","🎉","❤️","⭐","✅","📌","🔥","🎂","📅","🥳","😅","👍")
    private val colors = listOf("#FFC1E3", "#4FC3F7", "#FFF176", "#A5F3C9", "#FFAB40")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)

        edtTitle = findViewById(R.id.edtTitle)
        edtBody = findViewById(R.id.edtBody)
        drawingView = findViewById(R.id.drawingView)
        txtReminderPreview = findViewById(R.id.txtReminderPreview)

        requestPermissions()

        val noteId = intent.getLongExtra("noteId", -1L)
        if (noteId != -1L) {
            NoteRepository.getById(this, noteId)?.let {
                note = it
                edtTitle.setText(it.title)
                edtBody.setText(it.body)
                it.drawingBase64?.let { b64 -> drawingView.loadFromBase64(b64) }
                selectedDateTime = it.eventDateTime
                updateReminderPreview()
            }
        }

        setupEmojiRow()
        setupColorRow()

        findViewById<Button>(R.id.btnModeText).setOnClickListener { toggleMode(false) }
        findViewById<Button>(R.id.btnModeDraw).setOnClickListener { toggleMode(true) }

        findViewById<Button>(R.id.btnReminder).setOnClickListener { pickDateTime() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveNote() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            NoteRepository.delete(this, note.id)
            AlarmScheduler.cancel(this, note.id)
            finish()
        }
    }

    private fun toggleMode(drawMode: Boolean) {
        isDrawMode = drawMode
        edtBody.visibility = if (drawMode) android.view.View.GONE else android.view.View.VISIBLE
        drawingView.visibility = if (drawMode) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupEmojiRow() {
        val row = findViewById<LinearLayout>(R.id.emojiRow)
        for (e in emojis) {
            val btn = Button(this).apply {
                text = e
                setOnClickListener {
                    val target = if (edtBody.hasFocus()) edtBody else edtTitle
                    val pos = target.selectionStart.coerceAtLeast(0)
                    target.text.insert(pos, e)
                }
            }
            row.addView(btn)
        }
    }

    private fun setupColorRow() {
        val row = findViewById<LinearLayout>(R.id.colorRow)
        for (c in colors) {
            val swatch = android.view.View(this).apply {
                layoutParams = LinearLayout.LayoutParams(60, 60).apply { setMargins(8, 8, 8, 8) }
                setBackgroundColor(Color.parseColor(c))
                setOnClickListener { note.colorHex = c }
            }
            row.addView(swatch)
        }
    }

    private fun pickDateTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                cal.set(y, m, d, h, min, 0)
                selectedDateTime = cal.timeInMillis
                updateReminderPreview()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateReminderPreview() {
        txtReminderPreview.text = selectedDateTime?.let {
            "Reminder: " + SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(it))
        } ?: "No reminder set"
    }

    private fun saveNote() {
        note.title = edtTitle.text.toString()
        note.body = edtBody.text.toString()
        note.eventDateTime = selectedDateTime
        if (drawingView.hasContent()) note.drawingBase64 = drawingView.exportToBase64()

        NoteRepository.save(this, note)

        if (selectedDateTime != null) {
            checkExactAlarmPermission()
            AlarmScheduler.schedule(this, note)
            CalendarHelper.insertOrUpdateEvent(this, note)
        }
        finish()
    }

    private fun requestPermissions() {
        val perms = mutableListOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
        val notGranted = perms.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(android.content.Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName")
                ))
            }
        }
    }
}
