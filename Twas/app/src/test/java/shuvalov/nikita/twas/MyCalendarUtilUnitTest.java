package shuvalov.nikita.twas;

import org.junit.Test;

import java.util.Calendar;

import shuvalov.nikita.twas.Helpers_Managers.MyCalendarUtils;

import static org.junit.Assert.*;

/**
 * Created by NikitaShuvalov on 1/8/17.
 */

public class MyCalendarUtilUnitTest {
    @Test
    public void testGetAgeAsString(){
        Calendar calendar = Calendar.getInstance();

        calendar.set(2017,1,1);
        long newYearDay = calendar.getTimeInMillis();

        calendar.set(1989,4,24);
        long myBirthday = calendar.getTimeInMillis();//My age 27

        calendar.set(1970,1,2);
        long fortySixYearsOld = calendar.getTimeInMillis();

        calendar.set(1920,1,1);
        long ninetySevenNewYearsBaby = calendar.getTimeInMillis();


        assertEquals("27 years old",MyCalendarUtils.getAgeAsString(newYearDay,myBirthday));
        assertEquals("46 years old", MyCalendarUtils.getAgeAsString(newYearDay,fortySixYearsOld));
        assertEquals("97 years old", MyCalendarUtils.getAgeAsString(newYearDay,ninetySevenNewYearsBaby));
    }

    @Test
    public void testCheckLeapYear(){
        assertEquals(false,MyCalendarUtils.checkLeapYear(1989));
        assertEquals(true, MyCalendarUtils.checkLeapYear(2000));
        assertEquals(false, MyCalendarUtils.checkLeapYear(1900));
        assertEquals(true, MyCalendarUtils.checkLeapYear(1600));
        assertEquals(true,MyCalendarUtils.checkLeapYear(1980));
    }
}
