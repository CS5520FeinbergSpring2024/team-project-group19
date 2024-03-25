package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private List<Event> eventsList = new ArrayList<>();
    protected TextView userIdTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // init navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        // Retrieve the user ID from sharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        // Set the user ID to the TextView
        TextView userIdTextView = findViewById(R.id.userId_textview);
        userIdTextView.setText(userId);

        RecyclerView myEventRecyclerView = findViewById(R.id.my_events_recyclerview);
        RecyclerView.LayoutManager lLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myEventRecyclerView.setLayoutManager(lLayoutManager);

        EventAdapter eventAdapter = new EventAdapter(HomeActivity.this, eventsList);
        myEventRecyclerView.setAdapter(eventAdapter);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        databaseEvents.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Event event = snapshot.getValue(Event.class);
                if (event != null) {
                    eventsList.add(event); // Add the event to your list
                    eventAdapter.notifyItemInserted(eventsList.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Logout confirmation Dialog from Home page.
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


}