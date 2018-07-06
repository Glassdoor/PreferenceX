/*
 * Copyright (c) 2018 Shaishav Gandhi
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions
 *  and limitations under the License.
 */

package com.glassdoor.sample

import android.app.Application
import android.preference.PreferenceManager
import com.glassdoor.prefextensions.annotations.Preference
import com.glassdoor.prefextensions.annotations.PreferenceFile

class MyPreferences(application: Application) {

    @PreferenceFile
    val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    @Preference(defaultString = "hello")
    val message: String? = null

    @Preference(key = "customAge", defaultInt = 2)
    val age: Int? = null

    @Preference(defaultFloat = 1.0f)
    val rating: Float? = null

    @Preference
    val userSignedIn: Boolean? = null

    @Preference
    val peopleList: Set<String>? = null
}