package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


import javax.annotation.Nullable;

public class ViewSpareChange extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //list of coins in spare change
    private ArrayList<String> coinz = new ArrayList<String>();

    private ArrayAdapter arrayAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sparechange);
    }

    //takes user to sending coin window
    public  void sendcoins(View view) {
        //only go to the send coins if there are coins to send
        if (coinz.size()>0) {
            Intent intent = new Intent(this, SendingCoins.class);
            startActivity(intent);
        }else {
            new ConverterandDialogs().OKdialog("No coins to send, fill up your spare change before trying to send coins.",
                                               "No spare change", ViewSpareChange.this).show();
        }
    }

    //takes user back to bank view
    public  void gobacktobank(View view) {
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        listView = (ListView) findViewById(R.id.listview);
        curruser = mAuth.getCurrentUser().getEmail();

        //to avoid filling the arraylist with duplicates
        coinz.clear();
        //fill coinz list with all the coins in spare change
        db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    coinz.add(value + " " + curr);
                }
                arrayAdapter = new ArrayAdapter(ViewSpareChange.this,android.R.layout.simple_list_item_1, coinz);
                listView.setAdapter(arrayAdapter);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}

