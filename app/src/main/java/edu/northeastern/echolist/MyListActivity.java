package edu.northeastern.echolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MyListActivity extends AppCompatActivity {
    private WishListAdapter wishListAdapter;
    private RecyclerView wishListRecyclerView;
    private RecyclerView.LayoutManager lLayoutManager;
    private List<WishListItem> wishList = new ArrayList<>();
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        eventId = getIntent().getStringExtra("eventId");

        wishListRecyclerView = findViewById(R.id.wishlist_recyclerview);

        wishListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishListAdapter = new WishListAdapter(this, wishList);
        wishListRecyclerView.setAdapter(wishListAdapter);

        // init navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationRouter navigationRouter = new NavigationRouter(bottomNavigationView, this);
        navigationRouter.initNavigation();
    }

}