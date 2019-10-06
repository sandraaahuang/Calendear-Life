package com.sandra.calendearlife.calendar.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const.Companion.TYPE_CALENDAR
import com.sandra.calendearlife.constant.Const.Companion.putType
import com.sandra.calendearlife.constant.calendarTitleFormatter
import com.sandra.calendearlife.constant.selectionDateFormatter
import com.sandra.calendearlife.constant.timeSameYearFormatter
import com.sandra.calendearlife.databinding.FragmentCalendarMonthBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_month_day.view.*
import kotlinx.android.synthetic.main.calendar_month_header.view.*
import kotlinx.android.synthetic.main.fragment_calendar_month.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.temporal.WeekFields
import java.util.*

class CalendarMonthFragment : Fragment() {

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val viewModel: CalenderMonthViewModel by lazy {
        ViewModelProviders.of(this).get(CalenderMonthViewModel::class.java)
    }

    lateinit var binding: FragmentCalendarMonthBinding

    private val adapter = CalendarMonthAdapter(CalendarMonthAdapter.OnClickListener {
        putType(TYPE_CALENDAR)
        viewModel.displayCalendarDetails(it)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCalendarMonthBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.recyclerView.adapter = adapter

        viewModel.navigateToCalendarProperty.observe(this, androidx.lifecycle.Observer { calendar ->
            calendar?.let {
                this.findNavController().navigate(NavigationDirections.actionGlobalCalendarDetailFragment(it))
                viewModel.displayCalendarDetailsComplete()
            }
        })

        // floating action button
        val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)
        var isOpen = false

        binding.fabAdd.setOnClickListener {

            isOpen = if (isOpen) {

                binding.fabAdd.startAnimation(rotateBackward)
                binding.remindersFab.startAnimation(fabClose)
                binding.countdownsFab.startAnimation(fabClose)
                binding.calendarFab.startAnimation(fabClose)
                binding.addReminderText.startAnimation(fabClose)
                binding.addCountdownText.startAnimation(fabClose)
                binding.addEventText.startAnimation(fabClose)
                false

            } else {
                binding.fabAdd.startAnimation(rotateForward)
                binding.remindersFab.startAnimation(fabOpen)
                binding.countdownsFab.startAnimation(fabOpen)
                binding.calendarFab.startAnimation(fabOpen)
                binding.addReminderText.startAnimation(fabOpen)
                binding.addCountdownText.startAnimation(fabOpen)
                binding.addEventText.startAnimation(fabOpen)
                true
            }
        }

        binding.remindersFab.setOnClickListener {
            putType(TYPE_CALENDAR)
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            putType(TYPE_CALENDAR)
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            putType(TYPE_CALENDAR)
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        calendar.setup(currentMonth.minusMonths(10),
            currentMonth.plusMonths(10), daysOfWeek.first())
        calendar.scrollToMonth(currentMonth)

        if (savedInstanceState == null) {
            calendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }
        calendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val dayText = container.view.dayText
                val dotView = container.view.dotView

                dayText.text = day.date.dayOfMonth.toString()

                viewModel.liveAllCalendar.observe(this@CalendarMonthFragment,
                    androidx.lifecycle.Observer { calendar ->
                    calendar?.let {
                        for (value in it) {
                            if (value.date == day.date.toString() && day.owner == DayOwner.THIS_MONTH) {
                                dotView.visibility = View.VISIBLE
                            }
                        }
                    }
                })

                if (day.owner == DayOwner.THIS_MONTH) {
                    dayText.visibility = View.VISIBLE
                    when (day.date) {
                        today -> {

                            dayText.setTextColor(MyApplication.instance.getColor(R.color.white))
                            dayText.setBackgroundResource(R.drawable.today_bg)
                            dotView.visibility = View.INVISIBLE
                        }
                        selectedDate -> {

                            dayText.setBackgroundResource(R.drawable.selected_bg)
                            dotView.visibility = View.INVISIBLE
                        }

                        else -> {

                            dayText.setBackgroundResource(R.color.translucent_80)
                        }
                    }
                } else {

                    dayText.visibility = View.INVISIBLE
                    dotView.visibility = View.INVISIBLE
                }
            }
        }

        calendar.monthScrollListener = {

            requireActivity().textView.text = if (it.year == today.year) {
                timeSameYearFormatter(it.yearMonth)
            } else {
                calendarTitleFormatter(it.yearMonth)
            }

            // Select the first day of the month when
            // we scroll to a new month.
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }
        calendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }
                        .forEachIndexed { index, monthHeader ->
                        monthHeader.text = daysOfWeek[index].name.first().toString()
                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date

            viewModel.queryTodayEvent(Timestamp(DateTimeUtils.toSqlDate(date)))
            adapter.notifyDataSetChanged()

            oldDate?.let { calendar.notifyDateChanged(it) }
            calendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        selectedDateText.text = selectionDateFormatter(date)
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {

            daysOfWeek = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last) +
                    daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        }
        return daysOfWeek
    }
}

