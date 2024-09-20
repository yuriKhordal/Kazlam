package com.yurikh.kazlam;

import android.widget.Adapter;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Function;

public class Helper {
   public static final SimpleDateFormat DATETIME_FORMATTER =
      new SimpleDateFormat("yyyy/MM/dd HH:mm");
   public static final SimpleDateFormat DATE_FORMATTER =
      new SimpleDateFormat("yyyy/MM/dd");

   /**
    * Checks if a string is null or only whitespace characters.
    * @param str The string to check.
    */
   public static boolean StringEmpty(String str) {
      return str == null || str.trim().isEmpty();
   }

   /**
    * Search the index of an item by a specific key.
    * @param iterable The items to search over.
    * @param key The key value to search the item by.
    * @param keyFunc A function that returns a key from an item.
    * @return The index of the item with the specified key or -1 if none found.
    * @param <T> The type of the items in the iterable.
    * @param <TK> The type of the key of the items.
    */
   public static <T, TK> int searchByKey(Iterable<T> iterable, TK key,
   Function<T, TK> keyFunc) {
      int i = 0;
      for (T item : iterable) {
         if (keyFunc.apply(item).equals(key))
            return i;
         i++;
      }
      return -1;
   }

   /**
    * Search the index of an item by a specific key.
    * @param iterable The items to search over.
    * @param key The key value to search the item by.
    * @param keyFunc A function that returns a key from an item.
    * @param comparator A comparator that can compare keys.
    * @return The index of the item with the specified key or -1 if none found.
    * @param <T> The type of the items in the iterable.
    * @param <TK> The type of the key of the items.
    */
   public static <T, TK> int searchByKey(Iterable<T> iterable, TK key,
   Function<T, TK> keyFunc, Comparator<TK> comparator) {
      int i = 0;
      for (T item : iterable) {
         if (comparator.compare(keyFunc.apply(item), key) == 0)
            return i;
         i++;
      }
      return -1;
   }

   /**
    * Search the index of an item by a specific key.
    * @param adapter The items to search over.
    * @param key The key value to search the item by.
    * @param keyFunc A function that returns a key from an item.
    * @return The index of the item with the specified key or -1 if none found.
    * @param <TK> The type of the key of the items.
    */
   public static <T, TK> int searchByKey(Adapter adapter, TK key, Function<T, TK> keyFunc) {
      for (int i = 0; i < adapter.getCount(); i++) {
         T item = (T)adapter.getItem(i);
         if (keyFunc.apply(item).equals(key))
            return i;
      }
      return -1;
   }

   /**
    * Gets the current date(at the time of calling the function) as a string
    * of a 'yyyy/MM/DD HH:mm' format.
    */
   public static String getDateTimeString() {
      return DATETIME_FORMATTER.format(new Date());
   }

   /**
    * Gets the specified date as a string of a 'yyyy/MM/DD HH:mm' format.
    * @param date The date to format as a string.
    */
   public static String getDateTimeString(Date date) {
      return DATETIME_FORMATTER.format(date);
   }

   /**
    * Gets the specified date as a string of a 'yyyy/MM/DD' format.
    * @param date The date to format as a string.
    */
   public static String getDateString(Date date) {
      return DATE_FORMATTER.format(date);
   }

   /**
    * Gets the current date(at the time of calling the function) as a string
    *     * of a 'yyyy/MM/DD' format.
    */
   public static String getDateString() {
      return DATE_FORMATTER.format(new Date());
   }

   public static String getDateTimeString(int year, int month, int day, int hour, int minute) {
      return getDateString(year, month, day) + " " + getTimeString(hour, minute);
   }
   public static String getDateString(int year, int month, int day) {
      String syear = year + "";
      String smonth = (month < 10) ? ("0" + month) : ("" + month);
      String sday = (day < 10) ? ("0" + day) : ("" + day);

      return syear + "/" + smonth + "/" + sday;
   }

   public static String getTimeString(int hour, int minute) {
      String shour = (hour < 10) ? ("0" + hour) : ("" + hour);
      String sminute = (minute < 10) ? ("0" + minute) : ("" + minute);

      return shour + ":" + sminute;
   }

}
