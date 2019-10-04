package com.sandra.calendearlife.constant

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.UserManager

class GoogleCalendarProvider {
    companion object {
        val contentResolver: ContentResolver = MyApplication.instance.contentResolver
        val contentValues = ContentValues()
        val contentUri: Uri = CalendarContract.Calendars.CONTENT_URI

        //check permission
        val permissionReadCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance,
            Manifest.permission.READ_CALENDAR
        )
        val permissionWriteCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, Manifest.permission.WRITE_CALENDAR
        )

        const val SELECTION = (FirebaseKey.PARENTHESES + CalendarContract.Calendars.ACCOUNT_NAME + FirebaseKey.CONJUNCTION
                + CalendarContract.Calendars.ACCOUNT_TYPE + FirebaseKey.CONJUNCTION
                + CalendarContract.Calendars.OWNER_ACCOUNT + FirebaseKey.QUESTION_MARK)
        val selectionArgs =
            arrayOf(UserManager.userEmail, FirebaseKey.MAIL_FORMAT, UserManager.userEmail)

        val eventProjection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        // event data
        val instanceProjection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION
        )

        const val PROJECTION_ID_INDEX = 0
        const val PROJECTION_ACCOUNT_NAME_INDEX = 1
        const val PROJECTION_DISPLAY_NAME_INDEX = 2
        const val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        const val PROJECTION_CALENDAR_ACCESS_LEVEL = 4

        const val PROJECTION_BEGIN_INDEX = 1
        const val PROJECTION_END_INDEX = 2
        const val PROJECTION_TITLE_INDEX = 3
        const val PROJECTION_DESCRIPTION_INDEX = 4
    }
}