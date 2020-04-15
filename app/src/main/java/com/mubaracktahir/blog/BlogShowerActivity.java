package com.mubaracktahir.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mubaracktahir.blog.R;

public class BlogShowerActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    String name ;
    private ImageView imageView;
    private TextView titlTextView, descTextView;
    String title ;
    String description ;
    String image ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_shower);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("blog_post");
        Intent intent = getIntent();
        imageView = findViewById(R.id.image_blog_post);
        titlTextView = findViewById(R.id.title_text);
        descTextView = findViewById(R.id.description_text);
        String postKey = intent.getStringExtra("post_key");

        DatabaseReference mRef = databaseReference.child(postKey);
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 name = dataSnapshot.child("name").getValue().toString();
                 title = dataSnapshot.child("title").getValue().toString();
                 description = dataSnapshot.child("description").getValue().toString();
                 image = dataSnapshot.child("image").getValue().toString();
                Toast.makeText(getApplicationContext(),image+"  "+postKey,Toast.LENGTH_LONG).show();
                Glide.with(getApplicationContext()).load(image).into(imageView);
                titlTextView.setText(title);
                descTextView.setText(description);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
