package edu.northeastern.echolist;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationRouter {

    private final BottomNavigationView bottomNavigationView;
    private final Activity currentActivity;
    private boolean textEntered = false;

    public NavigationRouter(BottomNavigationView bottomNavigationView, Activity currentActivity) {
        this.bottomNavigationView = bottomNavigationView;
        this.currentActivity = currentActivity;

        this.bottomNavigationView.setItemHorizontalTranslationEnabled(false);
    }

    public void initNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_home) {
                if (!currentActivity.getClass().equals(HomeActivity.class)) {
                    if (currentActivity.getClass().equals(NewItemActivity.class) && textEntered) {
                        new AlertDialog.Builder(currentActivity)
                                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    navigate(HomeActivity.class);
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    }
                    navigate(HomeActivity.class);
                    return true;
                }
            } else if (item.getItemId() == R.id.page_add_post) {
                if (!currentActivity.getClass().equals(NewItemActivity.class)) {
                    navigate(NewItemActivity.class);
                    return true;
                }
            } else if (item.getItemId() == R.id.page_view_posts) {
                if (!currentActivity.getClass().equals(MyListActivity.class)) {
                    if (currentActivity.getClass().equals(NewItemActivity.class) && textEntered) {
                        new AlertDialog.Builder(currentActivity)
                                .setMessage("Are you sure you want to exit? The data will not be saved once you exit.")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    navigate(MyListActivity.class);
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    }
                    navigate(MyListActivity.class);
                    return true;
                }
            }
            return false;
        });
    }

    public void setTextEntered(boolean status) {
        textEntered = status;
    }

    public boolean isTextEntered() {
        return textEntered;
    }

    public void navigate(Class<?> activityClass) {
        Intent intent = new Intent(currentActivity, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        currentActivity.startActivity(intent);
        currentActivity.overridePendingTransition(0, 0);
        currentActivity.finish();
    }
}
