package com.example.parksmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
    private ArrayList<PaymentsObject> paymentlist;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView img;
        public TextView cost, timetotal, parkedslot, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageView);
            cost = itemView.findViewById(R.id.cost);
            timetotal = itemView.findViewById(R.id.timetotal);
            parkedslot = itemView.findViewById(R.id.parkedslot);
            date = itemView.findViewById(R.id.date);
        }
    }

    public PaymentAdapter(ArrayList<PaymentsObject> listofpayments){
        paymentlist = listofpayments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.payments, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return  vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentsObject pay = paymentlist.get(position);

        holder.img.setImageResource(pay.getImage());
        holder.parkedslot.setText(pay.getParkedSlot());
        holder.timetotal.setText((pay.getTimetotal()));
        holder.cost.setText(pay.getCost());
        holder.date.setText(pay.getDate());
    }

    @Override
    public int getItemCount() {
        return paymentlist.size();
    }
}
