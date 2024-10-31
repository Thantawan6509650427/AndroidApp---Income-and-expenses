package com.example.myapplication;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // ตั้งค่าพื้นที่ใน layout ตามขนาดของ system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // เริ่มต้น CalendarView
        CalendarView calendarView = findViewById(R.id.calendarView);

        // เริ่มต้น RecyclerView
        recyclerView = findViewById(R.id.calendar_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // เตรียมข้อมูลสำหรับ RecyclerView
        List<CalendarItem> calendarItems = new ArrayList<>();
        calendarItems.add(new CalendarItem("01/10/2024", 100.0, 50.0));
        calendarItems.add(new CalendarItem("02/10/2024", 200.0, 100.0));
        // เพิ่มข้อมูลตามต้องการ

        // ตั้งค่า Adapter สำหรับ RecyclerView
        calendarAdapter = new CalendarAdapter(calendarItems);
        recyclerView.setAdapter(calendarAdapter);
    }
}
