package s1640402.coinzgame.nishtha_coinz;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class SendingCoins extends AppCompatActivity {

    //coins list view arrays and variables
    private ArrayAdapter arrayAdaptercoins;
    private ListView listViewcoins;
    private ArrayList<String> coins = new ArrayList<String>();
    private ArrayList<String> selectedcoins = new ArrayList<String>();
    private HashMap<String,String> coinsandid = new HashMap<String,String>();

    //friends list view arrays and variables
    private ArrayAdapter arrayAdapterfriends;
    private ListView listViewfriends;
    private ArrayList<String> friends = new ArrayList<String>();

    //search box
    private EditText searchbox;
    private String selectedfriend = "";
    private ArrayList<String> searchfriends = new ArrayList<String>();

    //firebase variables
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    private Double goldcalc =0.0; //calculated gold variable to help with updating the gold of the user
    private String rates; //todays rates
    private double goldoffriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending_coins);

        //get today's exchange rate from the prefs file
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        rates = settings.getString("todayrate", rates);

        curruser = mAuth.getCurrentUser().getEmail();

        //intialize list view for coins
        listViewcoins = (ListView) findViewById(R.id.listviewcoins);
        listViewcoins.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //if the coins are selected then add them to the list of selected coins for later use
        listViewcoins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();

                if(selectedcoins.contains(selecteditem)) {
                    selectedcoins.remove(selecteditem);
                }else {
                    selectedcoins.add(selecteditem);
                }
            }
        });

        //intialize list view for friends
        listViewfriends = (ListView) findViewById(R.id.listviewfriends);
        listViewfriends.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //if the friends are selected then add them to the list of selected coins for later use
        listViewfriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();

                if(selectedfriend.equals(selecteditem)) {
                    selectedfriend = "";
                }else {
                    selectedfriend = selecteditem;
                }
            }
        });

        //initalize search textbox
        searchbox = (EditText) findViewById(R.id.emailsearch);
    }

    @Override
    public void onStart() {
        super.onStart();
        //COINS VIEW
        //clear arrays to avoid duplicates
        coins.clear();
        coinsandid.clear();
        //retrieve all coins in spare change and fill up arrays
        db.collection("users").document(curruser).collection("spare change").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    String id = queryDocumentSnapshots.getDocuments().get(i).get("id").toString();

                    coins.add(value + " " + curr);
                    coinsandid.put(id,value + " " + curr);
                }
                arrayAdaptercoins = new ArrayAdapter(SendingCoins.this, R.layout.viewcoinzrow,R.id.template, coins);
                listViewcoins.setAdapter(arrayAdaptercoins);
            }
        });

        //FRIENDS VIEW
        //get friends from database
        friends.clear();
        db.collection("users").document(curruser).collection("friends list").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    friends.add(queryDocumentSnapshots.getDocuments().get(i).get("Email").toString());
                }
                arrayAdapterfriends = new ArrayAdapter(SendingCoins.this, R.layout.viewcoinzrow,R.id.template, friends);
                listViewfriends.setAdapter(arrayAdapterfriends);
            }
        });
    }

    //look up friends with specfic username
    public void searchfriends(View view) {
        //clear results from last search
        searchfriends.clear();
        //if there has been text entered then search otherwise do nothing
        if (searchbox.getText().length()>0)  {
            db.collection("users").document(curruser).collection("friends list").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                    //find the users with that name and send to them
                    for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                       if (queryDocumentSnapshots.getDocuments().get(i).get("Email").toString().contentEquals(searchbox.getText()))
                           searchfriends.add(queryDocumentSnapshots.getDocuments().get(i).get("Email").toString());
                    }
                    //display results
                    arrayAdapterfriends = new ArrayAdapter(SendingCoins.this, R.layout.viewcoinzrow,R.id.template, searchfriends);
                    listViewfriends.setAdapter(arrayAdapterfriends);
                    listViewfriends.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                }
            });
        }
    }

    //calulate and send gold to the selected user
    public void calculateaandsend() {
        goldcalc = 0.0;
        goldoffriend =0;

        db.collection("users").document(selectedfriend).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {
                goldoffriend = Double.parseDouble(documentSnapshot.get("Gold").toString());
                Toast.makeText(SendingCoins.this, "" + goldoffriend, Toast.LENGTH_SHORT).show();
            }
        });

        //ask the user to confirm before sending
        AlertDialog.Builder builder = new AlertDialog.Builder(SendingCoins.this);
        builder.setCancelable(true);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to send these coins to " + selectedfriend + "?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 if (selectedcoins.size() > 0) {
                     db.collection("users").document(curruser).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                         @Override
                         public void onSuccess(@android.support.annotation.Nullable DocumentSnapshot documentSnapshot) {
                             for (int i = 0; i < selectedcoins.size(); i++) {
                                 goldcalc = goldcalc + (new ConverterandDialogs()).currconverter(rates, selectedcoins.get(i));

                                 //remove coins added from view
                                 for (int k = 0; k < coins.size(); k++) {
                                     if (coins.get(k).equals(selectedcoins.get(i)))
                                         coins.remove(k);
                                 }
                             }
                             //remove coins from spare change as sent
                             for (Map.Entry m : coinsandid.entrySet()) {
                                 if (selectedcoins.contains(m.getValue())) {
                                     String key = String.valueOf(m.getKey());
                                     db.collection("users").document(curruser).collection("spare change").document(key).delete();
                                 }
                             }

                             //added up previous gold to calculated gold and update today banked
                             goldcalc = goldcalc + goldoffriend;

                             //update list view
                             arrayAdaptercoins = new ArrayAdapter(SendingCoins.this, R.layout.viewcoinzrow, R.id.template, coins);
                             listViewcoins.setAdapter(arrayAdaptercoins);

                             db.collection("users").document(selectedfriend).update("Gold", goldcalc);

                             selectedcoins.clear();
                        }
                    });
                }
            }
        });

        //cancel sending
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //make sure user selects a friend and a set of coins to send
    public void sendcoins(View view) {
        if(selectedfriend.equals("")) {
            new ConverterandDialogs().OKdialog("Please select a friend to send coin to.", "Select a friend", SendingCoins.this).show();
        }
        else if (selectedcoins.size()<1) {
            new ConverterandDialogs().OKdialog("Please select coins to send to your friend", "Select coins", SendingCoins.this).show();
        }
        else {
            calculateaandsend();
        }
    }

    //go back to bank
    public void backtobank(View view) {
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }

    //reset the results to see the original list
    public void reset(View view) {
        arrayAdapterfriends = new ArrayAdapter(SendingCoins.this, R.layout.viewcoinzrow,R.id.template, friends);
        listViewfriends.setAdapter(arrayAdapterfriends);
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
