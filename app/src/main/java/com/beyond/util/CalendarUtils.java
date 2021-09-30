package com.beyond.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;

import com.beyond.exception.CalendarErrorException;

import java.util.TimeZone;

import androidx.annotation.NonNull;

/**
 * @author: beyond
 * @date: 2021/9/28
 */

public abstract class CalendarUtils {

    private static String calenderURL = "content://com.android.calendar/calendars";
    private static String calenderEventURL = "content://com.android.calendar/events";
    private static String calenderReminderURL = "content://com.android.calendar/reminders";
    private final static String CALENDARS_ACCOUNT_TYPE = "com.android.exchange";

    public static void insertEventAndReminder(Context context, String title, String description, long startMs) {
        Uri uri = insertEvent(context, title, description, startMs);
        long eventID = Long.parseLong(uri.getLastPathSegment());
        insertEventReminder(context, eventID);
    }

    @NonNull
    public static Uri insertEvent(Context context, String title, String description, long startMs) {
        ContentResolver contentResolver = context.getContentResolver();
        Long calendarId = initAccount(context);
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startMs);
        values.put(CalendarContract.Events.DTEND, startMs + 15 * 60 * 1000);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
        Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri == null) {
            throw new CalendarErrorException("add event failed");
        }
        return uri;
    }

    @NonNull
    public static Uri insertEventReminder(Context context, long eventId) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues reminderValues = new ContentValues();
        reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId);
        reminderValues.put(CalendarContract.Reminders.MINUTES, 0);
        reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        Uri reminderUri = contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues);
        if (reminderUri == null) {
            throw new CalendarErrorException("add reminder failed");
        }
        return reminderUri;
    }


    //初始化账户
    private static long initAccount(Context context) {
        long accountId;
        if (checkAccount(context) < 0) {
            accountId = addAccount(context, "note_beyond", "note_beyond");
        } else {
            accountId = checkAccount(context);
        }
        return accountId;
    }


    //检查是否有账户
    private static long checkAccount(Context context) {
        try (Cursor userCursor = context.getContentResolver().query(Uri.parse(calenderURL), null, null, null, null)) {
            if (userCursor == null) {
                //没有账户
                return -1;
            } else {
                //有账户返回第一个账户ID
                int count = userCursor.getCount();
                if (count > 0) {
                    //有账户
                    while (userCursor.moveToNext()) {
                        int columnIndex = userCursor.getColumnIndex(CalendarContract.Calendars._ID);
                        if (columnIndex == -1) {
                            continue;
                        }
                        return userCursor.getInt(columnIndex);
                    }
                    return -1;
                } else {
                    //账户数为负数或0
                    return -1;
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static long addAccount(Context context, String name, String accountName) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, name);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);
        Uri calendarUri = Uri.parse(calenderURL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();
        Uri result = context.getContentResolver().insert(calendarUri, value);
        return result == null ? -1 : ContentUris.parseId(result);
    }


}
