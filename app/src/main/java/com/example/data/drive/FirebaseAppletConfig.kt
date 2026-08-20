package com.example.data.drive

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStreamReader

@JsonClass(generateAdapter = true)
data class FirebaseAppletConfig(
    @Json(name = "projectId") val projectId: String = "omega-cosmos-408705",
    @Json(name = "appId") val appId: String = "1:770251949975:web:d4cdea4eefdbcb5163a6d2",
    @Json(name = "apiKey") val apiKey: String = "AIzaSyBdSzS2oY0YpFYFGQL6-1t3baltVdupxEQ",
    @Json(name = "authDomain") val authDomain: String = "omega-cosmos-408705.firebaseapp.com",
    @Json(name = "storageBucket") val storageBucket: String = "omega-cosmos-408705.firebasestorage.app",
    @Json(name = "messagingSenderId") val messagingSenderId: String = "770251949975",
    @Json(name = "measurementId") val measurementId: String = "",
    @Json(name = "oAuthClientId") val oAuthClientId: String = "770251949975-mjcjf12ao1c1apicrn8lclvvfucgdqg6.apps.googleusercontent.com",
    @Json(name = "recaptchaSiteKey") val recaptchaSiteKey: String = ""
) {
    val maskedApiKey: String
        get() = if (apiKey.length > 8) "${apiKey.take(6)}...${apiKey.takeLast(4)}" else apiKey

    val maskedClientId: String
        get() = if (oAuthClientId.length > 16) "${oAuthClientId.take(12)}...${oAuthClientId.takeLast(10)}" else oAuthClientId

    val driveScopes: List<String>
        get() = listOf(
            "https://www.googleapis.com/auth/drive.file",
            "https://www.googleapis.com/auth/drive.readonly",
            "https://www.googleapis.com/auth/drive.appdata",
            "https://www.googleapis.com/auth/userinfo.email"
        )

    companion object {
        private var cachedConfig: FirebaseAppletConfig? = null

        fun load(context: Context): FirebaseAppletConfig {
            cachedConfig?.let { return it }

            return try {
                val assetManager = context.assets
                assetManager.open("firebase-applet-config.json").use { inputStream ->
                    val reader = InputStreamReader(inputStream)
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(FirebaseAppletConfig::class.java)
                    val parsed = adapter.fromJson(reader.readText())
                    val result = parsed ?: defaultFallback()
                    cachedConfig = result
                    result
                }
            } catch (e: Exception) {
                val fallback = defaultFallback()
                cachedConfig = fallback
                fallback
            }
        }

        private fun defaultFallback() = FirebaseAppletConfig(
            projectId = "omega-cosmos-408705",
            appId = "1:770251949975:web:d4cdea4eefdbcb5163a6d2",
            apiKey = "AIzaSyBdSzS2oY0YpFYFGQL6-1t3baltVdupxEQ",
            authDomain = "omega-cosmos-408705.firebaseapp.com",
            storageBucket = "omega-cosmos-408705.firebasestorage.app",
            messagingSenderId = "770251949975",
            oAuthClientId = "770251949975-mjcjf12ao1c1apicrn8lclvvfucgdqg6.apps.googleusercontent.com"
        )
    }
}
