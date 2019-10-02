package com.sandra.calendearlife.calendar.month

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
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
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.Const.Companion.TYPECALENDAR
import com.sandra.calendearlife.constant.Const.Companion.putType
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.databinding.FragmentCalendarMonthBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_month_day.view.*
import kotlinx.android.synthetic.main.calendar_month_header.view.*
import kotlinx.android.synthetic.main.fragment_calendar_month.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.WeekFields
import java.util.*

class CalendarMonthFragment : Fragment() {

    companion object {
        const val month = "MMM"
        const val monthYear = "MMM yyyy"
        const val date = "yyyy-MMM-dd"
    }

    private val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern(month, locale)
    private val titleFormatter = DateTimeFormatter.ofPattern(monthYear, locale)
    private val selectionFormatter = DateTimeFormatter.ofPattern(date, locale)
    private val viewModel: CalenderMonthViewModel by lazy {
        ViewModelProviders.of(this).get(CalenderMonthViewModel::class.java)
    }

    lateinit var binding: FragmentCalendarMonthBinding

    private val adapter = CalendarMonthAdapter(CalendarMonthAdapter.OnClickListener {
        putType(TYPECALENDAR)
        viewModel.displayCalendarDetails(it)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCalendarMonthBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.recyclerView.adapter = adapter

        viewModel.navigateToCalendarProperty.observe(this, androidx.lifecycle.Observer {
            it?.let {
                this.findNavController().navigate(NavigationDirections.actionGlobalCalendarDetailFragment(it))
                viewModel.displayCalendarDetailsComplete()
            }
        })

        // floating action button
        val fabOpen = AnimationUtils.loadAnimation(this.context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this.context, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(this.context, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(this.context, R.anim.rotate_backward)
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
            putType(TYPECALENDAR)
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            putType(TYPECALENDAR)
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            putType(TYPECALENDAR)
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
                val textView = container.view.dayText
                val dotView = container.view.dotView

                textView.text = day.date.dayOfMonth.toString()

                viewModel.liveAllCalendar.observe(this@CalendarMonthFragment, androidx.lifecycle.Observer {
                    it?.let {
                        for (value in it){
                            if (value.date == day.date.toString() && day.owner == DayOwner.THIS_MONTH) {
                                dotView.visibility = View.VISIBLE
                            }
                        }
                    }
                })

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.visibility = View.VISIBLE
                    when (day.date) {
                        today -> {

                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.today_bg)
                            dotView.visibility = View.INVISIBLE
                        }
                        selectedDate -> {

                            textView.setBackgroundResource(R.drawable.selected_bg)
                            dotView.visibility = View.INVISIBLE
                        }

                        else -> {

                            textView.setBackgroundResource(R.color.translucent_80)
                             }
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                    dotView.visibility = View.INVISIBLE
                }
            }
        }

        calendar.monthScrollListener = {

            requireActivity().textView.text = if (it.year == today.year) {
                titleSameYearFormatter.format(it.yearMonth)
            } else {
                titleFormatter.format(it.yearMonth)
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
                        .forEachIndexed { index, textView ->
                        textView.text = daysOfWeek[index].name.first().toString()
                    }
                }
            }
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date

            viewModel.queryToday(Timestamp(DateTimeUtils.toSqlDate(date)))
            adapter.notifyDataSetChanged()

            oldDate?.let { calendar.notifyDateChanged(it) }
            calendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun updateAdapterForDate(date: LocalDate) {
        selectedDateText.text = selectionFormatter.format(date)
    }

    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        var daysOfWeek = DayOfWeek.values()
        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
        if (firstDayOfWeek != DayOfWeek.MONDAY) {
            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
            daysOfWeek = rhs + lhs
        }
        return daysOfWeek
    }

    private fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)
    private fun TextView.setTextColorRes(@ColorRes color: Int) = setTextColor(context.getColorCompat(color))
}

