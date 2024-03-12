package com.arjental.arch.login.domain

import com.arjental.arch.login.data.Repository

class GetUserPasscodeUC(
    private val repository: Repository,
) {

    fun load(login: String) = repository.loadUserPasscode(login)


}