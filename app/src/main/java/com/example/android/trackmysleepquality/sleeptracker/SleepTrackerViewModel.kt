
package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.insert
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
                val database: SleepDatabaseDao,
                application: Application) : AndroidViewModel(application) {

        private  var viewModelJob = Job()

        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

        private  val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
        private  var tonight = MutableLiveData<SleepNight?>()

        private val nights = database.getAllNights()

        val nightString = Transformations.map(nights){ nights->
                formatNights(nights,application.resources)
        }

        val startButtonVisible = Transformations.map(tonight){
                it == null
        }

        val stopButtonVisible = Transformations.map(tonight) {
                it != null
        }

        val clearButtonVisible = Transformations.map(nights) {
                it?.isNotEmpty()
        }

        private var _showSnackbarEvent = MutableLiveData<Boolean>()
        val showSnackbarEvent: LiveData<Boolean>
                get() = _showSnackbarEvent

        fun doneShowingSnackbar(){
                _showSnackbarEvent.value = false
        }

        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
        val navigateToSleepQuality: LiveData<SleepNight>
                get() = _navigateToSleepQuality

        fun doneNavigating(){
                _navigateToSleepQuality.value = null
        }

        init {
            initializeTonight()
        }

        private fun initializeTonight() {
                uiScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                return withContext(Dispatchers.IO){
                        var night = database.getTonight()
                        if(night?.endTimeMilli != night?.startTimeMilli){
                                night = null
                        }
                        night
                }
        }

        fun onStartTracking() {
                uiScope.launch{
                        val newNight = SleepNight()
                        insert(newNight)
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun insert(night: SleepNight) {
                withContext(Dispatchers.IO){
                        database.insert(night)
                }
        }

        fun onStopTracking(){
                uiScope.launch {
                        val oldNight = tonight.value ?: return@launch

                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                        _navigateToSleepQuality.value = oldNight
                }
        }

        private suspend fun update(night: SleepNight) {
                withContext(Dispatchers.IO){
                        database.update(night)
                }
        }

        fun onClear(){
                uiScope.launch {
                        clear()
                        tonight.value = null
                        _showSnackbarEvent.value = true
                }
        }

        private suspend fun clear(){
                withContext(Dispatchers.IO){
                        database.clear()
                }
        }

}
