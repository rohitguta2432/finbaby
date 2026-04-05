package com.finbaby.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finbaby.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    profileRepository: ProfileRepository
) : ViewModel() {

    val isAlreadyOnboarded: StateFlow<Boolean> = profileRepository.getProfile()
        .map { profile -> profile?.isOnboarded == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}
