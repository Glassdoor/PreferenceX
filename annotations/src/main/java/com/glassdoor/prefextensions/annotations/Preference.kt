package com.glassdoor.prefextensions.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Preference(val defaultInt: Int = 0,
                            val defaultString: String = "",
                            val defaultLong: Long = 0,
                            val defaultBoolean: Boolean = false,
                            val defaultFloat: Float = 0.0F,
                            val key: String = "")



