package com.blueapron.connect.protobuf;

import java.util.Calendar;
import java.util.TimeZone;

public class ProtobufUtils {
  static java.util.Date convertFromGoogleDate(com.google.type.Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setLenient(false);
    cal.set(Calendar.YEAR, date.getYear());
    // Months start at 0, not 1
    cal.set(Calendar.MONTH, date.getMonth() - 1);
    cal.set(Calendar.DAY_OF_MONTH, date.getDay());
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }

  static com.google.type.Date convertToGoogleDate(java.util.Date date) {
    TimeZone timeZone = TimeZone.getTimeZone("UTC");
    Calendar cal = Calendar.getInstance(timeZone);
    cal.setTime(date);
    com.google.type.Date.Builder dateBuilder = com.google.type.Date.newBuilder();
    dateBuilder.setDay(cal.get(Calendar.DAY_OF_MONTH));
    // Months start at 0, not 1
    dateBuilder.setMonth(cal.get(Calendar.MONTH) + 1);
    dateBuilder.setYear(cal.get(Calendar.YEAR));
    return dateBuilder.build();
  }
}
