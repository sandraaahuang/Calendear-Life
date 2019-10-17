package com.sandra.calendearlife.preview

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.FragmentPreviewBinding
import com.sandra.calendearlife.constant.Const.Companion.RC_SIGN_IN
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_CODE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.GOOGLEINFO
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ID
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.LOGIN
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USEREMAIL
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USERNAME
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USERPHOTO
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.widget.RemindersWidget

class PreviewFragment : Fragment() {

    private val viewModel: PreviewViewModel by lazy {
        ViewModelProviders.of(this).get(PreviewViewModel::class.java)
    }

    private lateinit var binding: FragmentPreviewBinding

    //Initialize Firebase Auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.recyclerView.adapter = PreviewImageAdapter(
            intArrayOf(
                R.drawable.preview_photo_widget,
                R.drawable.preview_photo_notify,
                R.drawable.preview_photo_home,
                R.drawable.preview_photo_mode
            )
        )

        binding.indicator.attachToRecyclerView(binding.recyclerView)
        LinearSnapHelper().apply {
            attachToRecyclerView(binding.recyclerView)
        }

        UserManager.id?.let {
            findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
        }

        binding.connect.setOnClickListener {
            signIn()
            setLogin(true)
            updateWidgetWhenIsLogin()
        }

        return binding.root
    }

    private fun signIn() {

        val signInIntent = GoogleSignIn.getClient(

            MyApplication.instance,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(
                    MyApplication.instance.getString(R.string.default_web_client_id)
                )
                .requestEmail()
                .build()

        ).signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        System.out.println(REQUEST_CODE)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        handleSignInResult(
            GoogleSignIn.getSignedInAccountFromIntent(data)
        )
    }

    private fun handleSignInResult(completeTask: Task<GoogleSignInAccount>) {
        try {
            completeTask.getResult(ApiException::class.java)?.let {
                firebaseAuthWithGoogle(it) }

        } catch (e: ApiException) {
            Logger.w("signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Logger.d("firebaseAuthWithGoogle:" + acct.id)

        auth.signInWithCredential(

            GoogleAuthProvider.getCredential(acct.idToken, null)

        ).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val preferences =
                        MyApplication.instance.getSharedPreferences(GOOGLEINFO, Context.MODE_PRIVATE)

                    preferences.edit().putString(ID, auth.uid).apply()
                    preferences.getString(ID, auth.uid)
                    UserManager.id = auth.uid

                    preferences.edit().putString(USERNAME, auth.currentUser?.displayName).apply()
                    preferences.getString(USERNAME, auth.currentUser?.displayName)
                    UserManager.userName = auth.currentUser?.displayName

                    preferences.edit().putString(USEREMAIL, auth.currentUser?.email).apply()
                    preferences.getString(USEREMAIL, auth.currentUser?.email)
                    UserManager.userEmail = auth.currentUser?.email

                    preferences.edit().putString(USERPHOTO, auth.currentUser?.photoUrl.toString()).apply()
                    preferences.getString(USERPHOTO, auth.currentUser?.photoUrl.toString())
                    UserManager.userPhoto = auth.currentUser?.photoUrl.toString()

                    viewModel.insertUserData2Firebase()
                    findNavController().navigate(NavigationDirections.actionGlobalSyncDialog())

                } else {
                    // If sign in fails, display a message to the user.
                    Logger.w("signInWithCredential:failure = ${task.exception}")
                }
            }
    }

    private fun setLogin(login: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(LOGIN, login)
            .apply()
    }

    private fun updateWidgetWhenIsLogin() {

        val thisWidget = ComponentName(context!!, RemindersWidget::class.java)
        val views = RemoteViews(this.context!!.packageName, R.layout.reminders_widget)

        if (PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean(LOGIN, false)) {
            views.setViewVisibility(R.id.remindersWidgetStackView, View.VISIBLE)
            views.setViewVisibility(R.id.empty, View.GONE)
        } else {
            views.setViewVisibility(R.id.remindersWidgetStackView, View.GONE)
            views.setViewVisibility(R.id.empty, View.VISIBLE)
        }
        AppWidgetManager.getInstance(this.context).updateAppWidget(thisWidget, views)
    }
}
