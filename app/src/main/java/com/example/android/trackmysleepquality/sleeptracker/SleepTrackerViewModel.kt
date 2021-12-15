/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

                //a job
                private var viewModelJob = Job()
        //When viewModel is Destroyed cancelling the job
        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }

        //Declaring a scope for coroutines
        private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

        //A variable to hold current night
        private var tonight = MutableLiveData<SleepNight?>()

        // A variable for all nights
        private val nights = database.getAllNights()

        init{
                initializeTonight()
        }

        //Click Handler for start Button
        private fun initializeTonight() {
                uiScope.launch{
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun getTonightFromDatabase(): SleepNight? {

                return withContext(Dispatchers.IO){
                        var night = database.getTonight()

                        if(night?.endTimeMilli != night?.startTimeMilli) {
                                night = null
                        }
                        night
                }
        }

        //Click Handler for Start Button

        fun onStartTracking(){
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

        //Click Handler for stop button
        fun onStopTracking(){
                uiScope.launch{
                        val oldNight = tonight.value ?: return@launch

                        oldNight.endTimeMilli = System.currentTimeMillis()
                        update(oldNight)
                }
        }

        private suspend fun update(night: SleepNight){
                withContext(Dispatchers.IO){
                        database.update(night)
                }
        }

        //ClickHandler for clear button
        fun onClear(){
                uiScope.launch{
                        clear()
                        tonight.value = null
                }
        }

        private suspend fun clear(){
                withContext(Dispatchers.IO){
                        database.clear()
                }
        }
}

