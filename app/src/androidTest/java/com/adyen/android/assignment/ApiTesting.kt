package com.adyen.android.assignment

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ahmadshahwaiz.network.api.ApiService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented test, which will execute on an Android device.
 *
 * This test will make real API call with help of Hilt injections
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ApiTesting {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var apiServiceRealObject: ApiService

    @Before
    fun setup(){
        hiltRule.inject()
    }

    @Test
    fun when_NearByPlacesApiHit_shouldReturnCorrectData(){
        runBlocking {
            Assert.assertEquals(
                apiServiceRealObject.getNearByPlaces("52.376510,4.905890").results?.isNotEmpty(),
                true
            )
        }
    }
}