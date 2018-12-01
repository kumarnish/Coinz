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
    //Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //variables for displaying coins
    private ListView listView;
    private ArrayList<String> coinz = new ArrayList<String>();
    private ArrayList<String> selectedcoinz = new ArrayList<String>();
    private ArrayAdapter arrayAdapter;

    //coins as displayed in the list and their unique ids
    private HashMap<String, String> coinsandid = new HashMap<String, String>();

    //variables for storing the updated gold and number banked before updating on the database
    private double calcgold = 0.0;
    private int calctodaybanked = 0;

    //current days string so we can get the gold
    private String currentrates;

    //wallet size
    private int walletsize =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallet);

        //get today's rate from Prefs File
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        currentrates = settings.getString("todayrate", currentrates);

        //setup list view
        listView = (ListView) findViewById(R.id.listviewwallet);

        //intialize firebase variables and get current user
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();

        //check date to update coins banked
        datechecker();

        //add the ticked coins to a list so we know which coins to manipulate
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();

                if (selectedcoinz.contains(selecteditem)) {
                    selectedcoinz.remove(selecteditem);

                } else {
                    selectedcoinz.add(selecteditem);
                }
            }
        });

    }

    //check if date has changed so we can update the date and the coins banked
    @Override
    protected void onStart() {
        super.onStart();
        datechecker();

        //clear to avoid adding duplicates
        coinz.clear();
        coinsandid.clear();

        //populate list view with coins in user's wallet
        db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    String id = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();
                    coinz.add(value + " " + curr);

                    //store with id so we know which coins to manipulate later on along with their values
                    coinsandid.put(id, value + " " + curr);
                }

                //set up list view with this list and the templates we created separately
                arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow, R.id.template, coinz);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });
    }

    //banking coins
    public void bankcoins(View view) {
        //check date to make sure we have updated version of total banked by user for the current day
        datechecker();
        //they can only bank 25 so selected should be 25 or less
        if (selectedcoinz.size() > 0 && selectedcoinz.size() <= 25) {

            db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {

                    int currtotalbanked = Integer.parseInt(documentSnapshot.get("Today Banked").toString());
                    //make sure that they havent banked 25 or that them banking the set amount doesnt
                    //lead to a total current banked of more than 25
                    if (currtotalbanked < 25 && currtotalbanked + selectedcoinz.size() <= 25) {
                        //update current banked
                        currtotalbanked = currtotalbanked + selectedcoinz.size();
                        //calculate gold values of all the coins and add up
                        for (int i = 0; i < selectedcoinz.size(); i++) {
                            //calcgold = calcgold + currconverter(selectedcoinz.get(i));
                            calcgold = calcgold + (new ConverterandDialogs()).currconverter(currentrates, selectedcoinz.get(i));

                            //remove coins added from view
                            for (int k = 0; k < coinz.size(); k++) {
                                if (coinz.get(k).equals(selectedcoinz.get(i)))
                                    coinz.remove(k);
                            }
                        }

                        //remove coins from wallet as banked
                        for (Map.Entry m : coinsandid.entrySet()) {
                            if (selectedcoinz.contains(m.getValue())) {
                                String key = String.valueOf(m.getKey());
                                db.collection("users").document(curruser).collection("wallet").document(key).delete();
                            }
                        }

                        //added up previous gold to calculated gold and update today banked
                        calcgold = calcgold + Double.parseDouble(documentSnapshot.get("Gold").toString());
                        calctodaybanked = currtotalbanked;

                        //update list view
                        arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow, R.id.template, coinz);
                        listView.setAdapter(arrayAdapter);

                    } else {
                        new ConverterandDialogs().OKdialog("You can only bank 25 in a day!","Banking limit reached",ViewWallet.this).show();
                    }
                    //update database
                    db.collection("users").document(curruser).update("Today Banked", "" + calctodaybanked);
                    db.collection("users").document(curruser).update("Gold", calcgold);

                    //reset values incase user wants to bank more
                    calctodaybanked = 0;
                    calcgold = 0.0;
                }
            });
        } else {
            if (selectedcoinz.size() <1)
                new ConverterandDialogs().OKdialog("Please select coins to bank!","No coins selected",ViewWallet.this).show();
            if (selectedcoinz.size() > 25)
                new ConverterandDialogs().OKdialog("You can not bank more than 25 coins in  a day!","Limit Exceeded",ViewWallet.this).show();
            }
        selectedcoinz.clear();
    }

    //move coins to spare change
    public void transfertosparechange(View view) {
        if (selectedcoinz.size()>0) {
            db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {
                    //delete from spare change view's array
                    for (int i = 0; i < selectedcoinz.size(); i++) {
                        for (int k = 0; k < coinz.size(); k++) {
                            if (coinz.get(k).equals(selectedcoinz.get(i)))
                                coinz.remove(k);
                        }
                    }
                    //remove from the wallet and move to spare change
                    for (Map.Entry m : coinsandid.entrySet()) {
                        if (selectedcoinz.contains(m.getValue())) {
                            String key = String.valueOf(m.getKey());

                            //value from hashmap which we manipulate to get currency and value
                            String value = String.valueOf(m.getValue());
                            String currency = value.substring(value.length() - 4, value.length());
                            String valueofcoin = value.substring(0, value.indexOf(" "));

                            Coin coin = new Coin(key, valueofcoin, currency);

                            db.collection("users").document(curruser).collection("spare change").document(key).set(coin);
                            db.collection("users").document(curruser).collection("wallet").document(key).delete();
                        }
                    }
                    //update view
                    arrayAdapter = new ArrayAdapter(ViewWallet.this, R.layout.viewcoinzrow, R.id.template, coinz);
                    listView.setAdapter(arrayAdapter);

                    new ConverterandDialogs().OKdialog("Coins Moved to Spare Change!", "Success", ViewWallet.this).show();
                }
            });
        }else {
            new ConverterandDialogs().OKdialog("Please select coins to transfer!", "No Coins Selected", ViewWallet.this).show();
        }
        selectedcoinz.clear();
    }

    //check if date has changed
    public void datechecker() {
        db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {
                String datedb = documentSnapshot.get("Date").toString();
                String converted = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                //if date has changed then today banked is reset to 0 and date is updated
                if (!converted.equals(datedb)) {
                    db.collection("users").document(curruser).update("Date", converted);
                    db.collection("users").document(curruser).update("Today Banked", "" + 0);
                }
            }
        });
    }

    //check size of wallet
    public int checksizeofwallet() {
        db.collection("users").document(curruser).collection("wallet").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                walletsize = queryDocumentSnapshots.size();
            }
        });
        return walletsize;
    }

    //go back to bank
    public void gobacktobank(View view) {
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
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