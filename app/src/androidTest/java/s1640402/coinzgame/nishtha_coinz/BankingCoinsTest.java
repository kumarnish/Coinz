package s1640402.coinzgame.nishtha_coinz;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

/*NOTE: please ensure before running this test that no user is logged in before starting the since
 *this test creates new accounts from Login View hence would need to start from there*/

//This test tests the banking of coins which are in wallet

@RunWith(AndroidJUnit4.class)
public class BankingCoinsTest {

    @Rule
    public ActivityTestRule<Loginview> mActivityTestRule = new ActivityTestRule<>(Loginview.class);

    //initialise database to add coins to the account
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Test
    public void secondhalf() {

        //====================================Create test user==================================
        //create randomized email for testing
        String[] name = {"amy","john","tom","tester"};
        String[] email = {"aol","gmail","email"};

        int randomnumber = (int)Math.floor((Math.random()*1000));

        //email will be generated with a randomly picked name from the name array, a randomly picked
        //email handle from the email array and a random number from 0 to 1000
        String generatedemail = name[(int)Math.floor((Math.random()*4))] + randomnumber + "@" +
                email[(int)Math.floor((Math.random()*3))] + ".com";

        //add sleep time
        addrest(5000);

        //enter email into email field
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.fieldEmail),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(generatedemail), closeSoftKeyboard());

        //enter password into password field
        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.fieldPassword),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText4.perform(replaceText("123456"), closeSoftKeyboard());

        //press sign up to create account and login
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

        //go to the bank view
        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.bank),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

        //====================================Add fake coins to wallet=============================
        //add fake coins to users account for banking
        //make sure database has been intialized
        if (db !=null) {

            //create 3 coin objects and add them to the users wallet
            Coin coin1 = new Coin("ce45-46d2-747d-d60d-ec44-73fc", "8.3149100141813", "PENY");
            db.collection("users").document(generatedemail)
                    .collection("wallet").document(coin1.getId()).set(coin1);

            Coin coin2 = new Coin("9dc3-9db0-93bf-bbbc-c827-9d49", "8.671072399253285","PENY");
            db.collection("users").document(generatedemail)
                    .collection("wallet").document(coin2.getId()).set(coin2);

            Coin coin3 = new Coin("c482-b96a-099f-124a-6e32-bd20", "0.8515900033105506", "QUID");
            db.collection("users").document(generatedemail)
                    .collection("wallet").document(coin3.getId()).set(coin3);
        }

        addrest(5000);

        //===========================Select all coins and bank them============================

        //go to view wallet view
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.viewwallet),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        addrest(5000);

        //select all coins in listview so we can bank them
        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewwallet),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)))
                .atPosition(0);
        appCompatCheckedTextView.perform(click());

        DataInteraction appCompatCheckedTextView2 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewwallet),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)))
                .atPosition(1);
        appCompatCheckedTextView2.perform(click());

        DataInteraction appCompatCheckedTextView3 = onData(anything())
                .inAdapterView(allOf(withId(R.id.listviewwallet),
                        childAtPosition(
                                withClassName(is("android.widget.RelativeLayout")),
                                2)))
                .atPosition(2);
        appCompatCheckedTextView3.perform(click());

        //press bank button to bank coins
        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.bank),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        addrest(5000);

        //=======================Check if gold value has been changed==============================

        //go back to bank view
        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.backtobank),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton3.perform(click());

        addrest(6000);

        //check if gold amount displayed is greater than 0 using a custom matcher
        ViewInteraction textview =  onView(withId(R.id.goldamt));
        textview.check(matches(checkifnotzero()));

        //back to main menu
        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.backtomain),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                11),
                        isDisplayed()));
        appCompatImageButton3.perform(click());

        addrest(3000);

        //sign out of the account
        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.signout),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                6),
                        isDisplayed()));
        appCompatImageButton4.perform(click());
    }

    //this method allows us to get around the changing exchange rates as the user will start off
    //with no gold so if the amount is greater than 0 then the banking would have been successful
    private static Matcher<View> checkifnotzero(){
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if(!(item instanceof TextView))
                    return false;

                //get gold value being displayed
                double currentgold = Double.valueOf(((TextView) item).getText().toString());

                //check and return if it is greater than 0
                return currentgold > 0.0;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Value expected is wrong");
            }
        };
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
