package com.sandra.calendearlife.sync

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.CalendarContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.DialogSyncBinding
import com.sandra.calendearlife.util.UserManager
import tr.com.harunkor.gifviewplayer.GifMovieView
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SyncDialog : AppCompatDialogFragment() {

    var db = FirebaseFirestore.getInstance()

    lateinit var binding: DialogSyncBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.MessageDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?
        , savedInstanceState: Bundle?
    )
            : View? {

        binding = DialogSyncBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.askMeLaterButton.setOnClickListener {
            restart()
//            findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
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
                query_calendar()
            } else {
                Toast.makeText(this.context, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun query_calendar() {
        val EVENT_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID, // 0 calendar id
            CalendarContract.Calendars.ACCOUNT_NAME, // 1 account name
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, // 2 display name
            CalendarContract.Calendars.OWNER_ACCOUNT, // 3 owner account
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        val PROJECTION_ID_INDEX = 0
        val PROJECTION_ACCOUNT_NAME_INDEX = 1
        val PROJECTION_DISPLAY_NAME_INDEX = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        val PROJECTION_CALENDAR_ACCESS_LEVEL = 4

        // event data
        val INSTANCE_PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID, // 0 event id
            CalendarContract.Instances.BEGIN, // 1 begin date
            CalendarContract.Instances.END, // 2 end date
            CalendarContract.Instances.TITLE, // 3 title
            CalendarContract.Instances.DESCRIPTION // 4 note
        )

        val PROJECTION_BEGIN_INDEX = 1
        val PROJECTION_END_INDEX = 2
        val PROJECTION_TITLE_INDEX = 3
        val PROJECTION_DESCRIPTION_INDEX = 4

        // Get user email
        val targetAccount = UserManager.userEmail!!
        // search calendar
        val cur: Cursor?
        val cr = MyApplication.instance.contentResolver
        val uri = CalendarContract.Calendars.CONTENT_URI
        // find
        val selection = ("((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))")
        val selectionArgs =
            arrayOf(targetAccount, "com.google", UserManager.userEmail)

        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        cur = cr?.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)
        if (cur != null) {
            while (cur.moveToNext()) {
                val calendarId: String = cur.getString(PROJECTION_ID_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val ownerAccount: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                val accessLevel = cur.getInt(PROJECTION_CALENDAR_ACCESS_LEVEL)

                Log.i("query_calendar", String.format("calendarId=%s", calendarId))
                Log.i("query_calendar", String.format("accountName=%s", accountName))
                Log.i("query_calendar", String.format("displayName=%s", displayName))
                Log.i("query_calendar", String.format("ownerAccount=%s", ownerAccount))
                Log.i("query_calendar", String.format("accessLevel=%s", accessLevel))
                // store calendar data
                accountNameList.add(displayName)
                calendarIdList.add(calendarId)

                Log.d("sandraaa", "accountNameList = $accountNameList,calendarIdList = $calendarIdList ")

                val targetCalendar = calendarId
                val beginTime = Calendar.getInstance()
                beginTime.set(2019, 8, 1, 24, 0)
                val startMillis = beginTime.timeInMillis
                val endTime = Calendar.getInstance()
                endTime.set(2020, 8, 1, 24, 0)
                val endMillis = endTime.timeInMillis

                // search event
                val cur2: Cursor?
                val cr2 = MyApplication.instance.contentResolver
                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()

                val selectionEvent = CalendarContract.Events.CALENDAR_ID + " = ?"
                val selectionEventArgs = arrayOf(targetCalendar)
                ContentUris.appendId(builder, startMillis)
                ContentUris.appendId(builder, endMillis)

                val eventIdList = ArrayList<String>()
                val beginList = ArrayList<Long>()
                val endList = ArrayList<Long>()
                val titleList = ArrayList<String>()
                val noteList = ArrayList<String>()

                    cur2 = cr2?.query(
                        builder.build(),
                        INSTANCE_PROJECTION,
                        selectionEvent,
                        selectionEventArgs, null
                    )
                    if (cur2 != null) {
                        while (cur2.moveToNext()) {
                            val eventID = cur2.getString(PROJECTION_ID_INDEX)
                            val beginVal = cur2.getLong(PROJECTION_BEGIN_INDEX)
                            val endVal = cur2.getLong(PROJECTION_END_INDEX)
                            val title = cur2.getString(PROJECTION_TITLE_INDEX)
                            val note = cur2.getString(PROJECTION_DESCRIPTION_INDEX)
                            // 取得所需的資料
                            Log.i("query_event", String.format("eventID=%s", eventID))
                            Log.i("query_event", String.format("beginVal=%s", beginVal))
                            Log.i("query_event", String.format("endVal=%s", endVal))
                            Log.i("query_event", String.format("title=%s", title))
                            Log.i("query_event", String.format("note=%s", note))
                            // 暫存資料讓使用者選擇
                            val beginDate = Timestamp(beginVal / 1000, 0)
                            val endDate = Timestamp(endVal / 1000, 0)
                            eventIdList.add(eventID)
                            beginList.add(beginVal)
                            endList.add(endVal)
                            titleList.add(title)
                            noteList.add(note)

                            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
                            val dateFormat = simpleDateFormat.format(beginDate.seconds * 1000)


                            val item = hashMapOf(
                                "date" to Timestamp(simpleDateFormat.parse(dateFormat)),
                                "setDate" to FieldValue.serverTimestamp(),
                                "beginDate" to beginDate,
                                "endDate" to endDate,
                                "title" to title,
                                "note" to note,
                                "fromGoogle" to true,
                                "color" to "245E2C",
                                "documentID" to eventID,
                                "hasCountdown" to false,
                                "hasReminders" to false
                            )
                            writeGoogleItem(item, eventID)
                        }
                        cur2.close()
                    }

            }
            cur.close()
        }

    }

    fun writeGoogleItem(item: Any, documentId: String) {

        // get all data from user at first
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .document(documentId)
            .set(item)
            .addOnSuccessListener { CdocumentReference ->
                Log.d(
                    "AddCountdownsIntoDB",
                    "DocumentSnapshot added with ID = $documentId"
                )
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