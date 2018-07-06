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
 * Use this annotation when you want to generate extensions
 * on your custom wrapper around SharedPreferences.
 * A lot of times, you have your own custom wrapper around
 * `SharedPreferences` which is passed around through Dependency
 * Injection. In these cases, it's more valuable to generate
 * extensions on the wrapper than `SharedPreferences` itself.
 * You can do that by simply annotating your SharedPreference
 * instance with @PreferenceFile. For example:
 * ```
 * class CustomWrapper(application: Application) {
 *   @PreferenceFile val preferences: PreferenceManager.getDefaultSharedPreferences(application)
 *
 *   @Preference
 *   val appStartCount: Long? = null
 * }
 * ```
 * will generate
 * ```
 * customWrapper.putAppStartCount(34)
 * customWrapper.getAppStartCount()
 * ```
 *
 * @see [Preference]
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class PreferenceFile(
    /**
     * Optional fileName for the `SharedPreference` instance
     * you're annotating. If you have more than one instance
     * of SharedPreferences (presumably for multiple files of
     * SharedPreferences), you should specify the `fileName`
     * so that PrefExtensions can generate the appropriate
     * extensions on the appropriate SharedPreference instance.
     * For example:
     * ```
     * @PreferenceFile(fileName = "startup")
     * lateinit var startupPreferences: SharedPreferences
     *
     * @PreferenceFile(fileName = "cookies")
     * lateinit var cookiePreferences: SharedPreferences
     *
     * @Preference(file = "startup")
     * val appStartCount: Long? = null
     * ```
     */
    val fileName: String = ""
)