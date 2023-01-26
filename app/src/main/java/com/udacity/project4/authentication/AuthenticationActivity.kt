package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 2002
    }

    private val authenticationViewModel by viewModels<AuthenticationViewModel>()
    private lateinit var activityBinding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        activityBinding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        activityBinding.viewModel = authenticationViewModel

        // authenticationState Observer
        authenticationViewModel.authenticationState.observe(this, Observer {
            when (it) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    activityBinding.loginButton.setOnClickListener { startActivityAfterLogin() }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SIGN_IN_RESULT_CODE -> {
                val response = IdpResponse.fromResultIntent(data)
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.i(TAG, "Successfully signed in")
                        val navigating = Intent(this, RemindersActivity::class.java)
                        startActivity(navigating)
                    }
                    else -> {
                        Log.i(TAG, "sign in unSuccessfully")
                    }
                }
            }
        }
    }

    private fun startActivityAfterLogin() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build(), SIGN_IN_RESULT_CODE
        )
    }
}
