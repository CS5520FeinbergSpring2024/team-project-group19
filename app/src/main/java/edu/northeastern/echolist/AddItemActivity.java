package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;
    private Spinner friendsSpinner;
    private boolean textEntered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_add_post);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home || item.getItemId() == R.id.page_view_posts) {
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            dialog.dismiss();
                            AddItemActivity.super.onBackPressed();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
            return false;
        });
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        DatabaseReference databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        Button saveEventButton = findViewById(R.id.saveEventButton);
        eventTitle = findViewById(R.id.eventTitle);
        eventLocation = findViewById(R.id.eventLocation);
        eventDate = findViewById(R.id.eventDate);
        friendsSpinner = findViewById(R.id.friendsSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        visibilitySpinner = findViewById(R.id.visibilitySpinner);

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

        saveEventButton.setOnClickListener(v -> {

            String title = eventTitle.getText().toString();
            String location = eventLocation.getText().toString();
            String date = eventDate.getText().toString();
            String category = categorySpinner.getSelectedItem().toString();
            String visibility = visibilitySpinner.getSelectedItem().toString();

            if (title.isEmpty() || date.isEmpty()) {
                new AlertDialog.Builder(AddItemActivity.this)
                        .setTitle("Missing Information")
                        .setMessage("Both Title and Date fields are required.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            String eventId = databaseEvents.push().getKey();
            Event event = new Event(eventId, userId, title, location,date,category,visibility);

            databaseEvents.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    snapshot.getRef().setValue(event).addOnSuccessListener(unused -> {
                    }).addOnFailureListener(e -> {

                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            Intent intent = new Intent(AddItemActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        String eventId = getIntent().getStringExtra("eventId");
        if (eventId != null) {
            saveEventButton.setVisibility(View.GONE);
            getEventDetailsAndUpdate(eventId);
        }

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!textEntered) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    AddItemActivity.super.onBackPressed();
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