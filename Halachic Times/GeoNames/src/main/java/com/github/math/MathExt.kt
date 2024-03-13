package com.github.math

// TODO move to :android-lib:kotlin

fun Double.toRadians(): Double = Math.toRadians(this)

fun Double.toDegrees(): Double = Math.toDegrees(this)

fun Float.toRadians(): Float = this.toDouble().toRadians().toFloat()

fun Float.toDegrees(): Float = this.toDouble().toDegrees().toFloat()

val Int.isEven get() = this.and(1) == 0

val Int.isOdd get() = this.and(1) == 1

fun sqr(a: Double): Double = a * a

operator fun IntArray.times(value: Int): IntArray {
    return IntArray(size) { i -> this[i] * value }
}

operator fun IntArray.times(value: Double): IntArray {
    return IntArray(size) { i -> (this[i] * value).toInt() }
}

operator fun IntArray.timesAssign(value: Int) {
    for (i in indices) {
        this[i] *= value
    }
}

operator fun IntArray.timesAssign(value: Double) {
    for (i in indices) {
        this[i] = (this[i] * value).toInt()
    }
}

fun IntArray.toDoubleArray(): DoubleArray {
    return DoubleArray(size) { i -> this[i].toDouble() }
}
