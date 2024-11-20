package com.example.cs361v2;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "transactions.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "transactions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_AMOUNT = "amount";

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_AMOUNT + " REAL)";
        db.execSQL(createTable);
        Log.d(TAG, "Database table created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.d(TAG, "Database table upgraded.");
    }

    // Method to get transactions by date
    public List<Transaction> getTransactionsByDate(String date) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // ปรับการ query เพื่อให้จับคู่เฉพาะวันที่ไม่สนใจเวลา
        // ใช้ WHERE clause ตัดเวลาออกโดยการใช้ SUBSTR เพื่อเลือกแค่ส่วนวันที่
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE SUBSTR(" + COLUMN_DATE + ", 1, 10) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{date});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String transactionDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));

                    // สร้าง Transaction object และเพิ่มในรายการ
                    Transaction transaction = new Transaction(id, transactionDate, type, category, amount);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return transactions;
    }


    // Method to insert a new transaction into the database
    public boolean addTransaction(String date, String type, String category, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_AMOUNT, amount);

        long result = db.insert(TABLE_NAME, null, values);
        db.close(); // Always close the database connection after use

        return result != -1; // Return true if insertion was successful
    }

    // Method to delete all transactions from the database
    public void deleteAllTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME); // Delete all records from the table
        db.close();
        Log.d(TAG, "All transactions deleted.");
    }

    // Method to get transactions by month (YYYY-MM)
    public List<Transaction> getTransactionsByMonth(String yearMonth) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // แปลง yearMonth ให้เป็นรูปแบบ yyyy-MM เช่น 2024-11
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE SUBSTR(" + COLUMN_DATE + ", 7, 4) || '-' || SUBSTR(" + COLUMN_DATE + ", 4, 2) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{yearMonth});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String transactionDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));

                    Log.d("DEBUG", "Transaction ID: " + id + ", Date: " + transactionDate + ", Type: " + type + ", Category: " + category + ", Amount: " + amount);

                    Transaction transaction = new Transaction(id, transactionDate, type, category, amount);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        Log.d("DEBUG", "Total Transactions Retrieved: " + transactions.size());

        return transactions;
    }

    public List<Transaction> getTransactionsByYear(String year) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for transactions of a specific year using SUBSTR (assuming date format is DD-MM-YYYY)
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE SUBSTR(" + COLUMN_DATE + ", 7, 4) = ?";
        Cursor cursor = db.rawQuery(query, new String[]{year});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String transactionDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));

                    Log.d("DEBUG", "Transaction ID: " + id + ", Date: " + transactionDate +
                            ", Type: " + type + ", Category: " + category + ", Amount: " + amount);

                    Transaction transaction = new Transaction(id, transactionDate, type, category, amount);
                    transactions.add(transaction);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        Log.d("DEBUG", "Total Transactions Retrieved for Year: " + transactions.size());

        return transactions;
    }

    public boolean deleteTransaction(String dateTime, String type, String category, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME,
                COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ? AND " +
                        COLUMN_CATEGORY + " = ? AND " + COLUMN_AMOUNT + " = ?",
                new String[]{dateTime, type, category, String.valueOf(amount)});
        db.close();
        return rowsDeleted > 0;
    }

    public void logAllTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all rows in the transactions table
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    // Extracting each column of the current row
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String transactionDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    @SuppressLint("Range") String type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    @SuppressLint("Range") String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
                    @SuppressLint("Range") double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_AMOUNT));

                    // Log each row to Logcat
                    Log.d("DEBUG", "Transaction ID: " + id +
                            ", Date: " + transactionDate +
                            ", Type: " + type +
                            ", Category: " + category +
                            ", Amount: " + amount);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // แสดงค่า transactionId เพื่อให้มั่นใจว่าเป็นค่า ID ที่ถูกต้อง
        Log.d("DeleteTransaction", "Transaction ID to delete: " + transactionId);

        int rowsDeleted = db.delete("transactions", "id = ?", new String[]{String.valueOf(transactionId)});
        Log.d("DeleteTransaction", "Rows deleted: " + rowsDeleted);

        if (rowsDeleted > 0) {
            Log.d("DeleteTransaction", "Transaction deleted successfully.");
        } else {
            Log.d("DeleteTransaction", "No transaction found with the given ID.");
        }

        return rowsDeleted > 0;
    }


}
