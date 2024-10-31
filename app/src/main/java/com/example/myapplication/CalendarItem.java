package com.example.myapplication;

public class CalendarItem {
    private String date;
    private double income;
    private double expense;

    public CalendarItem(String date, double income, double expense) {
        this.date = date;
        this.income = income;
        this.expense = expense;
    }

    public String getDate() {
        return date;
    }

    public double getIncome() {
        return income;
    }

    public double getExpense() {
        return expense;
    }
}
