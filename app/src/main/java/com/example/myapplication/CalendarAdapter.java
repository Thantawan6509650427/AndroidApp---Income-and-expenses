package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private List<CalendarItem> calendarItems;

    public CalendarAdapter(List<CalendarItem> calendarItems) {
        this.calendarItems = calendarItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarItem item = calendarItems.get(position);
        holder.dateTextView.setText(item.getDate());
        holder.incomeTextView.setText(String.format("Income: %.2f ฿", item.getIncome()));
        holder.expenseTextView.setText(String.format("Expense: %.2f ฿", item.getExpense()));
    }

    @Override
    public int getItemCount() {
        return calendarItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView incomeTextView;
        public TextView expenseTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            incomeTextView = itemView.findViewById(R.id.income_text_view);
            expenseTextView = itemView.findViewById(R.id.expense_text_view);
        }
    }
}
