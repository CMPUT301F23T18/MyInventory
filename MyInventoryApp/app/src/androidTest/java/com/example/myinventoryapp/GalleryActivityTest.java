package com.example.myinventoryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;

public class GalleryActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public ActivityScenarioRule<GalleryActivity> rule = new ActivityScenarioRule<GalleryActivity>(GalleryActivity.class);

    private void startActivity() {
        rule.getScenario().moveToState(Lifecycle.State.CREATED);
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText("Serial"),closeSoftKeyboard());
        onView(withId(R.id.acquired_da)).perform(ViewActions.typeText("20231111"),closeSoftKeyboard());
        onView(withId(R.id.make)).perform(ViewActions.typeText("Make"),closeSoftKeyboard());
        onView(withId(R.id.model)).perform(ViewActions.typeText("Model"),closeSoftKeyboard());
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("101202"),closeSoftKeyboard());

        //press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());
    }
    @Test
    public void CamCaptureTest() throws InterruptedException {
        // Credit to Maham for most of this test
        startActivity();
        onView(withId(R.id.cameraButton)).perform(click());
        sleep(1000);
        onView(withText("Capture")).perform(click());
        onView(withId(R.id.captureButtonCam)).perform(click());

        //Check if image added (number of pictures in gallery should change
        onView(withId(R.id.imageTotal)).check(matches(withText("1/6 Images")));
    }



    @Test
    public void galleryGrabTest() throws InterruptedException {
        startActivity();
        rule.getScenario().onActivity(GalleryActivity::addToGallery);
        onView(withId(R.id.cameraButton)).perform(click());
        sleep(1000);
        onView(withText("Gallery")).perform(click());
    }
}
