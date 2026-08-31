package com.mancebolabs.sushiclash

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mancebolabs.sushiclash.support.AppContainerTestRule
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

abstract class SushiClashInstrumentedTestCase {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: TestRule = RuleChain
        .outerRule(AppContainerTestRule())
        .around(composeRule)

    protected val composeTestRule get() = composeRule
}
