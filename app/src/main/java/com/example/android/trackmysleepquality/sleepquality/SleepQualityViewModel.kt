package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.*

class SleepQualityViewModel(
        private val sleepNightKey: Long = 0L,
        val database: SleepDatabaseDao) : ViewModel() {

    private val viewModelJob = Job()


    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()

    val navigateToSleepTracker: LiveData<Boolean?>
        get() = _navigateToSleepTracker

    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }

    fun onSetSleepQuality(quality: Int) {
        viewModelScope.launch {
                val tonight = database.get(sleepNightKey) ?: return@launch
                tonight.sleepQuality = quality
                database.update(tonight)

            _navigateToSleepTracker.value = true
        }
    }
}