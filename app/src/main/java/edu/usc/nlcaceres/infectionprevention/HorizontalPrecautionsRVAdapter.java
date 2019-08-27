package edu.usc.nlcaceres.infectionprevention;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class HorizontalPrecautionsRVAdapter extends RecyclerView.Adapter<HorizontalPrecautionsRVAdapter.ItemViewHolder> {

    // This is the data set for this adapter
    private List<HealthPractice> practices;
    // In order to check which practice type it is
    // and properly init the next portion of the app (dropdown menu)
    private int mRowIndex = -1;
    private RViewItemClickListener rvItemListener;

    HorizontalPrecautionsRVAdapter(List<HealthPractice> practices, RViewItemClickListener itemListener) {

        this.practices = practices;
        rvItemListener = itemListener;
    }

    // Probably not going to be used
    // but in cases where users want to add possible report types then useful
    public void updateData(List<HealthPractice> newPractices) {
        // Wipe clean the old set (so we don't double write)
        practices.clear();
        practices.addAll(newPractices);
        // Make sure the UI updates
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.precautions_horizontal_item, parent,false);
        //itemView.setOnClickListener(new);
        return new ItemViewHolder(itemView, rvItemListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        ItemViewHolder itemHolder = (ItemViewHolder) holder;

        // Set up the view with data
        Log.d("position with text", "onBindViewHolder: This is the position " + position + " that contains " + practices.get(position).getName());
        itemHolder.reportName.setText(practices.get(position).getName());
        itemHolder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {

        return practices.size();

    }

    public void setmRowIndex(int index) {

        mRowIndex = index;

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView reportName;
        private Button reportButton;
        private RViewItemClickListener rvItemListener;

        public ItemViewHolder(View itemView, RViewItemClickListener itemListener) {
            super(itemView);

            // This should set up the entire view to listen for clicks
            // Before update, it would only listen in on TextView
            rvItemListener = itemListener;
            itemView.setOnClickListener(this);

            reportName = itemView.findViewById(R.id.precautionButtonTV);
            reportButton = itemView.findViewById(R.id.precautionButton);
            reportButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == reportName.getId()) {
                Log.d("Horizontal RV Button", "onClick: This should be report name");
            }
            else if (view.getId() == reportButton.getId()) {
                Log.d("Horizontal RV Button", "onClick: This should be report button from " + getAdapterPosition());
            }
            else {
                Log.d("Horizontal RV Button", "onClick: This is apparently the item view");
            }
            rvItemListener.onClick(view, getAdapterPosition());
        }
    }

    public interface RViewItemClickListener {

        void onClick(View view, int position);
    }



}
