package edu.northeastern.echolist;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationRouter {

    private final BottomNavigationView bottomNavigationView;
    private final Activity currentActivity;

    public NavigationRouter(BottomNavigationView bottomNavigationView, Activity currentActivity) {
        this.bottomNavigationView = bottomNavigationView;
        this.currentActivity = currentActivity;
    }

    public void initNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home) {
                if (!currentActivity.getClass().equals(HomeActivity.class)) {
                    navigate(HomeActivity.class);
                    return true;
                }
            } else if (item.getItemId() == R.id.page_add_post) {
                if (!currentActivity.getClass().equals(AddItemActivity.class)) {
                    navigate(AddItemActivity.class);
                    return true;
                }
            } else if (item.getItemId() == R.id.page_view_posts) {
                if (!currentActivity.getClass().equals(MyListActivity.class)) {
                    navigate(MyListActivity.class);
                    return true;
                }
            }
            return false;
        });
    }

    private void navigate(Class<?> activityClass) {
        Intent intent = new Intent(currentActivity, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(0, 0);
        currentActivity.finish();
    }
}
