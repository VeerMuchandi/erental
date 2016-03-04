package com.pinaka.eRental.serviceFacade;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jboss.resteasy.util.FindAnnotation;
import org.jboss.resteasy.spi.StringParameterUnmarshaller;

public class CalendarFormatter implements StringParameterUnmarshaller<Calendar>
{
   private SimpleDateFormat formatter;

   public void setAnnotations(Annotation[] annotations)
   {
      CalendarFormat format = FindAnnotation.findAnnotation(annotations, CalendarFormat.class);
      formatter = new SimpleDateFormat(format.value());
   }

   public Calendar fromString(String str)
   {
      try
      {
    	 Calendar cal = Calendar.getInstance();
    	 cal.setTime(formatter.parse(str));
         return cal;
      }
      catch (ParseException e)
      {
         throw new RuntimeException(e);
      }
   }
}

