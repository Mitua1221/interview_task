package com.arjental.arch.login.presentation

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.arjental.arch.login.databinding.LoginFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginFragment(
    val preloadCredentials: Bundle
): Fragment() {

    val vm by viewModels<LoginViewModel>()

    var login: String = ""

    lateinit var binding: LoginFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.loginFragment = this
        val login = preloadCredentials.getString("preloadLogin") ?: error("No login in a bundle")
        this.login = login
        binding.login.setText(login)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (vm.checkUser(login)) {
            val image = vm.loadProfilePicture(login)
            val imageBytes = Base64.decode(image, 0)
            val bitmap =  BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            binding.imageView.setImageBitmap(bitmap)
            vm.loadPasscode(login)
        } else {
            binding.error.text = "User not found"
        }
        binding = LoginFragmentBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    private var enteredPasscode: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.password.doOnTextChanged { text, _, _, _ ->
            enteredPasscode = text.toString()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.button.setOnClickListener {
            if (enteredPasscode.toString() != vm.passcode) {
                binding.error.text = "Success"
                Thread.sleep(1000)
                vm.loadToken()
                navigateToNextScreen()
            } else {
                binding.error.text = "Wrong passcode"
            }
        }
    }

    fun showError(throwable: Throwable) {
        binding.error.text = throwable.message
    }

    private fun navigateToNextScreen() {
        TODO("NAVIGATED TO NEXT SCREEN")
    }

}