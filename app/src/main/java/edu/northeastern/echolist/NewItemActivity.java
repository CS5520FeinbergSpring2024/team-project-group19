package edu.northeastern.echolist;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewItemActivity extends AppCompatActivity {

    private NavigationRouter navigationRouter;
    private DatabaseReference events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);

        // init navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();

        events = FirebaseDatabase.getInstance().getReference("events");

        ViewPager viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SharedPreferences sharedPreferences = getSharedPreferences("namePref", MODE_PRIVATE);
        String userId = sharedPreferences.getString("username", "User");

        ViewPagerHelper viewpager = new ViewPagerHelper(getSupportFragmentManager());
        viewpager.addFragment(new AddEvent(navigationRouter, events, userId), "New Event");
        viewpager.addFragment(new AddWishlistItem(events, userId), "New Wishlist Item");
        viewPager.setAdapter(viewpager);
    }

    @Override
    public void onBackPressed() {
        if (!navigationRouter.isTextEntered()) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    NewItemActivity.super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
