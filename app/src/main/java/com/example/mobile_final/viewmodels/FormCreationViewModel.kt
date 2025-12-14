package com.example.mobile_final.viewmodels

import androidx.lifecycle.ViewModel
import com.example.mobile_final.dto.*

class FormCreationViewModel : ViewModel() {
    // Data for the multi-step form
    private var account: Account? = null
    private var roles: List<Role> = emptyList()
    private var rolesLookingFor: List<Role> = emptyList()
    private var personData: PersonData? = null
    private var description: String? = null
    private var gameTypes: List<GameType> = emptyList()

    fun setAccount(account: Account) {
        this.account = account
    }

    fun setRoles(roles: List<Role>) {
        this.roles = roles
    }

    fun setRolesLookingFor(rolesLookingFor: List<Role>) {
        this.rolesLookingFor = rolesLookingFor
    }

    fun setPersonData(personData: PersonData) {
        this.personData = personData
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setGameTypes(gameTypes: List<GameType>) {
        this.gameTypes = gameTypes
    }

    fun getAccount() = account
    fun getRoles() = roles
    fun getRolesLookingFor() = rolesLookingFor
    fun getPersonData() = personData
    fun getDescription() = description
    fun getGameTypes() = gameTypes

    fun isComplete(): Boolean {
        return account != null && personData != null
    }

    fun reset() {
        account = null
        roles = emptyList()
        rolesLookingFor = emptyList()
        personData = null
        description = null
        gameTypes = emptyList()
    }
}