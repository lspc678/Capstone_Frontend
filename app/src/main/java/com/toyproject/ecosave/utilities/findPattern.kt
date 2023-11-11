package com.toyproject.ecosave.utilities

fun findPattern(str: String, pattern: String) : Array<Int> {
    val str_len = str.length
    val pattern_len = pattern.length

    var i = 0
    var j = 0

    while (i < str_len) {
        if (str[i] == pattern[j]) {
            i += 1
            j += 1
        } else if (j > 0) {
            j = 0
        } else {
            i += 1
        }

        if (j == pattern_len) {
            return arrayOf(i - pattern_len, i)
        }
    }

    return arrayOf(-1, -1)
}