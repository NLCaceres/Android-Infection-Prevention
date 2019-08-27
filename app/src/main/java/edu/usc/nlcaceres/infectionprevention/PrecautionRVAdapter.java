package edu.usc.nlcaceres.infectionprevention;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PrecautionRVAdapter extends RecyclerView.Adapter<PrecautionRVAdapter.RowViewHolder> {

    // Create a class that holds the items for the names, the recyclerview
    // as well as an array for report names
    private static List<Precaution> precautions;
    private static int mRowIndex = 0;

    PrecautionRVAdapter(List<Precaution> precautions) {
        this.precautions = precautions;
        //viewPool = new RecyclerView.RecycledViewPool();
    }

    @Override
    public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View conView = LayoutInflater.from(parent.getContext()).inflate(R.layout.precautions_item, parent, false);

        //holder.innerRecyclerView.setRecycledViewPool(viewPool);
        return new RowViewHolder(conView);
    }

    @Override
    public void onBindViewHolder(RowViewHolder holder, final int position) {
        holder.title.setText(precautions.get(position).getName());
        holder.horizontalAdapter.setmRowIndex(position);
        setmRowIndex(position);
    }

    @Override
    public int getItemCount() {

        return precautions.size();
    }

    private void setmRowIndex(int index) {

        mRowIndex = index;

    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public RecyclerView horizontalRV;
        public HorizontalPrecautionsRVAdapter horizontalAdapter;

        public RowViewHolder(View itemView) {
            super(itemView);
            Context context = itemView.getContext();

            title = itemView.findViewById(R.id.precautionTypeTView);

            horizontalRV = itemView.findViewById(R.id.horizontalRecycleView);
            horizontalRV.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

            HorizontalPrecautionsRVAdapter.RViewItemClickListener itemListener = (view, position) -> {
                Log.d("Item Listener Position", "Item Click Listener Position is " + position);
            };

            // Designates which index value from the practices within the precaution arraylist class var
            // will be used to init horizonatal view
            //horizontalAdapter = new HorizontalPrecautionsRVAdapter(precautions.get(mRowIndex).getPractices(), itemListener);
            //horizontalRV.setAdapter(horizontalAdapter);
        }
    }

}
