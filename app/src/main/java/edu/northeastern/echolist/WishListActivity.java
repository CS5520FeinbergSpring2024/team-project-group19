package edu.northeastern.echolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WishListActivity extends AppCompatActivity {
    private WishListAdapter wishListAdapter;
    private RecyclerView wishListRecyclerView;
    private List<WishListItem> wishList = new ArrayList<>();
    private String eventId;

    private RecyclerView favoriteGiftsRecyclerView;
    private GiftAdapter giftAdapter;
    private final List<GiftItem> favoriteGifts = new ArrayList<>();
    private GiftViewModel giftViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist_layout);

        eventId = getIntent().getStringExtra("eventId");
        String eventTitle = getIntent().getStringExtra("eventTitle");

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarwishlist);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(eventTitle + " Wish List");
        }

        wishListRecyclerView = findViewById(R.id.wishlist_recyclerview);

        wishListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishListAdapter = new WishListAdapter(this, wishList, eventId);
        wishListRecyclerView.setAdapter(wishListAdapter);


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
                Toast.makeText(WishListActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = viewHolder.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();

                        WishListItem fromWishListItem = wishList.get(fromPosition);
                        wishList.remove(fromPosition);
                        wishList.add(toPosition, fromWishListItem);
                        wishListAdapter.notifyItemMoved(fromPosition, toPosition);

                        updateFirebaseWithNewOrder();

                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int index = viewHolder.getLayoutPosition();
                        WishListItem deletedWishListItem = wishList.get(index);


                        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(deletedWishListItem.getId());
                        itemRef.removeValue();
                        wishList.remove(index);
                        wishListAdapter.notifyItemRemoved(index);
                        Snackbar snackbar = Snackbar.make(wishListRecyclerView,
                                                "Wish List Item is deleted", Snackbar.LENGTH_SHORT)
                                        .setAction("Undo", v -> {
                                            itemRef.setValue(deletedWishListItem);
                                            wishList.add(index, deletedWishListItem);
                                            wishListAdapter.notifyItemInserted(index);
                                        });
                                snackbar.show();
                    }

                });
        itemTouchHelper.attachToRecyclerView(wishListRecyclerView);

        // favorites code
        favoriteGiftsRecyclerView = findViewById(R.id.favorite_gifts_recyclerview);
        Button toggleButton = findViewById(R.id.toggleButton);
        favoriteGiftsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        giftAdapter = new GiftAdapter(this, favoriteGifts, null, gift -> {
            showAddToWishListDialog(gift);
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean shouldExpand = favoriteGiftsRecyclerView.getVisibility() != View.VISIBLE;
                if (shouldExpand) {
                    if (favoriteGifts.isEmpty()) {
                        Toast.makeText(WishListActivity.this, "Add favorites from trending gifts on the home screen to use this feature!", Toast.LENGTH_SHORT).show();
                    } else {
                        animateRecyclerViewSlide(favoriteGiftsRecyclerView, true);
                        toggleButton.setText("Hide");
                    }
                } else {
                    animateRecyclerViewSlide(favoriteGiftsRecyclerView, false);
                    toggleButton.setText("Show");
                }
            }
        });

        favoriteGiftsRecyclerView.setAdapter(giftAdapter);

        giftViewModel = new ViewModelProvider(this).get(GiftViewModel.class);

        giftViewModel.fetchUserFavorites(getUserId());
        Log.d("WishListActivity", "User ID: " + getUserId());
        giftViewModel.getFavoriteGiftIds().observe(this, favoriteGiftIds -> {
            Log.d("WishListActivity", "User favorites: " + favoriteGiftIds);
            giftViewModel.fetchGiftItems(favoriteGiftIds);
            giftViewModel.getGiftItemsLiveData().observe(this, giftItems -> {
                Log.d("WishListActivity", "Gift Items: " + giftItems);
                if (giftItems != null && favoriteGiftIds != null) {
                    favoriteGifts.clear();
                    for (GiftItem giftItem : giftItems) {
                        if (favoriteGiftIds.contains(giftItem.getGiftId())) {
                            favoriteGifts.add(giftItem);
                        }
                    }
                    giftAdapter.setGiftItems(favoriteGifts);
                }
            });
        });
    }

    private String getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        return sharedPreferences.getString("username", "User");
    }

    private void showAddToWishListDialog(Gift gift) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add to Wish List");
        builder.setMessage("Do you want to add \"" + gift.getName() + "\" to the wish list?");
        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).push().getKey();
            int order = wishList.size();
            WishListItem newItem = new WishListItem(id, gift.getName(), false);
            if (id != null) {
                FirebaseDatabase.getInstance().getReference("wishlists").child(eventId).child(id).setValue(newItem);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void animateRecyclerViewSlide(final RecyclerView recyclerView, final boolean expand) {
        int startTranslationY = expand ? recyclerView.getHeight() : 0;
        int endTranslationY = expand ? 0 : recyclerView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(startTranslationY, endTranslationY);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            recyclerView.setTranslationY(animatedValue);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (expand) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!expand) {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    // Iterate over each wishlist item. Put the index of each wishlist item to a hashmap as the value
    // to store the order of each item. Update the order field of each item with the order from the
    // hashmap in Firebase
    private void updateFirebaseWithNewOrder() {
        DatabaseReference databaseWishLists = FirebaseDatabase.getInstance().getReference("wishlists").child(eventId);
        for (int i = 0; i < wishList.size(); i++) {
            WishListItem item = wishList.get(i);
            Map<String, Object> itemUpdates = new HashMap<>();
            itemUpdates.put("order", i + 1);
            databaseWishLists.child(item.getId()).updateChildren(itemUpdates);
        }
    }

    private void emptyEntryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Please enter name of the item");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }


    // save the state of the wishlist
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SaveWishList saveWishList = new SaveWishList();
        saveWishList.setWishList(wishList);
        ;       outState.putSerializable("wishlist_key", saveWishList);
    }

    // restore the state of the wishlist
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SaveWishList saveWishList = (SaveWishList) savedInstanceState.getSerializable("wishlist_key");
        wishListAdapter.setWishList(saveWishList.getWishList());
    }
}
