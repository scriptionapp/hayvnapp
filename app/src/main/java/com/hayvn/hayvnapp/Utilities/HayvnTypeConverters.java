package com.hayvn.hayvnapp.Utilities;

import android.util.Log;

import androidx.room.TypeConverter;

import com.google.firebase.Timestamp;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeFormatter;

/* Built based on:
    android docs: https://developer.android.com/training/data-storage/room/referencing-data#java
    room and type converter tutorial: https://medium.com/androiddevelopers/room-time-2b4cf9672b98
 */
public class HayvnTypeConverters {

    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @TypeConverter
    public static OffsetDateTime fromISO8601String(String input) {
        if (input == null) {
            return null;
        }
        return formatter.parse(input, OffsetDateTime::from);
    }

    @TypeConverter
    public static String dateToString(OffsetDateTime input) {
        if (input == null) {
            return null;
        }
        return input.format(formatter);
    }

    @TypeConverter
    public static Long timeStampToLong(Timestamp t) {
        return t.toDate().getTime();
    }
//
}
