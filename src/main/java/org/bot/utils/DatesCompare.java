package org.bot.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DatesCompare {
    public static boolean beforeOrSameMonth(Date actual, Date border)
    {
        LocalDate currentDate = actual.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int actualMonth = currentDate.getMonth().getValue();
        int actualYear = currentDate.getYear();

        LocalDate borderDate = border.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int borderMonth = borderDate.getMonth().getValue();
        int borderYear = borderDate.getYear();

        if(actualYear < borderYear)
            return true;
        if((actualYear == borderYear) && (actualMonth <=borderMonth))
            return true;
        return false;
    }

    public static boolean afterOrSameMonth(Date actual, Date border)
    {
        LocalDate currentDate = actual.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int actualMonth = currentDate.getMonth().getValue();
        int actualYear = currentDate.getYear();

        LocalDate borderDate = border.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int borderMonth = borderDate.getMonth().getValue();
        int borderYear = borderDate.getYear();

        if(actualYear > borderYear)
            return true;
        if((actualYear == borderYear) && (actualMonth >= borderMonth))
            return true;
        return false;
    }

    public static boolean beforeOrSameDay(Date actual, Date border)
    {
        LocalDate currentDate = actual.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int actualDay = currentDate.getDayOfYear();
        int actualYear = currentDate.getYear();

        LocalDate borderDate = border.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int borderDay = borderDate.getDayOfYear();
        int borderYear = borderDate.getYear();

        if(actualYear < borderYear)
            return true;
        if((actualYear == borderYear) && (actualDay <=borderDay))
            return true;
        return false;
    }
}
