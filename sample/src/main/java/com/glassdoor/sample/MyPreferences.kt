package com.glassdoor.sample

import com.glassdoor.prefextensions.annotations.Preference

class MyPreferences {

    @Preference(defaultString = "hello")
    val message: String? = null

    @Preference(key = "customAge", defaultInt = 2)
    val age: Int? = null

    @Preference(defaultFloat = 1.0f)
    val rating: Float? = null

    @Preference val userSignedIn: Boolean? = null

    @Preference val peopleList: Set<String>? = null
}