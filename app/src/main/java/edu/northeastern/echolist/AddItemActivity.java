package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    protected TextView userIdTextView;
    private Button saveEventButton;
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner friendsSpinner;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;
    private Button deleteEventButton;
    private Button updateEventButton;
    private BottomNavigationView bottomNavigationView;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_add_post);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        // Set the user ID to the TextView
        userIdTextView = findViewById(R.id.userId_textview);
        userIdTextView.setText(userId);

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        saveEventButton = findViewById(R.id.saveEventButton);
        eventTitle = findViewById(R.id.eventTitle);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        friendsSpinner = findViewById(R.id.friendsSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        visibilitySpinner = findViewById(R.id.visibilitySpinner);
        deleteEventButton = findViewById(R.id.deleteEventButton);
        updateEventButton = findViewById(R.id.updateEventButton);
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
                    public void onClick(View v) {
                        showDateDialog();
                    }
        });

        eventLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity();
            }
        });

        saveEventButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String title = eventTitle.getText().toString();
                        String location = eventLocation.getText().toString();
                        String date = eventDate.getText().toString();

                        if (title.isEmpty() || date.isEmpty()) {
                            new AlertDialog.Builder(AddItemActivity.this)
                                    .setTitle("Missing Information")
                                    .setMessage("Both Title and Date fields are required.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }

                        String eventId = databaseEvents.push().getKey();
                        Event event = new Event(eventId, userId, title, location, date);

                        databaseEvents.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                snapshot.getRef().setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            deleteEventButton.setVisibility(View.VISIBLE);
            updateEventButton.setVisibility(View.VISIBLE);
            saveEventButton.setVisibility(View.GONE);
            getEventDetailsAndUpdate(eventId);
        }

        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AddItemActivity.this)
                        .setMessage("Are you sure you want to delete this event")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference eventRef = databaseEvents.child(eventId);

                                eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Event event = snapshot.getValue(Event.class);
                                        if (event != null) {
                                            Event deletedEvent = new Event(event.getEventId(),
                                                    event.getUserId(), event.getTitle(),
                                                    event.getLocation(), event.getDate());

                                            snapshot.getRef().removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    intent.putExtra("eventDeleted", true);
                                                    intent.putExtra("deletedEventId", deletedEvent.getEventId());
                                                    intent.putExtra("deletedEventTitle", deletedEvent.getTitle());
                                                    intent.putExtra("deletedEventLocation", deletedEvent.getLocation());
                                                    intent.putExtra("deletedEventDate", deletedEvent.getDate());
                                                    intent.putExtra("deletedEventUserId", deletedEvent.getUserId());
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(AddItemActivity.this, "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.page_add_post);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home || item.getItemId() == R.id.page_view_posts) {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDAa1Wd5O8dpjeh1RdozE2_x221_tWiX00");
        }
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        eventDate.setText(selectedDate);
                    }
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void getEventDetailsAndUpdate(String eventId) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventId);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Event event = snapshot.getValue(Event.class);
                if (event != null) {
                    eventTitle.setText(event.getTitle());
                    eventLocation.setText(event.getLocation());
                    eventDate.setText(event.getDate());

                    updateEventButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String updatedTitle = eventTitle.getText().toString();
                            String updatedLocation = eventLocation.getText().toString();
                            String updatedDate = eventDate.getText().toString();

                            if (updatedTitle.isEmpty() || updatedDate.isEmpty()) {
                                new AlertDialog.Builder(AddItemActivity.this)
                                        .setTitle("Missing Information")
                                        .setMessage("Both Title and Date fields are required.")
                                        .setPositiveButton("OK", null)
                                        .show();
                                return;
                            }


                            DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(eventId);

                            eventRef.child("title").setValue(updatedTitle);
                            eventRef.child("location").setValue(updatedLocation);
                            eventRef.child("date").setValue(updatedDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(AddItemActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddItemActivity.this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    private void openAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                eventLocation.setText(place.getAddress());
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "User canceled autocomplete");
            }
        }
    }

}