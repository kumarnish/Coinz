package s1640402.coinzgame.nishtha_coinz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.content.Intent;
import android.widget.EditText;

public class MainView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);
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
