package s1640402.coinzgame.nishtha_coinz;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;


/*NOTE: please ensure before running this test that no user is login in before starting the since
*this test creates new accounts from Login View hence would need to start from there*/

//This test tests the adding and removing of friends by 2 test users

@RunWith(AndroidJUnit4.class)
public class AddingandRemovingFriendsTest {

    @Rule
    public ActivityTestRule<Loginview> mActivityTestRule = new ActivityTestRule<>(Loginview.class);

    @Test
    public void addingandRemovingFriendsTest() {

        //create first test email
        String[] name = {"amy","tester"};
        String[] email = {"aol","gmail","email"};

        int randomnumber = (int)Math.floor((Math.random()*1000));

        //email will be generated with a randomly picked name from the name array, a randomly picked
        //email handle from the email array and a random number from 0 to 1000
        String generatedemail1 = name[(int)Math.floor((Math.random()*2))] + randomnumber + "@" +
                email[(int)Math.floor((Math.random()*3))] + ".com";

        //create second test email
        String[] name2 = {"john","tom"};

        //create second email the same way as we created email 1
        String generatedemail2 = name2[(int)Math.floor((Math.random()*2))] + randomnumber + "@" +
                email[(int)Math.floor((Math.random()*3))] + ".com";

        //adds a sleep statement
        addrest(3000);

        //================================Sign up with email 1=================================
        //enter email in email text field
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.fieldEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(generatedemail1), closeSoftKeyboard());

        addrest(3000);

        //enter password in password field
        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.fieldPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("123456"), closeSoftKeyboard());

        //press signup button to create account and login with this account
        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.emailCreateAccountButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        addrest(5000);

        //log out so we can create the second account
        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.signout),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        addrest(5000);

        //================================Sign up with email 2=================================
        //enter email2 in email field
        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.fieldEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText(generatedemail2), closeSoftKeyboard());

        //enter password for email2 in password field
        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.fieldPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("123456"), closeSoftKeyboard());

        //create the account and login in
        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.emailCreateAccountButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        addrest(5000);

        //================================Add email 1 as a friend===============================
        //go to the friends view
        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.friends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton4.perform(click());

        addrest(5000);

        //enter email1 in search bar
        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.emailsearch),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        appCompatEditText5.perform(replaceText(generatedemail1), closeSoftKeyboard());

        //search for email 1
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.searchusers),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatButton.perform(click());

        addrest(2000);

        //select email1 from listview showing results
        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewaddfriends),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)))
                .atPosition(0);
        appCompatCheckedTextView.perform(click());

        addrest(2000);

        //press add friend button
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.addfriends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        addrest(2000);

        //go back to main menu
        ViewInteraction appCompatImageButton5 = onView(
                allOf(withId(R.id.gotomain),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatImageButton5.perform(click());

        addrest(5000);

        //log out
        ViewInteraction appCompatImageButton6 = onView(
                allOf(withId(R.id.signout),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatImageButton6.perform(click());

        addrest(5000);

        //================================Removing friends=================================
        //sign back in as email 1
        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.fieldEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText6.perform(replaceText(generatedemail1), closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.fieldPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText("123456"), closeSoftKeyboard());

        ViewInteraction appCompatImageButton7 = onView(
                allOf(withId(R.id.emailSignInButton),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton7.perform(click());

        addrest(5000);

        //go to friends view
        ViewInteraction appCompatImageButton8 = onView(
                allOf(withId(R.id.friends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatImageButton8.perform(click());

        addrest(5000);

        //view list of friends
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.viewfriends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        appCompatButton3.perform(click());

        addrest(5000);

        //select email2 as it would be listed under email1s friends
        DataInteraction appCompatCheckedTextView2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewfriends),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)))
                .atPosition(0);
        appCompatCheckedTextView2.perform(click());

        addrest(2000);

        //remove email2 as a friend
        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.removefriends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        addrest(2000);

        //confirm the action
        ViewInteraction appCompatButton5 = onView(
                allOf(withId(android.R.id.button1), withText("Confirm"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        appCompatButton5.perform(scrollTo(), click());

        addrest(3000);

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button3), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                0)));
        appCompatButton6.perform(scrollTo(), click());

        addrest(3000);

        //go back to the add friends view and email2 will be back on the list as it has been
        //removed as a friend
        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.addmorefriends),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                5),
                        isDisplayed()));
        appCompatButton7.perform(click());

        addrest(5000);

        //go back to main menu
        ViewInteraction appCompatImageButton9 = onView(
                allOf(withId(R.id.gotomain),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatImageButton9.perform(click());

        addrest(3000);

        //log out
        ViewInteraction appCompatImageButton10 = onView(
                allOf(withId(R.id.signout),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatImageButton10.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    //adds sleep between 2 actions
    //was made into a method to reduce the number of lines of  code
    private void addrest(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
