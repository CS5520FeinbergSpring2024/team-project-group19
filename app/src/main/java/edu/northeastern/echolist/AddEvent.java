package edu.northeastern.echolist;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class AddEvent extends Fragment {
    private final NavigationRouter navigationRouter;
    private final DatabaseReference events;
    private final String userId;
    private TextWatcher textWatcher;
    private EditText eventTitle;
    private EditText eventLocation;
    private EditText eventDate;
    private Spinner categorySpinner;
    private Spinner visibilitySpinner;
    private Spinner friendsSpinner;

    public AddEvent(NavigationRouter navigationRouter, DatabaseReference events, String userId) {
        this.navigationRouter = navigationRouter;
        this.events = events;
        this.userId = userId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        // watch for text input
        initTextWatcher();

        // init google places connection
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), "AIzaSyDAa1Wd5O8dpjeh1RdozE2_x221_tWiX00");
        }

        // Init input fields
        // title
        eventTitle = view.findViewById(R.id.eventTitle);
        eventTitle.addTextChangedListener(textWatcher);

        // location
        eventLocation = view.findViewById(R.id.eventLocation);
        eventLocation.setOnClickListener(v -> openAutocompleteActivity());
        eventLocation.addTextChangedListener(textWatcher);
        eventLocation.setOnFocusChangeListener((view12, b) -> {
            if (b) {
                openAutocompleteActivity();
            }
        });

        // date
        eventDate = view.findViewById(R.id.eventDate);
        eventDate.setOnClickListener(v -> showDateDialog());
        eventDate.setOnFocusChangeListener((view1, b) -> {
            if (b) {
                showDateDialog();
            }
        });
        eventDate.addTextChangedListener(textWatcher);

        // friends
        friendsSpinner = view.findViewById(R.id.friendsSpinner);
        DatabaseReference user = FirebaseDatabase.getInstance().getReference("users");
        Query userQuery = user.orderByChild("userId").equalTo(userId);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "DataSnapshot: " + snapshot.toString());
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    Log.d("TAG", "User object is null");
                    return;
                }
                List<String> friends = user.getFriends();
                if (friends == null || friends.isEmpty()) {
                    Log.d("TAG", "Friends list is null or empty");
                    return;
                }
                ArrayAdapter<String> friendAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        friends
                );
                friendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                friendsSpinner.setAdapter(friendAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error retrieving user data", error.toException());
            }
        });

        // categories
        categorySpinner = view.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // visibility
        visibilitySpinner = view.findViewById(R.id.visibilitySpinner);
        ArrayAdapter<CharSequence> visibilityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.visibility_options,
                android.R.layout.simple_spinner_item
        );
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilitySpinner.setAdapter(visibilityAdapter);

        // save event button
        Button saveEventButton = view.findViewById(R.id.saveEventButton);

        saveEventButton.setOnClickListener(v -> {

            String title = eventTitle.getText().toString();
            String date = eventDate.getText().toString();
            if (title.isEmpty() || date.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Missing Information")
                        .setMessage("Both Title and Date fields are required.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            String eventId = events.push().getKey();
            Event event = new Event(
                    eventId,
                    userId,
                    title,
                    eventLocation.getText().toString(),
                    date,
                    categorySpinner.getSelectedItem().toString(),
                    visibilitySpinner.getSelectedItem().toString()
            );
            assert eventId != null;
            events.child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
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

            navigationRouter.navigate(HomeActivity.class);
        });

        return view;
    }

    private void showDateDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
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

    private void openAutocompleteActivity() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext());
        startActivityForResult(intent, 1);
    }

    private void initTextWatcher() {
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the flag based on whether any text is entered
                navigationRouter.setTextEntered(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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