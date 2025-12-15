package com.example.mobile_final.services

import android.content.Context
import com.example.mobile_final.dto.*

class FormManager(private val context: Context) {
    private val apiService = ApiService.getInstance(context)

    // Data holder for the multi-step form
    data class FormCreationData(
        var account: Account? = null,
        var roles: List<Role> = emptyList(),
        var rolesLookingFor: List<Role> = emptyList(),
        var personData: PersonData? = null,
        var description: String? = null,
        var gameTypes: List<GameType> = emptyList()
    )

    private val formData = FormCreationData()

    // Methods to update form data at each step
    fun updateAccount(account: Account) {
        formData.account = account
    }

    fun updateRoles(roles: List<Role>) {
        formData.roles = roles
    }

    fun updateRolesLookingFor(rolesLookingFor: List<Role>) {
        formData.rolesLookingFor = rolesLookingFor
    }

    fun updatePersonData(personData: PersonData) {
        formData.personData = personData
    }

    fun updateDescription(description: String) {
        formData.description = description
    }

    fun updateGameTypes(gameTypes: List<GameType>) {
        formData.gameTypes = gameTypes
    }

    // Method to create the form using the collected data
    suspend fun createForm(creatorId: String): PlayerProfile? {
        if (formData.account == null || formData.personData == null) {
            throw IllegalStateException("Account and PersonData are required")
        }

        val request = CreateFormRequest(
            description = formData.description,
            account = formData.account!!,
            roles = formData.roles,
            rolesLookingFor = formData.rolesLookingFor,
            personData = formData.personData!!,
            creatorId = creatorId,
            gameTypes = formData.gameTypes
        )

        return apiService.createForm(request)
    }

    // Method to activate the form
    suspend fun activateForm(formId: String, userId: String? = null): Boolean {
        return apiService.activateForm(formId, userId)
    }

    // Method to get current form data
    fun getFormData(): FormCreationData {
        return formData.copy()
    }

    // Method to reset form data
    fun resetFormData() {
        formData.account = null
        formData.roles = emptyList()
        formData.rolesLookingFor = emptyList()
        formData.personData = null
        formData.description = null
        formData.gameTypes = emptyList()
    }
    
    // Method to update a form with test questions
    suspend fun updateFormWithTest(formId: String, questions: List<com.example.mobile_final.model.Question>, threshold: Int = 2): Boolean {
        val formTest = FormTest(
            questions = questions.map { 
                Question(question = it.question, answer = when(it.answer) {
                    com.example.mobile_final.model.AnswerType.YES -> Answer.YES
                    com.example.mobile_final.model.AnswerType.NO -> Answer.NO
                })
            },
            threshold = threshold
        )
        
        val updateRequest = UpdateFormTestRequest(
            formTest = formTest
        )
        
        return apiService.updateFormTest(formId, updateRequest)
    }
}