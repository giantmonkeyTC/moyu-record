package cn.troph.tomon.ui.utils;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import cn.troph.tomon.R;

public final class LocalDateUtils {
    private LocalDateUtils(){}
    private static final String ERROR_TAG = "error_tag";

    public static String timestampConverterDetailed(Context context, LocalDateTime timestamp) {
        LocalTime timeLocal = timestamp.toLocalTime();
        LocalDate dateLocal = timestamp.toLocalDate();
        String time_stamp_head;

        if (dateLocal.getMonth() == LocalDateTime.now().getMonth()
                &&
                dateLocal.getYear() == LocalDateTime.now().getYear()) {
            int deltaDayOfMonth = LocalDateTime.now().getDayOfMonth() - dateLocal.getDayOfMonth();
            if (deltaDayOfMonth == 0) {
                time_stamp_head = context.getString(R.string.today);
            } else if (deltaDayOfMonth == 1) {
                time_stamp_head = context.getString(R.string.yesterday);
            } else if (deltaDayOfMonth <= 6) {
                time_stamp_head = context.getString(R.string.in_a_week, deltaDayOfMonth);
            } else if (deltaDayOfMonth < 0) {
                time_stamp_head = ERROR_TAG;
            } else {
                time_stamp_head = context.getString(R.string.in_a_year_detailed,
                        dateLocal.getMonth().getValue(),
                        dateLocal.getDayOfMonth());
            }
        } else if (dateLocal.getYear() == LocalDateTime.now().getYear()) {
            if (LocalDateTime.now().getMonthValue() < dateLocal.getMonthValue()) {
                time_stamp_head = ERROR_TAG;
            } else {
                time_stamp_head = context.getString(R.string.in_a_year_detailed,
                        dateLocal.getMonthValue(),
                        dateLocal.getDayOfMonth());
            }

        } else if (dateLocal.getYear() <  LocalDateTime.now().getYear()) {
            time_stamp_head = context.getString(R.string.out_of_a_year_detailed,
                    dateLocal.getYear(),
                    dateLocal.getMonthValue(),
                    dateLocal.getDayOfMonth());
        } else {
            time_stamp_head = ERROR_TAG;
        }

        if (ERROR_TAG.equals(time_stamp_head)) {
            time_stamp_head = context.getString(R.string.in_the_future_detailed,
                    dateLocal.getYear(),
                    dateLocal.getMonthValue(),
                    dateLocal.getDayOfMonth());
        }

        String hour;
        String minute;
        int hourRaw = timeLocal.getHour();
        int minuteRaw = timeLocal.getMinute();

        if (hourRaw < 10) {
            hour = "0"+hourRaw;
        } else {
            hour = String.valueOf(hourRaw);
        }

        if (minuteRaw < 10) {
            minute = "0" + minuteRaw;
        } else {
            minute = String.valueOf(minuteRaw);
        }

        return context.getString(R.string.time_stamp_detailed,
                time_stamp_head,
                hour,
                minute);
    }

    public static String timestampConverterSimple(Context context, LocalDateTime timestamp) {
        boolean showTime = false;
        LocalTime timeLocal = timestamp.toLocalTime();
        LocalDate dateLocal = timestamp.toLocalDate();
        String time_stamp_head;

        if (dateLocal.getMonth() == LocalDateTime.now().getMonth()
                &&
                dateLocal.getYear() == LocalDateTime.now().getYear()) {
            int deltaDayOfMonth = LocalDateTime.now().getDayOfMonth() - dateLocal.getDayOfMonth();
            if (deltaDayOfMonth == 0) {
                time_stamp_head = context.getString(R.string.today);
                showTime = true;
            } else if (deltaDayOfMonth == 1) {
                time_stamp_head = context.getString(R.string.yesterday);
            } else if (deltaDayOfMonth <= 6) {
                time_stamp_head = context.getString(R.string.in_a_week, deltaDayOfMonth);
            } else if (deltaDayOfMonth < 0) {
                time_stamp_head = ERROR_TAG;
            } else {
                time_stamp_head = context.getString(R.string.in_a_year_simple,
                        dateLocal.getMonth().getValue(),
                        dateLocal.getDayOfMonth());
            }
        } else if (dateLocal.getYear() == LocalDateTime.now().getYear()) {
            if (LocalDateTime.now().getMonthValue() < dateLocal.getMonthValue()) {
                time_stamp_head = ERROR_TAG;
            } else {
                time_stamp_head = context.getString(R.string.in_a_year_simple,
                        dateLocal.getMonthValue(),
                        dateLocal.getDayOfMonth());
            }

        } else if (dateLocal.getYear() <  LocalDateTime.now().getYear()) {
            time_stamp_head = context.getString(R.string.out_of_a_year_simple,
                    dateLocal.getYear(),
                    dateLocal.getMonthValue(),
                    dateLocal.getDayOfMonth());
        } else {
            time_stamp_head = ERROR_TAG;
        }

        if (ERROR_TAG.equals(time_stamp_head)) {
            time_stamp_head = context.getString(R.string.in_the_future_simple,
                    dateLocal.getYear(),
                    dateLocal.getMonthValue(),
                    dateLocal.getDayOfMonth());
        }

        String hour;
        String minute;
        int hourRaw = timeLocal.getHour();
        int minuteRaw = timeLocal.getMinute();

        if (hourRaw < 10) {
            hour = "0"+hourRaw;
        } else {
            hour = String.valueOf(hourRaw);
        }

        if (minuteRaw < 10) {
            minute = "0" + minuteRaw;
        } else {
            minute = String.valueOf(minuteRaw);
        }

        if (showTime) {
            return context.getString(R.string.time_stamp_show_time_simple,
                    hour, minute);
        } else {
            return time_stamp_head;
        }
    }
}
