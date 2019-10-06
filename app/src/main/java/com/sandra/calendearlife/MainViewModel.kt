package com.sandra.calendearlife

import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentResolver
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import java.util.*

class MainViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val userEmail = UserManager.userEmail
    val userPhoto = UserManager.userPhoto

    lateinit var remindAdd: Reminders
    private val _liveReminders = MutableLiveData<Reminders>()
    val liveReminders: LiveData<Reminders>
        get() = _liveReminders

    private val set4AlarmManagerReminderItem = ArrayList<Reminders>()
    private val _liveSet4AlarmManagerReminderItem = MutableLiveData<List<Reminders>>()
    val liveSet4AlarmManagerReminderItem: LiveData<List<Reminders>>
        get() = _liveSet4AlarmManagerReminderItem

    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermission: LiveData<Boolean>
        get() = _hasPermission

    fun getRemindersItem4Widget(documentId: String) {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only ischecked is false )
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENT_ID, documentId)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    setupRemindersItem(reminder)

                                    _liveReminders.value = remindAdd
                                }
                            }
                    }
                }
        }

    }

    fun set4AlarmManagerReminderItem() {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only is_checked is false )
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(IS_CHECKED, false)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    setupRemindersItem(reminder)

                                    set4AlarmManagerReminderItem.add(remindAdd)
                                }
                                _liveSet4AlarmManagerReminderItem.value = set4AlarmManagerReminderItem
                            }
                    }
                }
        }

    }

    fun queryGoogleCalendar() {

        // search calendar
        val cur: Cursor?

        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        // give permission to read
        if (GoogleCalendarProvider.permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
            cur = contentResolver.query(
                GoogleCalendarProvider.contentUri,
                GoogleCalendarProvider.eventProjection,
                GoogleCalendarProvider.SELECTION,
                GoogleCalendarProvider.selectionArgs, null)
            if (cur != null) {
                while (cur.moveToNext()) {
                    val calendarId: String = cur.getString(GoogleCalendarProvider.PROJECTION_ID_INDEX)
                    val accountName: String = cur.getString(GoogleCalendarProvider.PROJECTION_ACCOUNT_NAME_INDEX)
                    val displayName: String = cur.getString(GoogleCalendarProvider.PROJECTION_DISPLAY_NAME_INDEX)
                    val ownerAccount: String = cur.getString(GoogleCalendarProvider.PROJECTION_OWNER_ACCOUNT_INDEX)
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


                    if (GoogleCalendarProvider.permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
                        cur2 = contentResolver.query(
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
                                        timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT,
                                            transferTimestamp2String(SIMPLE_DATE_FORMAT, beginDate))
                                    ),
                                    SET_DATE to FieldValue.serverTimestamp(),
                                    FirebaseKey.BEGIN_DATE to beginDate,
                                    FirebaseKey.END_DATE to endDate,
                                    TITLE to title,
                                    NOTE to note,
                                    FirebaseKey.FROM_GOOGLE to true,
                                    FirebaseKey.COLOR to FirebaseKey.COLOR_GOOGLE,
                                    DOCUMENT_ID to eventID,
                                    FirebaseKey.HAS_COUNTDOWN to false,
                                    FirebaseKey.HAS_REMINDERS to false,
                                    FirebaseKey.REMINDERS_DATE to beginDate,
                                    LOCATION to ""
                                )
                                writeGoogleItem(item, eventID)
                            }
                            cur2.close()
                        }
                    }
                }
                cur.close()
            }
        } else {
            _hasPermission.value = true
        }
    }

    private fun writeGoogleItem(item: Any, documentId: String) {

        // get all data from user at first
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .document(documentId)
                .set(item)
        }

    }

    private fun setupRemindersItem(reminder: QueryDocumentSnapshot) {
        remindAdd = Reminders(
            transferTimestamp2String(SIMPLE_DATE_FORMAT,
                reminder.data[SET_DATE] as Timestamp),
            reminder.data[TITLE].toString(),
            reminder.data[HAS_REMIND_DATE].toString().toBoolean(),
            transferTimestamp2String(DATE_TIME_FORMAT,
                reminder.data[REMIND_DATE] as Timestamp),
            reminder.data[REMIND_DATE] as Timestamp,
            reminder.data[IS_CHECKED].toString().toBoolean(),
            reminder.data[NOTE].toString(),
            reminder.data[FREQUENCY].toString(),
            reminder.data[DOCUMENT_ID].toString()
        )
    }
}
