package s1640402.coinzgame.nishtha_coinz;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/* =====================================LOGIN VIEW=======================================
In this activity the user can:
-The first activity the user will see when they launch the app for the first time
-Allows the user to create an account and login
-Allows the user to sign into their existing account
* */
public class Loginview extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "EmailPassword";
    //initialize firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    //text fields for inputting email and password
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginview);

        //email and password fields
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

        //sign in buttons
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart() {
        super.onStart();
        //check if someone is signed
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    //============================Creating a Account=============================================
    //when creating an account we make sure the parameters for correct login details is being met
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        //break and do not create the account
        if (!validateForm()) {
            return;
        }

        //create new account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //set default values for fields under user
                            HashMap <String,String> fieldsHashMap = new HashMap<String,String>();
                            fieldsHashMap.put("Gold","" + 0.0);
                            fieldsHashMap.put("Today Banked","" + 0);
                            fieldsHashMap.put("Date", LocalDate.now()
                                               .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

                            db.collection("users").document(user.getEmail()).set(fieldsHashMap);
                            updateUI(user);
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Loginview.this, "Authentication failed, " +
                                            "please check your network connection. Or make sure the " +
                                            "email you are signing up with is not associated with an " +
                                            "existing account.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });

    }

    //============================Sign into an Existing Account=====================================
    //sign in user if they already have an account
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        //make sure it everything is correct before signing in user
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
              new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(Loginview.this, "Authentication failed.Check your" +
                                        " email or password.",
                                    Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
        });
    }

    //make sure password and email fields are not empty
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    //if all goes well then take user to main menu of game
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, MainView.class);
            startActivity(intent);
        }
    }

    //link the buttons to their respective commands
    @Override
    public void onClick(View v) {
        int i = v.getId();
        //if the sign up button is clicked then use the create account method to create the account
        //and sign the user in with it
        if (i == R.id.emailCreateAccountButton) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.emailSignInButton) {
            //if the sign in button is clicked then use the sign method to sign in
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}

