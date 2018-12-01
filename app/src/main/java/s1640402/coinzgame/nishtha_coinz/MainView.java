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


public class MainView extends AppCompatActivity {

    //download date variables
    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String tag = "MainView";

    private String strMapData = ""; //mapdata variable

    private HashSet<String> fourdaysrates = new HashSet<String>(); //contains the rates of the previous 4 days
    private String rates = ""; // contains current days rate

    //firebase/firestore variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //username display textbox
    private TextView usernamedisp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
    }

    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();

        //set up username box on login
        usernamedisp = (TextView) findViewById(R.id.usernamedisp);
        usernamedisp.setText("Hi " + curruser);

        //Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        //get download date
        editor.putString("lastDownloadDate", downloadDate);
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        //format todays date to "yyyy/MM/dd" for usage in download link
        String todaydate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        //get today's exchange rates from prefs file
        settings.getString("todayrate", rates);

        //checks if date has changed if it has prefs data is updated
        if (!todaydate.equals(downloadDate)) {

            //set download day as current day since new day
            downloadDate = todaydate;
            //create download link and use to get map data
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";
            AsyncTask<String, Void, String> mapdata = new DownloadFileTask().execute(link);

            //if a new day then the rates for the previous days will also be needed
            getallratesfrom4days(getlast4daysdate());

            //update 4 days rate
            editor.putStringSet("prevrates", fourdaysrates);

            //in try and catch as there can be null pointer exception
            try {
                //since successful, put new map data in prefs file
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

            //delete removed list if date changed
            db.collection("users").document(curruser).collection("removedcoins").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (int i =0; i<queryDocumentSnapshots.size();i++) {
                        String key = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();
                        db.collection("users").document(curruser).collection("removedcoins").document(key).delete();
                    }
                }
            });
        }
        //if date has not changed then just get all date from prefs file for use
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
        //on Stop we will apply all the changes we made to the pref file for later use
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

    //takes user to bank
    public void gotobank(View view){
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

    //takes user to friends view
    public void gotofriendsandcoins(View view) {
        Intent intent = new Intent(this, AddFriends.class);
        startActivity(intent);
    }

    //finds the dates of the last 4 days and returns the links for their maps
    public String[] getlast4daysdate() {
        String[] prevdateslinks = new String[4];

        //since we are only provided with maps for 2018-2019 it makes sure to check if
        // it is a valid date so after the 1st of january 2018
        if(!LocalDate.now().equals("2018/01/01")) {

            for (int i = 1; i<5 ; i++)
            {
               String date = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                prevdateslinks[i-1] = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + date + "/coinzmap.geojson";
            }
        }

        return prevdateslinks;
    }

    //returns all the relevant dates'rates as hashset so that they can be stored in the prefs file
    public void getallratesfrom4days(String [] links){
        String[] arrrates = new String[4];
        for(int i =0; i<arrrates.length;i++) {

            //extra rates only from json string
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

    //log out of account and go back to login view to login again
    public void signout(View view){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(this, Loginview.class);
        startActivity(intent);
    }

}
