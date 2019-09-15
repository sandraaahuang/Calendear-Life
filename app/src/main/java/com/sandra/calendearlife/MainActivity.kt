package com.sandra.calendearlife

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.sandra.calendearlife.databinding.ActivityMainBinding
import com.sandra.calendearlife.databinding.NavHeaderMainBinding
import com.sandra.calendearlife.sync.DeleteWorker
import com.sandra.calendearlife.sync.ImportWorker
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import java.util.concurrent.TimeUnit
import android.content.Intent
import androidx.lifecycle.Observer
import com.sandra.calendearlife.data.Reminders


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    fun createIntent(context: Context, documentId: String?): Intent {

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("remindersItem", documentId)

        Log.d("sandraaa", "intent = ${intent.extras?.get("remindersItem")}")

        return intent

    }

    fun createFlagIntent(context: Context, documentId: String?, vararg flags: Int): Intent {

        val intent = createIntent(context, documentId)

        for (flag in flags) {
            intent.flags = flag
        }

        return intent

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupToolbar()
        sepupStatusBar()
        setDrawer()
        setupNavController()

        intent.extras?.let {
            viewModel.getItem(it.get("remindersItem").toString())
        }

        viewModel.liveReminders.observe(this, Observer {
            it?.let {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
            }
        })
    }

    fun setupToolbar() {
        binding.toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // hide title of toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.month -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
            }
            R.id.search -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCalendarSearchFragment())
            }
            R.id.home -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHomeFragment())
            }
            R.id.addCalendar -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCalendarEventFragment())
            }
            R.id.addCountdown -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCountdownFragment())
            }
            R.id.addReminder -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalRemindersFragment())
            }
            R.id.historyReminder -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHistoryReminders())
            }

            R.id.historyCountdown -> {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHistoryCountdown2())
            }

            R.id.sync -> {
                val deleteRequest = OneTimeWorkRequestBuilder<DeleteWorker>()
                    .build()

                val importWorker = OneTimeWorkRequestBuilder<ImportWorker>()
                    .setInitialDelay(3, TimeUnit.SECONDS)
                    .build()

                WorkManager.getInstance()
                    .beginWith(deleteRequest)
                    .then(importWorker)
                    .enqueue()

            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun sepupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun setDrawer() {
        val drawerLayout = binding.drawerLayout

        // Set up header of drawer ui using data binding
        val bindingNavHeader = NavHeaderMainBinding.inflate(
            LayoutInflater.from(this), binding.navView, false
        )

        bindingNavHeader.lifecycleOwner = this
        bindingNavHeader.viewModel = viewModel
        binding.navView.addHeaderView(bindingNavHeader.root)

        drawerLayout.fitsSystemWindows = true
        drawerLayout.clipToPadding = false

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources
            .getIdentifier("status_bar_height", "dimen", "android")

        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result

    }

    private fun setupNavController() {
        findNavController(R.id.myNavHostFragment).addOnDestinationChangedListener { navController: NavController, _: NavDestination, _: Bundle? ->
            viewModel.currentFragmentType.value = when (navController.currentDestination?.id) {
                R.id.homeFragment -> CurrentFragmentType.HOME
                R.id.previewFragment -> CurrentFragmentType.PREVIEW
                R.id.calendarDetailFragment -> CurrentFragmentType.DETAIL
                R.id.remindersDetailFragment -> CurrentFragmentType.DETAIL
                R.id.countdownDetailFragment -> CurrentFragmentType.DETAIL
                R.id.calendarEventFragment -> CurrentFragmentType.NEWEVENT
                R.id.countdownFragment -> CurrentFragmentType.NEWCOUNTDOWN
                R.id.remindersFragment -> CurrentFragmentType.NEWREMINDER
                R.id.calendarScheduleFragment -> CurrentFragmentType.SCHEDULE
                R.id.calendarDayFragment -> CurrentFragmentType.DAY
                R.id.calendarWeekFragment -> CurrentFragmentType.WEEK
                R.id.calendarSearchFragment -> CurrentFragmentType.SEARCH
                R.id.historyReminders -> CurrentFragmentType.HISTORY
                R.id.historyCountdown2 -> CurrentFragmentType.HISTORY
                else -> viewModel.currentFragmentType.value
            }
        }
    }
}
