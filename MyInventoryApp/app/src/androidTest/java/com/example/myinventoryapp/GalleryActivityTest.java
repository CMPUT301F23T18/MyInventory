package com.example.myinventoryapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.intercepting.SingleActivityFactory;

import com.example.myinventoryapp.ItemManagement.GalleryActivity;

import org.junit.Rule;
import org.junit.Test;

public class GalleryActivityTest {
    GalleryActivity subject;
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);


    SingleActivityFactory<GalleryActivity> activityFactory  = new SingleActivityFactory<GalleryActivity>(GalleryActivity.class) {
        @Override
        protected GalleryActivity create(Intent intent) {
            subject = spy(getActivityClassToIntercept());
            return subject;
        }
    };
    @Rule
    public ActivityScenarioRule<GalleryActivity> rule = new ActivityScenarioRule<GalleryActivity>(activityFactory.getActivityClassToIntercept());

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
        sleep(1000);
        //Check if image added (number of pictures in gallery should change
        onView(withId(R.id.imageTotal)).check(matches(withText("1/6 Images")));
    }



    @Test
    public void galleryGrabTest() throws InterruptedException {
        startActivity();
        //NOTE: Gives error "unable to make mock of type GalleryActivity
        subject = spy(activityFactory.getActivityClassToIntercept());
        Bitmap bm = BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.house_placeholder);

        onView(withId(R.id.cameraButton)).perform(click());

        //Stub gallery opening, then attach custom bitmap to object
        doNothing().when(subject).openPopup();
        sleep(1000);
        subject.attachToItem(bm);
        sleep(1000);

        // Check to see if image1 is visible
        onView(withId(R.id.image1Edit)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
