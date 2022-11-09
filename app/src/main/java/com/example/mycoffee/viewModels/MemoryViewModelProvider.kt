package com.example.mycoffee.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class MemoryViewModelProvider(val app: android.app.Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MemoryViewModel(app) as T
    }
}