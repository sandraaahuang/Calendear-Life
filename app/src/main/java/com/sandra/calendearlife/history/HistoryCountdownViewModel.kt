package com.sandra.calendearlife.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.getCountdownItemFromFirebase
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.util.UserManager

class HistoryCountdownViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private lateinit var countdownAdd: Countdown

    private val countdownItem = ArrayList<Countdown>()
    private val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

    init {
        getItem()
    }

    private fun getItem() {
        //connect to countdown data ( only the item that overdue is true )
        UserManager.id?.let { userManager ->
            db.collection(DATA)
                .document(userManager)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        // get countdowns
                        db.collection(DATA)
                            .document(userManager)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(COUNTDOWN)
                            .whereEqualTo(OVERDUE, true)
                            .get()
                            .addOnSuccessListener { countdownDocuments ->

                                for (countdown in countdownDocuments) {

                                    getCountdownItemFromFirebase(countdown, countdownItem)
                                }

                                _liveCountdown.value = countdownItem

                            }
                    }
                }
        }
    }
}