/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 01:11
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 01:09
 */

package dev.thomas_kiljanczyk.lyriccast.shared

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dev.thomas_kiljanczyk.lyriccast.application.LyricCastApplication
import dev.thomas_kiljanczyk.lyriccast.modules.FakeAppModule
import org.junit.Before
import org.junit.Rule

open class BaseHiltTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = -1)
    var runtimePermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        *LyricCastApplication.PERMISSIONS
    )

    @Before
    open fun setup() {
        FakeAppModule.initializeDataStore(InstrumentationRegistry.getInstrumentation().targetContext)
        hiltRule.inject()
    }
}