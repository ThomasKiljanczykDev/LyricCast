/*
 * Created by Tomasz Kiljanczyk on 06/01/2025, 12:56
 * Copyright (c) 2025 . All rights reserved.
 * Last modified 06/01/2025, 12:04
 */

package dev.thomas_kiljanczyk.lyriccast.common.extensions

import com.google.android.material.tabs.TabLayout

fun TabLayout.moveTabLeft(tab: TabLayout.Tab) {
    val position = tab.position
    if (position == 0) {
        return
    }

    val otherTab = this.getTabAt(position - 1) ?: return

    swapTabs(tab, otherTab)
}

fun TabLayout.moveTabRight(tab: TabLayout.Tab) {
    val position = tab.position
    if (position == this.tabCount - 2) {
        return
    }

    val otherTab = this.getTabAt(position + 1) ?: return

    swapTabs(tab, otherTab)
}

fun TabLayout.swapTabs(tab1: TabLayout.Tab, tab2: TabLayout.Tab) {
    val position1 = tab1.position
    val position2 = tab2.position

    val tabLeft: TabLayout.Tab = if (position1 < position2) tab1 else tab2
    val tabRight: TabLayout.Tab = if (position1 < position2) tab2 else tab1
    val isLeftTabSelected = tabLeft.isSelected

    val newTabLeft = this.newTab()
    newTabLeft.text = tabLeft.text

    val newTabRight = this.newTab()
    newTabRight.text = tabRight.text

    val positionLeft = tabLeft.position
    val positionRight = tabRight.position

    this.removeTab(tabLeft)
    this.removeTab(tabRight)

    this.addTab(newTabRight, positionLeft)
    this.addTab(newTabLeft, positionRight)

    this.selectTab(if (isLeftTabSelected) newTabLeft else newTabRight)
}