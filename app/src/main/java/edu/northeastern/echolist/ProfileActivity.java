package edu.northeastern.echolist;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private TextView userIdTextView;
    private ImageView profileImageView;
    private String userId;
    private Button addFriendButton;
    private String friendUserId; // String that contains the selected friendID
    private RecyclerView.Adapter friendsViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userIdTextView = findViewById(R.id.userId_textview);
        profileImageView = findViewById(R.id.profile_image);
        addFriendButton = findViewById(R.id.add_friend_button);

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        userId = sharedPreferences.getString("username", "User not found");

        userIdTextView.setText("User ID: " + userId);

        if ("User not found".equals(userId)) {
            Toast.makeText(this, "User not found. Please sign in.", Toast.LENGTH_SHORT).show();
        }  else {
            loadProfileImage(userId);
        }

        profileImageView.setOnClickListener(view -> {
            showImageSelectionOptions();
        });


        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriendAction(view);
            }
        });

        RecyclerView currentFriends = findViewById(R.id.friends_list);
        RecyclerView.LayoutManager friendsLayoutManager = new LinearLayoutManager(this);
        currentFriends.setLayoutManager(friendsLayoutManager);

        fetchAndUpdateFriendsList();

    }


    public void addFriendAction(View view){
            showUserSelectionDialog(); //Popping up dialog to pick from
    }

    private void showUserSelectionDialog() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String currentUserId = snapshot.child("userId").getValue(String.class);
                    if (currentUserId != null && !currentUserId.equals(userId)) {
                        userIds.add(currentUserId);
                    }
                }
                // Show dialog with users
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Current users");

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this, android.R.layout.simple_list_item_1, userIds);
                builder.setAdapter(adapter, (dialog, which) -> {
                    friendUserId = userIds.get(which); // Set the selected user ID to friendUserId variable
                    addFriend(userId, friendUserId);
                    addFriend(friendUserId, userId);
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve user IDs: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Method now implements a target friend, and a current user,
    public void addFriend(String targetUserId, String friendUserId) {
        if (targetUserId == null || friendUserId == null) {
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("userId").equalTo(targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User targetUser = snapshot.getValue(User.class);
                        if (targetUser != null) {
                            targetUser.addFriend(friendUserId); // Add friendUserId to targetUserId's list of friends

                            usersRef.child(snapshot.getKey()).setValue(targetUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Friend added to target user's list of friends successfully
                                        Toast.makeText(ProfileActivity.this, friendUserId + " added to " + targetUserId + "'s list of friends", Toast.LENGTH_SHORT).show();
                                        fetchAndUpdateFriendsList();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to add friend to target user's list of friends, handle error
                                        Toast.makeText(ProfileActivity.this, "Failed to add friend to " + targetUserId + "'s list of friends: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Target user not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }






    /*
    public void addFriend() {
        if (friendUserId == null) {
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            currentUser.addFriend(friendUserId);

                            usersRef.child(snapshot.getKey()).setValue(currentUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Friend added successfully
                                        Toast.makeText(ProfileActivity.this, friendUserId + " added successfully", Toast.LENGTH_SHORT).show();
                                        fetchAndUpdateFriendsList();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to add friend, handle error
                                        Toast.makeText(ProfileActivity.this, "Failed to add friend: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Here i have to update the added friend list.
    }
    */

    private void showImageSelectionOptions() {
        CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, (dialogInterface, i) -> {
            if (options[i].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    takePhoto();
                }
            } else if (options[i].equals("Choose from Gallery")) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
            } else if (options[i].equals("Cancel")) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    profileImageView.setImageBitmap(imageBitmap);
                    uploadImageToFirebaseDatabase(imageBitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    profileImageView.setImageBitmap(imageBitmap);
                    uploadImageToFirebaseDatabase(imageBitmap);
                } catch (Exception e) {
                    Log.e("ProfileActivity", "Failed to load image", e);
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadImageToFirebaseDatabase(Bitmap imageBitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();
        String base64ImageData = Base64.encodeToString(imageData, Base64.DEFAULT);

        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User not found");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("userId").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String userId = snapshot.getKey();
                        DatabaseReference userRef = usersRef.child(userId);
                        userRef.child("profileImageData").setValue(base64ImageData)
                                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Profile image added/updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProfileActivity.this, "Failed to add/update profile image", Toast.LENGTH_SHORT).show();
                                    Log.e("FirebaseUpload", "Failed to add/update profile image", e);
                                });
                        return;
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseUpload", "Database error: " + databaseError.getMessage());
            }
        });
    }
    private void loadProfileImage(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.getProfileImageData() != null) {
                            byte[] imageData = Base64.decode(user.getProfileImageData(), Base64.DEFAULT);
                            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                            if (imageBitmap != null) {
                                profileImageView.setImageBitmap(imageBitmap);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchAndUpdateFriendsList() {

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            // Add debug log to check if currentUser is retrieved correctly
                            Log.d("ProfileActivity", "Current user: " + currentUser.getUserId());
                            List<String> friendsList = currentUser.getFriends();

                            if (friendsList != null && !friendsList.isEmpty()) {
                                for (String friendId : friendsList) {
                                    Log.d("ProfileActivity", "Friend ID: " + friendId);
                                }

                                RecyclerView currentFriends = findViewById(R.id.friends_list);
                                FriendAdapter friendAdapter = new FriendAdapter(friendsList);
                                currentFriends.setAdapter(friendAdapter);
                            } else {
                                Log.d("ProfileActivity", "User has no friends");
                                Toast.makeText(ProfileActivity.this, "Add your friends!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }






}