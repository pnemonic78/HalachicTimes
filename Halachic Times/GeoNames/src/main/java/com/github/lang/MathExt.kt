package com.github.lang

// TODO move to :android-lib:kotlin

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
