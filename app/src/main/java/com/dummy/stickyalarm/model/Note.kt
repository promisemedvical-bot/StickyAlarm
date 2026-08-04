package com.dummy.stickyalarm.model

data class Note(
    var id: Long = System.currentTimeMillis(),
    var title: String = "",
    var body: String = "",
    var colorHex: String = "#FFF176",
    var drawingBase64: String? = null,
    var eventDateTime: Long? = null,
    var calendarEventId: Long? = null
)
