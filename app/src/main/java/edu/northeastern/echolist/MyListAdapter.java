package edu.northeastern.echolist;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private List<Event> eventsList;
    private Button editButton;

    public MyListAdapter(Context context, List<Event> eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_my_list_item, parent, false);
        return new MyListAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(MyListAdapter.ViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WishListActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventTitle", event.getTitle());
            v.getContext().startActivity(intent);
        });

        holder.moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(v.getContext(), holder.moreButton);
            menu.inflate(R.menu.event_settings_menu);
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.edit_event) {
                    Intent intent = new Intent(v.getContext(), EventDetailActivity.class);
                    intent.putExtra("eventId", event.getEventId());
                    intent.putExtra("sourceActivity", "MyListActivity");
                    v.getContext().startActivity(intent);
                } else if (item.getItemId() == R.id.delete_event) {
                    DatabaseReference events = FirebaseDatabase.getInstance().getReference("events").child(event.getEventId());
                    events.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Event event = snapshot.getValue(Event.class);
                            if (event != null) {
                                Event deletedEvent = new Event(event.getEventId(),
                                        event.getUserId(), event.getTitle(),
                                        event.getLocation(), event.getDate(),
                                        event.getCategory(),event.getVisibility(),
                                        new ArrayList<>());

                                snapshot.getRef().removeValue()
                                        .addOnSuccessListener(unused -> {
                                            Intent intent = new Intent(v.getContext(), HomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            // pass data between EventDetailActivity and HomeActivity
                                            intent.putExtra("eventDeleted", true);
                                            intent.putExtra("deletedEventId", deletedEvent.getEventId());
                                            intent.putExtra("deletedEventTitle", deletedEvent.getTitle());
                                            intent.putExtra("deletedEventLocation", deletedEvent.getLocation());
                                            intent.putExtra("deletedEventDate", deletedEvent.getDate());
                                            intent.putExtra("deletedEventUserId", deletedEvent.getUserId());
                                            v.getContext().startActivity(intent);
                                        }).addOnFailureListener(e -> Toast.makeText(v.getContext(), "Failed to delete event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                return true;
            });
            menu.show();
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public void removeEventById(String eventId) {
        for (int i = 0; i < eventsList.size(); i++) {
            if (eventsList.get(i).getEventId().equals(eventId)) {
                eventsList.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;
        public ImageButton moreButton;

        public ViewHolder(View itemView, MyListAdapter listAdapter) {
            super(itemView);
            this.titleTextView = itemView.findViewById(R.id.event_title);
            dateTextView = itemView.findViewById(R.id.event_date);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }

}
