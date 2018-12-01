package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
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

import javax.annotation.Nullable;

public class AddFriends extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //vairables for listview
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> selectedfriends = new ArrayList<String>();
    private ArrayList<String> users = new ArrayList<String>();

    //search box
    private EditText searchbox;
    private ArrayList<String> searchfriends = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        listView = (ListView) findViewById(R.id.listviewaddfriends);
        curruser = mAuth.getCurrentUser().getEmail();

        //set up the listview to keep track of the selected items in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();
                if(selectedfriends.contains(selecteditem)) {
                    selectedfriends.remove(selecteditem);
                }else {
                    selectedfriends.add(selecteditem);
                }
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //set up search box
        searchbox = (EditText) findViewById(R.id.emailsearch);
    }


    @Override
    protected void onStart() {
        super.onStart();

        //get all users
        db.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    if (!queryDocumentSnapshots.getDocuments().get(i).getId().equals(curruser)) {
                        users.add(queryDocumentSnapshots.getDocuments().get(i).getId());
                    }
                }
            }
        });

        //remove from the list created above users that the current user is already friends with
        db.collection("users").document(curruser).collection("friends list").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                    if (users.contains(queryDocumentSnapshots.getDocuments().get(i).getId())) {
                        Toast.makeText(AddFriends.this, "ma im in here", Toast.LENGTH_SHORT).show();
                        int index = users.indexOf(queryDocumentSnapshots.getDocuments().get(i).getId());
                        users.remove(index);
                    }
                }
                arrayAdapter = new ArrayAdapter(AddFriends.this, R.layout.viewcoinzrow,R.id.template, users);
                listView.setAdapter(arrayAdapter);
            }
        });
    }

    public void addfriends(View view) {

        //create a hashmap object to store the email of the current user as an object
        HashMap<String,String> user = new HashMap<String,String>();
        user.put("Email",curruser);

        for (int i =0; i<selectedfriends.size();i++) {
            //create a friend hashmap object to store all the users selected by the user
            HashMap<String,String> friend = new HashMap<String,String>();
            friend.put("Email",(selectedfriends.get(i)));

            //add the selected user as a friend in the friends list of the  current user and vice versa
            db.collection("users").document(curruser).collection("friends list").document(selectedfriends.get(i)).set(friend);
            db.collection("users").document(selectedfriends.get(i)).collection("friends list").document(curruser).set(user);
            int index = users.indexOf(selectedfriends.get(i));
            //update users list
            users.remove(index);
        }
        arrayAdapter = new ArrayAdapter(AddFriends.this, R.layout.viewcoinzrow,R.id.template, users);
        listView.setAdapter(arrayAdapter);
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
                    for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                        if (queryDocumentSnapshots.getDocuments().get(i).get("Email").toString().contentEquals(searchbox.getText()))
                            searchfriends.add(queryDocumentSnapshots.getDocuments().get(i).get("Email").toString());
                    }
                    //display results
                    arrayAdapter = new ArrayAdapter(AddFriends.this, R.layout.viewcoinzrow,R.id.template, searchfriends);
                    listView.setAdapter(arrayAdapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                }
            });
        }
    }

    //reset the results to see the original list
    public void reset(View view) {
        arrayAdapter = new ArrayAdapter(AddFriends.this, R.layout.viewcoinzrow,R.id.template, users);
        listView.setAdapter(arrayAdapter);
    }

    public void gobacktomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    public void viewfriends(View view) {
        Intent intent = new Intent(this, ViewFriends.class);
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
