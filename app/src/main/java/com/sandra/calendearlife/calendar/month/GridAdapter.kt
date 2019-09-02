package com.sandra.calendearlife.calendar.month

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.sandra.calendearlife.R
import java.util.*




class GridAdapter(context: Context, dates: ArrayList<Date>, currentDate: Calendar) :
    ArrayAdapter<Date>(context, R.layout.cell_month, dates) {

    private val inflater: LayoutInflater
    private val dates: List<Date>
    private val currentDate: Calendar


    init {
        this.dates = dates
        this.currentDate = currentDate
        inflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val monthDate = dates[position]
        val dateCalendar = Calendar.getInstance()
        dateCalendar.time = monthDate
        val dayNo = dateCalendar.get(Calendar.DAY_OF_MONTH)
        val displayMonth = dateCalendar.get(Calendar.MONTH) +1
        val displayYear = dateCalendar.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH) +1
        val currentYear = currentDate.get(Calendar.YEAR)
        val displayDate = dateCalendar.get(Calendar.DATE)
        val today = currentDate.get(Calendar.DATE)

        var view = convertView
        if (view == null){
            view = inflater.inflate(R.layout.cell_month, parent, false)
        }

        if (displayMonth == currentMonth && displayYear == currentYear){
            view?.setBackgroundColor(context.resources.getColor(R.color.white))
        } else  {
            view?.setBackgroundColor(Color.parseColor("#cccccc"))
        }

        val dayNumber = view?.findViewById<TextView>(R.id.calendar_day)

        dayNumber?.text = dayNo.toString()

        return view!!
    }

    override fun getCount(): Int {
        return dates.size
    }

    override fun getPosition(item: Date?): Int {
        return dates.indexOf(item)
    }

    override fun getItem(position: Int): Date? {
        return dates[position]
    }


}