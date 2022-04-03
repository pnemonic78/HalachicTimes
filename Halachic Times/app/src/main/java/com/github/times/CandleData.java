package com.github.times;

public class CandleData {
    public final int holidayToday;
    public final int countToday;
    public final int holidayTomorrow;
    public final int countTomorrow;
    public final int when;
    public final int candlesOffset;

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
