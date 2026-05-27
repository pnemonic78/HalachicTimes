package com.github.times

import com.github.util.dayOfMonth
import com.github.util.month
import com.github.util.year
import com.kosherjava.zmanim.AstronomicalCalendar
import com.kosherjava.zmanim.ComprehensiveZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

typealias TimeMillis = Long
typealias KosherDateTime = Instant?
typealias KosherDate = LocalDate

val NEVER: KosherDateTime = KosherDateTime.ofEpochMilli(ZmanimItem.NEVER)

operator fun KosherDateTime.compareTo(other: TimeMillis?): Int {
    if (this == null) {
        if (other.isNever()) return 0
        return -1
    }
    if (other.isNever()) return 1
    return time.compareTo(other)
}

operator fun KosherDateTime.plus(rhs: TimeMillis?): KosherDateTime {
    if (this == null) return NEVER
    if (rhs.isNever()) return this
    return KosherDateTime.ofEpochMilli(time + rhs)
}

operator fun KosherDateTime.plus(rhs: KosherDateTime): KosherDateTime {
    if (this == null) return NEVER
    if (rhs == null) return this
    return plus(rhs)
}

operator fun KosherDateTime.minus(rhs: TimeMillis?): KosherDateTime {
    if (this == null) return NEVER
    if (rhs.isNever()) return this
    return plus(-rhs)
}

operator fun KosherDateTime.minus(rhs: KosherDateTime): TimeMillis {
    if (this == null) return ZmanimItem.NEVER
    if (rhs.isNever()) return ZmanimItem.NEVER
    return time - rhs.time
}

operator fun KosherDateTime.times(rhs: TimeMillis?): KosherDateTime {
    if (this == null) return NEVER
    if (rhs.isNever()) return NEVER
    return KosherDateTime.ofEpochMilli(time * rhs)
}

operator fun KosherDateTime.div(rhs: TimeMillis): KosherDateTime {
    if (this == null) return NEVER
    if (rhs.isNever()) return NEVER
    return KosherDateTime.ofEpochMilli(time / rhs)
}

@OptIn(ExperimentalContracts::class)
fun KosherDateTime.isDate(): Boolean {
    contract {
        returns(true) implies (this@isDate != null)
    }
    return this != null && time != ZmanimItem.NEVER
}

@OptIn(ExperimentalContracts::class)
fun KosherDateTime.isNever(): Boolean {
    contract {
        returns(false) implies (this@isNever != null)
    }
    return this == null || time == ZmanimItem.NEVER
}

fun TimeMillis.isNever(): Boolean {
    return this == ZmanimItem.NEVER
}

@OptIn(ExperimentalContracts::class)
fun TimeMillis?.isNever(): Boolean {
    contract {
        returns(false) implies (this@isNever != null)
    }
    return this == null || this == ZmanimItem.NEVER
}

val TimeMillis.isTime: Boolean
    get() = this != ZmanimItem.NEVER

val TimeMillis?.isTime: Boolean
    get() = (this != null) && this.isTime

@Suppress("UNCHECKED_CAST")
fun <C : Calendar> C.copy(): C = clone() as C

@Suppress("UNCHECKED_CAST")
fun <C : AstronomicalCalendar> C.copy(): C = clone() as C

@Suppress("UNCHECKED_CAST")
fun <D : JewishDate> D.copy(): D = clone() as D

// Because `LocalDate` is immutable, you can simply reference the same object.
fun KosherDate.copy(): KosherDate = this

// Because `LocalDateTime` is immutable, you can simply reference the same object.
fun <D : KosherDateTime> D.copy(): D = this

fun <C : AstronomicalCalendar> C.add(field: Int, amount: Long): C {
    val date = localDate
    localDate = when (field) {
        Calendar.DATE -> date.plusDays(amount)
        Calendar.MONTH -> date.plusMonths(amount)
        Calendar.WEEK_OF_YEAR -> date.plusWeeks(amount)
        Calendar.YEAR -> date.plusYears(amount)
        else -> date
    }
    return this
}

fun KosherDate.toCalendar(timeZone: TimeZone): Calendar {
    val localDate: KosherDate = this
    return Calendar.getInstance(timeZone).apply {
        year = localDate.year
        month = localDate.monthValue - 1
        dayOfMonth = localDate.dayOfMonth
    }
}

fun Calendar.toKosherDate(): KosherDate {
    return KosherDate.ofInstant(toInstant(), timeZone.toZoneId())
}

fun Calendar.toKosherDateTime(): KosherDateTime {
    return this.toInstant()
}

fun KosherDateTime.toKosherDate(): LocalDate {
    return LocalDate.ofInstant(this, ZoneId.systemDefault())
}

fun KosherDate.toKosherDateTime(zone: ZoneId = ZoneOffset.systemDefault()): KosherDateTime {
    return atStartOfDay(zone).toInstant()
}

var AstronomicalCalendar.calendar: Calendar
    get() = localDate.toCalendar(TimeZone.getDefault())
    set(value) {
        localDate = value.toKosherDate()
    }

val KosherDateTime.time: TimeMillis get() = this!!.toEpochMilli()

val ComprehensiveZmanimCalendar.shaahZmanisMGA: Long get() = shaahZmanis72Minutes

fun Calendar.assign(time: KosherDateTime) {
    if (time == null) {
        timeInMillis = ZmanimItem.NEVER
        return
    }
    timeInMillis = time.toEpochMilli()
}

fun JewishDate.assign(time: KosherDateTime) {
    setGregorianDate(time.toKosherDate())
}

fun AstronomicalCalendar.assign(time: KosherDateTime) {
    localDate = time.toKosherDate()
}

var JewishDate.date: KosherDateTime
    get() = localDate.toKosherDateTime()
    set(value) {
        setGregorianDate(value.toKosherDate())
    }

fun isSameDay(expected: Calendar, actual: LocalDate): Boolean {
    val y1 = expected.year
    val m1 = expected.month
    val d1 = expected.dayOfMonth

    val y2 = actual.year
    val m2 = actual.monthValue - 1
    val d2 = actual.dayOfMonth

    return (y1 == y2) && (m1 == m2) && (d1 == d2)
}

fun isSameDay(expected: LocalDate, actual: Calendar): Boolean {
    return isSameDay(actual, expected)
}

fun isSameDay(expected: Calendar, actual: Instant): Boolean {
    return isSameDay(expected, actual.toKosherDate())
}

fun isSameDay(expected: LocalDate, actual: Instant): Boolean {
    return expected == actual.toKosherDate()
}

fun DateTimeFormatter.withZone(zone: TimeZone): DateTimeFormatter {
    return withZone(zone.toZoneId())
}

fun DateTimeFormatter.format(time: TimeMillis): String {
    return format(Instant.ofEpochMilli(time))
}

fun TimeMillis.toKosherDate(zone: TimeZone = TimeZone.getDefault()): KosherDate {
    return toKosherDate(zone.toZoneId())
}

fun TimeMillis.toKosherDate(zone: ZoneId = ZoneId.systemDefault()): KosherDate {
    val epochMilli: Long = this
    return KosherDate.ofInstant(Instant.ofEpochMilli(epochMilli), zone)
}
