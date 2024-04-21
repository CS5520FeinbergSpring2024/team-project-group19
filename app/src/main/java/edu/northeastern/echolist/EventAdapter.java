package edu.northeastern.echolist;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> eventsList;

    public EventAdapter(Context context, List<Event> eventsList) {
        this.eventsList = eventsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_event_item_layout, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.titleTextView.setText(event.getTitle());

        SimpleDateFormat originalDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;
        try {
            date = originalDate.parse(event.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat newFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String newDate = newFormat.format(date);



        holder.dateTextView.setText(newDate);

        // when user taps on an item of the event recycler view, the click listener start the
        // EventDetailActivity for the clicked event
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), WishListActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventTitle", event.getTitle());
            v.getContext().startActivity(intent);
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


    // display the recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView, EventAdapter eventAdapter) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.event_title);
            dateTextView = itemView.findViewById(R.id.event_date);
        }
    }
}
