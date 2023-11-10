package com.example.myinventoryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasToString;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.view.View;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule; import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ListActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public ActivityScenarioRule<ListActivity> scenario = new ActivityScenarioRule<ListActivity>(ListActivity.class);

    //TODO: login in
    @Test
    public void testLogIn(){
    }

    //TODO: signup
    @Test
    public void testSignUp(){
    }

    //TODO: google sign in
    @Test
    public void testGoogle(){
    }

    //Adds items, along with pictures
    @Test
    public void testAddItem() throws InterruptedException {
        sleep(1000);
        //click on add button
        onView(withId(R.id.add_button)).perform(click());

        //input information
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText("Serial"),closeSoftKeyboard());
        onView(withId(R.id.acquired_da)).perform(ViewActions.typeText("20231111"),closeSoftKeyboard());
        onView(withId(R.id.make)).perform(ViewActions.typeText("Make"),closeSoftKeyboard());
        onView(withId(R.id.model)).perform(ViewActions.typeText("Model"),closeSoftKeyboard());
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("101202"),closeSoftKeyboard());

        //press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());

        //add images
        onView(withId(R.id.cameraButton)).perform(click());
        sleep(1000);
        onView(withText("Capture")).perform(click());
        onView(withId(R.id.captureButtonCam)).perform(click());
        onView((withId(R.id.saveButtonGallery))).perform(click());
        sleep(1000);

        //check if item displayed
        onView(withText("$ 101202.00")).check(matches(isDisplayed()));
    }

    //Views an item
    @Test
    public void testViewItem() throws InterruptedException {
        sleep(1000);
        onView(withId(R.id.item_list)).perform(click());

        sleep(1000);
        //Check if an element from ViewItemActivity is present
        onView(withId(R.id.serialNumEdit)).check(matches(isDisplayed()));
    }

    //Deletes an item when viewing it
    @Test
    public void testDeleteItem() throws InterruptedException {
        // Click on an item
        sleep(1000);
        onView(withText("$ 101202.00")).perform(scrollTo(),click());

        //Click on the delete button
        onView(withId(R.id.delete_btn)).perform(click());

        // Click yes
        sleep(1000);
        onView(withId(R.id.yes_confirm)).perform(click());
        sleep(3000);

        //Check if item removed from the screen
        onView(withText("$ 101202.00")).check(doesNotExist());
    }

    //Deletes multiple items
    @Test
    public void testDeleteMultiple() throws InterruptedException {
        //Click on delete button
        sleep(1000);
        onView(withId(R.id.delete_btn)).perform(click());

        //Click on select all
        onView(withId(R.id.selectallButton)).perform(click());

        //Click on delete
        onView(withId(R.id.deleteButton)).perform(click());

        //Click yes
        onView(withId(R.id.yes_confirm)).perform(click());

        //Check if all items from list deleted
        sleep(1000);
        onView(withId(R.id.item_list)).check(matches((not(hasDescendant(any(View.class))))));

    }

    //TODO: adding tags
    @Test
    public void testAddTag(){
    }

    //Adds pictures to the gallery
    @Test
    public void testAddPic() throws InterruptedException {
        sleep(1000);
        //click on add button
        onView(withId(R.id.add_button)).perform(click());

        //input information
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText("Serial"),closeSoftKeyboard());
        onView(withId(R.id.acquired_da)).perform(ViewActions.typeText("20231111"),closeSoftKeyboard());
        onView(withId(R.id.make)).perform(ViewActions.typeText("Make"),closeSoftKeyboard());
        onView(withId(R.id.model)).perform(ViewActions.typeText("Model"),closeSoftKeyboard());
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("101202"),closeSoftKeyboard());

        //press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());

        //add images
        onView(withId(R.id.cameraButton)).perform(click());
        sleep(1000);
        onView(withText("Capture")).perform(click());
        onView(withId(R.id.captureButtonCam)).perform(click());

        //Check if image added (number of pictures in gallery should change
        onView(withText("1/6 Images")).check(matches(isDisplayed()));
    }

    //TODO: editing an item
    @Test
    public void testEditItem() throws InterruptedException {
        sleep(1000);
        //to add an item
        onView(withId(R.id.add_button)).perform(click());

        //input information
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText("Serial"),closeSoftKeyboard());
        onView(withId(R.id.acquired_da)).perform(ViewActions.typeText("20231111"),closeSoftKeyboard());
        onView(withId(R.id.make)).perform(ViewActions.typeText("Make"),closeSoftKeyboard());
        onView(withId(R.id.model)).perform(ViewActions.typeText("Model"),closeSoftKeyboard());
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("101202"),closeSoftKeyboard());

        //press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());

        //press save button without photograph
        onView(withId(R.id.saveButtonGallery)).perform(click());

        //press item




    }

}
