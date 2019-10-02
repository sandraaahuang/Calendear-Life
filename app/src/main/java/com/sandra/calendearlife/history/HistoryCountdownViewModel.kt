package com.sandra.calendearlife.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryCountdownViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val locale: Locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    lateinit var countdownAdd: Countdown

    private val countdownItem = ArrayList<Countdown>()
    private val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

    init {
        getItem()
    }

    fun getItem() {
        //connect to countdown data ( only the item that overdue is true )
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get countdowns
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .whereEqualTo(OVERDUE, true)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                val setDate = (countdown.data[SETDATE] as Timestamp)
                                val targetDate = (countdown.data[TARGETDATE] as Timestamp)

                                countdownAdd = Countdown(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    countdown.data[TITLE].toString(),
                                    countdown.data[NOTE].toString(),
                                    simpleDateFormat.format(targetDate.seconds * 1000),
                                    countdown.data[TARGETDATE] as Timestamp,
                                    countdown.data[OVERDUE].toString().toBoolean(),
                                    countdown.data[DOCUMENTID].toString()
                                )

                                countdownItem.add(countdownAdd)
                            }

                            _liveCountdown.value = countdownItem

                        }
                }
            }
    }
}