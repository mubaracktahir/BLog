package com.mubaracktahir.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

public class LoginActivity extends AppCompatActivity {
    private CircularImageView circularImageView;
    private EditText emailEdit_text;
    private EditText passwordEdit_text;
    private SignInButton mSignInBtn;
    private Button signIn_button;
    private TextView signUp_button;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    public GoogleSignInClient mGoogleSignInClient;
    public GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 1;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        circularImageView = findViewById(R.id.circularImageView2);
        passwordEdit_text = findViewById(R.id.password_edittext2);
        emailEdit_text = findViewById(R.id.email_edittext2);
        signIn_button = findViewById(R.id.signIn2);
        mSignInBtn = findViewById(R.id.goooglesignIn);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        progressDialog = new ProgressDialog(this);
        signIn_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveInputFromUser();
            }
        });
        authStateListener  = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


            }
        };




         gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public void receiveInputFromUser() {
        String emailAdress = emailEdit_text.getText().toString();
        String password = passwordEdit_text.getText().toString();
        if (TextUtils.isEmpty(emailAdress)) {
            emailEdit_text.setError("Email required");
            emailEdit_text.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            passwordEdit_text.setError("password is required");
            passwordEdit_text.requestFocus();
        } else if (TextUtils.isEmpty(emailAdress) && TextUtils.isEmpty(password)) {
            emailEdit_text.setError("field required!");
            passwordEdit_text.setError("field required!");
            emailEdit_text.requestFocus();
        } else if (!(TextUtils.isEmpty(emailAdress) && TextUtils.isEmpty(password))) {


                authenticateUser(emailAdress, password);
        }


    }

    public void authenticateUser(@NonNull String emailAddress, @NonNull String password) {

        if (TextUtils.isEmpty(emailAddress))
            Toast.makeText(getApplicationContext(), "Email address is required", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(password))
            Toast.makeText(getApplicationContext(), "Password is required", Toast.LENGTH_SHORT).show();
        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(password))
            Toast.makeText(getApplicationContext(), "Fields can not be blank!", Toast.LENGTH_SHORT).show();
        else {

            progressDialog.setMessage("loading ");
            progressDialog.setCancelable(false);
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(emailAddress, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {


                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        RegisterActivity.Alert2 alert2 = new RegisterActivity.Alert2("Invalid details");
                        alert2.show(getSupportFragmentManager(), "");
                    }
                }
            });
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            progressDialog.setMessage("Signin with Google...");
            progressDialog.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
           //     Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
              //  updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
       // Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
       // showProgressBar();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

                            String name = user.getDisplayName();
                            Uri uri = user.getPhotoUrl();

                            databaseReference.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                            databaseReference.child("image").setValue(uri.toString());
                            progressDialog.dismiss();
                            Intent in = new Intent(LoginActivity.this,HomeActivity.class);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(in);
                           // updateUI(user);
                        } else {


                        }


                    }
                });
    }
}
