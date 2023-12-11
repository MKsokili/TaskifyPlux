package com.example.taskmanager;

import static androidx.appcompat.widget.ResourceManagerInternal.get;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;

    private EditText etLoginEmail, etLoginPassword;

    private TextView tvSignup, tvLoginMsg;

    // for Firebase Login / Signup authentication
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    //Firebase connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userCollection = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Context context = this;

        checkInternetConnection();

        // Get a reference to the SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);

        // Clear all data in the SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit(); // or editor.apply()


        auth = FirebaseAuth.getInstance();

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvLoginMsg = findViewById(R.id.tvLoginMsg);

        getUserInfo(context);

        // set login button onClick listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = etLoginEmail.getText().toString();
                String pass = etLoginPassword.getText().toString();
                tvLoginMsg.setText("");

                if (!userEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(userEmail, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                                        getUserInfo(context);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                        tvLoginMsg.setText("Login Failed. Incorrect password or user does not exist.");
                                    }
                                });
                    } else {
                        etLoginPassword.setError("Password cannot be empty");
                    }
                } else if(userEmail.isEmpty()) {
                    etLoginEmail.setError("Email cannot be empty");
                } else {
                    etLoginEmail.setError("Please enter valid email");
                }
            }
        });

        // set signup textView onClick listener
        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }   //End of onCreate()
// ----------------------------------------------------------------------------------------------------------------------------------------
    // Get user information and redirect to MainActivity if the user logged in
    private void getUserInfo(Context context) {
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // if user is already logged in, get the user name and user id

            //get the user Id
            final String currentUserId = currentUser.getUid();

            // query the Firestore collection to get the user information
            userCollection.whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Get the first document (there should be only one with the provided UID)
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                                // Access user information fields from the document
                                String userId = documentSnapshot.getString("userId");
                                String userName = documentSnapshot.getString("userName");
                                String userEmail = documentSnapshot.getString("userEmail");
                                String userRole = documentSnapshot.getString("userRole");

                                // save the information to intent
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("userName", userName);
                                bundle.putString("userId", userId);
                                bundle.putString("userEmail", userEmail);
                                bundle.putString("userRole", userRole);
                                i.putExtras(bundle);

                                //redirect to MainActivity
                                context.startActivity(i);
                            } else {
                                // No user document found for the given UID
                                // Handle the case where the user data is missing
                                Toast.makeText(LoginActivity.this, "Please create an account", Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle any errors that occurred during the retrieval process
                        }
                    });
        }

    }   // End of getUserInfo()
//----------------------------------------------------------------------------------------------------------------------------------------
    private boolean checkInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            //Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, "Not connected to Internet. Please check.", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }


}