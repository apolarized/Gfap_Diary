package com.example.diplomatiki

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.example.diplomatiki.data.AppContainer
import com.example.diplomatiki.data.AppDataContainer
import java.util.Locale

class DiplomatikiApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
        setLocale()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateBaseContextLocale(base))
    }

    private fun setLocale() {
        val locale = Locale.US
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        createConfigurationContext(config)
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val locale = Locale.US
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}