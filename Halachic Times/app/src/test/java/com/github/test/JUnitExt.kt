package com.github.test

import junit.framework.TestCase.failNotEquals
import kotlin.math.abs

fun assertDelta(expected: Long, actual: Long, delta: Long) {
    assertDelta(null, expected, actual, delta)
}

fun assertDelta(message: String?, expected: Long, actual: Long, delta: Long) {
    if (longIsDifferent(expected, actual, delta)) {
        failNotEquals(message, expected, actual)
    }
}

private fun longIsDifferent(n1: Long, n2: Long, delta: Long): Boolean {
    if (n1 == n2) {
        return false
    }
    if ((abs(n1 - n2) <= delta)) {
        return false
    }
    return true
}

fun assertDelta(expected: Int, actual: Int, delta: Int) {
    assertDelta(null, expected, actual, delta)
}

fun assertDelta(message: String?, expected: Int, actual: Int, delta: Int) {
    if (intIsDifferent(expected, actual, delta)) {
        failNotEquals(message, expected, actual)
    }
}

private fun intIsDifferent(n1: Int, n2: Int, delta: Int): Boolean {
    if (n1 == n2) {
        return false
    }
    if ((abs(n1 - n2) <= delta)) {
        return false
    }
    return true
}
