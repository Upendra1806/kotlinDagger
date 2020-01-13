package com.juliusbaer.premarket.utils

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

object Util {
    fun monthBetweenToTimeStamps(startTimeStampSec: Long, endTimeStampSec: Long): Long {
        val zoneId = ZoneOffset.UTC
        val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeStampSec), zoneId)
        val endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTimeStampSec), zoneId)
        return ChronoUnit.MONTHS.between(startDateTime, endDateTime)
    }

    fun daysBetweenToTimeStamps(startTimeStamp: Long, endTimeStamp: Long): Long {
        val startDateTime = Instant.ofEpochMilli(startTimeStamp)
        val endDateTime = Instant.ofEpochMilli(endTimeStamp)
        return ChronoUnit.DAYS.between(startDateTime, endDateTime)
    }

    fun nearestInteger(value: Double): Int {
        return when{
            value > 0 -> ceil(value)
            value < 0 -> floor(value)
            else -> value
        }.toInt()
    }
}

