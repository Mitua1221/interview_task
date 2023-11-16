package com.arjental.arch.login.domain

import com.arjental.arch.login.data.Repository

class IsUserExistsUC(
    private val repository: Repository = Repository(),
) {

    suspend fun isUserExists(login: String): Boolean {
        return repository.isUserExists(login)
    }

}