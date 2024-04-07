package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private List<Event> eventsList = new ArrayList<>();
    private List<Event> sortedEventsList = new ArrayList<>(); // Keeps all events sorted
    private List<Event> upcomingEventsList = new ArrayList<>(); //Only events to be shown
    private EventAdapter upcomingEventAdapter;
    private GiftAdapter giftAdapter;
    private GiftViewModel giftViewModel;
    private List<Gift> trendingGifts = new ArrayList<>();
    private List<String> favoritedGiftIds = new ArrayList<>();
    protected TextView userIdTextView;
    private Event deletedEvent;


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
        RecyclerView.LayoutManager lLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        myEventRecyclerView.setLayoutManager(lLayoutManager);

        EventAdapter eventAdapter = new EventAdapter(HomeActivity.this, eventsList);
        myEventRecyclerView.setAdapter(eventAdapter);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        Query eventsByUser = databaseEvents.orderByChild("userId").equalTo(userId);

        ImageView userIcon = findViewById(R.id.user_icon);
        userIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch ProfileActivity on user icon click
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        eventsByUser.addChildEventListener(new ChildEventListener() {
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

        // Upcoming events
        RecyclerView upcomingEventRecyclerView = findViewById(R.id.upcoming_events_recyclerview);
        RecyclerView.LayoutManager upcomingLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        upcomingEventRecyclerView.setLayoutManager(upcomingLayoutManager);

        upcomingEventAdapter = new EventAdapter(HomeActivity.this, upcomingEventsList);
        upcomingEventRecyclerView.setAdapter(upcomingEventAdapter);

        Query upcomingEventsByUser = databaseEvents.orderByChild("userId")
                .equalTo(userId);

        upcomingEventsByUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                handleUpcomingEventChildAdded(snapshot);
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


        RecyclerView trendingGiftsRecyclerView = findViewById(R.id.trending_gift_recyclerview);
        RecyclerView.LayoutManager giftLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        trendingGiftsRecyclerView.setLayoutManager(giftLayoutManager);

        giftViewModel = new ViewModelProvider(this).get(GiftViewModel.class);

        giftViewModel.fetchUserFavoritedGiftIds(getUserId()).observe(this, favorites -> {
                    giftAdapter.setFavoritedGiftIds(favorites);
                });

        giftAdapter = new GiftAdapter(trendingGifts, favoritedGiftIds, (gift, isFavorite) -> {
            if (isFavorite) {
                giftViewModel.addGiftToUserFavorites(getUserId(), gift.getGiftId());
            } else {
                giftViewModel.removeGiftFromUserFavorites(getUserId(), gift.getGiftId());
            }
        }, gift -> {
            GiftDialog dialog = GiftDialog.newInstance(gift.getDescription());
            dialog.show(getSupportFragmentManager(), "GiftDialog");
        });

        trendingGiftsRecyclerView.setAdapter(giftAdapter);
        // fetch trending gifts from Firebase
        fetchTrendingGifts();

        Log.d("Gift View Model", "about to try to get user key");
        giftViewModel.getUserKey(getUserId()).observe(this, userKey -> {
            if (userKey != null) {
                // set up listener for favorited gift changes
                Log.d("Gift View Model", "user key: " + userKey);
                setupFavoritedGiftsListener(userKey);


            } else {
                Toast.makeText(HomeActivity.this, "Failed to fetch user key", Toast.LENGTH_SHORT).show();
            }
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
                            Toast.makeText(HomeActivity.this, "Event restored", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, "Failed to restore event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        return sharedPreferences.getString("username", "User");
    }

    // Logout confirmation Dialog from Home page.
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to log out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void setupFavoritedGiftsListener(String userKey) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
        DatabaseReference favoritedGiftsRef = userRef.child("favoritedGifts");
        Log.d("setupFavoriteGifts", "Got to the dbref node.");
        favoritedGiftsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.d("setupFavoriteGifts", "Snapshot does not exist.");
                    // create an empty "favoritedGifts" node for the user
                    favoritedGiftsRef.setValue(new HashMap<String, Boolean>() {{
                        put("placeholder", false); // using a placeholder to ensure it's created
                            }})
                            .addOnSuccessListener(aVoid -> {
                                // attach the listener to fetch the user's favorited gifts
                                attachFavoritedGiftsListener(userRef);
                            })
                            .addOnFailureListener(e -> {
                                // handle the error case
                                Log.e("setupFavoritedGifts", "Failed to create empty 'favoritedGifts' node", e);
                                Toast.makeText(HomeActivity.this, "Failed to create favorite gifts list.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // attach the listener to fetch the user's favorited gifts
                    attachFavoritedGiftsListener(userRef);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // handle the error case
                Toast.makeText(HomeActivity.this, "Failed to check favorite gifts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachFavoritedGiftsListener(DatabaseReference userRef) {
        userRef.child("favoritedGifts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String giftId = snapshot.getKey();
                if (!favoritedGiftIds.contains(giftId)) {
                    favoritedGiftIds.add(giftId);
                    Log.d("FavoritedGiftsListener", "added giftId" + giftId);
                    Log.d("FavoritedGiftsListener", "favoritedGiftIds: " + favoritedGiftIds);
                    giftAdapter.setFavoritedGiftIds(favoritedGiftIds);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // not needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String giftId = snapshot.getKey();
                favoritedGiftIds.remove(giftId);
                Log.d("FavoritedGiftsListener", "removed giftId" + giftId);
                Log.d("FavoritedGiftsListener", "favoritedGiftIds: " + favoritedGiftIds);
                giftAdapter.setFavoritedGiftIds(favoritedGiftIds);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // not needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to fetch user's favorite gifts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTrendingGifts() {
        DatabaseReference giftsRef = FirebaseDatabase.getInstance().getReference("gifts");
        giftsRef.orderByChild("isTrending").equalTo(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        trendingGifts.clear();
                        for (DataSnapshot giftSnapshot : dataSnapshot.getChildren()) {
                            Gift gift = giftSnapshot.getValue(Gift.class);
                            if (gift != null) {
                                trendingGifts.add(gift);
                            }
                        }
                        // randomly select gifts from the trendingGifts list
                        List<Gift> selectedGifts = getRandomGifts(trendingGifts, 5); // select 5 random gifts
                        // update the UI with the selected gifts
                        giftAdapter.setGifts(selectedGifts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // todo: error handling
                    }
                });
    }
    private List<Gift> getRandomGifts(List<Gift> gifts, int count) {
        List<Gift> randomGifts = new ArrayList<>(gifts);
        Collections.shuffle(randomGifts);
        return randomGifts.subList(0, Math.min(count, randomGifts.size()));
    }

    private void handleUpcomingEventChildAdded(DataSnapshot snapshot) {
        Event event = snapshot.getValue(Event.class);

        if (event != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date eventDate = null;
            try {
                eventDate = dateFormat.parse(event.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Included to make today inclusive.
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date today = calendar.getTime();

            if (eventDate != null && !eventDate.before(today)) {
                sortedEventsList.add(event);
                Collections.sort(sortedEventsList, eventDateComparator);

                if (sortedEventsList.size() > 2) {
                    sortedEventsList.subList(2, sortedEventsList.size()).clear();
                }

                upcomingEventsList.clear();
                upcomingEventsList.addAll(sortedEventsList);
                upcomingEventAdapter.notifyDataSetChanged();
            }
        }
    }

    // Comparator for sorted events.
    Comparator<Event> eventDateComparator = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date1 = dateFormat.parse(e1.getDate());
                Date date2 = dateFormat.parse(e2.getDate());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    };
}