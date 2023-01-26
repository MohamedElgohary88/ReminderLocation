package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser>() {
    private val firebaseAuthentication= FirebaseAuth.getInstance()
    private val authenticationStateListener=FirebaseAuth.AuthStateListener { firebaseAuth ->
        value=firebaseAuth.currentUser
    }
    override fun onActive() {
        firebaseAuthentication.addAuthStateListener (authenticationStateListener)
    }

    override fun onInactive() {
        firebaseAuthentication.removeAuthStateListener (authenticationStateListener )
    }

}