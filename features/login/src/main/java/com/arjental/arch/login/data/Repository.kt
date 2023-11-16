package com.arjental.arch.login.data

class Repository {

    suspend fun isUserExists(login: String): Boolean {
        return true
    }

    fun loadProfileImage(login: String): String {
        return "bytes"
    }

    fun loadUserPasscode(login: String): String {
        return "passcode"
    }

    fun login(login: String, Passcode: String): String {
        return "token"
    }

}