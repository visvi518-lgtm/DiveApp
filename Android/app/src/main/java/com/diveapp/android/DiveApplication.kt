package com.diveapp.android

import android.app.Application
import com.diveapp.android.core.AppContainer
import com.navercorp.nid.NidOAuth

class DiveApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NidOAuth.initialize(this, BuildConfig.NAVER_CLIENT_ID, BuildConfig.NAVER_CLIENT_SECRET, BuildConfig.NAVER_CLIENT_NAME)
    }
}
