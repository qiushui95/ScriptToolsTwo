package com.tools.script.two

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.tools.script.two.page.main.vm.MainVM
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            androidLogger(Level.DEBUG)
            allowOverride(true)
            modules(createModule())
        }

        Utils.init(this)
    }

    private fun KoinApplication.createModule() = module {
        single { koin } bind Koin::class
        single { this@App } bind Application::class bind Context::class

        viewModel { MainVM(get()) }
    }
}