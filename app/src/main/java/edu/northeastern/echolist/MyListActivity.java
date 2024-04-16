package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyListActivity extends AppCompatActivity {

    private List<Event> eventsList = new ArrayList<>();
    protected TextView userIdTextView;
    private Event deletedEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        // Retrieve the user ID from sharedPreference
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        RecyclerView myListRecyclerView = findViewById(R.id.mylist_recyclerview);
        RecyclerView.LayoutManager lLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        myListRecyclerView.setLayoutManager(lLayoutManager);

        MyListAdapter myListAdapter = new MyListAdapter(this, eventsList);
        myListRecyclerView.setAdapter(myListAdapter);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        Query eventsByUser = databaseEvents.orderByChild("userId").equalTo(userId);

        eventsByUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Event event = snapshot.getValue(Event.class);
                if (event != null) {
                    eventsList.add(event); // Add the event to your list
                    myListAdapter.notifyItemInserted(eventsList.size() - 1);
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

        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home) {
                Intent intent = new Intent(MyListActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.page_add_post) {
                Intent intent = new Intent(MyListActivity.this, AddItemActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("eventDeleted", false)) {
            String eventId = intent.getStringExtra("deletedEventId");
            String eventTitle = intent.getStringExtra("deletedEventTitle");
            String eventLocation = intent.getStringExtra("deletedEventLocation");
            String eventDate = intent.getStringExtra("deletedEventDate");
            String userId = intent.getStringExtra("deletedEventUserId");

            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Event deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Event restoredEvent = new Event(eventId, userId, eventTitle, eventLocation, eventDate, "","");

                    DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");
                    databaseEvents.child(eventId).setValue(restoredEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MyListActivity.this, "Event restored", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MyListActivity.this, "Failed to restore event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Logout confirmation Dialog from Home page.
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(MyListActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}