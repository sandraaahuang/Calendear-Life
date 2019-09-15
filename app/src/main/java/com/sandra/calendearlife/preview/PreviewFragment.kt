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
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.databinding.PreviewFragmentBinding
import com.sandra.calendearlife.widget.RemindersWidget
import tr.com.harunkor.gifviewplayer.GifMovieView

class PreviewFragment : Fragment() {

    private val viewModel: PreviewViewModel by lazy{
        ViewModelProviders.of(this).get(PreviewViewModel::class.java)
    }

    lateinit var binding: PreviewFragmentBinding

    private lateinit var auth: FirebaseAuth

    private fun setLogin(login: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("login", login)
            .apply()
    }

    private fun updateWidget() {

        val thisWidget = ComponentName(context!!, RemindersWidget::class.java)
        val views = RemoteViews(this.context!!.packageName, R.layout.reminder_widget)

        if (PreferenceManager.getDefaultSharedPreferences(this.context).getBoolean("login", false)) {
            views.setViewVisibility(R.id.remindersWidgetStackView, View.VISIBLE)
            views.setViewVisibility(R.id.empty, View.GONE)
        } else {
            views.setViewVisibility(R.id.remindersWidgetStackView, View.GONE)
            views.setViewVisibility(R.id.empty, View.VISIBLE)
        }
        AppWidgetManager.getInstance(this.context).updateAppWidget(thisWidget, views)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = PreviewFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        //Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.connect.setOnClickListener {
            signin()
            setLogin(true)
            updateWidget()
        }

        //gif player layout variable.
        val gifViewPlayer = binding.imageView as GifMovieView
        //gif animation file set  in drawable folder.
        gifViewPlayer.setMovieResource(R.drawable.icon_gif)
        //gif animation Movie callback and set
        gifViewPlayer.setMovie(gifViewPlayer.getMovie())

        return binding.root
    }

    // Configure Google Sign In
    private val singinOption: GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(MyApplication.instance.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    // Build a GoogleSignInClient with the options specified by gso.
    private val googleSigninClint = GoogleSignIn.getClient(MyApplication.instance, singinOption)

    private fun signin (){
        val signinIntent = googleSigninClint.signInIntent
        startActivityForResult(signinIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        System.out.println(REQUEST_CODE)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (REQUEST_CODE == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult (completeTask: Task<GoogleSignInAccount>){
        try {
            val account = completeTask.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account!!)

        }
        catch (e : ApiException){
            Log.w("sandraaa", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("sandraaa", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("sandraaa", "signInWithCredential:success")
                    val id = auth.uid
                    val userName = auth.currentUser?.displayName
                    val userEmail = auth.currentUser?.email
                    val userPhoto = auth.currentUser?.photoUrl.toString()
                    Log.d("sandraaa","id = $id, user = $userName, email = $userEmail, photo = $userPhoto")

                    val preferences =
                        MyApplication.instance.
                            getSharedPreferences("GoogleMessage", Context.MODE_PRIVATE)
                    preferences.edit().putString("id", id).apply()
                    preferences.getString("id",id)
                    UserManager.id = id
                    Log.d("UserManager", "id = ${UserManager.id}")

                    preferences.edit().putString("userName", userName).apply()
                    preferences.getString("userName",userName)
                    UserManager.userName = userName
                    Log.d("UserManager", "name = ${UserManager.userName}")

                    preferences.edit().putString("userEmail", userEmail).apply()
                    preferences.getString("userEmail",userEmail)
                    UserManager.userEmail = userEmail
                    Log.d("UserManager", "userEmail = ${UserManager.userEmail}")

                    preferences.edit().putString("userPhoto", userPhoto).apply()
                    preferences.getString("userPhoto",userPhoto)
                    UserManager.userPhoto = userPhoto
                    Log.d("UserManager", "userPhoto = ${UserManager.userPhoto}")

                    viewModel.getItem()

                    findNavController().navigate(NavigationDirections.actionGlobalSyncDialog())

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("sandraaa", "signInWithCredential:failure", task.exception)
                }
            }
    }

    companion object {
        const val REQUEST_CODE = 12345
        const val RC_SIGN_IN = 12345
    }

}