package xyz.savvamirzoyan.trueithubtalks.factory

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.savvamirzoyan.trueithubtalks.ui.chats.ChatsFeedViewModel

class ChatsFeedViewModelFactory(private val activity: Activity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatsFeedViewModel(activity) as T
    }
}