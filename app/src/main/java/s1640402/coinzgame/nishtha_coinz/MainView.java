package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.content.Intent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainView extends AppCompatActivity {

    private String downloadDate = ""; // Format: YYYY/MM/DD
    private final String preferencesFile = "MyPrefsFile"; // for storing preferences
    private String tag = "MainView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
    }

    public void onStart() {
        super.onStart();

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);

        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate" , "");
        downloadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’" + downloadDate + "’");
    }

    public void onStop(){
        super.onStop();

        Log.d(tag,"[onStop] Storing lastDownloadDate of " + downloadDate);
        // All objects are from android.context.Context

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        // We need an Editor object to make preference changes.
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate", downloadDate);
        // Apply the edits!
        editor.apply();
    }


    //on click of "lets collect some coinz button" takes user to mapview
    public void playgame(View view){
        Intent intent = new Intent(this, PlayGame.class);
        startActivity(intent);
    }

    //takes user to stock market
    public void gotostockmarket(View view) {
        Intent intent = new Intent (this, StockMarket.class);
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


}
