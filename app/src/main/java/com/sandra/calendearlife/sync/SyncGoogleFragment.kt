package com.sandra.calendearlife.sync

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.databinding.FragmentSyncGoogleBinding
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import java.util.*

class SyncGoogleFragment : AppCompatDialogFragment() {

    var db = FirebaseFirestore.getInstance()

    lateinit var binding: FragmentSyncGoogleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.MessageDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?
        , savedInstanceState: Bundle?
    )
            : View? {

        binding = FragmentSyncGoogleBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.askMeLaterButton.setOnClickListener {
            restart()
        }

        binding.nextButton.setOnClickListener {
            requestPermission()
        }

        return binding.root
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 926
        )

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 926) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(this.view!!, "Success", Snackbar.LENGTH_LONG).show()
                queryCalendar()
            } else {
                Toast.makeText(this.context, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun queryCalendar() {
        val eventProjection = arrayOf(
            CalendarContract.Calendars._ID, // 0 calendar id
            CalendarContract.Calendars.ACCOUNT_NAME, // 1 account name
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2 display name
            CalendarContract.Calendars.OWNER_ACCOUNT, // 3 owner account
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        val projectionIdIndex = 0
        val projectionAccountNameIndex = 1
        val projectionDisplayNameIndex = 2
        val projectionOwnerAccountIndex = 3
        val projectionCalendarAccessLevel = 4

        // event data
        val instanceProjection = arrayOf(
            CalendarContract.Instances.EVENT_ID, // 0 event id
            CalendarContract.Instances.BEGIN, // 1 begin date
            CalendarContract.Instances.END, // 2 end date
            CalendarContract.Instances.TITLE, // 3 title
            CalendarContract.Instances.DESCRIPTION // 4 note
        )

        val projectionBeginIndex = 1
        val projectionEndIndex = 2
        val projectionTitleIndex = 3
        val projectionDescriptionIndex = 4

        // Get user email
        val targetAccount = UserManager.userEmail
        // search calendar
        val cur: Cursor?
        val cr = MyApplication.instance.contentResolver
        val uri = CalendarContract.Calendars.CONTENT_URI
        // find
        val selection = (FirebaseKey.PARENTHESES + CalendarContract.Calendars.ACCOUNT_NAME + FirebaseKey.CONJUNCTION
                + CalendarContract.Calendars.ACCOUNT_TYPE + FirebaseKey.CONJUNCTION
                + CalendarContract.Calendars.OWNER_ACCOUNT + FirebaseKey.QUESTION_MARK)
        val selectionArgs =
            arrayOf(targetAccount, FirebaseKey.MAIL_FORMAT, UserManager.userEmail)

        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        cur = cr?.query(uri, eventProjection, selection, selectionArgs, null)
        if (cur != null) {
            while (cur.moveToNext()) {
                val calendarId: String = cur.getString(projectionIdIndex)
                val accountName: String = cur.getString(projectionAccountNameIndex)
                val displayName: String = cur.getString(projectionDisplayNameIndex)
                val ownerAccount: String = cur.getString(projectionOwnerAccountIndex)
                val accessLevel = cur.getInt(projectionCalendarAccessLevel)

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
                val cr2 = MyApplication.instance.contentResolver
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

                    cur2 = cr2?.query(
                        builder.build(),
                        instanceProjection,
                        selectionEvent,
                        selectionEventArgs, null
                    )
                    if (cur2 != null) {
                        while (cur2.moveToNext()) {
                            val eventID = cur2.getString(projectionIdIndex)
                            val beginVal = cur2.getLong(projectionBeginIndex)
                            val endVal = cur2.getLong(projectionEndIndex)
                            val title = cur2.getString(projectionTitleIndex)
                            val note = cur2.getString(projectionDescriptionIndex)
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
                                DATE to transferTimestamp2String(SIMPLE_DATE_FORMAT, beginDate),
                                SET_DATE to FieldValue.serverTimestamp(),
                                BEGIN_DATE to beginDate,
                                END_DATE to endDate,
                                TITLE to title,
                                NOTE to note,
                                FROM_GOOGLE to true,
                                COLOR to COLOR_GOOGLE,
                                DOCUMENT_ID to eventID,
                                HAS_COUNTDOWN to false,
                                HAS_REMINDERS to false
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
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .document(documentId)
            .set(item)
            .addOnSuccessListener {
                Logger.d("DocumentSnapshot added with ID = $documentId")
            }
            .addOnCompleteListener {
                restart()
            }
    }

    private fun restart(){
        val intent = Intent(this.context, MainActivity::class.java)
        startActivity(intent)

    }

}