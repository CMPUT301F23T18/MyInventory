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
public class ListActivityTest {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public ActivityScenarioRule<ListActivity> rule = new ActivityScenarioRule<ListActivity>(ListActivity.class);

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

        ArrayList<Item> items = getItems(R.id.item_list);
        Log.d("items", String.valueOf(items));
        //check if item displayed
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).onChildView(withText("$ 101202.00")).check(matches(isDisplayed()));
    }

    //Views an item
    @Test
    public void testViewItem() throws InterruptedException {
        sleep(1000);
        //to add an item
        onView(withId(R.id.add_button)).perform(click());

        sleep(1000);
        //input information
        onView(withId(R.id.serial_numb)).perform(ViewActions.typeText("Serial"),closeSoftKeyboard());
        onView(withId(R.id.acquired_da)).perform(ViewActions.typeText("20231111"),closeSoftKeyboard());
        onView(withId(R.id.make)).perform(ViewActions.typeText("Make"),closeSoftKeyboard());
        onView(withId(R.id.model)).perform(ViewActions.typeText("Model"),closeSoftKeyboard());
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("101202"),closeSoftKeyboard());

        sleep(1000);
        // press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());

        sleep(1000);
        // press save button without photograph
        onView(withId(R.id.saveButtonGallery)).perform(click());

        sleep(1000);
        // Click item in item list view (given id of that adapter view) since there is data
        // (which is an instance of Item) located at position zero.
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0)
                .onChildView(withText("$ 101202.00")).perform(click());

        sleep(1000);
        // Check if ViewItemActivity is displayed
        onView(withId(R.id.acqDateEdit)).check(matches(isDisplayed()));

    }

    //Deletes an item when viewing it
    @Test
    public void testDeleteItem() throws InterruptedException {
        // Click on an item
        sleep(1000);
        int itemCountBeforeDeletion = getCount(R.id.item_list); //find the number of items in list before deletion
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0).onChildView(withId(R.id.itemCostView)).atPosition(0).perform(click());

        //Click on the delete button
        onView(withId(R.id.delete_btn)).perform(click());

        // Click yes
        sleep(1000);
        onView(withId(R.id.yes_confirm)).perform(click());
        sleep(3000);

        //Check if item removed from the screen
        int itemCountAfterDeletion = getCount(R.id.item_list); //find the number of items in list after deletion
        assertEquals(itemCountBeforeDeletion - 1, itemCountAfterDeletion);
    }

    //Get the number of items in the list
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

    //Deletes multiple items
    @Test
    public void testDeleteMultiple() throws InterruptedException {
        //Click on delete button
        sleep(1000);
        int itemCountBeforeDeletion = getCount(R.id.item_list);
        onView(withId(R.id.delete_btn)).perform(click());

        //Click on select all
        onView(withId(R.id.selectallButton)).perform(click());

        //Click on delete
        onView(withId(R.id.deleteButton)).perform(click());

        //Click yes
        onView(withId(R.id.yes_confirm)).perform(click());

        //Check if all items from list deleted
        sleep(1000);
        assertEquals(itemCountBeforeDeletion - 1, 0);

    }

    @Test
    public void testAddTag() throws InterruptedException {
        sleep(1000);
        onView(withId(R.id.tag_btn)).perform(click());

        //Click on select all
        onView(withId(R.id.selectallButton)).perform(click());

        onView(withId(R.id.add_tag_button)).perform(click());

        // Add tag
        onView(withId(R.id.tagEditText)).perform(ViewActions.typeText("Test Tag"),closeSoftKeyboard());
        onView(withId(R.id.create_tag)).perform(click());

        onView(withText("Test Tag")).check(matches(isDisplayed()));
    }

    //Edits an item
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
        onView(withId(R.id.estimated_p)).perform(ViewActions.typeText("112233"),closeSoftKeyboard());

        //press next button
        onView(withId(R.id.forwardButtonAdd)).perform(click());

        //press save button without photograph
        onView(withId(R.id.saveButtonGallery)).perform(click());
        sleep(1000);

        //press item
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0)
                .onChildView(withText("$ 112233.00")).perform(click());

        //press edit
        onView(withId(R.id.editButton)).perform(click());
        sleep(1000);

        //edit serial num
        onView(withId(R.id.serialNumEdit)).perform(replaceText("DIFFERENT"),closeSoftKeyboard());

        //save
        onView(withId(R.id.saveButton)).perform(click());

        //return to list
        onView(withId(R.id.doneButton)).perform(click());
        sleep(1000);

        //press on item again
        onData(anything()).inAdapterView(withId(R.id.item_list)).atPosition(0)
                .onChildView(withText("$ 112233.00")).perform(click());

        //check if serial num changed
        onView(anyOf(withText("DIFFERENT"))).check(matches(isDisplayed()));
    }
}

