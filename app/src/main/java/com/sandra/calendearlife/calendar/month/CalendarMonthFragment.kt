package com.sandra.calendearlife.calendar.month

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.sandra.calendearlife.*
import com.sandra.calendearlife.databinding.CalendarMonthFragmentBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.calendar_month_day.view.*
import kotlinx.android.synthetic.main.calendar_month_fragment.*
import kotlinx.android.synthetic.main.calendar_month_header.view.*
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.WeekFields
import java.util.*

class CalendarMonthFragment : Fragment() {

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    private val selectionFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    private val viewModel: CalenderMonthViewModel by lazy {
        ViewModelProviders.of(this).get(CalenderMonthViewModel::class.java)
    }

    lateinit var binding: CalendarMonthFragmentBinding

    private val adapter = CalendarMonthAdapter(CalendarMonthAdapter.OnClickListener {
        viewModel.displayCalendarDetails(it)
        Log.d("sandraaa", "click item = $it")
    })


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CalendarMonthFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.recyclerView.adapter = adapter

        viewModel.navigateToCalendarProperty.observe(this, androidx.lifecycle.Observer {
            if (null != it) {
                // Must find the NavController from the Fragment
                this.findNavController().navigate(NavigationDirections.actionGlobalCalendarDetailFragment(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayCalendarDetailsComplete()
            }
        })

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        calendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        calendar.scrollToMonth(currentMonth)

        if (savedInstanceState == null) {
            calendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val textView = view.dayText
//            val dotView = view.dotView

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
                val textView = container.textView
//                val dotView = container.dotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.today_bg)
//                            dotView.makeInVisible()
                        }
//                        selectedDate -> {
//                            textView.setTextColorRes(R.color.calendar_today_badge)
//                            textView.setBackgroundResource(R.drawable.selected_bg)
////                            dotView.makeInVisible()
//                        }
                        else -> {

//                            dotView.isVisible = viewModel.liveCalendar.value?.isNotEmpty() ?: false
                        }
                    }
                } else {
                    textView.makeInVisible()
//                    dotView.makeInVisible()
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
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
//                        tv.setTextColorRes(R.color.black)
                    }
                }
            }
        }

        remindersFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        countdownsFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        calendarFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date

            val localDate = DateTimeUtils.toSqlDate(date)

            viewModel.queryToday(Timestamp(localDate))
            adapter.notifyDataSetChanged()
            Log.d(
                "sandraaa",
                "Timestamp(localDate) = ${Timestamp(localDate)}, liveDate = ${viewModel.liveCalendar.value}"
            )


            oldDate?.let { calendar.notifyDateChanged(it) }
            calendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
        Log.d("sandraaa", "select date = $date")
    }

    private fun updateAdapterForDate(date: LocalDate) {
        selectedDateText.text = selectionFormatter.format(date)
    }

//    override fun onStart() {
//        super.onStart()
//
//        (activity as AppCompatActivity).toolbar.setBackgroundColor(R.styleable.ds_backgroundcolor)
//        requireActivity().window.statusBarColor = R.styleable.ds_backgroundcolor
//
//    }

//    override fun onStop() {
//        super.onStop()
//        (activity as AppCompatActivity).toolbar.setBackgroundColor(R.styleable.ds_backgroundcolor)
//        requireActivity().window.statusBarColor = R.styleable.ds_backgroundcolor
//    }

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

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeInVisible() {
    visibility = View.INVISIBLE
}