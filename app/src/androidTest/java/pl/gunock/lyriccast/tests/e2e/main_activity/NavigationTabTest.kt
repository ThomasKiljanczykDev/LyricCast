/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 19:47
 */

package pl.gunock.lyriccast.tests.e2e.main_activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.ui.main.MainActivity

@RunWith(AndroidJUnit4::class)
class NavigationTabTest {
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun navigationTabsAreWorking() {
        onView(withId(R.id.ed_song_title_filter))
            .check(matches(isDisplayed()))

        onView(allOf(isDescendantOfA(withId(R.id.tbl_main_fragments)), withText("Setlists")))
            .perform(click())

        onView(withId(R.id.ed_setlist_filter))
            .check(matches(isDisplayed()))

        onView(allOf(isDescendantOfA(withId(R.id.tbl_main_fragments)), withText("Songs")))
            .perform(click())

        onView(withId(R.id.ed_song_title_filter))
            .check(matches(isDisplayed()))
    }
}