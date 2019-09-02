package com.sandra.calendearlife.calendar.month

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.ItemCalendarMonthBinding
import kotlinx.android.synthetic.main.cell_month.view.*
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
//    var events = ArrayList<Events>()
    var dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    var monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
    var yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

    companion object {
        const val MAX_CALENDAR_DAYS = 35
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        intializeLayout()
        Log.d("sandraaa","calendar = $calendar")
        previous_month.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            setupCalendar()
            Log.d("sandraaa","calendar = $calendar")
        }
        last_month.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            setupCalendar()
            Log.d("sandraaa","calendar = $calendar")
        }
        goToToday.setOnClickListener {
            Log.d("sandraaa","calendar = $calendar")
            val substractMonth = calendar.get(Calendar.MONTH) - Calendar.getInstance().get(Calendar.MONTH)
            val substractYear = calendar.get(Calendar.YEAR) - Calendar.getInstance().get(Calendar.YEAR)
            Log.d ("sandraaa", " substract = $substractMonth")
            calendar.add(Calendar.MONTH, - substractMonth)
            calendar.add(Calendar.YEAR, - substractYear)
            setupCalendar()
        }
        gridView.setOnItemClickListener { adapterView, view, position, id ->
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
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