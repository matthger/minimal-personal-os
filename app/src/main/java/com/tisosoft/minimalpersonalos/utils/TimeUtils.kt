package com.tisosoft.minimalpersonalos.utils

import android.content.Context
import android.text.format.DateFormat
import java.util.Date

fun getCurrentTimeString(context: Context): String {
    return DateFormat.getTimeFormat(context).format(Date())
}
