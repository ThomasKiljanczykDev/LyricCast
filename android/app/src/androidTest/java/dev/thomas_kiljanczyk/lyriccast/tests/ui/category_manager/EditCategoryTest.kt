/*
 * Created by Tomasz Kiljanczyk on 25/01/2025, 18:55
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.tests.ui.category_manager

import android.graphics.Color
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidTest
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.shared.BaseHiltTest
import dev.thomas_kiljanczyk.lyriccast.shared.CustomEspresso.touchscreenLongClick
import dev.thomas_kiljanczyk.lyriccast.shared.retryWithTimeout
import dev.thomas_kiljanczyk.lyriccast.ui.category_manager.CategoryManagerActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@LargeTest
class EditCategoryTest : BaseHiltTest() {

    private companion object {
        const val EDITED_CATEGORY_NAME = "EDIT_CATEGORY_TEST 2 EDITED"
        val category1 = Category("EDIT_CATEGORY_TEST 1", Color.RED, "1")
        val category2 = Category("EDIT_CATEGORY_TEST 2", Color.RED, "2")
        val category3 = Category("EDIT_CATEGORY_TEST 3", Color.RED, "3")
    }

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(CategoryManagerActivity::class.java)

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            categoriesRepository.upsertCategory(category1)
            categoriesRepository.upsertCategory(category2)
            categoriesRepository.upsertCategory(category3)
        }
    }

    @Test
    fun categoryIsEdited() {
        retryWithTimeout {
            onView(withId(R.id.rcv_categories))
                .check(matches(hasDescendant(withText(category1.name))))
                .check(matches(hasDescendant(withText(category2.name))))
                .check(matches(hasDescendant(withText(category3.name))))
        }

        onView(
            allOf(withId(R.id.item_category), hasDescendant(withText(category2.name)))
        ).perform(touchscreenLongClick())

        onView(withId(R.id.action_menu_edit)).perform(click())

        onView(withId(com.google.android.material.R.id.alertTitle))
            .check(matches(withText("Edit category")))

        onView(withId(R.id.ed_category_name)).perform(replaceText(EDITED_CATEGORY_NAME))
        onView(withId(android.R.id.button1)).perform(click())

        retryWithTimeout {
            onView(withId(R.id.rcv_categories))
                .check(matches(hasDescendant(withText(category1.name))))
                .check(matches(not(hasDescendant(withText(category2.name)))))
                .check(matches(hasDescendant(withText(category3.name))))
                .check(matches(hasDescendant(withText(EDITED_CATEGORY_NAME))))
        }
    }

}