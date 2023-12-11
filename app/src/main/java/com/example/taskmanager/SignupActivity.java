package com.example.taskmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private EditText etSignupName, etSignupEmail, etSignupPassword;
    private Button btnSignup;
    private TextView tvSignupMsg;

    private Context context;

    String[] roles = {"Project Member", "Project Manager"};
    AutoCompleteTextView tvSignupRole;
    ArrayAdapter<String> roleAdapter;

    // for Firebase Authentication
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firebase connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        context = getApplicationContext();

        etSignupName = findViewById(R.id.etSignupName);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvSignupMsg = findViewById(R.id.tvSignupMsg);

        // assign items to Role field
        tvSignupRole = findViewById(R.id.tvSignupRole);
        roleAdapter = new ArrayAdapter<String>(this, R.layout.background_memberlist, roles);
        tvSignupRole.setAdapter(roleAdapter);
        //tvSignupRole.setText("Patient");

        auth = FirebaseAuth.getInstance();


        // sign up function
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = etSignupName.getText().toString().trim();
                String userEmail = etSignupEmail.getText().toString().trim();
                String pass = etSignupPassword.getText().toString().trim();
                String userRole_temp = tvSignupRole.getText().toString();
                String userRole;
                if (userRole_temp.equals("Project Manager"))
                    userRole = "Doctor";
                else
                    userRole = "Patient";

                //clear previous error msg
                tvSignupMsg.setText("");

                if (userName.isEmpty()) {
                    etSignupName.setError("Role cannot be empty");
                }

                if (userEmail.isEmpty()) {
                    etSignupEmail.setError("Email cannot be empty");
                }

                if (pass.isEmpty()) {
                    etSignupPassword.setError("Password cannot be empty");
                }

                if (userRole.isEmpty()) {
                    tvSignupRole.setError("Role cannot be empty");
                }

                if (!userName.isEmpty() && !userEmail.isEmpty() && !pass.isEmpty() && !userRole.isEmpty()) {
                    // create an user account
                    auth.createUserWithEmailAndPassword(userEmail, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = auth.getCurrentUser();
                                assert currentUser != null;
                                final String currentUserId = currentUser.getUid();

                                // create a userMap so that we can create a user in the User Collection in Firebase Firestore
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("userName", userName);
                                userObj.put("userEmail", userEmail);
                                userObj.put("userRole", userRole);

                                // adding users to Firestore database
                                userCollection.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                    String name = task.getResult().getString("userName");

                                                                    Toast.makeText(SignupActivity.this, "SignUp Successful", Toast.LENGTH_LONG).show();

                                                                    // if the user is registered successfully, redirect the user to DashBoardActivity
                                                                    // save the information to intent
                                                                    Intent i = new Intent(SignupActivity.this, MainActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putString("userName", userName);
                                                                    bundle.putString("userId", currentUserId);
                                                                    bundle.putString("userEmail", userEmail);
                                                                    bundle.putString("userRole", userRole);
                                                                    i.putExtras(bundle);

                                                                    //redirect to MainActivity
                                                                    startActivity(i);
                                                                }
                                                            }
                                                        });
                                            }
                                        });

                            } else {
                                // otherwise, display error msg
                                tvSignupMsg.setText("Signup Failed: " + task.getException().getMessage());
                            }
                        }
                    });
                }
            }
        });
    }
}