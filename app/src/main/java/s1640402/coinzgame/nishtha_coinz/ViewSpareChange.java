package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class ViewSpareChange extends AppCompatActivity {

    private ListView listView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String curruser;
    private ArrayList<String> coinz = new ArrayList<String>();
    private ArrayList<String> selectedcoinz = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sparechange);

        listView = (ListView) findViewById(R.id.listview);
        mAuth = FirebaseAuth.getInstance();
        curruser = mAuth.getCurrentUser().getEmail();

        db.collection("users").document(curruser).collection("sparechange").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                //Toast.makeText(PlayGame.this, queryDocumentSnapshots.size() +"", Toast.LENGTH_SHORT).show();
               for(int i = 0; i<queryDocumentSnapshots.size();i++) {
                   String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                   String curr = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                   coinz.add(value + " " + curr);
               }
                ArrayAdapter arrayAdapter = new ArrayAdapter(ViewSpareChange.this, R.layout.viewcoinzrow,R.id.template, coinz);
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
        if(selectedcoinz.size()<0)
        Toast.makeText(this, selectedcoinz.get(0), Toast.LENGTH_SHORT).show();
    }

    public  void gobacktobank(View view) {
        Intent intent = new Intent(this, Bank.class);
        startActivity(intent);
    }
}
