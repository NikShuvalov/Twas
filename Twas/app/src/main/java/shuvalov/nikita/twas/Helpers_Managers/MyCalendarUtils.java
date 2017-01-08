package shuvalov.nikita.twas.Helpers_Managers;

import java.util.Calendar;

/**
 * Created by NikitaShuvalov on 1/8/17.
 */

public class MyCalendarUtils {

    public static String getAgeAsString(long current, long bday){
        Calendar calendar = Calendar.getInstance();
        long diff = Math.abs(current - bday);
        calendar.setTimeInMillis(diff);
        long age = calendar.get(Calendar.YEAR)-1970;
        return (age + " years old");
    }

    public static boolean checkLeapYear(int year){
        if(year%4!=0){
            return false;
        }else if (year%100==0 && year%400!=0){
            return false;
        }
        return true;
    }
}
