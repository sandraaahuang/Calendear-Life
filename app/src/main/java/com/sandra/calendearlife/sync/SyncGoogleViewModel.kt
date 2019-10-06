package com.sandra.calendearlife.sync

import android.content.ContentUris
import android.database.Cursor
import android.provider.CalendarContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import java.util.*

class SyncGoogleViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()

    private val _writeCompleted = MutableLiveData<Boolean>()
    val writeCompleted: LiveData<Boolean>
        get() = _writeCompleted

    fun queryCalendar() {

        // search calendar
        val cur: Cursor? = GoogleCalendarProvider.contentResolver.query(
            GoogleCalendarProvider.contentUri,
            GoogleCalendarProvider.eventProjection,
            GoogleCalendarProvider.SELECTION,
            GoogleCalendarProvider.selectionArgs, null)
        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        if (cur != null) {
            while (cur.moveToNext()) {
                val calendarId = cur.getString(GoogleCalendarProvider.PROJECTION_ID_INDEX)
                val accountName = cur.getString(GoogleCalendarProvider.PROJECTION_ACCOUNT_NAME_INDEX)
                val displayName = cur.getString(GoogleCalendarProvider.PROJECTION_DISPLAY_NAME_INDEX)
                val ownerAccount = cur.getString(GoogleCalendarProvider.PROJECTION_OWNER_ACCOUNT_INDEX)
                val accessLevel = cur.getInt(GoogleCalendarProvider.PROJECTION_CALENDAR_ACCESS_LEVEL)

                Logger.i(String.format("calendarId=%s", calendarId))
                Logger.i(String.format("accountName=%s", accountName))
                Logger.i(String.format("displayName=%s", displayName))
                Logger.i(String.format("ownerAccount=%s", ownerAccount))
                Logger.i(String.format("accessLevel=%s", accessLevel))
                // store calendar data
                accountNameList.add(displayName)
                calendarIdList.add(calendarId)

                val beginTime = Calendar.getInstance()
                beginTime.set(2019, 8, 1, 24, 0)
                val startMillis = beginTime.timeInMillis
                val endTime = Calendar.getInstance()
                endTime.set(2029, 8, 1, 24, 0)
                val endMillis = endTime.timeInMillis

                // search event
                val cur2: Cursor?
                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()

                val selectionEvent = CalendarContract.Events.CALENDAR_ID + " = ?"
                val selectionEventArgs = arrayOf(calendarId)
                ContentUris.appendId(builder, startMillis)
                ContentUris.appendId(builder, endMillis)

                val eventIdList = ArrayList<String>()
                val beginList = ArrayList<Long>()
                val endList = ArrayList<Long>()
                val titleList = ArrayList<String>()
                val noteList = ArrayList<String>()

                cur2 = GoogleCalendarProvider.contentResolver.query(
                    builder.build(),
                    GoogleCalendarProvider.instanceProjection,
                    selectionEvent,
                    selectionEventArgs, null
                )
                if (cur2 != null) {
                    while (cur2.moveToNext()) {
                        val eventID = cur2.getString(GoogleCalendarProvider.PROJECTION_ID_INDEX)
                        val beginVal = cur2.getLong(GoogleCalendarProvider.PROJECTION_BEGIN_INDEX)
                        val endVal = cur2.getLong(GoogleCalendarProvider.PROJECTION_END_INDEX)
                        val title = cur2.getString(GoogleCalendarProvider.PROJECTION_TITLE_INDEX)
                        val note = cur2.getString(GoogleCalendarProvider.PROJECTION_DESCRIPTION_INDEX)
                        // 取得所需的資料
                        Logger.i(String.format("eventID=%s", eventID))
                        Logger.i(String.format("beginVal=%s", beginVal))
                        Logger.i(String.format("endVal=%s", endVal))
                        Logger.i(String.format("title=%s", title))
                        Logger.i(String.format("note=%s", note))
                        // 暫存資料讓使用者選擇
                        val beginDate = Timestamp(beginVal / 1000, 0)
                        val endDate = Timestamp(endVal / 1000, 0)
                        eventIdList.add(eventID)
                        beginList.add(beginVal)
                        endList.add(endVal)
                        titleList.add(title)
                        noteList.add(note)

                        val item = hashMapOf(
                            FirebaseKey.DATE to Timestamp(
                                timeFormat2SqlTimestamp(
                                    SIMPLE_DATE_FORMAT,
                                    transferTimestamp2String(SIMPLE_DATE_FORMAT, beginDate)
                                )
                            ),
                            FirebaseKey.SET_DATE to FieldValue.serverTimestamp(),
                            FirebaseKey.BEGIN_DATE to beginDate,
                            FirebaseKey.END_DATE to endDate,
                            FirebaseKey.TITLE to title,
                            FirebaseKey.NOTE to note,
                            FirebaseKey.FROM_GOOGLE to true,
                            FirebaseKey.COLOR to FirebaseKey.COLOR_GOOGLE,
                            FirebaseKey.DOCUMENT_ID to eventID,
                            FirebaseKey.HAS_COUNTDOWN to false,
                            FirebaseKey.HAS_REMINDERS to false,
                            FirebaseKey.REMINDERS_DATE to beginDate,
                            FirebaseKey.LOCATION to ""
                        )
                        writeGoogleItem(item, eventID)
                    }
                    cur2.close()
                }

            }
            cur.close()
        }

    }

    private fun writeGoogleItem(item: Any, documentId: String) {

        // get all data from user at first
        db.collection(FirebaseKey.DATA)
            .document(UserManager.id!!)
            .collection(FirebaseKey.CALENDAR)
            .document(documentId)
            .set(item)
            .addOnSuccessListener {
                Logger.d("DocumentSnapshot added with ID = $documentId")
            }
            .addOnCompleteListener {
                _writeCompleted.value = true
            }
    }


}