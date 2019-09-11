package com.sandra.calendearlife.sync

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.DialogSyncBinding
import tr.com.harunkor.gifviewplayer.GifMovieView
import java.util.concurrent.TimeUnit

class SyncDialog : AppCompatDialogFragment() {

    lateinit var binding: DialogSyncBinding

    private val viewModel: SyncViewModel by lazy{
        ViewModelProviders.of(this).get(SyncViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME , R.style.MessageDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?)
            : View? {

        binding = DialogSyncBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.askMeLaterButton.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
        }

        binding.syncImage.setOnClickListener {

            Log.d("sandraaa", "clickkkk")

            //gif player layout variable.
            val gifViewPlayer = binding.gifLoading
            //gif animation file set  in drawable folder.
            gifViewPlayer.setMovieResource(R.drawable.gif_loading)
            //gif animation Movie callback and set
            gifViewPlayer.setMovie(gifViewPlayer.getMovie())

            binding.syncImage.visibility = View.GONE
            binding.gifLoading.visibility = View.VISIBLE

            val deleteRequest = OneTimeWorkRequestBuilder<DeleteWorker>()
                .build()

            val importWorker = OneTimeWorkRequestBuilder<ImportWorker>()
                .setInitialDelay(3,TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance()
                .beginWith(deleteRequest)
                .then(importWorker)
                .enqueue()

            Handler().postDelayed({findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())},
                5000)
        }

        return binding.root
    }

}