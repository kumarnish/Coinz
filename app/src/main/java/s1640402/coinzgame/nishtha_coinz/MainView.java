package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

/* =====================================MAIN VIEW=======================================
In this activity the user can:
-Navigate to the Bank, Add friends view, Play game view, Stock market view and the help page
-They can also sign out

The view is also responsible for downloading the map data
*/
public class MainView extends AppCompatActivity {

    //download date variables
    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String tag = "MainView";

    //will contain the geojson string of the downloaded map
    private String strMapData = "";

    //contains the rates of the previous 4 days
    private HashSet<String> fourdaysrates = new HashSet<>();
    private String rates = ""; // contains current days rate

    //firebase/firestore variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //username display textbox
    private TextView username_disp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
    }

    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();

        //display up username box on login
        username_disp = (TextView) findViewById(R.id.usernamedisp);
        String text = "Hi, " + curruser;
        username_disp.setText(text);

        //Restore preferences and initialize the editor to update the pref file
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        //get download date from the pref files
        editor.putString("lastDownloadDate", downloadDate);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");

        //format today's date to "yyyy/MM/dd" for usage in download link
        String todaydate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        //get today's exchange rates from prefs file
        settings.getString("todayrate", rates);

        //================================Download new map======================================

        //checks if date has changed if it has prefs data is updated
        if (!todaydate.equals(downloadDate)) {
            //set download day as current day since new day
            downloadDate = todaydate;

            //create download link and use to get map data
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";
            AsyncTask<String, Void, String> mapdata = new DownloadFileTask().execute(link);

            //the rates for the previous days will also be needed
            getallratesfrom4days(getlast4daysdate());

            //update the prefs file with them
            editor.putStringSet("prevrates", fourdaysrates);

            try {
                //put new map data in prefs file
                strMapData = mapdata.get();
                editor.putString("mapdata", strMapData);
                try {
                    //get todays rates from this mapdata file and update in prefs file
                    JSONObject rategetter = new JSONObject(strMapData);
                    rates = rategetter.getString("rates");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            //empty the removed coins from yesterday as they will not present in the new map
            db.collection("users").document(curruser)
              .collection("removedcoins").get().addOnSuccessListener(
              new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    //loop through the list of coins in removed coins and delete them
                    for (int i =0; i<queryDocumentSnapshots.size();i++) {
                        String key = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();
                        db.collection("users").document(curruser)
                          .collection("removedcoins").document(key).delete();
                    }
                }
            });
        }
        //================================END Download new map======================================
        //if date has not changed then just get all data from prefs file
        else {
            strMapData = settings.getString("mapdata", strMapData);
            rates = settings.getString("todayrate",rates);

            //checks if fourdaysrates set is empty so that it can add maps
            if (fourdaysrates.size() == 0) {
                getallratesfrom4days(getlast4daysdate());
                editor.putStringSet("prevrates", fourdaysrates);
            }
            else {
                settings.getStringSet("prevdatesrates", fourdaysrates);
            }
        }
    }

    public void onStop(){
        super.onStop();

        Log.d(tag,"[onStop] Storing lastDownloadDate of " + downloadDate);

        //we will apply all the changes we made to the pref file for later use
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("lastDownloadDate", downloadDate);
        editor.putString("mapdata", strMapData);
        editor.putString("todayrate", rates);
        editor.putStringSet("prevdatesrates",fourdaysrates);
        // Apply the edits!
        editor.apply();
    }

    //on click of "lets collect some coinz button" takes user to mapview
    public void playgame(View view){
        Intent intent = new Intent(this, PlayGame.class);
        //send map data geo json to map view
        intent.putExtra("strMapData", strMapData);
        startActivity(intent);
    }

    //takes user to stock market
    public void gotostockmarket(View view) {
        Intent intent = new Intent (this, StockMarket.class);
        //send rates to stock market view
        intent.putExtra("exrates", rates);
        intent.putExtra("prevdaysrates",fourdaysrates.toArray(new String[fourdaysrates.size()]));
        startActivity(intent);
    }

    //takes user to bank view
    public void gotobank(View view){
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

    //takes user to friends view
    public void gotofriendsandcoins(View view) {
        Intent intent = new Intent(this, AddFriends.class);
        startActivity(intent);
    }

    //takes user to help view
    public void help(View view){
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    //finds the dates of the last 4 days and returns the links for their maps
    public String[] getlast4daysdate() {
        String[] prevdateslinks = new String[4];

        //since we are only provided with maps for 2018-2019 it makes sure to check if
        // it is a valid date so after the 1st of january 2018
        if(!LocalDate.now().equals("2018/01/01")) {
            for (int i = 1; i<5 ; i++) {
                //day i days before the current date
                String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                //concatenate the date with other parts of the links to get the download link
                prevdateslinks[i-1] = "http://www.homepages.inf.ed.ac.uk/stg/coinz/"
                                                  + date + "/coinzmap.geojson";
            }
        }
        return prevdateslinks;
    }

    //returns all the relevant dates'rates as hash set so that they can be stored in the prefs file
    public void getallratesfrom4days(String [] links){
        String[] arrrates = new String[4];
        for(int i =0; i<arrrates.length;i++) {

            //extra rates only from their respective json string
            try{
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject((new DownloadFileTask().execute(links[i])).get());
                    arrrates[i] = jsonResponse.getString("rates");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        fourdaysrates =  new HashSet<String>(Arrays.asList(arrrates));
    }

    //log out of account and go back to login view
    public void signout(View view){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(this, Loginview.class);
        startActivity(intent);
    }

}
