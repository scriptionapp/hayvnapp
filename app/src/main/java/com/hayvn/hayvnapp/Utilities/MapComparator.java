package com.hayvn.hayvnapp.Utilities;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class MapComparator implements Comparator<Map> {
    private final String key1;
    private final String key2;
    private final String time_key3;

    public MapComparator(String key1, String key2, String time_key3)
    {
        this.key1 = key1;
        this.key2 = key2;
        this.time_key3 = time_key3;
    }

    public int compare(Map first,
                       Map second)
    {
        String s1_1 = Objects.requireNonNull(first.get(key1)).toString();
        String s1_2 = Objects.requireNonNull(second.get(key1)).toString();
        if(s1_1.equals((s1_2))){
            s1_1 = Objects.requireNonNull(first.get(key2)).toString();
            s1_2 = Objects.requireNonNull(second.get(key2)).toString();
            if(s1_1.equals((s1_2))) {
                s1_1 = Objects.requireNonNull(first.get(time_key3)).toString();
                s1_2 = Objects.requireNonNull(second.get(time_key3)).toString();
            }
        }
        return s1_1.compareTo(s1_2); //-1 if s1 precedes
    }
}
