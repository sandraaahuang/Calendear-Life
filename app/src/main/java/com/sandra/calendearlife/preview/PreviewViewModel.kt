package com.sandra.calendearlife.preview

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.GMAIL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_ACCOUNT
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.USER_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.USER_NAME
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager

class PreviewViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()

    private val userData = hashMapOf(
        SET_DATE to FieldValue.serverTimestamp(),
        USER_ID to UserManager.id,
        USER_NAME to UserManager.userName,
        GMAIL to UserManager.userEmail,
        HAS_ACCOUNT to true
    )

    //get all data to determine if the user is already existed
    fun insertUserData2Firebase() {
        db.collection(DATA)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    if (document.id != UserManager.id) {

                        // to add the user info first
                        UserManager.id?.let {
                            db.collection(DATA)
                                .document(it)
                                .set(userData)
                                .addOnSuccessListener { documentReference ->
                                    Logger.d("DocumentSnapshot added with ID: $documentReference")
                                }
                        }

                    }

                }
            }
    }
}