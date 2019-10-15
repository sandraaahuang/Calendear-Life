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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
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
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentResolver
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentUri
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.eventProjection
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.instanceProjection
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ACCOUNT_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_BEGIN_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_CALENDAR_ACCESS_LEVEL
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_DESCRIPTION_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_DISPLAY_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_END_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ID_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_OWNER_ACCOUNT_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_TITLE_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.SELECTION
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.selectionArgs
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.timeFormat2FirebaseTimestamp
import com.sandra.calendearlife.constant.timeFormat2SqlTimestamp
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.databinding.FragmentSyncGoogleBinding
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class SyncGoogleFragment : AppCompatDialogFragment() {

    lateinit var binding: FragmentSyncGoogleBinding

    private val viewModel: SyncGoogleViewModel by lazy {
        ViewModelProviders.of(this).get(SyncGoogleViewModel::class.java)
    }

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
            findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
        }

        binding.nextButton.setOnClickListener {
            requestPermission()
        }

        viewModel.writeCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
            }
        })

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
                viewModel.queryCalendar()
            } else {
                Toast.makeText(this.context, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }
}