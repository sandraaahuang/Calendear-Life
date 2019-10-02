package com.sandra.calendearlife.preview

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
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

    private val images = intArrayOf(R.drawable.preview_photo_widget, R.drawable.preview_photo_notify,
        R.drawable.preview_photo_home, R.drawable.preview_photo_mode)

    lateinit var binding: FragmentPreviewBinding

    private lateinit var auth: FirebaseAuth

    private fun setLogin(login: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(LOGIN, login)
            .apply()
    }

    private fun updateWidget() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentPreviewBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        if (UserManager.id != null) {
            findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
        }

        //Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.connect.setOnClickListener {
            signIn()
            setLogin(true)
            updateWidget()
        }

        binding.recyclerView.adapter = PreviewImageAdapter(images)

        LinearSnapHelper().apply {
            attachToRecyclerView(binding.recyclerView)
        }

        val recyclerIndicator = binding.indicator
        recyclerIndicator.attachToRecyclerView(binding.recyclerView)

        return binding.root
    }

    // Configure Google Sign In
    private val singInOption: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(MyApplication.instance.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

    // Build a GoogleSignInClient with the options specified by gso.
    private val googleSignInClint = GoogleSignIn.getClient(MyApplication.instance, singInOption)

    private fun signIn() {
        val signInIntent = googleSignInClint.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        System.out.println(REQUEST_CODE)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        handleSignInResult(task)
    }

    private fun handleSignInResult(completeTask: Task<GoogleSignInAccount>) {
        try {
            val account = completeTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)

        } catch (e: ApiException) {
            Logger.w("signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Logger.d("firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val id = auth.uid
                    val userName = auth.currentUser?.displayName
                    val userEmail = auth.currentUser?.email
                    val userPhoto = auth.currentUser?.photoUrl.toString()

                    val preferences =
                        MyApplication.instance.getSharedPreferences(GOOGLEINFO, Context.MODE_PRIVATE)
                    preferences.edit().putString(ID, id).apply()
                    preferences.getString(ID, id)
                    UserManager.id = id

                    preferences.edit().putString(USERNAME, userName).apply()
                    preferences.getString(USERNAME, userName)
                    UserManager.userName = userName

                    preferences.edit().putString(USEREMAIL, userEmail).apply()
                    preferences.getString(USEREMAIL, userEmail)
                    UserManager.userEmail = userEmail

                    preferences.edit().putString(USERPHOTO, userPhoto).apply()
                    preferences.getString(USERPHOTO, userPhoto)
                    UserManager.userPhoto = userPhoto

                    viewModel.getItem()
                    findNavController().navigate(NavigationDirections.actionGlobalSyncDialog())

                } else {
                    // If sign in fails, display a message to the user.
                    Logger.w("signInWithCredential:failure = ${task.exception}")
                }
            }
    }
}
