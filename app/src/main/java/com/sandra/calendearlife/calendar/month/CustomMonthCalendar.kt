package com.sandra.calendearlife.calendar.month

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.ItemCalendarMonthBinding
import kotlinx.android.synthetic.main.item_calendar_month.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMonthCalendar : ConstraintLayout {

    private lateinit var previous_month: ImageView
    private lateinit var last_month: ImageView
    private lateinit var currentMonth: TextView
    private lateinit var gridView: GridView
    private lateinit var gridAdapter: GridAdapter
    private lateinit var goToToday: ImageView
    var calendar = Calendar.getInstance(Locale.ENGLISH)
    var dates = ArrayList<Date>()
    var dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    var monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
    var yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

    companion object {
        const val MAX_CALENDAR_DAYS = 35
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        intializeLayout()
        previous_month.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }
        last_month.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            setupCalendar()
        }
        goToToday.setOnClickListener {
            calendar.get(Calendar.DATE)
            Log.d("sandraaa","${calendar.get(Calendar.DATE)}")
        }
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    fun intializeLayout(){

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemCalendarMonthBinding.inflate(inflater, this, true)
        previous_month = binding.previousMonth
        last_month = binding.lastMonth
        currentMonth = binding.currentMonth
        gridView = binding.gridView
        goToToday = binding.goToToday
        setupDates()
    }

    fun setupCalendar(){
        val currentDate: String = dateFormat.format(calendar.time)
        currentMonth.text = currentDate
        setupDates()
    }

    fun setupDates(){
        dates.clear()
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDatOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) -1
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDatOfMonth)

        while (dates.size < MAX_CALENDAR_DAYS){
            dates.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        gridAdapter = GridAdapter(context, dates, calendar)
        gridView.adapter = gridAdapter
    }
}