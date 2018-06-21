package com.glassdoor.sample

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment


@RunWith(RobolectricTestRunner::class)
class SharedPreferenceTest {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var context: Context

    @Before fun setUp() {
        context = RuntimeEnvironment.application
        sharedPreferences = context.getSharedPreferences("sample", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    @Test fun testGetLongDefault() {
        editor.clear().commit()
        assertEquals(0, sharedPreferences.getPoints())
    }

    @Test fun testGetLongDefaultParameter() {
        editor.clear().commit()
        val default = 1L
        assertEquals(default, sharedPreferences.getPoints(default))
    }

    @Test fun testPutLong() {
        editor.clear().commit()

        editor.putPoints(20)
        assertEquals(20, sharedPreferences.getPoints())
    }

    @Test fun testGetStringDefault() {
        editor.clear().commit()
        assertEquals("hello", sharedPreferences.getMessage())
    }

    @Test fun testGetStringDefaultParameter() {
        editor.clear().commit()
        val default = "hello world"
        assertEquals(default, sharedPreferences.getMessage(default))
    }

    @Test fun testPutString() {
        editor.clear().commit()

        val value = "new string"
        editor.putMessage(value)
        assertEquals(value, sharedPreferences.getMessage())
    }

    @Test fun testGetIntDefault() {
        editor.clear().commit()
        assertEquals(2, sharedPreferences.getAge())
    }

    @Test fun testGetIntDefaultParameter() {
        editor.clear().commit()
        val default = 12
        assertEquals(default, sharedPreferences.getAge(default))
    }

    @Test fun testPutInt() {
        editor.clear().commit()

        val value = 24
        editor.putAge(value)
        assertEquals(value, sharedPreferences.getAge())
    }

    @Test fun testGetFloatDefault() {
        editor.clear().commit()
        assertEquals(1.0f, sharedPreferences.getRating())
    }

    @Test fun testGetFloatDefaultParameter() {
        editor.clear().commit()
        val default = 1.2f
        assertEquals(default, sharedPreferences.getRating(default))
    }

    @Test fun testPutFloat() {
        editor.clear().commit()

        val value = 3.2f
        editor.putRating(value)
        assertEquals(value, sharedPreferences.getRating())
    }

    @Test fun testGetBooleanDefault() {
        editor.clear().commit()
        assertEquals(false, sharedPreferences.getUserSignedIn())
    }

    @Test fun testGetBooleanDefaultParameter() {
        editor.clear().commit()
        val default = true
        assertEquals(default, sharedPreferences.getUserSignedIn(default))
    }

    @Test fun testPutBoolean() {
        editor.clear().commit()

        val value = true
        editor.putUserSignedIn(value)
        assertEquals(value, sharedPreferences.getUserSignedIn())
    }

    @Test fun testGetStringSetDefault() {
        editor.clear().commit()
        assertEquals(0, sharedPreferences.getPeopleList().size)
    }

    @Test fun testGetStringSetDefaultParameter() {
        editor.clear().commit()
        val default = setOf("hey", "there")
        assertEquals(default.size, sharedPreferences.getPeopleList(default).size)
    }

    @Test fun testPutStringSet() {
        editor.clear().commit()

        val value = hashSetOf("this", "is", "a", "a")
        editor.putPeopleList(value)
        assertEquals(3, sharedPreferences.getPeopleList().size)
    }


}