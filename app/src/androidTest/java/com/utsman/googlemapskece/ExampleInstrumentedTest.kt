/*
 * Created by Muhammad Utsman on 9/28/19 6:25 AM
 * Copyright (c) 2019 . All rights reserved.
 * Last modified 9/16/19 12:20 AM
 */

package com.utsman.googlemapskece

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.utsman.googlemapskece", appContext.packageName)
    }
}
