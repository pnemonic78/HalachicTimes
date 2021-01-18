package com.github.times;

public class CandleData {
    final int holidayToday;
    final int countToday;
    final int holidayTomorrow;
    final int countTomorrow;
    final int when;
    final int candlesOffset;

    public CandleData(int holidayToday, int countToday, int holidayTomorrow, int countTomorrow, int when, int candlesOffset) {
        this.holidayToday = holidayToday;
        this.countToday = countToday;
        this.holidayTomorrow = holidayTomorrow;
        this.countTomorrow = countTomorrow;
        this.when = when;
        this.candlesOffset = candlesOffset;
    }

    public CandleData() {
        this(0, 0, 0, 0, 0, 0);
    }
}
