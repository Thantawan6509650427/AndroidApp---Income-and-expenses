package com.example.cs361v2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {
    private CalendarView calendarView;
    private Calendar calendar;
    private LinearLayout historyList; // Used to display transaction history
    private TextView noTransactionsText; // TextView for "No Transactions" message
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private static final String TAG = "HomeFragment";
    private TextView editTextDate;
    private DatabaseHelper databaseHelper; // Database helper

    private final String[] incomeCategories = {"Salary", "Other Income", "Transfer In", "Interest"};
    private final String[] outcomeCategories = {"Food & Beverages", "Bills & Utilities", "Shopping",
            "Household Items", "Family", "Travel", "Health & Fitness", "Education", "Entertainment",
            "Giving & Donations", "Insurance", "Other Expenses"};

    private String selectedDate;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        calendar = Calendar.getInstance();

        databaseHelper = new DatabaseHelper(getActivity());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());

        getDate();

        Button conclusionButton = view.findViewById(R.id.conclusionButton);
        historyList = view.findViewById(R.id.history_list);

        EditText goalInput = view.findViewById(R.id.goal_input);
        EditText searchBillText = view.findViewById(R.id.searchBillText);

        noTransactionsText = view.findViewById(R.id.no_transactions_message);
        noTransactionsText.setVisibility(View.VISIBLE);

        ImageButton addTransactionButton = view.findViewById(R.id.addTransactionButton);

        List<Transaction> transactions = databaseHelper.getTransactionsByDate(selectedDate);

        if (transactions.isEmpty()) {
            noTransactionsText.setVisibility(View.VISIBLE);
        } else {
            noTransactionsText.setVisibility(View.GONE);
            for (Transaction transaction : transactions) {
                addTransactionToLayout(transaction);
            }
        }

        addTransactionButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                // ใช้วันที่ที่เก็บไว้จาก CalendarView
                String[] dateParts = selectedDate.split("/");
                int year = Integer.parseInt(dateParts[2]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[0]);
                showAddTransactionDialog(year, month, day);
            } else {
                Toast.makeText(getActivity(), "กรุณาเลือกวันที่ก่อน", Toast.LENGTH_SHORT).show();
            }
        });

        // ใช้ TextWatcher เพื่อตรวจสอบการพิมพ์ของผู้ใช้
        searchBillText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ไม่ต้องทำอะไรในส่วนนี้
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // เรียกใช้เมธอดกรองรายการเมื่อมีการพิมพ์
                filterBills(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // ไม่ต้องทำอะไรในส่วนนี้
            }
        });

        conclusionButton.setOnClickListener(v -> {
            // ตรวจสอบว่า selectedDate ไม่เป็น null และมีค่า
            if (selectedDate != null && !selectedDate.isEmpty()) {
                // ดึงข้อมูลธุรกรรมทั้งหมดจากฐานข้อมูลตามวันที่ที่เลือก
                List<Transaction> conclusionTransactions = databaseHelper.getTransactionsByDate(selectedDate);

                if (conclusionTransactions != null && !conclusionTransactions.isEmpty()) {
                    int dailyTotal = 0;
                    for (Transaction transaction : conclusionTransactions) {
                        // ตรวจสอบว่าเป็นธุรกรรมประเภท "outcome" หรือไม่
                        if ("Outcome".equals(transaction.getType())) {
                            dailyTotal += transaction.getAmount();
                        }
                    }

                    // ตรวจสอบเป้าหมาย
                    String goalString = goalInput.getText().toString();
                    if (!goalString.isEmpty()) {
                        int goal = Integer.parseInt(goalString);  // แปลงค่าจาก String เป็น Integer
                        String statusMessage;

                        // ตรวจสอบสถานะการใช้จ่าย
                        if (dailyTotal > goal) {
                            statusMessage = "You have exceeded your spending goal!";
                        } else if (dailyTotal == goal) {
                            statusMessage = "You have met your spending goal exactly!";
                        } else {
                            statusMessage = "Your spending is below the goal!";
                        }

                        // ส่งข้อมูลไปยัง ConclusionFragment
                        Bundle bundle = new Bundle();
                        bundle.putInt("DAILY_TOTAL", dailyTotal);
                        bundle.putInt("GOAL", goal);
                        bundle.putString("STATUS_MESSAGE", statusMessage);
                        bundle.putString("SELECTED_DATE", selectedDate);

                        ConclusionFragment fragment = new ConclusionFragment();
                        fragment.setArguments(bundle);

                        // แสดง Fragment โดยไม่ต้องเปลี่ยนแปลง Activity
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.frameLayout, fragment) // แทนที่ด้วย container ที่เหมาะสม
                                .addToBackStack(null) // เพิ่มไปที่ back stack
                                .commit();
                    } else {
                        Toast.makeText(requireActivity(), "กรุณากรอกเป้าหมายการใช้จ่าย", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "ไม่พบข้อมูลธุรกรรมในวันที่เลือก", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireActivity(), "กรุณาเลือกวันที่", Toast.LENGTH_SHORT).show();
            }
        });




        editTextDate = view.findViewById(R.id.editTextDate);
        editTextDate.setText(selectedDate);
        editTextDate.setOnClickListener(view1 -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            // เก็บวันที่ที่เลือกในรูปแบบ dd/MM/yyyy
            selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);

            // อัปเดต editTextDate ด้วยวันที่ที่เลือก
            editTextDate.setText(selectedDate);

            // แสดง Toast (สามารถลบออกได้หากไม่ต้องการ)
            Toast.makeText(getActivity(), "Selected date: " + selectedDate, Toast.LENGTH_SHORT).show();

            // ลบเฉพาะ View ที่มีแท็ก "transaction" ใน historyList
            for (int i = historyList.getChildCount() - 1; i >= 0; i--) {
                View child = historyList.getChildAt(i);
                if ("transaction".equals(child.getTag())) {
                    historyList.removeView(child);
                }
            }

            // ดึงข้อมูลรายการจากฐานข้อมูลตามวันที่ที่เลือก
            List<Transaction> dailyTransactions = databaseHelper.getTransactionsByDate(selectedDate);

            // ตรวจสอบว่ามีรายการหรือไม่และแสดงผล
            if (dailyTransactions.isEmpty()) {
                noTransactionsText.setVisibility(View.VISIBLE);
            } else {
                noTransactionsText.setVisibility(View.GONE);
                for (Transaction transaction : dailyTransactions) {
                    addTransactionToLayout(transaction);
                }
            }
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month += 1; // เดือนเริ่มจาก 0 จึงต้องบวกเพิ่ม 1
            selectedDate = String.format("%02d/%02d/%d", day, month, year); // เก็บวันที่ที่เลือก

            // อัปเดต editTextDate ด้วยวันที่ที่เลือก
            editTextDate.setText(selectedDate);

            // ตั้งค่า calendarView ให้แสดงวันที่ที่เลือก
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month - 1, day);
            calendarView.setDate(selectedCalendar.getTimeInMillis(), true, true);

            // ลบเฉพาะ View ที่มีแท็ก "transaction" ใน historyList
            for (int i = historyList.getChildCount() - 1; i >= 0; i--) {
                View child = historyList.getChildAt(i);
                if ("transaction".equals(child.getTag())) {
                    historyList.removeView(child);
                }
            }

            // ดึงข้อมูลจากฐานข้อมูลตามวันที่ที่เลือก
            List<Transaction> dailyTransactions = databaseHelper.getTransactionsByDate(selectedDate);

            // ตรวจสอบว่ามีรายการหรือไม่และแสดงผล
            if (dailyTransactions.isEmpty()) {
                noTransactionsText.setVisibility(View.VISIBLE);
            } else {
                noTransactionsText.setVisibility(View.GONE);
                for (Transaction transaction : dailyTransactions) {
                    addTransactionToLayout(transaction);
                }
            }
        };

        return view;
    }

    public void getDate() {
        long date = calendarView.getDate();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        calendar.setTimeInMillis(date);
        String selected_date = simpleDateFormat.format(calendar.getTime());
        Toast.makeText(getActivity(), selected_date, Toast.LENGTH_SHORT).show();
    }

    // Function to show dialog for adding transaction
    @SuppressLint("SetTextI18n")
    private void showAddTransactionDialog(int year, int month, int day) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Transaction");

        // Create Layout for Dialog
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        // Income Checkbox
        CheckBox incomeCheckbox = new CheckBox(getActivity());
        incomeCheckbox.setText("Income");
        layout.addView(incomeCheckbox);

        // Outcome Checkbox
        CheckBox outcomeCheckbox = new CheckBox(getActivity());
        outcomeCheckbox.setText("Outcome");
        layout.addView(outcomeCheckbox);

        // Spinner for categories
        final Spinner categorySpinner = new Spinner(getActivity());
        layout.addView(categorySpinner);

        // Update the category spinner based on the selected checkboxes
        incomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                outcomeCheckbox.setChecked(false); // Uncheck outcome if income is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, incomeCategories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        outcomeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                incomeCheckbox.setChecked(false); // Uncheck income if outcome is selected
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, outcomeCategories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(adapter);
            }
        });

        // Add EditText for amount input
        final EditText amountInput = new EditText(getActivity());
        amountInput.setHint("Enter Amount");
        layout.addView(amountInput);

        builder.setView(layout);

        // Dialog buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String billType = "";
            if (incomeCheckbox.isChecked()) {
                billType = "Income";
            } else if (outcomeCheckbox.isChecked()) {
                billType = "Outcome";
            }

            String amount = amountInput.getText().toString();
            String category = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : ""; // Get selected category

            if (!amount.isEmpty() && !billType.isEmpty()) {
                addTransaction(year, month, day, billType, amount, category); // Pass category to the method
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void addTransaction(int year, int month, int day, String billType, String amount, String category) {
        // Create a LinearLayout to hold the transaction details
        LinearLayout transactionContainer = new LinearLayout(getActivity());
        transactionContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        transactionContainer.setOrientation(LinearLayout.VERTICAL);
        transactionContainer.setPadding(16, 16, 16, 16);

        // ตั้งค่า Tag เพื่อใช้ในการลบในอนาคต
        transactionContainer.setTag("transaction");

        // Generate random color for the border
        int[] colors = {0xFFE57373, 0xFF81C784, 0xFF64B5F6, 0xFFFFB74D, 0xFFBA68C8};
        int randomColor = colors[new Random().nextInt(colors.length)];
        transactionContainer.setBackground(createBorderDrawable(randomColor));

        // Create a TextView to display the transaction details
        TextView transactionView = new TextView(getActivity());

        // ใช้ SimpleDateFormat เพื่อให้ตรงกับรูปแบบวันที่ที่เก็บในฐานข้อมูล
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDateTime = dateTimeFormat.format(Calendar.getInstance().getTime());

        // Set the transaction text with date, time, type, category, and amount
        transactionView.setText(String.format("%s - %s: %s - %s baht", formattedDateTime, billType, category, amount));
        transactionContainer.addView(transactionView);

        // Add the transaction view to the history list
        historyList.addView(transactionContainer);

        // Hide "No Transactions" message if there are transactions
        noTransactionsText.setVisibility(View.GONE);

        // Convert amount to double before adding to the database
        try {
            double amountDouble = Double.parseDouble(amount);
            // Save transaction to the database with the correct date format
            databaseHelper.addTransaction(formattedDateTime, billType, category, amountDouble);
            Toast.makeText(getActivity(), "Transaction added", Toast.LENGTH_SHORT).show();

            // **เพิ่มปุ่มลบ**
            Button deleteButton = new Button(getActivity());
            deleteButton.setText("Delete");
            transactionContainer.addView(deleteButton);

            // Set up delete action
            deleteButton.setOnClickListener(v -> {
                // สร้าง AlertDialog เพื่อยืนยันการลบ
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // ลบรายการจากฐานข้อมูล
                            boolean isDeleted = databaseHelper.deleteTransaction(formattedDateTime, billType, category, amountDouble);
                            if (isDeleted) {
                                // ลบรายการจากหน้าจอ
                                historyList.removeView(transactionContainer);
                                Toast.makeText(getActivity(), "Transaction deleted", Toast.LENGTH_SHORT).show();

                                // หากไม่มีรายการเหลือ ให้แสดงข้อความ "No Transactions"
                                if (historyList.getChildCount() == 0) {
                                    noTransactionsText.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // ปิดกล่องข้อความหากผู้ใช้ไม่ต้องการลบ
                            dialog.dismiss();
                        })
                        .show();  // แสดง Dialog
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "Invalid amount entered", Toast.LENGTH_SHORT).show();
        }
    }


    private GradientDrawable createBorderDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.WHITE);
        drawable.setStroke(4, color); // Set border width and color
        return drawable;
    }

    @SuppressLint("DefaultLocale")
    private void addTransactionToLayout(Transaction transaction) {
        LinearLayout transactionContainer = new LinearLayout(getActivity());
        transactionContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        transactionContainer.setOrientation(LinearLayout.VERTICAL);
        transactionContainer.setPadding(16, 16, 16, 16);

        // ตั้งค่า Tag เพื่อใช้ในการลบ
        transactionContainer.setTag("transaction");

        int[] colors = {0xFFE57373, 0xFF81C784, 0xFF64B5F6, 0xFFFFB74D, 0xFFBA68C8};
        int randomColor = colors[new Random().nextInt(colors.length)];

        transactionContainer.setBackground(createBorderDrawable(randomColor));

        TextView transactionView = new TextView(getActivity());
        transactionView.setText(String.format("%s - %s: %s - %d baht",
                transaction.getDate(), transaction.getType(),
                transaction.getCategory(), (int) transaction.getAmount()));
        transactionContainer.addView(transactionView);

        // **สร้างปุ่มลบ**
        Button deleteButton = new Button(getActivity());
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> {
            // สร้าง AlertDialog เพื่อยืนยันการลบ
            new AlertDialog.Builder(getActivity())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // ลบรายการจากฐานข้อมูล
                        boolean isDeleted = databaseHelper.deleteTransaction(
                                transaction.getDate(),
                                transaction.getType(),
                                transaction.getCategory(),
                                transaction.getAmount()
                        );

                        if (isDeleted) {
                            // ลบรายการจากหน้าจอ
                            historyList.removeView(transactionContainer);
                            Toast.makeText(getActivity(), "Transaction deleted", Toast.LENGTH_SHORT).show();

                            // หากไม่มีรายการเหลือ ให้แสดงข้อความ "No Transactions"
                            if (historyList.getChildCount() == 0) {
                                noTransactionsText.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // ปิดกล่องข้อความหากผู้ใช้ไม่ต้องการลบ
                        dialog.dismiss();
                    })
                    .show();  // แสดง Dialog
        });
        transactionContainer.addView(deleteButton);

        historyList.addView(transactionContainer);
    }


    private void filterBills(String query) {
        // ตรวจสอบว่ามีรายการใน historyList หรือไม่
        if (historyList.getChildCount() > 0) {
            for (int i = 0; i < historyList.getChildCount(); i++) {
                View child = historyList.getChildAt(i);

                // ตรวจสอบว่า View มีแท็ก "transaction" หรือไม่
                if ("transaction".equals(child.getTag())) {
                    // ค้นหา TextView ภายในแต่ละ transactionContainer
                    TextView transactionView = (TextView) ((LinearLayout) child).getChildAt(0);
                    String transactionText = transactionView.getText().toString().toLowerCase();

                    // แสดงหรือซ่อน View ตามคำที่ค้นหา
                    if (transactionText.contains(query.toLowerCase())) {
                        child.setVisibility(View.VISIBLE);
                    } else {
                        child.setVisibility(View.GONE);
                    }
                }
            }
        }
    }



}
