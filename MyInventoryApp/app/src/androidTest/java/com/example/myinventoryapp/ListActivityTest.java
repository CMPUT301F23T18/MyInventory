package com.example.myinventoryapp;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import org.junit.Rule; import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ListActivityTest {
    @Rule
    public ActivityScenarioRule<ListActivity> scenario = new ActivityScenarioRule<ListActivity>(ListActivity.class);

    //TODO: adding an item
    @Test
    public void testAddItem(){
    }

    //TODO: viewing an item
    @Test
    public void testViewItem(){
    }

    //TODO: deleting individual items
    @Test
    public void testDeleteItem(){
        // Click on an item
        onView(withId(R.id.item_list)).perform(click());
        //Click on the delete button
        onView(withId(R.id.editText_name)).perform(ViewActions.typeText("Edmonton"));

        //Click yes
        onView(withId(R.id.button_confirm)).perform(click());

        //Check if item removed from the screen (length should decrease by 1)
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.city_list)).atPosition(0).check(matches((withText("Edmonton"))));
    }

    //TODO: multiple items
    @Test
    public void testDeleteMultiple(){
        //Click on delete button

        //Click on select all

        //Click on delete

        //Click yes

        //Check if all items from list deleted (length should be 0)
    }

    //TODO: adding tags
    @Test
    public void testAddTag(){
    }

    //TODO: adding pictures
    @Test
    public void testAddPic(){
    }

    //TODO: editing an item
    @Test
    public void testEditItem(){
    }

}
