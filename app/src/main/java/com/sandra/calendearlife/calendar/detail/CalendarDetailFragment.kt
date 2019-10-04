package com.sandra.calendearlife.calendar.detail

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.DateFormat.Companion.BEGINTIME
import com.sandra.calendearlife.constant.DateFormat.Companion.ENDTIME
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.hour
import com.sandra.calendearlife.constant.DateFormat.Companion.minute
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_ALL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentCalendarDetailBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.util.Logger
import java.util.*

class CalendarDetailFragment : Fragment() {

    lateinit var binding: FragmentCalendarDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val calendarArguments = CalendarDetailFragmentArgs.fromBundle(arguments!!).calendar
        val viewModelFactory = CalendarDetailFactory(calendarArguments, requireNotNull(activity).application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(CalendarDetailViewModel::class.java)

        binding = FragmentCalendarDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        if (calendarArguments.color == COLOR_GOOGLE
            ||calendarArguments.color == COLOR_CAL
            || calendarArguments.color == COLOR_REMIND_CAL
            || calendarArguments.color == "53777A"
            ||calendarArguments.color == COLOR_ALL
            || calendarArguments.color == COLOR_COUNTDOWN_CAL){

            binding.allDayLayout.visibility = View.VISIBLE
            binding.beginDate.visibility = View.VISIBLE


            if (calendarArguments.color == COLOR_CAL
                || calendarArguments.color == COLOR_REMIND_CAL
                ||calendarArguments.color == COLOR_ALL
                || calendarArguments.color == COLOR_COUNTDOWN_CAL){

                if (calendarArguments.isAllDay){
                    binding.beginTime.visibility = View.GONE
                    binding.endDateText.visibility = View.GONE
                    binding.endDate.visibility = View.GONE
                    binding.endTime.visibility = View.GONE
                }
                else {
                    binding.endDate.visibility = View.VISIBLE
                    binding.endTime.visibility = View.VISIBLE
                }
            }

        } else {
            binding.allDayLayout.visibility = View.GONE
        }

        if (calendarArguments.color == COLOR_REMIND
            || calendarArguments.color == COLOR_REMIND_CAL
            || calendarArguments.color == COLOR_ALL
            ||calendarArguments.hasReminders){
            binding.remindLayout.visibility = View.VISIBLE
        } else {
            binding.remindLayout.visibility = View.GONE
        }

        if (calendarArguments.color == COLOR_COUNTDOWN
            ||calendarArguments.color == "53777A"
            ||calendarArguments.color == COLOR_ALL
            ||calendarArguments.hasCountdown
            || calendarArguments.color == COLOR_COUNTDOWN_CAL) {
            binding.countdownLayout.visibility = View.VISIBLE
        } else {
            binding.countdownLayout.visibility = View.GONE
        }

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showDatePicker(it)
            }
        })

        viewModel.showTimePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showTimePicker(it)
            }
        })

        viewModel.hasPermission.observe(this, androidx.lifecycle.Observer {
            it?.let {
                requestPermission()
            }
        })

        binding.deleteButton.setOnClickListener {
            calendarArguments.documentID?.let {
                viewModel.deleteEvent(it)

                if (calendarArguments.fromGoogle) {
                    viewModel.deleteGoogleEvent(it)
                } else {
                    Logger.d("is not google item")
                }
            }

            view?.let {
                Snackbar.make(it, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
            }

        }

        binding.saveButton.setOnClickListener {

            var beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
            var endDate = "${binding.endDate.text} ${binding.endTime.text}"
            val targetDate = "${binding.targetDateInput.text}"
            val remindDate = "${binding.remindDate.text} ${binding.remindTime.text}"

            val updateCalendar: HashMap<String, Any>

            val updateRemind = hashMapOf(
                TITLE to "${binding.detailTitleInput.text}".trim(),
                NOTE to "${binding.noteInput.text}".trim(),
                REMIND_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, remindDate)
            )

            val updateCountdown = hashMapOf(
                TITLE to "${binding.detailTitleInput.text}".trim(),
                NOTE to "${binding.noteInput.text}".trim(),
                TARGET_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate)
            )

            when (calendarArguments.color) {

                COLOR_REMIND -> {
//
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, remindDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, remindDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, remindDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim()
                    )
                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }
                }

                COLOR_COUNTDOWN -> {

                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                        END_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim()
                    )

                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }

                }

                COLOR_CAL -> {
                    if (calendarArguments.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }

                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, beginDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, beginDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )
                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }

                }

                COLOR_REMIND_CAL -> {
                    if (calendarArguments.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, beginDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, beginDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )

                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }
                }

                COLOR_COUNTDOWN_CAL -> {
                    if (calendarArguments.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, beginDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, beginDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )

                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }
                }

                COLOR_ALL -> {
                    if (calendarArguments.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, beginDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, beginDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}"
                    )
                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }

                }

                COLOR_GOOGLE -> {
                    if (calendarArguments.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, beginDate),
                        BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, beginDate),
                        END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )
                    calendarArguments.documentID?.let {
                        viewModel.updateEvent(it, updateCalendar, updateCountdown, updateRemind)
                    }

                }
            }

            if (calendarArguments.fromGoogle) {

                calendarArguments.documentID?.let {
                    viewModel.updateGoogleEvent(
                        it,
                        "${binding.detailTitleInput.text}",
                        "${binding.noteInput.text}",
                        timeFormat2FirebaseTimestamp(DATE_TIME_FORMAT, beginDate),
                        timeFormat2FirebaseTimestamp(DATE_TIME_FORMAT, endDate)
                    )
                }

            } else {
                Logger.d("is not google item")
            }

            view?.let {
                Snackbar.make(it, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
            }
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveButton.isClickable = false
                binding.deleteButton.isClickable = false
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@CalendarDetailFragment.fragmentManager?.let {
                    DiscardDialog().show(it, SHOW)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            this.fragmentManager?.let {
                DiscardDialog().show(it, SHOW)
            }
        }

        return binding.root
    }


    private fun showDatePicker(inputDate: TextView) {
        context?.let {
            DatePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    inputDate.text =
                        timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
                }, year, monthOfYear, dayOfMonth
            ).show()
        }
    }

    private fun showTimePicker(inputTime: TextView) {
        context?.let {
            TimePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { _, hour, minute ->
                    inputTime.text =
                        timeFormat2String4TimePicker(TIME_FORMAT, hour, minute)
                }, hour, minute, false
            ).show()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            (activity as MainActivity),
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1
        )
    }

    private fun isAllDayEvent() {

        binding.allDayLayout.visibility = View.VISIBLE
        binding.beginDate.visibility = View.VISIBLE
        binding.beginDateText.text = context?.getString(R.string.date_b)
    }

    private fun isNotAllDayEvent() {

        binding.allDayLayout.visibility = View.VISIBLE
        binding.beginDate.visibility = View.VISIBLE
        binding.endDate.visibility = View.VISIBLE
        binding.endTime.visibility = View.VISIBLE
    }

    private fun showRemindLayout() {

        binding.remindLayout.visibility = View.VISIBLE
    }

    private fun showCountdownLayout() {

        binding.countdownLayout.visibility = View.VISIBLE
    }
}