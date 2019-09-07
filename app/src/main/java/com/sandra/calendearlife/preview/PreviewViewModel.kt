package com.sandra.calendearlife.preview

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager

class PreviewViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()

    val userDate = hashMapOf(
        "setDate" to FieldValue.serverTimestamp(),
        "userId" to UserManager.id,
        "userName" to UserManager.userName,
        "gmail" to UserManager.userEmail,
        "hasAccount" to true
    )

    //get all data to determine if the user is already existed
    fun getItem() {
        db.collection("data")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("getExistedUser", "${document.id} => ${document.data}")
                    if (document.id != UserManager.id){
                        // to add the user info first
                            db.collection("data").document(UserManager.id!!)
                                .set(userDate)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "AddNewDataIntoDB",
                                        "DocumentSnapshot added with ID: $documentReference"
                                    )
                                }
                    }

                }
            }

            .addOnFailureListener { exception ->
                Log.w("getExistedUser", "Error getting documents: ", exception)
            }
    }
}