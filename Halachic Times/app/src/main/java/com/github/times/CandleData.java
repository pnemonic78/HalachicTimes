package com.github.times;

public class CandleData {
    public final int holidayToday;
    public final int countToday;
    public final int holidayTomorrow;
    public final int countTomorrow;
    public final int whenCandles;
    public final int candlesOffset;
    public final int omerToday;
    public final int omerTomorrow;

    public CandleData(
            int holidayToday,
            int countToday,
            int holidayTomorrow,
            int countTomorrow,
            int whenCandles,
            int candlesOffset,
            int omerToday,
            int omerTomorrow
    ) {
        this.holidayToday = holidayToday;
        this.countToday = countToday;
        this.holidayTomorrow = holidayTomorrow;
        this.countTomorrow = countTomorrow;
        this.whenCandles = whenCandles;
        this.candlesOffset = candlesOffset;
        this.omerToday = omerToday;
        this.omerTomorrow = omerTomorrow;
    }

    public CandleData() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }
}
