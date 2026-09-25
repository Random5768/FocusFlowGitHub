package com.focusflow.planner;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout tasks, subjects;
    android.content.SharedPreferences prefs;
    ArrayList<String> taskList = new ArrayList<>();

    int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    TextView label(String s, int size){
        TextView t=new TextView(this); t.setText(s); t.setTextColor(Color.WHITE); t.setTextSize(size);
        t.setPadding(dp(14),dp(12),dp(14),dp(12)); return t;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        prefs=getSharedPreferences("focusflow",0);
        tasks=findViewById(R.id.taskContainer); subjects=findViewById(R.id.subjectContainer);
        load();
        findViewById(R.id.btnAdd).setOnClickListener(v->addTaskDialog());
        findViewById(R.id.btnPomodoro).setOnClickListener(v->pomodoro());
        findViewById(R.id.navCalendar).setOnClickListener(v->calendar());
        findViewById(R.id.navProgress).setOnClickListener(v->progress());
        findViewById(R.id.navNotes).setOnClickListener(v->notes());
    }

    void load(){
        String raw=prefs.getString("tasks","Math — Algebra — 07:00–08:00|Physics — Motion — 10:00–11:00|English — Vocabulary — 16:00–16:45");
        taskList.clear(); taskList.addAll(Arrays.asList(raw.split("\\|")));
        renderTasks();
        subjects.removeAllViews();
        subjects.addView(label("📐 Mathematics     8 topics",16));
        subjects.addView(label("⚛ Physics              5 topics",16));
        subjects.addView(label("📖 English              6 topics",16));
    }

    void renderTasks(){
        tasks.removeAllViews();
        for(String s: taskList){
            LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(Color.rgb(20,27,49));
            TextView tv=label("○  "+s,15); row.addView(tv,new LinearLayout.LayoutParams(0,dp(68),1));
            Button done=new Button(this); done.setText("✓"); row.addView(done,new LinearLayout.LayoutParams(dp(58),dp(68)));
            done.setOnClickListener(v->{ tv.setText("✓  "+s+"  • Done"); tv.setTextColor(Color.rgb(53,211,154)); done.setEnabled(false); });
            row.setPadding(dp(4),dp(4),dp(4),dp(4)); tasks.addView(row);
        }
    }

    void addTaskDialog(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(5),dp(20),dp(5));
        EditText subject=new EditText(this); subject.setHint("Subject / Task");
        EditText topic=new EditText(this); topic.setHint("Topic");
        EditText time=new EditText(this); time.setHint("Time (e.g. 19:00–20:00)");
        box.addView(subject); box.addView(topic); box.addView(time);
        new AlertDialog.Builder(this).setTitle("Add routine").setView(box)
          .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
             String s=subject.getText().toString().trim()+" — "+topic.getText().toString().trim()+" — "+time.getText().toString().trim();
             if(!s.startsWith("—")) { taskList.add(s); save(); renderTasks(); }
          }).show();
    }

    void save(){ prefs.edit().putString("tasks",String.join("|",taskList)).apply(); }

    void pomodoro(){
        final String[] time={"25:00"};
        new AlertDialog.Builder(this).setTitle("⏱ Pomodoro").setMessage("Focus session: 25 minutes\n\nUse this session for your selected topic.").setPositiveButton("Start", (d,w)->{
            Toast.makeText(this,"Pomodoro started — focus mode on!",Toast.LENGTH_LONG).show();
        }).setNegativeButton("Close",null).show();
    }

    void calendar(){ new AlertDialog.Builder(this).setTitle("📅 Weekly Calendar").setMessage("MON  Math  07:00\nTUE  Physics  10:00\nWED  English  16:00\nTHU  Math  07:00\nFRI  Revision  19:00\nSAT  Practice  11:00\nSUN  Rest / Review").setPositiveButton("OK",null).show(); }
    void progress(){ new AlertDialog.Builder(this).setTitle("📊 Progress").setMessage("Weekly focus: 18h 40m\nGoal: 24h\nCompleted tasks: 32 / 40\nConsistency: 80%\nTop subject: Mathematics").setPositiveButton("OK",null).show(); }
    void notes(){ final EditText e=new EditText(this); e.setHint("Write your study notes..."); e.setMinLines(6); e.setText(prefs.getString("notes","")); new AlertDialog.Builder(this).setTitle("📝 Notes").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->prefs.edit().putString("notes",e.getText().toString()).apply()).show(); }
}
