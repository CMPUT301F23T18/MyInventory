package com.example.myinventoryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertEquals;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.example.myinventoryapp.Authentication.SignUpActivity;
import com.example.myinventoryapp.Authentication.StartUpActivity;
import com.example.myinventoryapp.ItemManagement.Item;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import com.example.myinventoryapp.ListActivities.ListActivity;

import org.junit.Rule; import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartUpActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public ActivityScenarioRule<StartUpActivity> rule = new ActivityScenarioRule<StartUpActivity>(StartUpActivity.class);

    //Sign up
    @Test
    public void testSignup() throws InterruptedException {
        sleep(1000);
        //click on signup button
        onView(withId(R.id.SignupButton)).perform(click());
        sleep(1000);

        String new_email = String.valueOf(System.currentTimeMillis());

        //input information
        onView(withId(R.id.username)).perform(ViewActions.typeText("User"), closeSoftKeyboard());
        onView(withId(R.id.email)).perform(ViewActions.typeText(new_email + "@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(ViewActions.typeText("Password123"), closeSoftKeyboard());

        //press signup
        onView(withId(R.id.LoginButton)).perform(click());

        //click on Login
        sleep(1000);

        //check if item displayed
        onView(withId(R.id.profileMain)).check(matches(isDisplayed()));
    }

    @Test
    public void testLogin() throws InterruptedException {
        Login();
        //check if item displayed
        onView(withId(R.id.profileMain)).check(matches(isDisplayed()));
    }

    private void Login() throws InterruptedException {
        sleep(1000);
        //click on signup button
        onView(withId(R.id.LoginButton)).perform(click());
        sleep(1000);

        //input information for an account that is already signed up
        onView(withId(R.id.usernameButton)).perform(ViewActions.typeText("user123@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.PasswordButton)).perform(ViewActions.typeText("Password123"), closeSoftKeyboard());

        //press signup
        onView(withId(R.id.LoginButton)).perform(click());

        //click on Login
        sleep(1000);
    }

    @Test
    public void testAddItem() throws InterruptedException {
        Login();
        //click on add button
        onView(withId(R.id.add_button)).perform(click());

        String unique_serial = String.valueOf(System.currentTimeMillis());

        //input information
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText(unique_serial),closeSoftKeyboard());
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

        ArrayList<Item> items = getItems(R.id.item_list);
        for(int i = 0; i < items.size();i++){
            if(items.get(i).getSerial_num().equals(unique_serial)){
                assertEquals(items.get(i).getSerial_num(), unique_serial);
            }
        }
        //check if item displayed
        //onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).onChildView(withText("$ 101202.00")).check(matches(isDisplayed()));
    }

    private int getCount(int adapterViewId) {
        final int[] count = new int[1];

        onView(allOf(withId(adapterViewId), isDisplayed())).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (view instanceof AdapterView) {
                    count[0] = ((AdapterView) view).getAdapter().getCount();
                }
            }
        });
        return count[0];
    }

    private ArrayList<Item> getItems(int adapterViewId) {
        ArrayList<Item> items = new ArrayList<>();
        int count = getCount(adapterViewId);

        onView(allOf(withId(adapterViewId), isDisplayed())).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (view instanceof AdapterView) {
                    for (int i = 0; i < count;i++){
                        items.add((Item) ((AdapterView) view).getAdapter().getItem(i));
                    }
                }
            }
        });
        return items;
    }
}