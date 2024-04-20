package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner friendsSpinner;
    private boolean textEntered;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;
    private Button updateEventButton;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail_layout);

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        eventTitle = findViewById(R.id.eventTitle);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        friendsSpinner = findViewById(R.id.friendsSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        visibilitySpinner = findViewById(R.id.visibilitySpinner);
        updateEventButton = findViewById(R.id.updateEventButton);
        cancelButton = findViewById(R.id.cancelButton);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the flag based on whether any text is entered
                textEntered = !TextUtils.isEmpty(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        eventTitle.addTextChangedListener(textWatcher);
        eventLocation.addTextChangedListener(textWatcher);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> visibilityAdapter = ArrayAdapter.createFromResource(this,
                R.array.visibility_options, android.R.layout.simple_spinner_item);
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilitySpinner.setAdapter(visibilityAdapter);

        eventDate.setOnClickListener(v -> showDateDialog());

        eventLocation.setOnClickListener(v -> openAutocompleteActivity());

        // Fetch event data based on eventId
        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            updateEventButton.setVisibility(View.VISIBLE);
            getEventDetailsAndUpdate(eventId);
        }

        cancelButton.setOnClickListener(v -> {
            // initialize activityClass as HomeActivity.class
            Class<?> activityClass = HomeActivity.class;

            // Check where the activity was started from
            String sourceActivity = getIntent().getStringExtra("sourceActivity");
            if (sourceActivity != null) {
                if (sourceActivity.equals("MyListActivity")) {
                    activityClass = MyListActivity.class;
                }
            }
            // Start the parent activity
            Intent intent = new Intent(EventDetailActivity.this, activityClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        // initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDAa1Wd5O8dpjeh1RdozE2_x221_tWiX00");
        }
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    eventDate.setText(selectedDate);
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
                    if (event.getCategory() != null) {
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) categorySpinner.getAdapter();
                        int position = adapter.getPosition(event.getCategory());
                        categorySpinner.setSelection(position);
                    }
                    if (event.getVisibility() != null) {
                        ArrayAdapter<CharSequence> visibilityAdapter = (ArrayAdapter<CharSequence>) visibilitySpinner.getAdapter();
                        int visibilityPosition = visibilityAdapter.getPosition(event.getVisibility());
                        visibilitySpinner.setSelection(visibilityPosition);
                    }

                    updateEventButton.setOnClickListener(v -> {
                        String updatedTitle = eventTitle.getText().toString();
                        String updatedLocation = eventLocation.getText().toString();
                        String updatedDate = eventDate.getText().toString();
                        String updatedCategory = categorySpinner.getSelectedItem().toString();
                        String updatedVisibility = visibilitySpinner.getSelectedItem().toString();

                        if (updatedTitle.isEmpty() || updatedDate.isEmpty()) {
                            new AlertDialog.Builder(EventDetailActivity.this)
                                    .setTitle("Missing Information")
                                    .setMessage("Both Title and Date fields are required.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }


                        DatabaseReference eventRef1 = FirebaseDatabase.getInstance().getReference("events").child(eventId);

                        eventRef1.child("title").setValue(updatedTitle);
                        eventRef1.child("location").setValue(updatedLocation);
                        eventRef1.child("category").setValue(updatedCategory);
                        eventRef1.child("visibility").setValue(updatedVisibility);


                        eventRef1.child("date").setValue(updatedDate).addOnSuccessListener(unused -> {
                            Toast.makeText(EventDetailActivity.this, "Event updated successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(EventDetailActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }).addOnFailureListener(e -> Toast.makeText(EventDetailActivity.this, "Failed to update event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
            super.onBackPressed();
        // initialize activityClass as HomeActivity.class
        Class<?> activityClass = HomeActivity.class;

        // Check where the activity was started from
        String sourceActivity = getIntent().getStringExtra("sourceActivity");
        if (sourceActivity != null) {
            if (sourceActivity.equals("MyListActivity")) {
                activityClass = MyListActivity.class;
            }
        }
        // Start the parent activity
        Intent intent = new Intent(EventDetailActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // launch Google Places Autocomplete to allow user to search and select a place from Google
    // Places API
    private void openAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, 1);
    }

    // Google Places Autocomplete react with user's selection or cancellation
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
