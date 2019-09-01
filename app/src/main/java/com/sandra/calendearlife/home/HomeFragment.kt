package com.sandra.calendearlife.home


import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.provider.FontsContractCompat.FontRequestCallback.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.AccountPicker
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: HomeFragmentBinding
    lateinit var mockdata2: ArrayList<Reminders>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter(HomeCountdownAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2())
        })
        val remindersAdapter = HomeRemindersAdapter(this, HomeRemindersAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment())
        })
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewModel

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteCallback(
                remindersAdapter,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.remindersRecyclerView)

        //mock data
        val mockData = ArrayList<Countdown>()
        mockData.add(Countdown("88", "倒數數起來^^", "20200101"))
        mockData.add(Countdown("99", "倒數數起來:D", "20210101"))

        mockdata2 = ArrayList()
        mockdata2.add(Reminders("please remind me!!!", "20201010", false, false))
        mockdata2.add(Reminders("who am I!!!", "20201012", false, false))

        binding.countdownRecyclerView.adapter = countdownAdapter
        binding.remindersRecyclerView.adapter = remindersAdapter

        countdownAdapter.submitList(mockData)
        remindersAdapter.submitList(mockdata2)


        Log.d("sandraaa","mockData = $mockData")

        // floating action button
        binding.remindersFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }

        binding.linkToGoogle.setOnClickListener {
            // Google Account Picker
                val googlePicker = AccountPicker
                    .newChooseAccountIntent(
                        null, null, arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
                        true, null, null, null, null
                    )
                startActivityForResult(googlePicker, REQUEST_CODE)

        }

        return binding.root
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val accountName = data!!.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        Log.d("sandraaa","accountName = $accountName")
        }
        else{Log.d("sandraaa","accountName = null")}
    }

    companion object {
        const val REQUEST_CODE = 12345
    }

}


