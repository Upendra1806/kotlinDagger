package com.juliusbaer.premarket

import android.content.res.Resources
import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.ViewPagerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.juliusbaer.premarket.espresso.EspressoClassRule
import com.juliusbaer.premarket.ui.splash.SplashActivity
import com.juliusbaer.premarket.ui.underluings.HomeAdapter
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@LargeTest
@RunWith(AndroidJUnit4::class)
class EspressoSampleTest {
    @JvmField
    @Rule
    val activityTestRule = ActivityScenarioRule(SplashActivity::class.java)

    @JvmField
    @Rule
    val countingTaskExecutorRule = CountingTaskExecutorRule()

    @Test
    fun testMainActivity() {
        var res: Resources? = null
        activityTestRule.scenario.onActivity {
            res = it.resources
        }

        //HomeFragment
        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()))

        //UnderlyingListFragment (smi)
        onView(allOf(withId(R.id.recyclerViewHome), isCompletelyDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<HomeAdapter.ViewHolder>(0, click()))
        testCompany()

        Espresso.pressBack()

        //UnderlyingListFragment (mid cap)
        onView(withId(R.id.viewPager)).perform(ViewPagerActions.scrollToPage(1))
        onView(allOf(withId(R.id.recyclerViewHome), isCompletelyDisplayed())).perform(RecyclerViewActions.actionOnItemAtPosition<HomeAdapter.ViewHolder>(0, click()))
        testCompany()

        Espresso.pressBack()

        //index test
        onView(allOf(withId(R.id.include11), isCompletelyDisplayed())).perform(click())
        testIndex()
        //onView(allOf(withText(res!!.getString(R.id.equities)), isDescendantOfA(withId(R.id.tabBarHome)), isDisplayed())).perform(click())
        //drain()
    }

    private fun testIndex() {
        onView(withId(R.id.tabBarCompany)).check(matches(isDisplayed()))

        //index performance
        onView(withId(R.id.txtLastTrade)).check(matches(not(withText(""))))

        testCompanyWarrantsAndNews()
    }

    private fun testCompany() {
        onView(withId(R.id.tabBarCompany)).check(matches(isDisplayed()))

        //company performance
        onView(withId(R.id.txtPriceValue)).check(matches(not(withText(""))))

        testCompanyWarrantsAndNews()
    }

    private fun testCompanyWarrantsAndNews() {
        //company warrants
        onView(withId(R.id.viewPager)).perform(ViewPagerActions.scrollToPage(1))
        onView(withId(R.id.txtFilterProducts)).check(matches(isDisplayed()))
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.txtNoWarrants)).check(matches(not(isDisplayed())))

        //company news
        onView(withId(R.id.viewPager)).perform(ViewPagerActions.scrollToPage(2))
        onView(withId(R.id.recyclerNews)).check(matches(isDisplayed()))
        //onView(withId(R.id.txtNoNews)).check(matches(not(isDisplayed())))
    }

    @Throws(TimeoutException::class, InterruptedException::class)
    private fun drain() {
        countingTaskExecutorRule.drainTasks(1, TimeUnit.MINUTES)
    }

    companion object {
        @JvmField
        @ClassRule
        val espressoRule = EspressoClassRule()
    }
}