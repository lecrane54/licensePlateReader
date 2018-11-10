package com.example.klefe.licenseplaterrecognizer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by klefe on 11/9/18.
 */

public class PlateListAdapter extends RecyclerView.Adapter<PlateListAdapter.ViewHolder> {

    private List<Plate> plates;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    PlateListAdapter(Context context, List<Plate> plates) {
        this.mInflater = LayoutInflater.from(context);
        this.plates = plates;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Plate p = plates.get(position);
        String state = p.getState();
        String plate = p.getPlateId();
        holder.stateTextView.setText("STATE: " +state);
        holder.plateTextView.setText("NUMBER: " +plate);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return plates.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView stateTextView;
        TextView plateTextView;

        ViewHolder(View itemView) {
            super(itemView);
            stateTextView = itemView.findViewById(R.id.state);
            plateTextView = itemView.findViewById(R.id.plateId);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return plates.get(id).getPlate();
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}