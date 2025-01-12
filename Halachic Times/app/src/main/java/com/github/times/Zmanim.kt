package com.github.times

import com.kosherjava.zmanim.AstronomicalCalendar
import com.kosherjava.zmanim.hebrewcalendar.JewishDate
import java.util.Calendar
import java.util.Date
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

typealias TimeMillis = Long
typealias KosherDate = Date?

val NEVER: KosherDate = KosherDate(ZmanimItem.NEVER)

operator fun Date?.compareTo(other: TimeMillis?): Int {
    if (this == null) {
        if (other.isNever()) return 0
        return -1
    }
    if (other.isNever()) return 1
    return time.compareTo(other)
}

operator fun Date?.plus(rhs: TimeMillis?): Date? {
    if (this == null) return NEVER
    if (rhs.isNever()) return this
    return Date(time + rhs)
}

operator fun Date?.plus(rhs: Date?): Date? {
    if (this == null) return NEVER
    if (rhs == null) return this
    return plus(rhs)
}

operator fun Date?.minus(rhs: TimeMillis?): Date? {
    if (this == null) return NEVER
    if (rhs.isNever()) return this
    return Date(time - rhs)
}

operator fun Date?.minus(rhs: Date?): TimeMillis {
    if (this == null) return ZmanimItem.NEVER
    if (rhs.isNever()) return ZmanimItem.NEVER
    return time - rhs.time
}

operator fun Date?.times(rhs: TimeMillis?): Date? {
    if (this == null) return NEVER
    if (rhs.isNever()) return NEVER
    return Date(time * rhs)
}

operator fun Date?.div(rhs: TimeMillis): Date? {
    if (this == null) return NEVER
    if (rhs.isNever()) return NEVER
    return Date(time / rhs)
}

@OptIn(ExperimentalContracts::class)
fun KosherDate.isDate(): Boolean {
    contract {
        returns(true) implies (this@isDate != null)
    }
    return this != null && time != ZmanimItem.NEVER
}

@OptIn(ExperimentalContracts::class)
fun KosherDate.isNever(): Boolean {
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

fun <C : AstronomicalCalendar> C.add(field: Int, amount: Int): C {
    calendar.add(field, amount)
    return this
}
