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

    public void playgame(View view){
        Intent intent = new Intent(this, PlayGame.class);
        startActivity(intent);


    }



}
