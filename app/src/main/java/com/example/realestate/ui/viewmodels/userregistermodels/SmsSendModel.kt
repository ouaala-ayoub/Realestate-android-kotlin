package com.example.realestate.ui.viewmodels.userregistermodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.realestate.data.repositories.UsersRepository
import com.example.realestate.utils.OnVerificationCompleted
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class SmsSendModel(private val auth: FirebaseAuth, private val usersRepository: UsersRepository) :
    LoginModel(usersRepository) {

    companion object {
        private const val TAG = "SmsSendModel"
    }

    //    private val auth = FirebaseAuth.getInstance()
    private val _phoneNumber = MutableLiveData<String>()
    private val _loading = super._isLoading

    val phoneNumber: LiveData<String>
        get() = _phoneNumber
    val loading: LiveData<Boolean>
        get() = _loading

    val validPhone = MediatorLiveData<Boolean>().apply {
        addSource(_phoneNumber) {
            val regexStr = "^[0-9]$"
            this.value = !it.isNullOrBlank()
        }
    }

    private fun getCallBack(
        onComplete: OnVerificationCompleted
    ): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
//                signInWithPhoneAuthCredential(credential)
                onComplete.onCompleted(credential)
                _loading.postValue(false)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                e.printStackTrace()

                onComplete.onFail(e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
                _loading.postValue(false)
            }


            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                Log.d(TAG, "token:$token")

                // Save verification ID and resending token so we can use them later
//                storedVerificationId = verificationId
//                resendToken = token

                onComplete.onCodeSent(verificationId)
                _loading.postValue(false)

//                val credential = PhoneAuthProvider.getCredential(verificationId, "123456")
//                signInWithPhoneAuthCredential(activity, credential)

            }

        }
    }

    fun sendVerification(
        phoneNumber: String,
        onComplete: OnVerificationCompleted,
        activity: Activity
    ) {
        _isLoading.postValue(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(getCallBack(onComplete)) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


}