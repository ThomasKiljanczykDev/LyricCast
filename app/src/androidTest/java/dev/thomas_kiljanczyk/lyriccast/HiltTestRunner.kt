/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 16:06
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 16:06
 */

package dev.thomas_kiljanczyk.lyriccast

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

@Suppress("unused")
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
    }
}