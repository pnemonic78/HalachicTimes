package com.github.lang

operator fun IntArray.times(value: Int): IntArray {
    return map { it * value }.toIntArray()
}

operator fun IntArray.times(value: Double): IntArray {
    return map { (it * value).toInt() }.toIntArray()
}
