package s1640402.coinzgame.nishtha_coinz;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/* =====================================VIEW FRIENDS VIEW=======================================
In this activity the user can:
-User can see their current friends
-Remove friends
-Access main menu and the add friends view
* */
public class ViewFriends extends AppCompatActivity {

    //fire base variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;

    //list variables
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> selectedfriends = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends);

        listView = findViewById(R.id.listviewfriends);
        mAuth = FirebaseAuth.getInstance();
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

        //loads list of user's current friends
        db.collection("users").document(curruser).collection("friends list")
          .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                  List<DocumentSnapshot> friendslist = queryDocumentSnapshots.getDocuments();
                  //loop through the list of friends from the database and add them to the arraylist
                  //being feed to the listview
                  for(int i = 0; i<friendslist.size();i++) {
                      friends.add(friendslist.get(i).getId());
                  }
                  //setup and added adapter to the view
                  arrayAdapter = new ArrayAdapter(ViewFriends.this,
                                                       R.layout.viewcoinzrow,R.id.template, friends);
                  listView.setAdapter(arrayAdapter);
              }
        });
    }

    //======================================Remove Friends==========================================
    //remove selected friends from the friends list of the user and vice versa
    public void removefriends() {
        //ask the user to confirm before removing friends
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewFriends.this);
        builder.setCancelable(true);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to remove these friends?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if the list isn't empty remove the friends
                if (selectedfriends.size() > 0) {
                    db.collection("users").document(curruser).collection("friends list")
                      .get().addOnSuccessListener(
                           new OnSuccessListener<QuerySnapshot>() {
                           @Override
                           public void onSuccess(@Nullable QuerySnapshot queryDocumentSnapshots) {
                               for (int i =0; i<selectedfriends.size();i++) {
                                   for (int k =0; k<queryDocumentSnapshots.size();k++) {
                                       String email = queryDocumentSnapshots.getDocuments().get(k).get("Email").toString();
                                        //when you find the friend delete them and then the user
                                        //from their friends list too
                                        if (selectedfriends.get(i).equals(email)) {

                                            db.collection("users").document(curruser)
                                                    .collection("friends list").document(email).delete();

                                            db.collection("users").document(email)
                                                    .collection("friends list").document(curruser).delete();

                                            //remove from the array list used to show the friends
                                            //of the current user
                                            friends.remove(friends.indexOf(email));
                                        }
                                   }
                               }
                               arrayAdapter = new ArrayAdapter(ViewFriends.this,
                                                     R.layout.viewcoinzrow,R.id.template, friends);
                               listView.setAdapter(arrayAdapter);
                           }
                    });
                    //shows dialog when successful
                    new ConverterandDialogs().OKdialog("Removed successfully.",
                                                   "Success",ViewFriends.this).show();
                }
            }
        });
        //===================================END Remove Friends=====================================

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

    //check if the list of friends selected isnt empty to avoid errors
    public void removefriendsvalidate(View view) {
        if (selectedfriends.size()>0)
            removefriends();
        else
            new ConverterandDialogs().OKdialog("Please select friends to remove.",
                                        "No friend selected",ViewFriends.this).show();
    }

    //back to main menu
    public void gobacktomain(View view) {
        Intent intent = new Intent(this, MainView.class);
        startActivity(intent);
    }

    //back to add friends view
    public void addmorefriends(View view) {
        Intent intent = new Intent(this, AddFriends.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
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
