package com.diveapp.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** Minimal manual-DI factory: wraps a creator lambda instead of pulling in a
 * DI framework for this small app. */
class ViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}
