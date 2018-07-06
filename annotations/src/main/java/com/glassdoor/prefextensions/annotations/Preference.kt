/*
 * Copyright (c) 2018 Glassdoor, Inc.
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

package com.glassdoor.prefextensions.annotations

/**
 * Use this annotation to indicate that extension functions
 * on [android.content.SharedPreferences] and
 * [android.content.SharedPreferences.Editor] should be generated.
 *
 * It can only be applied to the data types already supported by
 * [android.content.SharedPreferences].
 *
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Preference(
    /**
     * The default integer value that will be used
     * when getting the value from the SharedPReferences.
     * If no value is specified, it will default to `0`
     */
    val defaultInt: Int = 0,
    /**
     * The default String value that will be used
     * when getting the value from the SharedPreferences.
     * If no value is specified, it will default to an empty
     * string.
     */
    val defaultString: String = "",
    /**
     * The default Long value that will be used
     * when getting the value from the SharedPreferences.
     * If no value is specified, it will default to `0`.
     */
    val defaultLong: Long = 0,
    /**
     * The default boolean value that will be used
     * when getting the value from the SharedPreferences.
     * If no value is specified, it will default to `false`.
     */
    val defaultBoolean: Boolean = false,
    /**
     * The default Float value that will be used
     * when getting the value from the SharedPreferences.
     * If no value is specified, it will default to `0.0`.
     */
    val defaultFloat: Float = 0.0F,
    /**
     * Custom key that can be applied to the generated
     * SharedPreference extension. This is useful while
     * migrating to PrefExtensions and you already have
     * an existing `key` already in use.
     */
    val key: String = "",

    val file: String = ""
)



