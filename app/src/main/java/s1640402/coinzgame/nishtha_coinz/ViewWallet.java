package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import javax.annotation.Nullable;

public class ViewWallet extends AppCompatActivity {

    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;
    private ArrayList<String> coinz = new ArrayList<String>();
    private ArrayList<String> selectedcoinz = new ArrayList<String>();
    private HashMap<String,String> coinsandid = new HashMap<String,String>();
    private double calcgold = 0.0;
    private int calctodaybanked =0;
    private String currentrates;
    private ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallet);

        listView = (ListView) findViewById(R.id.listviewwallet);
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();
        datechecker();

        db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    String id = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();
                    coinz.add(value + " " + curr);
                    coinsandid.put(id,value + " " + curr);
                }
                arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow,R.id.template, coinz);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();

                if(selectedcoinz.contains(selecteditem)) {
                    selectedcoinz.remove(selecteditem);

                }else {
                    selectedcoinz.add(selecteditem);
                }
            }
        });

    }

    public  void showselected(View view) {
        if(selectedcoinz.size()>0) {
            bankcoins();

        }
    }


    public void bankcoins() {
        datechecker();
        if(selectedcoinz.size()>0 && selectedcoinz.size()<=25) {

            db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {

                    int currtotalbanked = Integer.parseInt(documentSnapshot.get("Today Banked").toString());

                    if(currtotalbanked <25 && currtotalbanked+selectedcoinz.size()<=25) {
                        currtotalbanked = currtotalbanked+selectedcoinz.size();

                        for (int i = 0; i<selectedcoinz.size(); i++) {
                            calcgold = calcgold + currconverter(selectedcoinz.get(i));

                            for (int k=0; k<coinz.size();k++) {
                                if(coinz.get(k).equals(selectedcoinz.get(i)))
                                    coinz.remove(k);
                            }
                        }

                        for(Map.Entry m : coinsandid.entrySet()){
                            if (selectedcoinz.contains(m.getValue())) {
                                String key = String.valueOf(m.getKey());
                                db.collection("users").document(curruser).collection("wallet").document(key).delete();
                            }
                        }

                        calcgold = calcgold + Double.parseDouble(documentSnapshot.get("Gold").toString());
                        calctodaybanked = currtotalbanked;

                        arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow,R.id.template, coinz);
                        listView.setAdapter(arrayAdapter);

                    }
                    else{
                        Toast.makeText(ViewWallet.this,"You can only bank 25 in a day!", Toast.LENGTH_SHORT).show();
                    }
                    db.collection("users").document(curruser).update("Today Banked", ""+calctodaybanked);
                    db.collection("users").document(curruser).update("Gold", calcgold);
                    calctodaybanked = 0;
                    calcgold = 0.0;
                }
            });
        }else{
            if (selectedcoinz.size() ==0)
                Toast.makeText(ViewWallet.this,"Please select coins!", Toast.LENGTH_SHORT).show();
            if(selectedcoinz.size()>25)
                Toast.makeText(ViewWallet.this,"You can not bank more than 25 coins in  a day!", Toast.LENGTH_SHORT).show();
        }
    }

    public double currconverter(String coin) {
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        currentrates = settings.getString("todayrate",currentrates);
        float[] ratestoday = getrates(currentrates);

        if (coin.contains("SHIL"))
            return ratestoday[0]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else if (coin.contains("DOLR"))
            return ratestoday[1]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else if (coin.contains("QUID"))
            return ratestoday[2]*Double.parseDouble(coin.substring(0,coin.length()-5));
        else
            return ratestoday[3]*Double.parseDouble(coin.substring(0,coin.length()-5));

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

    public  void gobacktobank(View view) {
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

    public void transfertosparechange(View view) {
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {

                for (int i = 0; i<selectedcoinz.size(); i++) {
                    for (int k=0; k<coinz.size();k++) {
                        if(coinz.get(k).equals(selectedcoinz.get(i)))
                            coinz.remove(k);
                    }
                }

                for(Map.Entry m : coinsandid.entrySet()){
                    if (selectedcoinz.contains(m.getValue())) {
                        String key = String.valueOf(m.getKey());
                        String value = String.valueOf(m.getValue());
                        String currency = value.substring(value.length()-4,value.length());
                        String valueofcoin = value.substring(0,value.indexOf(" "));
                        Coin coin = new Coin(key,valueofcoin,currency);

                        db.collection("users").document(curruser).collection("spare change").document(key).set(coin);
                        db.collection("users").document(curruser).collection("wallet").document(key).delete();
                    }
                }

                arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow,R.id.template, coinz);
                listView.setAdapter(arrayAdapter);

            }
        });
        Toast.makeText(this, "Coins Moved to Spare Change!", Toast.LENGTH_SHORT).show();
    }

    public void datechecker() {
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {
                String datedb = documentSnapshot.get("Date").toString();
                String converted = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                if (!converted.equals(datedb)){
                    db.collection("users").document(curruser).update("Date", converted);
                    db.collection("users").document(curruser).update("Today Banked","" + 0);
                    //db.collection("users").document(curruser).update("Date", converted);
                }

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        datechecker();
    }
}
