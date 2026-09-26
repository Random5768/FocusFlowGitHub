package com.focusflow.planner;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    FocusView view;
    android.content.SharedPreferences prefs;
    ArrayList<Task> tasks = new ArrayList<>();
    ArrayList<String> notes = new ArrayList<>();
    int page = 0;
    long pomEnd = 0;
    boolean pomRunning = false;
    Handler handler = new Handler(Looper.getMainLooper());

    static class Task {
        String subject, topic, name, time, repeat, priority, status;
        Task(String s,String t,String n,String tm,String r,String p,String st){
            subject=s; topic=t; name=n; time=tm; repeat=r; priority=p; status=st;
        }
        String encode(){ return subject+"~"+topic+"~"+name+"~"+time+"~"+repeat+"~"+priority+"~"+status; }
        static Task decode(String x){
            String[] a=x.split("~",-1);
            if(a.length<7)return null;
            return new Task(a[0],a[1],a[2],a[3],a[4],a[5],a[6]);
        }
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7,17,31));
        getWindow().setNavigationBarColor(Color.rgb(7,17,31));
        prefs=getSharedPreferences("focusflow2",0);
        load();
        view=new FocusView(this);
        setContentView(view);
        handler.postDelayed(new Runnable(){ public void run(){ if(pomRunning){ view.invalidate(); } handler.postDelayed(this,1000); }},1000);
    }

    void load(){
        String raw=prefs.getString("tasks","");
        if(!raw.isEmpty()) for(String x:raw.split("\\|")){
            Task t=Task.decode(x); if(t!=null) tasks.add(t);
        }
        if(tasks.isEmpty()){
            tasks.add(new Task("Mathematics","Algebra - Chapter 3","Solve Exercise 3.1 - 3.5","08:00 - 10:00","Daily","Medium","Completed"));
            tasks.add(new Task("Physics","Motion","Numerical practice","10:30 - 12:00","Daily","High","In Progress"));
            tasks.add(new Task("English","Essay","Write introduction","01:30 - 03:00","Daily","Low","Pending"));
            tasks.add(new Task("Chemistry","Reactions","Reaction revision","04:00 - 05:00","Weekly","Medium","Pending"));
            tasks.add(new Task("Review & Practice","Mixed","Daily revision","07:00 - 08:00","Daily","Medium","Pending"));
        }
        String ns=prefs.getString("notes","Algebra formulas|Physics important questions|English essay ideas|Chemistry reaction notes");
        notes.addAll(Arrays.asList(ns.split("\\|")));
    }

    void save(){
        ArrayList<String> a=new ArrayList<>();
        for(Task t:tasks)a.add(t.encode());
        prefs.edit().putString("tasks",String.join("|",a)).putString("notes",String.join("|",notes)).apply();
    }

    void addTaskDialog(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(24,4,24,4);
        EditText s=e("Subject", "Mathematics"); EditText topic=e("Topic","Algebra - Chapter 3");
        EditText name=e("Task name","Solve Exercise 3.1 - 3.5"); EditText time=e("Time","08:00 - 10:00");
        EditText repeat=e("Repeat","Daily"); EditText priority=e("Priority","Medium");
        box.addView(s);box.addView(topic);box.addView(name);box.addView(time);box.addView(repeat);box.addView(priority);
        new AlertDialog.Builder(this).setTitle("Add New Task").setView(box)
            .setNegativeButton("Cancel",null).setPositiveButton("Save Task",(d,w)->{
                tasks.add(new Task(s.getText().toString(),topic.getText().toString(),name.getText().toString(),
                    time.getText().toString(),repeat.getText().toString(),priority.getText().toString(),"Pending"));
                save(); view.invalidate();
            }).show();
    }
    EditText e(String hint,String val){ EditText e=new EditText(this);e.setHint(hint);e.setText(val);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY); return e; }

    void notesDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(20,4,20,4);
        EditText e=new EditText(this);e.setHint("Write a note...");e.setMinLines(5);e.setTextColor(Color.WHITE);box.addView(e);
        new AlertDialog.Builder(this).setTitle("New Note").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            String n=e.getText().toString().trim(); if(!n.isEmpty()){notes.add(0,n);save();view.invalidate();}
        }).show();
    }

    void startPomodoro(){
        pomEnd=System.currentTimeMillis()+25*60*1000; pomRunning=true; page=4; view.invalidate();
    }

    void taskDone(int i){ if(i>=0&&i<tasks.size()){tasks.get(i).status="Completed";save();view.invalidate();} }

    class FocusView extends View {
        Paint p=new Paint(3); float den; RectF r=new RectF();
        int W,H;
        int white=Color.rgb(245,248,255), muted=Color.rgb(143,162,186), bg=Color.rgb(7,17,31), card=Color.rgb(13,28,46);
        int accent=Color.rgb(91,99,255), green=Color.rgb(33,211,155), orange=Color.rgb(255,159,47), pink=Color.rgb(245,78,162), purple=Color.rgb(155,98,255), cyan=Color.rgb(39,216,232);
        FocusView(Context c){super(c);den=getResources().getDisplayMetrics().density;p.setTypeface(Typeface.create("sans",Typeface.NORMAL));setBackgroundColor(bg);}
        float d(float x){return x*den;}
        void rect(Canvas c,float l,float t,float rr,float bb,int color,float rad){p.setColor(color);p.setStyle(Paint.Style.FILL);r.set(d(l),d(t),d(rr),d(bb));c.drawRoundRect(r,d(rad),d(rad),p);}
        void text(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",Typeface.NORMAL));c.drawText(s,d(x),d(y),p);}
        void bold(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(d(size));p.setTypeface(Typeface.create("sans",Typeface.BOLD));c.drawText(s,d(x),d(y),p);}
        void line(Canvas c,float x1,float y1,float x2,float y2,int color,float sw){p.setColor(color);p.setStrokeWidth(d(sw));c.drawLine(d(x1),d(y1),d(x2),d(y2),p);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c); W=getWidth();H=getHeight();
            c.drawColor(bg);
            if(page==0)home(c); else if(page==1)routine(c); else if(page==2)subjects(c); else if(page==3)calendar(c);
            else if(page==4)pomodoro(c); else if(page==5)notesPage(c); else if(page==6)progress(c); else settings(c);
            bottom(c);
        }

        void header(Canvas c,String title,String sub){
            bold(c,title,22,34,22,white); text(c,sub,22,55,11,muted);
            rect(c, W/den-62,15,W/den-20,57,card,22); text(c,"⚙",W/den-49,43,20,white);
        }

        void home(Canvas c){
            header(c,"FocusFlow","Plan  •  Study  •  Grow");
            text(c,"Good morning,",22,86,13,muted);bold(c,"Rafsan 👋",22,108,25,white);text(c,"Keep going! Your goals are closer than you think.",22,128,11,muted);
            stat(c,22,145,120,218,"Today's Study","4h 30m","/ 8h goal",green);
            stat(c,130,145,228,218,"Tasks","5 / 8","Completed",cyan);
            stat(c,238,145,336,218,"Streak","12 days","",orange);
            stat(c,346,145, W/den-22,218,"Total Study","132.5 h","",purple);
            rect(c,22,232,W/den-22,450,card,14);bold(c,"Today's Routine",34,258,17,white);text(c,"View Full Routine →",W/den-135,258,10,cyan);
            int y=276;
            for(int i=0;i<Math.min(5,tasks.size());i++){Task t=tasks.get(i);row(c,34,y,t,i);y+=32;}
            rect(c,22,464,W/den-22,548,card,14);bold(c,"Quick Actions",34,489,15,white);
            action(c,34,501,"＋ Add Task",0);action(c,155,501,"⏱ Pomodoro",4);action(c,276,501,"📅 Calendar",3);
        }

        void stat(Canvas c,float l,float t,float rr,float bb,String a,String b,String sub,int color){
            rect(c,l,t,rr,bb,card2,12);text(c,a,l+10,t+20,9,muted);bold(c,b,l+10,t+47,17,white);text(c,sub,l+10,t+62,8,muted);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(4));p.setColor(color);c.drawArc(new RectF(d(rr-39),d(t+14),d(rr-10),d(t+43)),-90,250,false,p);p.setStyle(Paint.Style.FILL);
        }

        void row(Canvas c,float x,float y,Task t,int i){
            rect(c,x,y,W/den-34,y+28,card2,8);int col=i==0?green:i==1?cyan:i==2?pink:purple;rect(c,x+5,y+5,x+23,y+23,col,5);
            text(c,t.time,x+29,y+13,9,white);text(c,t.subject,x+98,y+13,11,white);text(c,t.status,x+98,y+24,7,muted);
            if(t.status.equals("Completed"))text(c,"✓",W/den-55,y+18,14,green);
        }
        void action(Canvas c,float x,float y,String s,int target){rect(c,x,y,x+105,y+37,card2,9);text(c,s,x+10,y+23,10,white);}

        void routine(Canvas c){
            header(c,"My Routine","Daily • Weekly • Monthly");
            rect(c,22,72,W/den-22,112,card,20);rect(c,24,74,24+(W/den-50)/3,110,accent,18);text(c,"Daily",42,97,11,white);text(c,"Weekly",42+(W/den-50)/3,97,11,muted);text(c,"Monthly",42+2*(W/den-50)/3,97,11,muted);
            rect(c,22,124,W/den-22,204,card,12);bold(c,"4h 30m",40,158,23,white);text(c,"Study Time",40,178,10,muted);text(c,"8h Daily Goal",170,158,11,muted);rect(c,170,168,W/den-42,177,card2,5);rect(c,170,168,170+(W/den-212)*.56f,177,green,5);
            int y=216;for(int i=0;i<tasks.size();i++){row(c,22,y,tasks.get(i),i);y+=44;if(y>H/den-80)break;}
            rect(c,22,H/den-86,W/den-22,H/den-32,accent,14);text(c,"＋  Add Task",W/den/2-42,H/den-52,14,white);
        }

        void subjects(Canvas c){
            header(c,"Subjects & Topics","Track chapters and completion");
            rect(c,22,72,W/den-22,110,card,12);text(c,"⌕  Search subject or topic...",36,97,11,muted);
            String[] ss={"Mathematics","Physics","Chemistry","English"};int[] cc={green,orange,purple,pink};int y=124;
            for(int i=0;i<ss.length;i++){rect(c,22,y,W/den-22,y+76,card,12);rect(c,34,y+14,66,y+46,cc[i],8);bold(c,ss[i],78,y+27,14,white);text(c,(6-i)+" topics   •   "+(8-i)+"/12 completed",78,y+46,10,muted);rect(c,78,y+56,W/den-38,y+61,card2,3);rect(c,78,y+56,78+(W/den-116)*(0.67f-i*.12f),y+61,cc[i],3);text(c,(67-i*9)+"%",W/den-58,y+28,11,white);y+=88;}
        }

        void calendar(Canvas c){
            header(c,"Calendar","21 - 27 Apr 2025");
            rect(c,22,72,W/den-22,118,card,18);text(c,"Day",48,99,11,muted);bold(c,"Week",145,99,11,white);text(c,"Month",245,99,11,muted);
            rect(c,22,130,W/den-22,570,card,12);String[] days={"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};for(int i=0;i<7;i++){text(c,days[i],31+i*47,158,9,muted);text(c,""+(21+i),35+i*47,178,10,white);}
            for(int h=0;h<6;h++){text(c,(8+h*2)+":00",26,214+h*57,8,muted);line(c,72,205+h*57,W/den-30,205+h*57,Color.rgb(25,48,68),1);}
            block(c,80,208,"Math  2h",green);block(c,128,208,"Math  2h",green);block(c,80,266,"Physics 1.5h",orange);block(c,128,266,"Physics 1.5h",orange);block(c,176,323,"English 1.5h",pink);block(c,224,380,"Chemistry 1h",purple);
        }
        void block(Canvas c,float x,float y,String s,int color){rect(c,x,y,x+43,y+48,color,6);text(c,s,x+3,y+19,7,Color.WHITE);}

        void progress(Canvas c){
            header(c,"Progress & Analytics","Overview • Subject-wise • Time Analysis");
            rect(c,22,72,W/den-22,114,card,20);rect(c,24,74,24+(W/den-50)/3,112,accent,18);text(c,"Overview",42,99,10,white);text(c,"Subject-wise",130,99,10,muted);text(c,"Time Analysis",230,99,10,muted);
            stat(c,22,128,(W/den)/2-8,202,"Total Study","132.5 h","+12%",green);stat(c,(W/den)/2+4,128,W/den-22,202,"Daily Average","4.2 h","+0.8 h",cyan);
            rect(c,22,216,W/den-22,410,card,12);bold(c,"Subject Performance",36,244,15,white);text(c,"Mathematics   34%",54,279,11,white);text(c,"Physics       22%",54,305,11,white);text(c,"Chemistry     18%",54,331,11,white);text(c,"English       16%",54,357,11,white);text(c,"Others        10%",54,383,11,muted);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(18));int[] cs={green,orange,purple,pink,cyan};float start=-90;float[] vals={.34f,.22f,.18f,.16f,.10f};for(int i=0;i<5;i++){p.setColor(cs[i]);c.drawArc(new RectF(d(W/den-145),d(265),d(W/den-65),d(345)),start,360*vals[i],false,p);start+=360*vals[i];}p.setStyle(Paint.Style.FILL);
            rect(c,22,424,W/den-22,560,card,12);bold(c,"Weekly Study Hours",36,450,15,white);for(int i=0;i<7;i++){float bh=25+(i%4)*16;rect(c,40+i*43,530-bh,61+i*43,530,accent,4);text(c,""+(4+i%4)+"h",42+i*43,520-bh,8,muted);}
        }

        void pomodoro(Canvas c){
            header(c,"Pomodoro","Focus • Short Break • Long Break");
            float cx=W/den/2, cy=245; p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(10));p.setColor(Color.rgb(47,55,130));c.drawCircle(d(cx),d(cy),d(105),p);p.setColor(accent);c.drawArc(new RectF(d(cx-105),d(cy-105),d(cx+105),d(cy+105)),-90,250,false,p);p.setStyle(Paint.Style.FILL);
            long left=pomRunning?Math.max(0,pomEnd-System.currentTimeMillis()):25*60*1000;long sec=left/1000;String tm=String.format(Locale.US,"%02d:%02d",sec/60,sec%60);text(c,"Focus Time",cx-38,cy-20,13,muted);bold(c,tm,cx-48,cy+22,30,white);
            rect(c,cx-35,cy+50,cx+35,cy+88,accent,19);text(c,pomRunning?"❚❚":"▶",cx-10,cy+76,16,white);
            option(c,120,390,"Focus Time","25 minutes");option(c,120,440,"Short Break","5 minutes");option(c,120,490,"Long Break","15 minutes");
            if(pomRunning && left==0){pomRunning=false;Toast.makeText(MainActivity.this,"Pomodoro complete!",Toast.LENGTH_LONG).show();}
        }
        void option(Canvas c,float x,float y,String a,String b){rect(c,x,y,W/den-50,y+38,card,10);text(c,a,x+12,y+24,11,white);text(c,b,W/den-115,y+24,9,muted);}

        void notesPage(Canvas c){
            header(c,"Notes","Study • Work • Personal");
            rect(c,22,72,W/den-22,110,card,12);text(c,"⌕  Search notes...",36,97,11,muted);
            int y=124;for(int i=0;i<notes.size();i++){rect(c,22,y,W/den-22,y+58,card,11);rect(c,34,y+13,60,y+39,i%2==0?green:orange,6);bold(c,notes.get(i),72,y+25,12,white);text(c,"Study  •  Today",72,y+43,8,muted);y+=70;}
            rect(c,W/den-70,H/den-92,W/den-22,H/den-44,accent,24);text(c,"+",W/den-54,H/den-61,24,white);
        }

        void settings(Canvas c){
            header(c,"Profile & Settings","Rafsan Ahmed");
            rect(c,22,72,W/den-22,145,card,14);bold(c,"Rafsan Ahmed",40,101,17,white);text(c,"rafasan@focusflow.com",40,121,10,muted);
            option(c,22,160,"Theme","Dark Mode");option(c,22,208,"Notifications","On");option(c,22,256,"Sync with Google Calendar","On");option(c,22,304,"Data Backup","Last sync: Today");option(c,22,352,"App Version","2.0");rect(c,22,414,W/den-22,458,card,12);text(c,"Log Out",40,442,12,Color.RED);
        }

        void bottom(Canvas c){
            float y=H/den-58;rect(c,0,y,W/den,H/den,Color.rgb(8,20,34),0);String[] a={"⌂ Home","▣ Routine","◈ Subjects","▦ Calendar","◎ Focus","◫ Notes","◉ Progress","⚙ Settings"};
            float w=W/den/a.length;for(int i=0;i<a.length;i++){int col=i==page?white:muted;text(c,a[i],i*w+8,y+34,8,col);}
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            float x=e.getX()/den,y=e.getY()/den;
            if(y>H/den-70){
                int idx=(int)(x/(W/den/8)); if(idx>=0&&idx<8){page=idx;invalidate();return true;}
            }
            if(page==0){
                if(y>490&&y<545&&x<145){addTaskDialog();return true;}
                if(y>490&&y<545&&x<270){startPomodoro();return true;}
                if(y>490&&y<545&&x>270){page=3;invalidate();return true;}
                if(y>276&&y<450){int i=(int)((y-276)/32); if(i<tasks.size())taskDone(i);}
            } else if(page==4 && y>290&&y<370){ if(!pomRunning)startPomodoro(); else {pomRunning=false;invalidate();} }
            else if(page==5 && y>H/den-120){notesDialog();}
            else if(page==1 && y>H/den-120){addTaskDialog();}
            return true;
        }
    }
}
