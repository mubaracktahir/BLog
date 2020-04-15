package com.mubaracktahir.blog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.mubaracktahir.blog.model.Blog;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference mDabasereference;
    private Query query;
    private DatabaseReference likeRef;
    private FirebaseRecyclerOptions<Blog> options;
    private FloatingActionButton floatingActionButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Context context;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = getApplicationContext();
        floatingActionButton = findViewById(R.id.fab);
        mDabasereference = FirebaseDatabase.getInstance().getReference();
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        likeRef = FirebaseDatabase.getInstance().getReference().child("likes");
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent registerActivity = new Intent(HomeActivity.this, RegisterActivity.class);
                    registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(registerActivity);
                }
            }
        };
        query = mDabasereference
                .child("blog_post")
                .limitToLast(50);
        options =
                new FirebaseRecyclerOptions.Builder<Blog>()
                        .setQuery(query, Blog.class)
                        .build();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, PostActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                options) {
            @Override
            protected void onBindViewHolder(@NonNull BlogViewHolder holder, int position, @NonNull Blog model) {
                String post_key = getRef(position).getKey();
                holder.setTitle(model.getTitle());
                holder.setDescription(model.getDescription());
                holder.setImage(getApplicationContext(), model.getImage());
                holder.setUserName(model.getName());
                holder.setLikeButton(post_key);
                holder.setProfilePicture(getApplicationContext(),model.getProfileimage());
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(HomeActivity.this, BlogShowerActivity.class);
                        intent.putExtra("post_key", post_key);
                        startActivity(intent);
                    }
                });
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isLiked = true;
                            likeRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(isLiked) {

                                        if (dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                            likeRef.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                            isLiked = false;
                                        } else {
                                            likeRef.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue(1);
                                            isLiked = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    }
                });
            }

            @NonNull
            @Override
            public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.reecycler_view, parent, false);

                return new BlogViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.notifyDataSetChanged();
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView userNameText;
        private ImageButton imageButton; 
        private ImageView circularImageView;


        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imageButton = mView.findViewById(R.id.like);
            userNameText = mView.findViewById(R.id.user_name);
            userNameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //  Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                }
            });
            databaseReference  = FirebaseDatabase.getInstance().getReference().child("likes");
            firebaseAuth = FirebaseAuth.getInstance();
        }

        public void setTitle(String title) {
            TextView textView = mView.findViewById(R.id.blog_title);
            textView.setText(title);

        }

        public void setDescription(String description) {
            TextView textView = mView.findViewById(R.id.blog_description);
            textView.setText(description);
        }

        public void setImage(Context context, String image) {
            ImageView imageView = mView.findViewById(R.id.blog_image);
            Glide.with(context).load(image).into(imageView);
            /*Picasso.get().load(image).into(imageView);*/
        }

        public void setUserName(String userName) {

            userNameText.setText(userName);
            userNameText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }
        public void setProfilePicture(Context context,String imgUrl){
            circularImageView = mView.findViewById(R.id.profile);
            Glide.with(context).load("https://firebasestorage.googleapis.com/v0/b/blog-2697f.appspot.com/o/Blog%2Fprofile_pictures%2Fcropped4771803036198972741.jpg?alt=media&token=02571326-9158-4e54-8c29-7b76218b6a93").into(circularImageView);

        }

        DatabaseReference databaseReference;
        FirebaseAuth firebaseAuth;

        public void setLikeButton(String post_id) {
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_id).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                       imageButton.setImageResource(R.drawable.heart);
                    } else {

                        imageButton.setImageResource(R.drawable.nselect);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
