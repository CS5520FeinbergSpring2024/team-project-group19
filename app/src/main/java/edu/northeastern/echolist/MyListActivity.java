package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyListActivity extends AppCompatActivity {
    private FloatingActionButton fabAddWishListItem;
    private WishListAdapter wishListAdapter;
    private RecyclerView wishListRecyclerView;
    private RecyclerView.LayoutManager lLayoutManager;
    private List<WishListItem> wishList = new ArrayList<>();
    private String eventId;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        eventId = getIntent().getStringExtra("eventId");

        fabAddWishListItem = findViewById(R.id.fabAddWishListItem);
        wishListRecyclerView = findViewById(R.id.wishlist_recyclerview);

        wishListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishListAdapter = new WishListAdapter(this, wishList);
        wishListRecyclerView.setAdapter(wishListAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId);

        databaseWishLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wishList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    WishListItem item = snapshot.getValue(WishListItem.class);
                    wishList.add(item);
                }
                wishListAdapter.notifyDataSetChanged(); // Refresh the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                Toast.makeText(MyListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
        fabAddWishListItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyListActivity.this);
                        builder.setTitle("Add Wish List Item");

                        View view = getLayoutInflater().inflate(R.layout.activity_wishlist_item_layout, null);
                        builder.setView(view);

                        EditText nameAdd = view.findViewById(R.id.name);

                        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = nameAdd.getText().toString();
                                if (!name.isEmpty()) {
                                    String id = databaseWishLists.push().getKey();
                                    WishListItem newItem = new WishListItem(id, name);
                                    if (id != null) {
                                        databaseWishLists.child(id).setValue(newItem);
                                    }
                                } else {
                                    // a error dialog will show if name and url are empty
                                    emptyEntryDialog();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();

                    }
                });

        bottomNavigationView.setSelectedItemId(R.id.page_view_posts);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home) {
                Intent intent = new Intent(MyListActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }


    private void emptyEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Please enter name of the item");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

}