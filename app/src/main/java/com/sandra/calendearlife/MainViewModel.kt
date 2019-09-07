package com.sandra.calendearlife

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager

class MainViewModel : ViewModel() {

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val userEmail = UserManager.userEmail
    val userPhoto = UserManager.userPhoto

}