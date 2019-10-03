package com.sandra.calendearlife.calendar.detail

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
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
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_ALL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ENDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentCalendarDetailBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.util.Logger
import java.util.*

class CalendarDetailFragment : Fragment() {

    lateinit var binding: FragmentCalendarDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCalendarDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val calendar = CalendarDetailFragmentArgs.fromBundle(arguments!!).calendar
        val viewModelFactory = CalendarDetailFactory(calendar, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(CalendarDetailViewModel::class.java)
        binding.viewModel = viewModel

        when (calendar.color) {
            COLOR_GOOGLE -> {
                if (calendar.isAllDay) {
                    isAllDayVisibility()
                } else {
                    isNotAllDayVisibility()
                }
            }
            COLOR_CAL -> {
                if (calendar.isAllDay) {
                    isAllDayVisibility()
                } else {
                    isNotAllDayVisibility()
                }
            }
            COLOR_REMIND_CAL -> {
                remindLayoutVisibility()
                if (calendar.isAllDay) {
                    isAllDayVisibility()
                } else {
                    isNotAllDayVisibility()
                }
            }
            COLOR_ALL -> {
                remindLayoutVisibility()
                countdownVisibility()
                if (calendar.isAllDay) {
                    isAllDayVisibility()
                } else {
                    isNotAllDayVisibility()
                }
            }
            COLOR_COUNTDOWN_CAL -> {
                countdownVisibility()
                if (calendar.isAllDay) {
                    isAllDayVisibility()
                } else {
                    isNotAllDayVisibility()
                }
            }
            COLOR_REMIND -> {
                remindLayoutVisibility()
            }
            COLOR_COUNTDOWN -> {
                countdownVisibility()
            }
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

        binding.deleteButton.setOnClickListener {
            viewModel.deleteItem(calendar.documentID!!)

            if (calendar.fromGoogle) {
                viewModel.deleteEvent(calendar.documentID)
            } else {
                Logger.d("is not google item")
            }

            Snackbar.make(this.view!!, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
        }

        binding.saveButton.setOnClickListener {

            var beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
            var endDate = "${binding.endDate.text} ${binding.endTime.text}"
            val targetDate = "${binding.targetDateInput.text}"
            val remindDate = "${binding.remindDate.text} ${binding.remindTime.text}"

            val updateCalendar: HashMap<String, Any>

            var updateRemind = hashMapOf(
                TITLE to "${binding.detailTitleInput.text}".trim(),
                NOTE to "${binding.noteInput.text}".trim(),
                REMINDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate)
            )

            var updateCountdown = hashMapOf(
                TITLE to "${binding.detailTitleInput.text}".trim(),
                NOTE to "${binding.noteInput.text}".trim(),
                TARGETDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate)
            )

            when (calendar.color) {

                COLOR_REMIND -> {
                    updateRemind = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        REMINDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate)
                    )
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, remindDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim()
                    )

                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_COUNTDOWN -> {
                    updateCountdown = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        TARGETDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate)
                    )
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate),
                        BEGINDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate),
                        ENDDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim()
                    )

                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_CAL -> {
                    if (calendar.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }

                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, beginDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, beginDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )
                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_REMIND_CAL -> {
                    if (calendar.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, beginDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, beginDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )
                    updateRemind = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        REMINDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate)
                    )
                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_COUNTDOWN_CAL -> {
                    if (calendar.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, beginDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, beginDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )
                    updateCountdown = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        TARGETDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate)
                    )
                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_ALL -> {
                    if (calendar.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, beginDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, beginDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}"
                    )
                    updateCountdown = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        TARGETDATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, targetDate)
                    )
                    updateRemind = hashMapOf(
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        REMINDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, remindDate)
                    )
                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }

                COLOR_GOOGLE -> {
                    if (calendar.isAllDay) {
                        beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                        endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                    } else {
                        beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                        endDate = "${binding.endDate.text} ${binding.endTime.text}"
                    }
                    updateCalendar = hashMapOf(
                        DATE to timeFormat2SQLTimestamp(SIMPLEDATEFORMAT, beginDate),
                        BEGINDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, beginDate),
                        ENDDATE to timeFormat2SQLTimestamp(DATETIMEFORMAT, endDate),
                        TITLE to "${binding.detailTitleInput.text}".trim(),
                        NOTE to "${binding.noteInput.text}".trim(),
                        LOCATION to "${binding.locationInput.text}".trim()
                    )

                    viewModel.updateItem(calendar.documentID!!, updateCalendar, updateCountdown, updateRemind)
                }
            }

            if (calendar.fromGoogle) {
                viewModel.updateEvent(
                    calendar.documentID!!,
                    "${binding.detailTitleInput.text}",
                    "${binding.noteInput.text}",
                    timeFormat2FirebaseTimestamp(DATETIMEFORMAT, beginDate),
                    timeFormat2FirebaseTimestamp(DATETIMEFORMAT, endDate)
                )
            } else {
                Logger.d("is not google item")
            }

            Snackbar.make(this.view!!, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
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
                DiscardDialog().show(this@CalendarDetailFragment.fragmentManager!!, SHOW)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, SHOW)
        }

        return binding.root
    }


    private fun showDatePicker(inputDate: TextView) {
        val datePickerDialog = DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, _, _, _ ->
                inputDate.text = timeFormat2String(SIMPLEDATEFORMAT)
            }, year, monthOfYear, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(inputTime: TextView) {
        val timePickerDialog = TimePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { _, _, _ ->
                inputTime.text = timeFormat2String(TIMEFORMAT)
            }, hour, minute, false
        )
        timePickerDialog.show()
    }

    private fun isAllDayVisibility() {
        binding.allDayLayout.visibility = View.VISIBLE
        binding.beginDate.visibility = View.VISIBLE
        binding.beginTime.visibility = View.GONE
        binding.endDateText.visibility = View.GONE
        binding.endDate.visibility = View.GONE
        binding.endTime.visibility = View.GONE
        binding.remindLayout.visibility = View.GONE
        binding.countdownLayout.visibility = View.GONE
    }

    private fun isNotAllDayVisibility() {
        binding.allDayLayout.visibility = View.VISIBLE
        binding.beginDate.visibility = View.VISIBLE
        binding.endDate.visibility = View.VISIBLE
        binding.endTime.visibility = View.VISIBLE
        binding.remindLayout.visibility = View.GONE
        binding.countdownLayout.visibility = View.GONE
    }

    private fun remindLayoutVisibility() {
        binding.allDayLayout.visibility = View.GONE
        binding.countdownLayout.visibility = View.GONE
        binding.remindLayout.visibility = View.VISIBLE
    }

    private fun countdownVisibility() {
        binding.allDayLayout.visibility = View.GONE
        binding.countdownLayout.visibility = View.VISIBLE
        binding.remindLayout.visibility = View.GONE

    }
}