package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Bank extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String curruser;

    private TextView goldview; // current gold amount in account display

    private String currentrates; //todays exchange rates

    private HashMap<String, Double> coinsandgoldval = new HashMap<String,Double>(); //coin as the key for its respective gold value

    private String keyofmaxexchange; //key from coinandgoldval with the highest gold value

    private int sizeofsparechange; //has the size of spare change

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();
        goldview = (TextView) findViewById(R.id.goldamt);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        currentrates = settings.getString("todayrate", currentrates);

        //retrieve gold in users account from database and display in goldview textbox
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               String gold = documentSnapshot.get("Gold").toString();
               goldview.setText(gold);
            }
        });

        //check if there are coins in spare to send
        db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                sizeofsparechange = querySnapshot.size();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //retrieve gold in users account from database and display in goldview textbox
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String gold = documentSnapshot.get("Gold").toString();
                goldview.setText(gold);
            }
        });
    }

    //goes to view spare change
    public void sparechangeviewer(View view) {
        Intent intent = new Intent(this, ViewSpareChange.class);
        startActivity(intent);
    }

    //goes to view wallet
    public void walletviewer(View view) {
        Intent intent = new Intent(this, ViewWallet.class);
        startActivity(intent);
    }

    //goes to sending coins view
    public void sendcoinsview(View view) {
        if (sizeofsparechange>0) {
            Intent intent = new Intent(this, SendingCoins.class);
            startActivity(intent);
        }else {
            new ConverterandDialogs().OKdialog("No coins to send, fill up your spare change before trying to send coins.",
                    "No spare change", Bank.this).show();
        }
    }

    //goes to back to main menu
    public void backtomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    //exchange returns the gold value of the highest coin
    public void exchange(View view) {
        db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                //make sure there are coins in spare change
                if (queryDocumentSnapshots.size()>0) {
                    //get their currency and value to get the gold and for the keys for the hashmap
                    for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                        String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                        String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();

                        String valueofcoin = value + " " + curr;
                        double gold = (new ConverterandDialogs().currconverter(currentrates,valueofcoin));
                        coinsandgoldval.put(value + " " + curr,gold);
                    }
                    //find the max gold value and its key
                    Double maxval = 0.0;
                    String maxkey ="";
                    for(Map.Entry m : coinsandgoldval.entrySet()){
                        if (Double.parseDouble(m.getValue().toString()) > maxval) {
                            maxkey = String.valueOf(m.getKey());
                            maxval = Double.parseDouble(m.getValue().toString());
                        }
                    }
                    keyofmaxexchange =maxkey;

                    //reset gold view to new value
                    db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@Nullable DocumentSnapshot documentSnapshot) {
                            Double gold = coinsandgoldval.get(keyofmaxexchange) + Double.parseDouble(documentSnapshot.get("Gold").toString());
                            db.collection("users").document(curruser).update("Gold", gold);
                            goldview.setText("" + gold);
                        }
                    });

                    //clear spare change
                    db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (int i =0; i<queryDocumentSnapshots.size();i++) {
                                String key = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();
                                db.collection("users").document(curruser).collection("spare change").document(key).delete();
                            }
                        }
                    });

                    //alert the user that the transaction was successful
                    new ConverterandDialogs().OKdialog("Yayy, you've put all those unbankable coins to good use!",
                            "Success", Bank.this).show();
                }else {
                    //alert the user that their spare change is empty
                    new ConverterandDialogs().OKdialog("Your spare change is empty, please fill it before exchanging",
                                                        "Empty Spare Change", Bank.this).show();
                }
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
