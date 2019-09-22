package com.sandra.calendearlife

import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ActivityMainBinding
import com.sandra.calendearlife.databinding.NavHeaderMainBinding
import com.sandra.calendearlife.sync.DeleteWorker
import com.sandra.calendearlife.sync.ImportWorker
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.util.getString
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private fun createIntent(context: Context, documentId: String?): Intent {

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

    @TargetApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }

        val actionbar = actionBar
        actionbar?.title = resources.getString(R.string.app_name)
        loadLocale()


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

        val value = intent.getStringExtra("turn")
        if (!TextUtils.isEmpty(value)) {
            when (value) {
                "addFragment" -> {
                    findNavController(R.id.myNavHostFragment)
                        .navigate(NavigationDirections.actionGlobalRemindersFragment())
                }
                "login" -> {
                    findNavController(R.id.myNavHostFragment)
                        .navigate(NavigationDirections.actionGlobalPreviewFragment())
                }
            }
        }

        if (UserManager.id != null) {
            viewModel.dnrItem()
            // setup countdown notification
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
            val alarmManager = MyApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(MyApplication.instance, AlarmReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                MyApplication.instance,
                1234, intent.setAction("countdown"), PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, Timestamp.now().seconds * 1000,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )

            viewModel.livednr.observe(this, Observer {
                it?.let {
                    for ((index, value) in it.withIndex()) {

                        when (value.frequency) {
                            "Does not repeat" -> {
                                Log.d("sandraaa", "DNR = ${value.remindTimestamp.seconds}")
                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val dnrPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction("dnr"), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    value.remindTimestamp.seconds * 1000, dnrPending
                                )
                            }
                            "Every day" -> {
                                Log.d("sandraaa", "ED = ${value.remindTimestamp.seconds}")
                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction("ED"), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    value.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY, edPending
                                )
                            }
                            "Every week" -> {
                                Log.d("sandraaa", "EW = ${value.remindTimestamp.seconds}")
                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction("EW"), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    value.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 7, edPending
                                )

                            }
                            "Every month" -> {
                                Log.d("sandraaa", "EM = ${value.remindTimestamp.seconds}")
                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction("EM"), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    value.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 30, edPending
                                )

                            }
                            "Every year" -> {
                                Log.d("sandraaa", "EY = ${value.remindTimestamp.seconds}")
                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction("EY"), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    value.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 365, edPending
                                )

                            }
                        }


                    }
                }
            })
        }


        val menuItem = binding.navView.menu.findItem(R.id.changeMode)
        val actionView = MenuItemCompat.getActionView(menuItem) as SwitchCompat

        actionView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                restartApp()

                actionView.isChecked = true
                Log.d("sandraa", "change mode")
            } else if ( !isChecked ) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                restartApp()
                actionView.isChecked = false
            }
        }
    }


    private fun setupToolbar() {
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
                val importWorker = OneTimeWorkRequestBuilder<ImportWorker>()
                    .build()

                WorkManager.getInstance()
                    .enqueue(importWorker)
            }

            R.id.changeLanguage -> {
                showChangeLanguageList()
            }
        }


        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showChangeLanguageList() {

        val listItem = arrayOfNulls<String>(2)
        listItem[0] = getString(R.string.chinese)
        listItem[1] = getString(R.string.english)
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(getString(R.string.language))
        builder.setSingleChoiceItems(listItem, -1) { dialogInterface, i ->
            if (i == 0) {
                setLocale("zh")
                restartApp()
            } else if (i ==1 ) {
                setLocale("en")
                restartApp()
            }
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale (lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = android.content.res.Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("Lang", lang)
        editor.apply()
    }

    private fun loadLocale() {
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("Lang", "")
        setLocale(language!!)
    }

    private fun restartApp() {

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()

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
                R.id.historyReminders -> CurrentFragmentType.HISTORY
                R.id.historyCountdown2 -> CurrentFragmentType.HISTORY
                else -> viewModel.currentFragmentType.value
            }
        }
    }
}


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {

        when (p1?.action) {
            "countdown" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "countdown trigger time = ${Timestamp.now().seconds * 1000}")

                lateinit
                var countdownAdd: Countdown
                val countdownItem = ArrayList<Countdown>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            // get countdowns
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("countdowns")
                                .whereEqualTo("overdue", false)
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (countdown in documents) {
                                        val setDate = (countdown.data["setDate"] as Timestamp)
                                        val targetDate = (countdown.data["targetDate"] as Timestamp)

                                        countdownAdd = Countdown(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            countdown.data["title"].toString(),
                                            countdown.data["note"].toString(),
                                            simpleDateFormat.format(targetDate.seconds * 1000),
                                            countdown.data["targetDate"] as Timestamp,
                                            countdown.data["overdue"].toString().toBoolean(),
                                            countdown.data["documentID"].toString()
                                        )

                                        countdownItem.add(countdownAdd)
                                    }

                                    for ((index, value) in countdownItem.withIndex()) {

                                        val textTitle =
                                            "${((value.targetTimestamp.seconds - Timestamp.now().seconds) / 86400)} days " +
                                                    "before ${value.title}"
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }
            }
            "dnr" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "reminder trigger time = ${Timestamp.now().seconds * 1000}")
                lateinit var remindAdd: Reminders
                val remindersItem = ArrayList<Reminders>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .whereEqualTo("isChecked", false)
                                .whereEqualTo("setRemindDate", true)
                                .whereEqualTo("frequency", "Does not repeat")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        val setDate = (reminder.data["setDate"] as Timestamp)
                                        val remindDate = (reminder.data["remindDate"] as Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds * 1000),
                                            reminder.data["remindDate"] as Timestamp,
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString(),
                                            reminder.data["documentID"].toString()
                                        )

                                        remindersItem.add(remindAdd)
                                    }

                                    for ((index, value) in remindersItem.withIndex()) {

                                        val textTitle = value.title
                                        val textContent = value.note
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }

            }
            "ED" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "reminder trigger time = ${Timestamp.now().seconds * 1000}")
                lateinit var remindAdd: Reminders
                val remindersItem = ArrayList<Reminders>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .whereEqualTo("isChecked", false)
                                .whereEqualTo("setRemindDate", true)
                                .whereEqualTo("frequency", "Every day")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        val setDate = (reminder.data["setDate"] as Timestamp)
                                        val remindDate = (reminder.data["remindDate"] as Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds * 1000),
                                            reminder.data["remindDate"] as Timestamp,
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString(),
                                            reminder.data["documentID"].toString()
                                        )

                                        remindersItem.add(remindAdd)
                                    }

                                    for ((index, value) in remindersItem.withIndex()) {

                                        val textTitle = value.title
                                        val textContent = value.note
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }

            }
            "EW" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "reminder trigger time = ${Timestamp.now().seconds * 1000}")
                lateinit var remindAdd: Reminders
                val remindersItem = ArrayList<Reminders>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .whereEqualTo("isChecked", false)
                                .whereEqualTo("setRemindDate", true)
                                .whereEqualTo("frequency", "Every week")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        val setDate = (reminder.data["setDate"] as Timestamp)
                                        val remindDate = (reminder.data["remindDate"] as Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds * 1000),
                                            reminder.data["remindDate"] as Timestamp,
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString(),
                                            reminder.data["documentID"].toString()
                                        )

                                        remindersItem.add(remindAdd)
                                    }

                                    for ((index, value) in remindersItem.withIndex()) {

                                        val textTitle = value.title
                                        val textContent = value.note
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }

            }
            "EM" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "reminder trigger time = ${Timestamp.now().seconds * 1000}")
                lateinit var remindAdd: Reminders
                val remindersItem = ArrayList<Reminders>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .whereEqualTo("isChecked", false)
                                .whereEqualTo("setRemindDate", true)
                                .whereEqualTo("frequency", "Every month")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        val setDate = (reminder.data["setDate"] as Timestamp)
                                        val remindDate = (reminder.data["remindDate"] as Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds * 1000),
                                            reminder.data["remindDate"] as Timestamp,
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString(),
                                            reminder.data["documentID"].toString()
                                        )

                                        remindersItem.add(remindAdd)
                                    }

                                    for ((index, value) in remindersItem.withIndex()) {

                                        val textTitle = value.title
                                        val textContent = value.note
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }

            }
            "EY" -> {
                val db = FirebaseFirestore.getInstance()
                val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

                Log.d("alarmManager", "reminder trigger time = ${Timestamp.now().seconds * 1000}")
                lateinit var remindAdd: Reminders
                val remindersItem = ArrayList<Reminders>()

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .whereEqualTo("isChecked", false)
                                .whereEqualTo("setRemindDate", true)
                                .whereEqualTo("frequency", "Every year")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        val setDate = (reminder.data["setDate"] as Timestamp)
                                        val remindDate = (reminder.data["remindDate"] as Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds * 1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds * 1000),
                                            reminder.data["remindDate"] as Timestamp,
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString(),
                                            reminder.data["documentID"].toString()
                                        )

                                        remindersItem.add(remindAdd)
                                    }

                                    for ((index, value) in remindersItem.withIndex()) {

                                        val textTitle = value.title
                                        val textContent = value.note
                                        val CHANNEL_ID = "Calendear"
                                        val notificationId = index


                                        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.app_line)
                                            .setContentTitle(textTitle)
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setAutoCancel(true)

                                        // Create the NotificationChannel, but only on API 26+ because
                                        // the NotificationChannel class is new and not in the support library

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            val name = getString(R.string.create_channel)
                                            val descriptionText =
                                                getString(R.string.create_channel)
                                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                                            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                                description = descriptionText
                                            }
                                            // Register the channel with the system
                                            val notificationManager: NotificationManager =
                                                MyApplication.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                            notificationManager.createNotificationChannel(channel)
                                        }
                                        with(NotificationManagerCompat.from(MyApplication.instance)) {
                                            // notificationId is a unique int for each notification that you must define
                                            notify(notificationId, builder.build())
                                        }
                                    }

                                }

                        }
                    }

            }
        }

    }
}


