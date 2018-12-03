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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

/* =====================================ADD FRIENDS VIEW=======================================
In this activity the user can:
-Select users from a list a users that the current user is not friends with and add them as a friend
-Go to the view friends view to see their friends lists
-Search for specific users to add
-Go back to the main menu
* */
public class AddFriends extends AppCompatActivity {

    //firebase variables
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //variables for list view
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> selectedfriends = new ArrayList<>();
    private ArrayList<String> users = new ArrayList<>();

    //search box
    private EditText searchbox;
    private ArrayList<String> searchfriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        listView = findViewById(R.id.listviewaddfriends);
        curruser = mAuth.getCurrentUser().getEmail();

        //set up the list view to keep track of the selected items in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selecteditem = ((TextView) view).getText().toString();
                //when clicked checked if the item clicked on is in selectedfriends
                //if it is remove it hence untick else add them hence tick the item
                if(selectedfriends.contains(selecteditem)) {
                    selectedfriends.remove(selecteditem);
                }else {
                    selectedfriends.add(selecteditem);
                }
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //initialize search text box
        searchbox = findViewById(R.id.emailsearch);
    }

    //============Setup List of users that the current user is not friends with============
    @Override
    protected void onStart() {
        super.onStart();

        db.collection("users").get().addOnSuccessListener(
                new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                        //loop through all users in database and add to arraylist of users
                        for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                            users.add(queryDocumentSnapshots.getDocuments().get(i).getId());
                        }

                        //remove the current user from that list
                        for (int i =0; i<users.size();i++) {
                            if (users.get(i).equals(curruser)) {
                                users.remove(i);
                                break;
                            }
                        }

                        //remove the users friends from this list
                        db.collection("users").document(curruser)
                           .collection("friends list").get().addOnSuccessListener(
                           new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                                    //get list of users friends
                                    List<DocumentSnapshot> friendslist = queryDocumentSnapshots.getDocuments();
                                    //loop through the current list of users and remove the ones
                                    //that are friends with the user
                                    for (int i =0; i<users.size();i++) {
                                        for (int k =0; k<friendslist.size();k++) {
                                            if (users.get(i).equals(friendslist.get(k).getId())) {
                                                users.remove(i);
                                            }
                                        }
                                    }
                                    //set up the array adapter with the layout template file and
                                    //fill with the updated users list
                                    arrayAdapter = new ArrayAdapter(AddFriends.this,
                                                        R.layout.viewcoinzrow,R.id.template, users);
                                    //add to listview
                                    listView.setAdapter(arrayAdapter);
                            }
                        });
                    }
                });
    }

    //============Add the selected list of users as friends for the current user============
    public void addfriends(View view) {
        //create a hash map object to store the email of the current user
        HashMap<String,String> user = new HashMap<>();
        user.put("Email",curruser);

        //loop through the selected user and add them under the current users friends list
        for (int i =0; i<selectedfriends.size();i++) {
            //create a hashmap to store all the users selected by the user
            HashMap<String,String> friend = new HashMap<>();
            friend.put("Email",(selectedfriends.get(i)));

            //add the selected user as a friend in the friends list of the  current user and vice versa
            db.collection("users").document(curruser)
                    .collection("friends list").document(selectedfriends.get(i)).set(friend);

            db.collection("users").document(selectedfriends.get(i))
                    .collection("friends list").document(curruser).set(user);

            //get index of this user in the users arraylist and remove as the current user is friends
            //with them
            int index = users.indexOf(selectedfriends.get(i));
            users.remove(index);
        }
        //set the adapter for list view again
        arrayAdapter = new ArrayAdapter(AddFriends.this,
                              R.layout.viewcoinzrow,R.id.template, users);
        listView.setAdapter(arrayAdapter);
    }

    //=========================================look up users with specific email===================
    public void searchfriends(View view) {
        //clear results from last search
        searchfriends.clear();
        //if there has been text entered then search otherwise do nothing
        if (searchbox.getText().length()>0)  {
            for (int i=0;i<users.size();i++) {
                //loop through the list of users if they match add the respective one to the search
                //results arraylist (searchfriends)
                if (users.get(i).contentEquals(searchbox.getText()))
                    searchfriends.add(users.get(i));
            }
            //display results
            arrayAdapter = new ArrayAdapter(AddFriends.this,
                    R.layout.viewcoinzrow,R.id.template, searchfriends);
            listView.setAdapter(arrayAdapter);
        }
    }

    //reset the results to see the original list
    public void reset(View view) {
        arrayAdapter = new ArrayAdapter(AddFriends.this,
                           R.layout.viewcoinzrow,R.id.template, users);
        listView.setAdapter(arrayAdapter);
    }

    //go to the main menu
    public void gobacktomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    //go to the view friends view
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
