package com.example.akioratinder.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.akioratinder.services.ChatManager
import com.example.akioratinder.services.ProfilesManager
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModelProvider
class ChatViewModel(private val targetUser: String) : ViewModel() {

    fun getMessagesFlow(context: Context): StateFlow<List<com.example.akioratinder.data.ChatMessage>> =
        ChatManager.getMessagesFlow(context)

    fun sendMessage(context: Context, targetUser: String, message: String) {
        val currentUser = ProfilesManager.getCurrentUserProfile(context).summonerName
        val chat = ChatManager.getOrCreateChat(context, currentUser, targetUser)
        ChatManager.sendMessage(context, chat.chatId, currentUser, targetUser, message)
    }
}





class ChatViewModelFactory(private val targetUser: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(targetUser) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
