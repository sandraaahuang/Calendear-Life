package com.sandra.calendearlife

import com.google.firebase.Timestamp
import org.junit.Assert
import org.junit.Test

fun subtractDay(inputDay: Long, today: Long): Long {
    return (inputDay - today)/86400
}

class CountdownUnitTest {
    @Test
    fun subtract_countdown_day_isCorrect() {
        Assert.assertEquals(17, (subtractDay(1572418564, Timestamp.now().seconds)))
    }
    @Test
    fun subtract_countdown_day_isWrong() {
        Assert.assertNotEquals(0, (subtractDay(1572418564, Timestamp.now().seconds)))
    }
}