package com.sandra.calendearlife

import android.Manifest
import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
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
import com.google.firebase.firestore.FieldValue
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
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ACCOUNT_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_BEGIN_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_CALENDAR_ACCESS_LEVEL
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_DESCRIPTION_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_DISPLAY_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_END_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ID_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_OWNER_ACCOUNT_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_TITLE_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.SELECTION
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentUri
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.eventProjection
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.instanceProjection
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.permissionReadCheck
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.selectionArgs
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

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

        if (Locale.getDefault().language == "" && nowLanguage == ZH) {
            setLocale(CHINESE)

        } else {
            loadLocale()

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupToolbar()
        setupStatusBar()
        setDrawer()
        setupNavController()

        intent.extras?.let {
            if (UserManager.id != null) {
                viewModel.getItem(it.get(REMINDERSITEM).toString())
            } else {
                Logger.d("don't have user")
            }
        }

        viewModel.liveReminders.observe(this, Observer {
            it?.let {
                findNavController(R.id.myNavHostFragment)
                    .navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
            }
        })


        val value = intent.getStringExtra(TURN)

        if (!TextUtils.isEmpty(value)) {
            when (value) {
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

        if (UserManager.id != null) {
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

                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction(ED), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY, edPending
                                )
                            }
                            EVERY_WEEK -> {

                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction(EW), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 7, edPending
                                )

                            }
                            EVERY_MONTH -> {

                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction(EM), PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                alarmManager.setInexactRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    reminders.remindTimestamp.seconds * 1000,
                                    AlarmManager.INTERVAL_DAY * 30, edPending
                                )

                            }
                            EVERY_YEAR -> {

                                val dnrIntent = Intent(MyApplication.instance, AlarmReceiver::class.java)
                                val edPending = PendingIntent.getBroadcast(
                                    MyApplication.instance,
                                    1234, dnrIntent.setAction(EY), PendingIntent.FLAG_UPDATE_CURRENT
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

        val menuItem = binding.navView.menu.findItem(R.id.changeMode)
        val actionView = MenuItemCompat.getActionView(menuItem) as SwitchCompat

        if (preferences.getString(STATUS, null) == DARK) {

            actionView.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            actionView.isChecked = false
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


        actionView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                preferences.edit().putString(STATUS, DARK).apply()
                restartApp()

            } else if (!isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                preferences.edit().putString(STATUS, LIGHT).apply()
                restartApp()

            }
        }

        Logger.d("default language = ${Locale.getDefault()}")
        when (Locale.getDefault().language) {
            CHINESE -> updateZhEnum()
            else -> updateEnEnum()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            (this),
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1
        )
    }

    private fun queryCalendar() {

        // search calendar
        val cur: Cursor?

        // create list to store result
        val accountNameList = java.util.ArrayList<String>()
        val calendarIdList = java.util.ArrayList<String>()

        // give permission to read
        if (permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
            cur = contentResolver?.query(contentUri, eventProjection, SELECTION, selectionArgs, null)
            if (cur != null) {
                while (cur.moveToNext()) {
                    val calendarId: String = cur.getString(PROJECTION_ID_INDEX)
                    val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                    val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                    val ownerAccount: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                    val accessLevel = cur.getInt(PROJECTION_CALENDAR_ACCESS_LEVEL)

                    Logger.i(String.format("calendarId=%s", calendarId))
                    Logger.i(String.format("accountName=%s", accountName))
                    Logger.i(String.format("displayName=%s", displayName))
                    Logger.i(String.format("ownerAccount=%s", ownerAccount))
                    Logger.i(String.format("accessLevel=%s", accessLevel))
                    // store calendar data
                    accountNameList.add(displayName)
                    calendarIdList.add(calendarId)

                    val beginTime = getInstance()
                    beginTime.set(2019, 8, 1, 24, 0)
                    val startMillis = beginTime.timeInMillis
                    val endTime = getInstance()
                    endTime.set(2029, 8, 1, 24, 0)
                    val endMillis = endTime.timeInMillis

                    // search event
                    val cur2: Cursor?

                    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()

                    val selectionEvent = CalendarContract.Events.CALENDAR_ID + " = ?"
                    val selectionEventArgs = arrayOf(calendarId)
                    ContentUris.appendId(builder, startMillis)
                    ContentUris.appendId(builder, endMillis)

                    val eventIdList = java.util.ArrayList<String>()
                    val beginList = java.util.ArrayList<Long>()
                    val endList = java.util.ArrayList<Long>()
                    val titleList = java.util.ArrayList<String>()
                    val noteList = java.util.ArrayList<String>()


                    if (permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
                        cur2 = contentResolver?.query(
                            builder.build(),
                            instanceProjection,
                            selectionEvent,
                            selectionEventArgs, null
                        )
                        if (cur2 != null) {
                            while (cur2.moveToNext()) {
                                val eventID = cur2.getString(PROJECTION_ID_INDEX)
                                val beginVal = cur2.getLong(PROJECTION_BEGIN_INDEX)
                                val endVal = cur2.getLong(PROJECTION_END_INDEX)
                                val title = cur2.getString(PROJECTION_TITLE_INDEX)
                                val note = cur2.getString(PROJECTION_DESCRIPTION_INDEX)
                                // 取得所需的資料
                                Logger.i(String.format("eventID=%s", eventID))
                                Logger.i(String.format("beginVal=%s", beginVal))
                                Logger.i(String.format("endVal=%s", endVal))
                                Logger.i(String.format("title=%s", title))
                                Logger.i(String.format("note=%s", note))
                                // 暫存資料讓使用者選擇
                                val beginDate = Timestamp(beginVal / 1000, 0)
                                val endDate = Timestamp(endVal / 1000, 0)
                                eventIdList.add(eventID)
                                beginList.add(beginVal)
                                endList.add(endVal)
                                titleList.add(title)
                                noteList.add(note)

                                val item = hashMapOf(
                                    FirebaseKey.DATE to Timestamp(timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT,
                                        transferTimestamp2String(SIMPLE_DATE_FORMAT, beginDate))),
                                    SET_DATE to FieldValue.serverTimestamp(),
                                    FirebaseKey.BEGIN_DATE to beginDate,
                                    FirebaseKey.END_DATE to endDate,
                                    TITLE to title,
                                    NOTE to note,
                                    FirebaseKey.FROM_GOOGLE to true,
                                    FirebaseKey.COLOR to FirebaseKey.COLOR_GOOGLE,
                                    DOCUMENT_ID to eventID,
                                    FirebaseKey.HAS_COUNTDOWN to false,
                                    FirebaseKey.HAS_REMINDERS to false
                                )
                                viewModel.writeGoogleItem(item, eventID)
                            }
                            cur2.close()
                        }
                    }
                }
                cur.close()
            }
        } else {
            requestPermission()
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
                queryCalendar()
            }

            R.id.changeLanguage -> {
                showChangeLanguageList()
            }

            R.id.changeMode -> {
                return true
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
        val prefs = getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
        val language = prefs.getString(LANG, "")
        setLocale(language!!)
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
}


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            SharedPreferenceKey.COUNTDOWN -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("countdown trigger time = ${Timestamp.now().seconds * 1000}")

                val countdownItem = ArrayList<Countdown>()

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
                                .collection(FirebaseKey.COUNTDOWN)
                                .whereEqualTo(IS_OVERDUE, false)
                                .get()
                                .addOnSuccessListener { countdownDocuments ->

                                    for (countdown in countdownDocuments) {

                                        getCountdownItemFromFirebase(countdown, countdownItem)
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
            DNR -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection(DATA)
                                .document(UserManager.id!!)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(IS_CHECKED, false)
                                .whereEqualTo(HAS_REMIND_DATE, true)
                                .whereEqualTo(FREQUENCY, DOES_NOT_REPEAT)
                                .get()
                                .addOnSuccessListener { remindersDocuments ->

                                    for (reminder in remindersDocuments) {

                                        getRemindersItemFromFirebase(reminder, remindersItem)
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
            ED -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { remindersDocuments ->

                        for (reminder in remindersDocuments) {

                            getRemindersItemFromFirebase(reminder, remindersItem)
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
            EW -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { remindersDocuments ->

                        for (reminder in remindersDocuments) {

                            getRemindersItemFromFirebase(reminder, remindersItem)
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
            EM -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection(DATA)
                                .document(UserManager.id!!)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(IS_CHECKED, false)
                                .whereEqualTo(HAS_REMIND_DATE, true)
                                .whereEqualTo(FREQUENCY, EVERY_MONTH)
                                .get()
                                .addOnSuccessListener { remindersDocuments ->

                                    for (reminder in remindersDocuments) {

                                        getRemindersItemFromFirebase(reminder, remindersItem)
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
            EY -> {
                val db = FirebaseFirestore.getInstance()

                Logger.d("reminder trigger time = ${Timestamp.now().seconds * 1000}")

                val remindersItem = ArrayList<Reminders>()

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only ischecked is false )
                            db.collection(DATA)
                                .document(UserManager.id!!)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(IS_CHECKED, false)
                                .whereEqualTo(HAS_REMIND_DATE, true)
                                .whereEqualTo(FREQUENCY, EVERY_YEAR)
                                .get()
                                .addOnSuccessListener { remindersDocuments ->

                                    for (reminder in remindersDocuments) {

                                        getRemindersItemFromFirebase(reminder, remindersItem)
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
