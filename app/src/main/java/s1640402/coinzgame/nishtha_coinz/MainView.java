package s1640402.coinzgame.nishtha_coinz;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class MainView extends AppCompatActivity {

    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String tag = "MainView";
    private String strMapData = "";
    private HashSet<String> fourdaysrates = new HashSet<String>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

    }

    public void onStart() {
        super.onStart();
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);

        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "");
        String todaydate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        //checks if date has changed if it has the new map is downloaded to the prefs
        if (!todaydate.equals(downloadDate)) {

            downloadDate = todaydate;
            String link = "http://www.homepages.inf.ed.ac.uk/stg/coinz/" + downloadDate + "/coinzmap.geojson";
            AsyncTask<String, Void, String> mapdata = new DownloadFileTask().execute(link);

            //if a new day then the rates for the previuos days will also be needed
            getallratesfrom4days(getlast4daysdate());
            editor.putStringSet("prevrates", fourdaysrates);

            try {
                strMapData = mapdata.get();
                editor.putString("mapdata", strMapData);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "map data is being downloaded", Toast.LENGTH_SHORT).show();
        }
        else {
            strMapData = settings.getString("mapdata", strMapData);

            //checks if fourdaysrates set is empty so that it can add maps
            if (fourdaysrates.size() == 0) {
                getallratesfrom4days(getlast4daysdate());
                editor.putStringSet("prevrates", fourdaysrates);
            }
            else {
                settings.getStringSet("prevdatesrates", fourdaysrates);
            }

            Toast.makeText(this, "map data is saved", Toast.LENGTH_SHORT).show();
        }

        //Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");

    }

    public void onStop(){
        super.onStop();

        Log.d(tag,"[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        editor.putString("mapdata", strMapData);
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
        String rates ="";
        Intent intent = new Intent (this, StockMarket.class);

        //parse rates into array
        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(strMapData);
            rates = jsonResponse.getString("rates");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, fourdaysrates.size() + "", Toast.LENGTH_SHORT).show();
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

    //takes user to settings
    public void gotosettings(View view) {
        Intent intent = new Intent(this, Settings.class);
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

    public void signout(View view){
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        Intent intent = new Intent(this, Loginview.class);
        startActivity(intent);
    }


}
