package com.sandra.calendearlife

import android.Manifest
import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.Const.Companion.DOES_NOT_REPEAT
import com.sandra.calendearlife.constant.Const.Companion.EVERY_DAY
import com.sandra.calendearlife.constant.Const.Companion.EVERY_MONTH
import com.sandra.calendearlife.constant.Const.Companion.EVERY_WEEK
import com.sandra.calendearlife.constant.Const.Companion.EVERY_YEAR
import com.sandra.calendearlife.constant.Const.Companion.TYPE_CALENDAR
import com.sandra.calendearlife.constant.Const.Companion.TYPE_HOME
import com.sandra.calendearlife.constant.Const.Companion.putType
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ADDFRAGMENT
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.DARK
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.DARKMODE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.DNR
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ED
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.EM
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ENGLISH
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.EW
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.EY
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.LANG
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.LIGHT
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.LOGIN
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REMINDERSITEM
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.SETTINGS
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.STATUS
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.TURN
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ZH
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ActivityMainBinding
import com.sandra.calendearlife.databinding.NavHeaderMainBinding
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.util.getString
import java.util.*
import java.util.Calendar.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val preferences =
        MyApplication.instance.getSharedPreferences(DARKMODE, Context.MODE_PRIVATE)

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    private fun createIntent(context: Context, documentId: String?): Intent {

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(REMINDERSITEM, documentId)
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

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme)
        }

        val actionbar = actionBar
        actionbar?.title = resources.getString(R.string.app_name)
        val nowLanguage = Locale.getDefault().language

        loadLocale()

        when {
            (Locale.getDefault().language.isNullOrEmpty() && nowLanguage == ZH) -> setLocale(CHINESE)
            else -> loadLocale()
        }

        when (Locale.getDefault().language) {
            CHINESE -> updateZhEnum()
            else -> updateEnEnum()
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupToolbar()
        setDrawer()
        setupNavController()
        setupStatusBar()
        registerConnectionReceiver()
        setupRemindersWidgetIntent()

        when {
            UserManager.id == null -> setDrawerEnabled(false)
        }

        intent.extras?.let { extras ->
            UserManager.id?.let {
                viewModel.getWidgetItemAndNavigate2Detail(extras.get(REMINDERSITEM).toString())
            }
        }

        viewModel.liveReminders.observe(this, Observer {
            it?.let { remindersProperty ->
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalRemindersDetailFragment(remindersProperty))
            }
        })

        viewModel.hasPermission.observe(this, Observer {
            it?.let {
                requestPermission4Calendar()
            }
        })

        UserManager.id?.let {
            viewModel.set4AlarmManagerReminderItem()
            // setup countdown notification

            val customCal = getInstance()

            customCal.set(HOUR_OF_DAY, 9)
            customCal.set(MINUTE, 0)
            customCal.set(SECOND, 0)

            val alarmManager = MyApplication.instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(MyApplication.instance, AlarmReceiver::class.java)
            intent.action = SharedPreferenceKey.COUNTDOWN
            val pendingIntent = PendingIntent.getBroadcast(
                MyApplication.instance,
                1234, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, customCal.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent
            )
            Logger.i("alarm set success")

            viewModel.liveSet4AlarmManagerReminderItem.observe(this, Observer {
                it?.let {
                    for (reminders in it) {

                        when (reminders.frequency) {
                            DOES_NOT_REPEAT -> {

                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val dnrPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction(DNR), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setExact(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000, dnrPending
                                )
                            }
                            EVERY_DAY -> {

                                val everydayIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, everydayIntent.setAction(ED),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY, edPending
                                )
                            }
                            EVERY_WEEK -> {

                                val everyweekIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, everyweekIntent.setAction(EW),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 7, edPending
                                )

                            }
                            EVERY_MONTH -> {

                                val everyMonthIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, everyMonthIntent.setAction(EM),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 30, edPending
                                )

                            }
                            EVERY_YEAR -> {

                                val everyYearIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, everyYearIntent.setAction(EY),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 365, edPending
                                )

                            }
                        }


                    }
                }
            })
        }

        val actionView = MenuItemCompat.getActionView(
            binding.navView.menu.findItem(R.id.changeMode)
        ) as SwitchCompat

        if (preferences.getString(STATUS, null) == DARK) {

            actionView.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            actionView.isChecked = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        actionView.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    preferences.edit().putString(STATUS, DARK).apply()
                    restartApp()
                }
                else -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    preferences.edit().putString(STATUS, LIGHT).apply()
                    restartApp()
                }
            }
        }
    }

    private fun setupRemindersWidgetIntent() {
        if (!TextUtils.isEmpty(intent.getStringExtra(TURN))) {
            when (intent.getStringExtra(TURN)) {
                ADDFRAGMENT -> {
                    findNavController(R.id.myNavHostFragment)
                        .navigate(NavigationDirections.actionGlobalRemindersFragment())
                }
                LOGIN -> {
                    findNavController(R.id.myNavHostFragment)
                        .navigate(NavigationDirections.actionGlobalPreviewFragment())
                }
            }
        }
    }

    private fun requestPermission4Calendar() {
        ActivityCompat.requestPermissions(
            (this),
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1
        )
    }

    private fun setupToolbar() {
        binding.toolbar.setPadding(0, getStatusBarHeight(), 0, 0)
        setSupportActionBar(binding.toolbar)

        // hide title of toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
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
                putType(TYPE_CALENDAR)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
            }
            R.id.home -> {
                putType(TYPE_HOME)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHomeFragment())
            }
            R.id.addCalendar -> {
                putType(TYPE_CALENDAR)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCalendarEventFragment())
            }
            R.id.addCountdown -> {
                putType(TYPE_HOME)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalCountdownFragment())
            }
            R.id.addReminder -> {
                putType(TYPE_HOME)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalRemindersFragment())
            }
            R.id.historyReminder -> {
                putType(TYPE_HOME)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHistoryReminders())
            }

            R.id.historyCountdown -> {
                putType(TYPE_HOME)
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalHistoryCountdown2())
            }

            R.id.sync -> {
                viewModel.queryGoogleCalendar()
            }

            R.id.changeLanguage -> {
                showChangeLanguageList()
            }

            R.id.changeMode -> {
                return true
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled) {
            DrawerLayout.LOCK_MODE_UNLOCKED
        } else {
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        }
        binding.drawerLayout.setDrawerLockMode(lockMode)
    }

    private fun showChangeLanguageList() {

        val listItem = arrayOfNulls<String>(2)
        listItem[0] = getString(R.string.chinese)
        listItem[1] = getString(R.string.english)
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(getString(R.string.language))
        builder.setSingleChoiceItems(listItem, -1) { dialogInterface, i ->
            if (i == 0) {
                setLocale(CHINESE)
                restartApp()
            } else if (i == 1) {
                setLocale(ENGLISH)
                restartApp()
            }
            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale(lang: String) {
        var locale = Locale(lang)
        Locale.setDefault(locale)
        if (lang == CHINESE) {
            locale = Locale.TAIWAN
        }
        val config = android.content.res.Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        val editor = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE).edit()
        editor.putString(LANG, lang)
        editor.apply()
    }

    private fun loadLocale() {
        val languagePreference = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val language = languagePreference.getString(LANG, "")
        language?.let {
            setLocale(it)
        }
    }

    private fun restartApp() {

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    private fun setupStatusBar() {

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

    }

    private fun setDrawer() {
        // Set up header of drawer ui using data binding
        val bindingNavHeader = NavHeaderMainBinding.inflate(
            LayoutInflater.from(this), binding.navView, false)

        bindingNavHeader.lifecycleOwner = this
        bindingNavHeader.viewModel = viewModel
        binding.navView.addHeaderView(bindingNavHeader.root)

        binding.drawerLayout.fitsSystemWindows = true
        binding.drawerLayout.clipToPadding = false

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

    private fun updateZhEnum() {
        CurrentFragmentType.DETAIL.value = "詳細資訊"
        CurrentFragmentType.NEWEVENT.value = "新增事件"
        CurrentFragmentType.NEWREMINDERS.value = "新增提醒事件"
        CurrentFragmentType.NEWCOUNTDOWN.value = "新增倒數事件"
        CurrentFragmentType.HISTORY.value = "歷史紀錄"
    }

    private fun updateEnEnum() {
        CurrentFragmentType.DETAIL.value = "Detail"
        CurrentFragmentType.NEWEVENT.value = "New Event"
        CurrentFragmentType.NEWREMINDERS.value = "New Reminder"
        CurrentFragmentType.NEWCOUNTDOWN.value = "New Countdown"
        CurrentFragmentType.HISTORY.value = "History"
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
                R.id.remindersFragment -> CurrentFragmentType.NEWREMINDERS
                R.id.historyReminders -> CurrentFragmentType.HISTORY
                R.id.historyCountdown2 -> CurrentFragmentType.HISTORY
                else -> viewModel.currentFragmentType.value
            }
        }
    }

    private fun registerConnectionReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            registerReceiver(InternetReceiver(), intentFilter)
        }
    }

}


class AlarmReceiver : BroadcastReceiver() {
    val db = FirebaseFirestore.getInstance()
    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            SharedPreferenceKey.COUNTDOWN -> {

                Logger.d("countdown trigger time = ${Timestamp.now().seconds * 1000}")

                val countdownItem = ArrayList<Countdown>()

                UserManager.id?.let {
                    db.collection(DATA)
                        .document(it)
                        .collection(CALENDAR)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (calendar in documents) {

                                // get countdowns
                                db.collection(DATA)
                                    .document(it)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(FirebaseKey.COUNTDOWN)
                                    .whereEqualTo(IS_OVERDUE, false)
                                    .get()
                                    .addOnSuccessListener { countdownDocuments ->

                                        for (countdown in countdownDocuments) {

                                            getCountdownItemFromFirebase(countdown, countdownItem)
                                        }

                                        for ((index, value) in countdownItem.withIndex()) {

                                            setupNotification(
                                                "${((value.targetTimestamp.seconds -
                                                        Timestamp.now().seconds) / 86400)} days " +
                                                        "before ${value.title}",
                                                null, index)
                                        }

                                    }

                            }
                        }
                }
            }
            DNR -> {

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                setupRemindersAlarmItem(remindersItem, DOES_NOT_REPEAT)
            }
            ED -> {

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                setupRemindersAlarmItem(remindersItem, EVERY_DAY)
            }
            EW -> {

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                setupRemindersAlarmItem(remindersItem, EVERY_WEEK)

            }
            EM -> {

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                setupRemindersAlarmItem(remindersItem, EVERY_MONTH)

            }
            EY -> {

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                setupRemindersAlarmItem(remindersItem, EVERY_YEAR)
            }
        }

    }

    private fun setupRemindersAlarmItem(remindersItem: ArrayList<Reminders>, frequency: String) {
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only ischecked is false )
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(IS_CHECKED, false)
                            .whereEqualTo(HAS_REMIND_DATE, true)
                            .whereEqualTo(FREQUENCY, frequency)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    getRemindersItemFromFirebase(reminder, remindersItem)
                                }

                                for ((index, value) in remindersItem.withIndex()) {
                                    value.title?.let {
                                        setupNotification(it, value.note, index)
                                    }
                                }

                            }

                    }
                }
        }
    }

    private fun setupNotification(notificationTitle: String,
                                  notificationContent: String?,
                                  id: Int) {

        val CHANNEL_ID = "Calendear"

        val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_line)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
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
            notify(id, builder.build())
        }
    }
}
