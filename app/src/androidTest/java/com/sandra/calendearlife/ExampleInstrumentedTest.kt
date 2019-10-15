package com.sandra.calendearlife

import android.view.View
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.sandra.calendearlife.home.HomeFragment
import org.hamcrest.Matchers.not

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.Matcher


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.sandra.calendearlife", appContext.packageName)
    }

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    @Throws(Exception::class)
    fun createReminders() {
        onView(withId(R.id.fab_add)).perform(click())
        onView(withId(R.id.remindersFab)).check(matches(isEnabled()))
            .perform(setVisibility(true))
            .perform(click())
        onView(withId(R.id.remindersTitleInput)).perform(typeText("good night"))
        onView(withId(R.id.remindersNoteInput))
            .perform(typeText("Don't wakeup"))
            .perform(ViewActions.closeSoftKeyboard())
        onView(withId(R.id.saveText)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.remindersRecyclerView))
            .perform(RecyclerViewActions
                .actionOnItemAtPosition<RecyclerView.ViewHolder>(0, clickOnViewChild(R.id.remindersChecked)))
        onView(withId(R.id.drawerLayout)).perform(DrawerActions.open())
        Thread.sleep(1000)
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.historyReminder))
        Thread.sleep(3000)
    }

    private fun setVisibility(value: Boolean): ViewAction {
        return object : ViewAction {

            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(FloatingActionButton::class.java)
            }

            override fun perform(uiController: UiController, view: View) {
                view.visibility = if (value) View.VISIBLE else View.GONE
            }

            override fun getDescription(): String {
                return "Show / Hide View"
            }
        }
    }

    private fun clickOnViewChild(viewId: Int) = object : ViewAction {
        override fun getConstraints() = null

        override fun getDescription() = "Click on a child view with specified id."

        override fun perform(uiController: UiController, view: View) = click().perform(uiController, view.findViewById<View>(viewId))
    }
}

