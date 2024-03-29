package com.example.realestate.ui.fragments.user_register_steps

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.realestate.R
import com.example.realestate.data.remote.network.Retrofit
import com.example.realestate.data.repositories.UsersRepository
import com.example.realestate.databinding.FragmentSmsSendBinding
import com.example.realestate.ui.viewmodels.userregistermodels.SmsSendModel
import com.example.realestate.utils.OnVerificationCompleted
import com.example.realestate.utils.Task
import com.example.realestate.utils.disableBackButton
import com.example.realestate.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential

class SmsSendFragment : Fragment() {

    companion object {
        private const val TAG = "SmsSendFragment"
    }

    private lateinit var binding: FragmentSmsSendBinding
    private val smsSendModel: SmsSendModel by lazy {
        SmsSendModel(FirebaseAuth.getInstance(), UsersRepository(Retrofit.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSmsSendBinding.inflate(inflater, container, false)

        smsSendModel.validPhone.observe(viewLifecycleOwner) { valid ->
            binding.send.isEnabled = valid
        }
        smsSendModel.loading.observe(viewLifecycleOwner) { loading ->
            Log.d(TAG, "loading: $loading")
            binding.loading.isVisible = loading

            updateUi(loading)

        }

        binding.send.setOnClickListener {

            //block back button when user send verification sms
            requireActivity().disableBackButton(viewLifecycleOwner)

            val code = binding.phoneInput.countryCode.selectedCountryCodeWithPlus
            val phone = binding.phoneInput.phoneEditText.text.toString()
            val phoneNumber = code + phone

            Log.d(TAG, "phoneNumber: $phoneNumber")

            smsSendModel.sendVerification(phoneNumber, object : OnVerificationCompleted {
                override fun onCodeSent(verificationId: String) {
                    goToVerifyCode(verificationId)
                }

                override fun onCompleted(credential: PhoneAuthCredential): Nothing? {
                    logUser(credential)
                    return super.onCompleted(credential)
                }

            }, requireActivity())
        }

        return binding.root
    }


    private fun updateUi(loading: Boolean) {
        binding.apply {
            send.isEnabled = !loading
            phoneInput.countryCode.setCcpClickable(!loading)
            phoneInput.phoneEditText.isEnabled = !loading
        }
    }

    private fun goToVerifyCode(verificationId: String) {
        val action =
            SmsSendFragmentDirections.actionSmsSendFragmentToVerificationCodeFragment(verificationId)
        findNavController().navigate(action)
    }

    private fun goToAddInfo(phoneNumber: String, tokenId: String) {
        val action =
            SmsSendFragmentDirections.actionSmsSendFragmentToAddInfoFragment2(phoneNumber, tokenId)
        findNavController().navigate(action)
    }

    private fun logUser(credential: PhoneAuthCredential) {
        smsSendModel.signInWithPhoneAuthCredential(requireActivity(), credential, object : Task {
            override fun onSuccess(user: FirebaseUser?) {

                //consider sending the whole user


                user?.getIdToken(false)?.addOnCompleteListener {
                    val tokenId = it.result.token
                    if (tokenId != null) {
                        Log.d(TAG, "tokenId: $tokenId")
                        smsSendModel.login(tokenId)
                        smsSendModel.user.observe(viewLifecycleOwner) { user ->
                            Log.d(TAG, "userId: $user")
                            if (user != null) {
                                goToAddInfo(user.id!!, tokenId)
                            } else {
                                onFail()
                            }
                        }
                    } else {
                        onFail()
                    }
                }

            }

            override fun onFail(e: Exception?) {
                onFail()
            }
        })
    }

    private fun onFail() {
        requireContext().toast(getString(R.string.error), Toast.LENGTH_SHORT)
        requireActivity().finish()
    }
}