package com.mubaracktahir.blog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static com.mubaracktahir.blog.PostActivity.GALLARY_INTENT;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "SignUp FirebaseUser";
    private CircularImageView circularImageView;
    private EditText emailEdit_text;
    private EditText passwordEdit_text;
    private EditText nameedit_text;

    private EditText confirm_passwordEdit_text;
    private SignInButton mSignInBtn;

    public GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 1;
    public GoogleSignInOptions gso;
    private Button signIn_button;
    private TextView signUp_button;
    public static FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri profilePhoto = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        storageReference = FirebaseStorage.getInstance().getReference();
        circularImageView = findViewById(R.id.circularImageView);
        passwordEdit_text = findViewById(R.id.password_edittext);
        emailEdit_text = findViewById(R.id.email_edittext);
        nameedit_text = findViewById(R.id.name_edittext);
        signIn_button = findViewById(R.id.signIn2);
        confirm_passwordEdit_text = findViewById(R.id.comf_password_edittext);
        signIn_button = findViewById(R.id.signIn);
        signUp_button = findViewById(R.id.signup);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        mSignInBtn = findViewById(R.id.gooogle_signIn);

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
        circularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent = new Intent(Intent.ACTION_PICK);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLARY_INTENT);
            }
        });
        signIn_button.setOnClickListener(onclick -> {
            if (checkConnection()) {
                receiveInputFromUser();
            } else {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.signup), "No internet connection", Snackbar.LENGTH_LONG)
                        .setAction("Retry", n -> {

                            if (checkConnection())
                                receiveInputFromUser();
                            else
                                Toast.makeText(RegisterActivity.this, "On your mobile data or connect to a wifi", Toast.LENGTH_LONG).show();

                        });
                snackbar.show();
            }
        });
        signUp_button.setOnClickListener(onClick -> {

                    if (checkConnection()) {

                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(R.id.signup), "No internet connection", Snackbar.LENGTH_LONG)
                                .setAction("Retry", n -> {

                                    if (checkConnection())

                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));

                                    else
                                        Toast.makeText(RegisterActivity.this, "On your mobile data or connect to a wifi", Toast.LENGTH_LONG).show();
                                });
                        snackbar.show();
                    }
                }
        );
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public void receiveInputFromUser() {
        String emailAdress = emailEdit_text.getText().toString();
        String password = passwordEdit_text.getText().toString();
        String password1 = confirm_passwordEdit_text.getText().toString();
        String name = nameedit_text.getText().toString();
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

            if (!(password.equals(password1))) {
                passwordEdit_text.setError("Password do not match");
                confirm_passwordEdit_text.setError("Password do not match");
                passwordEdit_text.requestFocus();

            } else {
                signUpNewUsers(emailAdress, password,name);
            }
        }


    }


    public void signUpNewUsers(String email, String password,String name) {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            DatabaseReference mRef = databaseReference.child(uid);

                            if(profilePhoto == null) {
                                mRef.child("name").setValue(name);
                                mRef.child("image").setValue("default");

                            }
                            else if (profilePhoto != null){

                                StorageReference childRef = storageReference.child("Blog").child("profile_pictures").child(profilePhoto.getLastPathSegment());
                                progressDialog.setMessage("Loading...");
                                progressDialog.show();


                                childRef.putFile(profilePhoto).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        childRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl2 = uri;
                                                mRef.child("name").setValue(name);
                                                mRef.child("image").setValue(downloadUrl2.toString());
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }
                                });

                            }


                            Toast.makeText(getApplicationContext(), "added successfully", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();

                            Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();


                        } else {
                            // If sign in fails, display a message to the user.

                            progressDialog.dismiss();
                            try
                            {
                                throw task.getException();
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthWeakPasswordException weakPassword)
                            {
                                Log.d(TAG, "onComplete: weak_password");
                                Toast.makeText(getApplicationContext(),"password is too weak",Toast.LENGTH_LONG).show();
                                // TODO: take your actions!
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                            {
                                Log.d(TAG, "onComplete: malformed_email");
                                Toast.makeText(getApplicationContext(),"Email does not exist",Toast.LENGTH_LONG).show();

                                // TODO: Take your action
                            }
                            catch (FirebaseAuthUserCollisionException existEmail)
                            {
                                Log.d(TAG, "onComplete: exist_email");
                                Toast.makeText(getApplicationContext(),"email already exist",Toast.LENGTH_LONG).show();

                                // TODO: Take your action
                            }
                            catch (Exception e)
                            {
                                Log.d(TAG, "onComplete: " + e.getMessage());
                            }
                        }

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
       // mAuth.addAuthStateListener(authStateListener);
    }

    public boolean checkConnection() {
        boolean checkConnection = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
        return checkConnection;
    }

    public static class Alert2 extends DialogFragment {
        String message;

        public Alert2(String message) {
            this.message = message;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Alert")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }

                    })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setMessage(message);
            return builder.create();

        }

    }

    public void checkExistance(String email){
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {

                        boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                        if (isNewUser) {
                            Log.e("TAG", "Is New User!");
                        } else {
                            Log.e("TAG", "Is Old User!");
                        }

                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        if(requestCode == GALLARY_INTENT && resultCode == RESULT_OK){
            Uri uri = data.getData();

            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4,4)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setMaxZoom(500)
                    .setMaxCropResultSize(500,500)
                    .setMinCropResultSize(200,200)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                profilePhoto = resultUri;
                circularImageView.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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

                            databaseReference.child("name").setValue(name);
                            databaseReference.child("image").setValue(uri.toString());
                            progressDialog.dismiss();
                            Intent in = new Intent(RegisterActivity.this,HomeActivity.class);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(in);
                            // updateUI(user);
                        } else {


                        }


                    }
                });
    }
}
