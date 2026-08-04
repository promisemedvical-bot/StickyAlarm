package com.dummy.stickyalarm.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.dummy.stickyalarm.data.NoteRepository
import com.dummy.stickyalarm.model.Note
import java.util.TimeZone

object CalendarHelper {

    fun insertOrUpdateEvent(context: Context, note: Note) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission || note.eventDateTime == null) return

        val resolver = context.contentResolver
        val calId = getDefaultCalendarId(context) ?: return

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, note.title)
            put(CalendarContract.Events.DESCRIPTION, note.body)
            put(CalendarContract.Events.DTSTART, note.eventDateTime)
            put(CalendarContract.Events.DTEND, note.eventDateTime!! + 3600000)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        if (note.calendarEventId == null) {
            val uri = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            note.calendarEventId = uri?.lastPathSegment?.toLongOrNull()
            NoteRepository.save(context, note)
        } else {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, note.calendarEventId!!)
            resolver.update(uri, values, null, null)
        }
    }

    private fun getDefaultCalendarId(context: Context): Long? {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return null

        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID), null, null, null
        )
        cursor?.use { if (it.moveToFirst()) return it.getLong(0) }
        return null
    }
}
