package com.juliusbaer.premarket.helpers.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class HourAxisValueFormatter : ValueFormatter() {
    companion object {
        private val systemZoneId = ZoneId.systemDefault()
    }

    private val formatter = DateTimeFormatter.ofPattern("MMM,dd", Locale.ENGLISH)

    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond((value).toLong()), systemZoneId).format(formatter)
    }
}