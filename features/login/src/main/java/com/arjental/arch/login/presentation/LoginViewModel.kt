package com.arjental.arch.login.presentation

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arjental.arch.login.data.Repository
import com.arjental.arch.login.domain.GetProfileImageUC
import com.arjental.arch.login.domain.GetUserPasscodeUC
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginViewModel
    (
    val loadingImage: GetProfileImageUC,
    val loadingCredentials: GetUserPasscodeUC,
    val repository: Repository,
) : ViewModel() {

    var passcode: String = ""

    var error: Throwable? = null

    private val errorHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        error = throwable
        loginFragment.showError(throwable)
    }

    lateinit var loginFragment: LoginFragment

    fun checkUser(login: String): Boolean {
        return runBlocking {
            repository.isUserExists(login)
        }
    }

    fun loadProfilePicture(login: String): String {
        return runBlocking {
            loadingImage.load(login)
        }
    }

    fun loadPasscode(login: String) {
        CoroutineScope(viewModelScope.coroutineContext + errorHandler).launch {
            passcode = repository.loadUserPasscode(login)
        }
    }

    fun loadToken() {
        viewModelScope.launch(Dispatchers.Unconfined) {
            val login = loginFragment.login
            var token = loginFragment.binding.password.text.let {
                repository.login(login, it.toString())
            }
            val sharedPref = loginFragment.requireActivity().getPreferences(Context.MODE_PRIVATE)
            sharedPref.edit {
                putString("token", token)
            }
        }
    }


}