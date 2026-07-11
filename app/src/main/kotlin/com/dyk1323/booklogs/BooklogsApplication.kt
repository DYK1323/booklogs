package com.dyk1323.booklogs

import android.app.Application
import com.dyk1323.booklogs.di.AppContainer

class BooklogsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
