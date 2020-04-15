package com.mubaracktahir.blog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class PostActivity extends AppCompatActivity {
//    View decorView = getWindow().getDecorView();

    public static final int GALLARY_INTENT = 123;
    private static Uri downloadUri;
    private Button postBtn;
    private EditText postTitle;
    private EditText postDesc;
    private ImageButton selectedImage;
    private StorageReference mReference;
    private DatabaseReference databaseReference;
    private Uri selectedImageUri = null;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference firebaseUserProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("blog_post");
        firebaseUserProfile = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        postBtn = findViewById(R.id.uploadButton);
        postTitle = findViewById(R.id.title_edit);
        postDesc = findViewById(R.id.desc_edit);
        selectedImage = findViewById(R.id.selected_image);
        postDesc.getSelectionEnd();

        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");

                startActivityForResult(intent, GALLARY_INTENT);
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadToServer();

            }
        });

//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        /*View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
        (new View.OnSystemUiVisibilityChangeListener() {
         @Override
    public void onSystemUiVisibilityChange(int visibility) {
        // Note that system bars will only be2wq 9E_NAVIGATION, or FULLSCREEN flags are set.
        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            // TODO: The system bars are visible. Make any desired
            // adjustments to your UI, such as showing the action bar or
            // other navigational controls.
        } else {
            // TODO: The system bars are NOT visible. Make any desired
            // adjustments to your UI, such as hiding the action bar or
            // other navigational controls.
        }
    }
});*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLARY_INTENT && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .setMaxZoom(500)
                    .setMaxCropResultSize(1000, 1000)
                    .setMinCropResultSize(200, 200)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                selectedImageUri = resultUri;
                selectedImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void uploadToServer() {

        String title = postTitle.getText().toString();
        String desc = postDesc.getText().toString();


        if ((TextUtils.isEmpty(title) && TextUtils.isEmpty(title))) {
            postTitle.setError("A title is required!");
            postDesc.setError("A description is required!");
            postTitle.setFocusable(true);
        } else if (TextUtils.isEmpty(desc)) {
            postDesc.setError("A description is required!");
            postDesc.setFocusable(true);
        } else if (TextUtils.isEmpty(title)) {

            postTitle.setError("A title is required!");
            postTitle.setFocusable(true);

        } else if (!(TextUtils.isEmpty(desc) && TextUtils.isEmpty(title) && selectedImageUri != null)) {
            StorageReference storageReference = mReference.child("Blog").child("photos").child(selectedImageUri.getLastPathSegment());
            progressDialog.setMessage("Loading...");
            progressDialog.show();


            storageReference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl2 = uri;
                            firebaseUserProfile.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    DatabaseReference mdatabaseRef = databaseReference.push();
                                    mdatabaseRef.child("title").setValue(title);
                                    mdatabaseRef.child("description").setValue(desc);
                                    mdatabaseRef.child("image").setValue(downloadUrl2.toString());
                                    mdatabaseRef.child("uid").setValue(firebaseUser.getUid());
                                    mdatabaseRef.child("name").setValue(dataSnapshot.child("name").getValue());
                                    mdatabaseRef.child("profileimage").setValue(dataSnapshot.child("image").getValue());

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            progressDialog.dismiss();
                            Intent i = new Intent(PostActivity.this, HomeActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);

                        }
                    });
                }
            });

        }

    }
}
