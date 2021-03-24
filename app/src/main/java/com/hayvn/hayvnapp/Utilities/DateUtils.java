package com.hayvn.hayvnapp.Utilities;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    public static Date stringToDate(String long_time){
        DateTimeFormatter sdfISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate odt1 = org.threeten.bp.LocalDate.parse(long_time, sdfISO);
        Date latestDate = DateTimeUtils.toDate(
                odt1.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        return latestDate;
    }

    public static String dateToString(Date date){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        return sdf1.format(date);
    }

    public static String dateToStringTime(Date date){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf1.format(date);
    }

    public static int hoursToNow(Long dt){
        Long now = new Date().getTime();
        int hours = (int) ((now - dt)/(1000*60*60));
        return hours;
    }

}
