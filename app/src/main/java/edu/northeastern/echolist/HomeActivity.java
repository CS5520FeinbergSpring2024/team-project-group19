package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class HomeActivity extends AppCompatActivity {
    private List<Event> eventsList = new ArrayList<>();
    private UniqueEventList sortedEventsList = new UniqueEventList(); // Keeps all events sorted
    private List<Event> upcomingEventsList = new ArrayList<>(); //Only events to be shown
    private EventAdapter upcomingEventAdapter;
    private GiftAdapter giftAdapter;
    private GiftViewModel giftViewModel;
    private RecyclerView trendingGiftsRecyclerView;
    private final List<Gift> gifts = new ArrayList<>();
    private final List<GiftItem> giftItems = new ArrayList<>();
    private List<String> favoriteGiftIds = new ArrayList<>();


    public class UniqueEventList extends ArrayList<Event> {
        private Set<String> eventIds = new HashSet<>();

        @Override
        public boolean add(Event event) {
            if (event != null && !eventIds.contains(event.getEventId())) {
                eventIds.add(event.getEventId());
                return super.add(event);
            }
            return false;
        }

    }


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

        updateUpcomingEvents();

        RecyclerView myEventRecyclerView = findViewById(R.id.my_events_recyclerview);
        RecyclerView.LayoutManager lLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        myEventRecyclerView.setLayoutManager(lLayoutManager);

        EventAdapter eventAdapter = new EventAdapter(HomeActivity.this, eventsList);
        myEventRecyclerView.setAdapter(eventAdapter);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        Query eventsByUser = databaseEvents.orderByChild("userId").equalTo(userId);

        ImageView userIcon = findViewById(R.id.user_icon);
        userIcon.setOnClickListener(v -> {
            // Launch ProfileActivity on user icon click
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
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


        giftViewModel = new ViewModelProvider(this).get(GiftViewModel.class);
        trendingGiftsRecyclerView = findViewById(R.id.trending_gift_recyclerview);
        setupGiftRecyclerView();

        if (giftViewModel.getFavoriteGiftIds().getValue() == null) {
            giftViewModel.fetchUserFavorites(getUserId());
        }
        giftViewModel.getGiftItemsLiveData().observe(this, giftItems -> {
            if (giftItems != null && !giftItems.isEmpty()) {
                initializeGiftAdapter(giftItems);
            }
        });
        giftViewModel.getFavoriteGiftIds().observe(this, favoriteGiftIds -> {
            if (favoriteGiftIds != null) {
                this.favoriteGiftIds = favoriteGiftIds;
                Log.d("HomeActivity", "Favorite gift IDs: " + favoriteGiftIds);
            }
        });

        giftViewModel.fetchGiftData(getUserId(), 5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUpcomingEvents();
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("eventDeleted", false)) {
            String eventId = intent.getStringExtra("deletedEventId");
            String eventTitle = intent.getStringExtra("deletedEventTitle");
            String eventLocation = intent.getStringExtra("deletedEventLocation");
            String eventDate = intent.getStringExtra("deletedEventDate");
            String userId = intent.getStringExtra("deletedEventUserId");

            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Event deleted", Snackbar.LENGTH_LONG);
            snackbar.setAction("Undo", v -> {
                Event restoredEvent = new Event(eventId, userId, eventTitle, eventLocation, eventDate, "","", new ArrayList<>());

                DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");
                databaseEvents.child(eventId).setValue(restoredEvent).addOnSuccessListener(aVoid -> Toast.makeText(HomeActivity.this, "Event restored", Toast.LENGTH_SHORT).show()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "Failed to restore event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
            snackbar.show();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUpcomingEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void updateUpcomingEvents() {
        sortedEventsList = new UniqueEventList();

        // Upcoming events
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");
        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

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

        handleUpcomingEventChildAddedFriends(userId);
    }


    private void setupGiftRecyclerView() {
        RecyclerView.LayoutManager giftLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        trendingGiftsRecyclerView.setLayoutManager(giftLayoutManager);

    }

    private void initializeGiftAdapter(List<GiftItem> giftItems) {
        if (giftAdapter == null) {
            giftAdapter = new GiftAdapter(this, giftItems, new GiftAdapter.OnGiftFavoriteListener() {
                @Override
                public void onGiftFavoriteChanged(Gift gift, boolean isFavorite) {
                    if (isFavorite) {
                        addToFavorites(gift.getGiftId());
                    } else {
                        removeFromFavorites(gift.getGiftId());
                    }
                }
            }, giftItem -> {
                Gift gift = giftItem;
                boolean isFavorite = favoriteGiftIds.contains(gift.getGiftId());
                String buttonText = isFavorite ? "Remove from Favorites" : "Add to Favorites";
                GiftDialog dialog = GiftDialog.newInstance(gift.getGiftId(), gift.getName(), gift.getDescription(), buttonText, (giftId, giftName) -> {
                    if (isFavorite) {
                        removeFromFavorites(gift.getGiftId());
                    } else {
                        addToFavorites(gift.getGiftId());
                    }
                    for (int i = 0; i < giftItems.size(); i++) {
                        if (giftItems.get(i).getGiftId().equals(giftId)) {
                            giftItems.get(i).setFavorite(!isFavorite);
                            giftAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                });
                dialog.setOnDismissListener(() -> giftAdapter.notifyDataSetChanged());
                dialog.show(getSupportFragmentManager(), "GiftDialog");
            });
            trendingGiftsRecyclerView.setAdapter(giftAdapter);
        } else {
            giftAdapter.setGiftItems(giftItems);
        }
    }

    private void addToFavorites(String giftId) {
        giftViewModel.addGiftToUserFavorites(getUserId(), giftId, new GiftViewModel.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(HomeActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                Log.d("HomeActivity", "Gift added to favorites: " + giftId);
                Log.d("HomeActivity", "Current favorite gift IDs: " + favoriteGiftIds);
                favoriteGiftIds.add(giftId);
                for (int i = 0; i < giftItems.size(); i++) {
                    if (giftItems.get(i).getGiftId().equals(giftId)) {
                        giftItems.get(i).setFavorite(true);
                        giftAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Error adding to favorites: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromFavorites(String giftId) {
        giftViewModel.removeGiftFromUserFavorites(getUserId(), giftId, new GiftViewModel.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(HomeActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                Log.d("HomeActivity", "Gift removed from favorites: " + giftId);
                Log.d("HomeActivity", "Current favorite gift IDs: " + favoriteGiftIds);
                favoriteGiftIds.remove(giftId);
                for (int i = 0; i < giftItems.size(); i++) {
                    if (giftItems.get(i).getGiftId().equals(giftId)) {
                        giftItems.get(i).setFavorite(false);
                        giftAdapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomeActivity.this, "Error removing from favorites: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getGiftPosition(String giftId) {
        for (int i = 0; i < gifts.size(); i++) {
            if (gifts.get(i).getGiftId().equals(giftId)) {
                return i;
            }
        }
        return -1;
    }

    private void handleUpcomingEventChildAdded(DataSnapshot snapshot) {
        Event event = snapshot.getValue(Event.class);
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        int numEventsToShow = sharedPreferences.getInt("numEvents", 2);
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
                upcomingEventUpdater(numEventsToShow);

            }
        }
    }


    private void upcomingEventUpdater(Integer numEventsToShow) {
        if (sortedEventsList.size() > numEventsToShow) {
            sortedEventsList.subList(numEventsToShow, sortedEventsList.size()).clear();
        }

        upcomingEventsList.clear();
        upcomingEventsList.addAll(sortedEventsList);
        upcomingEventAdapter.notifyDataSetChanged();

    }


    private void handleUpcomingEventChildAddedFriends(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Set<String> friendsSet = new HashSet<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            // Add debug log to check if currentUser is retrieved correctly
                            Log.d("Debuggy", "Current user: " + currentUser.getUserId());
                            List<String> friendsList = currentUser.getFriends();

                            if (friendsList != null && !friendsList.isEmpty()) {
                                for (String friendId : friendsList) {
                                    Log.d("ProfileActivity", "Friend ID: " + friendId);
                                    friendsSet.add(friendId);
                                }
                            }
                        }
                    }
                    Log.d("HandleFriends", "Friends set: " + friendsSet);

                    for (String friendId : friendsSet) {
                        fetchPublicEventsForFriend(friendId);
                    }

                } else {
                    Log.d("HandleFriends", "No data found for user: " + userId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HandleFriends", "Error fetching user data: " + databaseError.getMessage());
            }
        });
    }


    private void fetchPublicEventsForFriend(String friendId) {
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        int numEventsToShow = sharedPreferences.getInt("numEvents", 2);
        if (friendId == null) {
            upcomingEventUpdater(numEventsToShow);
        }

        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsRef.orderByChild("userId").equalTo(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    if (null != event.getVisibility()) {
                        if (event != null && event.getVisibility().equals("Public")) {

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
                            }
                        }
                }
                }

                Collections.sort(sortedEventsList, eventDateComparator);
                upcomingEventUpdater(numEventsToShow);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchEvents", "Error fetching events: " + databaseError.getMessage());
            }
        });
    }


    // Comparator for sorted events.
    Comparator<Event> eventDateComparator = (e1, e2) -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = dateFormat.parse(e1.getDate());
            Date date2 = dateFormat.parse(e2.getDate());
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    };
}