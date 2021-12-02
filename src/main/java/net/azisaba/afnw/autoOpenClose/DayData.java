package net.azisaba.afnw.autoOpenClose;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

public record DayData(List<LocalTime> opens, List<LocalTime> closes) {
    public static final DayData EMPTY = new DayData(Collections.emptyList(), Collections.emptyList());
}
