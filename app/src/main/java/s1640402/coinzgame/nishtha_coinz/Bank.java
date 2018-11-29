package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Bank extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String curruser;
    private Double goldamt;
    private TextView goldview;
    private String currentrates;
    private HashMap<String, Double> coinsandgoldval = new HashMap<String,Double>();
    private String keyofmaxexchange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();
        goldview = (TextView) findViewById(R.id.goldamt);

        //retrieve gold
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
               String gold = documentSnapshot.get("Gold").toString();
               goldamt = Double.parseDouble(gold);
            }

        });
        goldview = (TextView) findViewById(R.id.goldamt);
        goldview.setText("" + goldamt);

    }

    @Override
    public void onStart() {
        super.onStart();
        //retrieve gold
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String gold = documentSnapshot.get("Gold").toString();
                goldview.setText(gold);
                goldamt = Double.parseDouble(gold);
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

    public void sparechangeviewer(View view) {
        Intent intent = new Intent(this, ViewSpareChange.class);
        startActivity(intent);
    }

    public void walletviewer(View view) {
        Intent intent = new Intent(this, ViewWallet.class);
        startActivity(intent);
    }

    public void backtomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    public void exchange(View view) {
        db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.size()>0) {
                    for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                        String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                        String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                        double gold = currconverter(curr,value);
                        coinsandgoldval.put(value + " " + curr,gold);
                    }
                    Double maxval = 0.0;
                    String maxkey ="";
                    for(Map.Entry m : coinsandgoldval.entrySet()){
                        if (Double.parseDouble(m.getValue().toString()) > maxval) {
                            maxkey = String.valueOf(m.getKey());
                            maxval = Double.parseDouble(m.getValue().toString());
                        }
                    }

                    keyofmaxexchange =maxkey;

                    db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@Nullable DocumentSnapshot documentSnapshot) {
                            Double gold = coinsandgoldval.get(keyofmaxexchange) + Double.parseDouble(documentSnapshot.get("Gold").toString());
                            db.collection("users").document(curruser).update("Gold", gold);
                            goldview.setText("" + gold);
                        }
                    });

                }
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
    }


    public double currconverter(String curr, String value) {
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        currentrates = settings.getString("todayrate",currentrates);
        float[] ratestoday = getrates(currentrates);

        if (curr.contains("SHIL"))
            return ratestoday[0]*Double.parseDouble(value);
        else if (curr.contains("DOLR"))
            return ratestoday[1]*Double.parseDouble(value);
        else if (curr.contains("QUID"))
            return ratestoday[2]*Double.parseDouble(value);
        else
            return ratestoday[3]*Double.parseDouble(value);

    }

    //gets rates out of string
    public float[] getrates(String r){
        //create an array that separates each currency into an element of a string array
        String[] strrates = (r.substring(0,r.length()-2)).split(",");
        float[] rates = new float[4];
        String numstring;

        //the array has the rates in the order they are present in the geojson file
        // Shil, Dolr, Quid, Peny hence rates[0] is the rate of shil and etc..
        for (int i =0; i<strrates.length; i++) {
            numstring = strrates[i].substring(strrates[i].indexOf(":")+1);
            rates[i] = Float.parseFloat(numstring);
        }

        return rates;
    }


}
