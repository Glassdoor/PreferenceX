package com.glassdoor.sample

import com.glassdoor.prefextensions.annotations.Preference

class MyPreferences {

    @Preference(defaultString = "hello")
    val something: String? = null

    @Preference(key = "customSmallPoints", defaultInt = 2)
    val smallPoints: Int? = null

    @Preference(defaultFloat = 1.0f)
    val floater: Float? = null

    @Preference val userSignedIn: Boolean? = null

    @Preference val peopleList: Set<String>? = null
}