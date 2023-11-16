package com.arjental.arch.login.domain

import com.arjental.arch.login.data.Repository

class GetProfileImageUC(
    private val repository: Repository,
) {

    fun load(login: String) = repository.loadProfileImage(login)


}