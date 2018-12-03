package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/* =====================================BANK VIEW=======================================
In this activity the user can:
-View their current gold balance
-Access their wallet, spare change and the send coins window
-Use the Exchange coins feature to get the gold highest gold value in exchange for their spare change
* */
public class Bank extends AppCompatActivity {

    //Firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    // current gold amount in account display
    private TextView goldview;

    //coin as the key for its respective gold value
    private HashMap<String, Double> coinsandgoldval = new HashMap<>();

    //today's exchange rates
    private String currentrates;

    //key from coinandgoldval with the highest gold value
    private String keyofmaxexchange;

    //has the size of spare change
    private int sizeofsparechange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();
        goldview = findViewById(R.id.goldamt);

        //store size of spare change for later use
        db.collection("users").document(curruser)
          .collection("spare change").get()
          .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        db.collection("users").document(curruser).get()
          .addOnSuccessListener(
          new OnSuccessListener<DocumentSnapshot>() {
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
            new ConverterandDialogs().OKdialog("No coins to send, fill up your spare change"
                            + " before trying to send coins.", "No spare change",
                            Bank.this).show();
        }
    }

    //goes to back to main menu
    public void backtomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    //===============================Exchange bonus feature=======================================
    //exchange returns the gold value of the highest coin in spare change and removes all spare
    //change in return for the gold
    public void exchange(View view) {
        //get todays rates from the prefs file
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        currentrates  = settings.getString("todayrate", currentrates);

        db.collection("users").document(curruser).collection("spare change")
          .get().addOnSuccessListener(
          new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    //make sure there are coins in spare change
                    if (queryDocumentSnapshots.size()>0) {
                        //get their currency and value to get the gold and the keys for the hash map
                        for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                            String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                            String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();

                            String valueofcoin = value + " " + curr;
                            //convert each coin into gold
                            double gold = (new ConverterandDialogs().currconverter(currentrates,valueofcoin));
                            //store the coin currency and value as the key for it's gold value
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

                        db.collection("users").document(curruser).get()
                          .addOnSuccessListener(
                           new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(@Nullable DocumentSnapshot documentSnapshot) {
                                    //add the users current gold with the gold gotten above to update their
                                    //current gold to this new value
                                    Double gold = coinsandgoldval.get(keyofmaxexchange)
                                            + Double.parseDouble(documentSnapshot.get("Gold").toString());

                                    db.collection("users").document(curruser).update("Gold", gold);

                                    String text = gold + "";
                                    goldview.setText(text);
                                }
                        });

                    //clear spare change
                    db.collection("users").document(curruser).collection("spare change")
                      .get().addOnSuccessListener(
                      new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (int i =0; i<queryDocumentSnapshots.size();i++) {
                                    String key = queryDocumentSnapshots.getDocuments()
                                                                       .get(i).get("id").toString();

                                db.collection("users").document(curruser)
                                  .collection("spare change").document(key).delete();
                            }
                        }
                    });
                        Toast.makeText(Bank.this, "Successful, your gold has updated!",
                                        Toast.LENGTH_SHORT).show();
                }else {
                    //alert the user that their spare change is empty
                    new ConverterandDialogs().OKdialog("Your spare change is empty, " +
                                    "please fill it before exchanging", "Empty Spare Change",
                                     Bank.this).show();
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
