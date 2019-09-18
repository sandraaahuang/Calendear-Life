package com.sandra.calendearlife.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.HomeFragmentBinding
import android.content.Intent.getIntent
import android.util.Log
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.sandra.calendearlife.calendar.notification.CountdownWorker
import java.sql.Time
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: HomeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter(HomeCountdownAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2(it))
        },viewModel)

        val remindersAdapter = HomeRemindersAdapter(viewModel, HomeRemindersAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
        })
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteCallback(
                remindersAdapter,
                viewModel
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.remindersRecyclerView)

        binding.countdownRecyclerView.adapter = countdownAdapter
        binding.remindersRecyclerView.adapter = remindersAdapter

        val recyclerIndicator = binding.indicator
        recyclerIndicator.attachToRecyclerView(binding.countdownRecyclerView)

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

        // setNotification
        val initialDate: LocalDateTime
        val timestampInitialDate: Timestamp
        val zoneId = ZoneId.of("Asia/Taipei")
        val nowHour = LocalDateTime.now(zoneId).hour

        if (nowHour > 9) {

            initialDate = LocalDateTime.of(
                LocalDate.now().year, LocalDate.now().monthValue,
                LocalDateTime.now().dayOfMonth.plus(1), 9, 0
            )

            val seconds = initialDate.atZone(zoneId).toEpochSecond()
            val nanos = initialDate.nano
            timestampInitialDate = Timestamp(seconds, nanos)

            Log.d("sandraaa", "initialDate = ${timestampInitialDate.seconds}")

        } else {
            initialDate = LocalDateTime.of(
                LocalDate.now().year, LocalDate.now().monthValue,
                LocalDateTime.now().dayOfMonth, 9, 0
            )

            val seconds = initialDate.atZone(zoneId).toEpochSecond()
            val nanos = initialDate.nano
            timestampInitialDate = Timestamp(seconds, nanos)

            Log.d("sandraaa", "initialDate = ${timestampInitialDate.seconds}")
        }

        val countdownRequest = OneTimeWorkRequestBuilder<CountdownWorker>()
            .setInitialDelay(timestampInitialDate.seconds - Timestamp.now().seconds, TimeUnit.SECONDS)
            .build()

        val countdownRepeat
                = PeriodicWorkRequestBuilder<CountdownWorker>(1, TimeUnit.DAYS)
            .setPeriodStartTime(timestampInitialDate.seconds - Timestamp.now().seconds, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance()
            .enqueue(countdownRequest)

        WorkManager.getInstance()
            .enqueue(countdownRepeat)

        WorkManager.getInstance().getWorkInfoByIdLiveData(countdownRequest.id)
            .observe(this, Observer<WorkInfo> {
                val status = it.state.name
                Log.d("countdownRequest","countdownRequest status1 = $status")
            })

        WorkManager.getInstance().getWorkInfoByIdLiveData(countdownRepeat.id)
            .observe(this, Observer<WorkInfo> {
                val status = it.state.name
                Log.d("countdownRequest","countdownRequest status2 = $status")
            })

//        viewModel.liveRemindersDnr.observe(this, Observer {
//            it?.let{
//                for ((index, value) in it.withIndex()) {
//                    val reminderDnrRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
//                        .setInitialDelay(
//                            ((value.remindTimestamp.seconds - com.google.firebase.Timestamp.now().seconds)),
//                            TimeUnit.SECONDS
//                        )
//                        .build()
//
//                    WorkManager.getInstance()
//                        .enqueue(reminderDnrRequest)
//
//                    WorkManager.getInstance().getWorkInfoByIdLiveData(reminderDnrRequest.id)
//                        .observe(this, Observer<WorkInfo> {
//                            val status = it.state.name
//                            Log.d("workmanager","reminderDnrRequest status = $status")
//                        })
//
//                    Log.d(
//                        "workmanager",
//                        "${value.remindTimestamp.seconds}, ${com.google.firebase.Timestamp.now().seconds}"
//                    )
//                }
//            }
//        })

        return binding.root
    }
}


