package com.focusflow.planner;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import android.util.Base64;
import java.util.*;

public class MainActivity extends Activity {
    final int BG=Color.rgb(7,17,31), CARD=Color.rgb(13,29,49), CARD2=Color.rgb(17,41,67);
    final int TEXT=Color.rgb(246,248,255), MUTED=Color.rgb(145,164,189), BLUE=Color.rgb(93,107,255);
    final int CYAN=Color.rgb(42,214,232), GREEN=Color.rgb(31,211,154), ORANGE=Color.rgb(255,157,46);
    final int PINK=Color.rgb(244,81,166), PURPLE=Color.rgb(155,92,255), LINE=Color.rgb(32,59,88);

    LinearLayout content, root, nav;
    SharedPreferences pref;
    ArrayList<Topic> topics=new ArrayList<>();
    ArrayList<Routine> routines=new ArrayList<>();
    ArrayList<Target> targets=new ArrayList<>();
    ArrayList<Task> tasks=new ArrayList<>();
    Handler handler=new Handler(Looper.getMainLooper());
    int page=0;
    boolean pom=false; long pomEnd=0;

    static class Topic {
        String subject,name,chapter,note;
        Topic(String s,String n,String c,String x){subject=s;name=n;chapter=c;note=x;}
        String enc(){return e(subject)+"~"+e(name)+"~"+e(chapter)+"~"+e(note);}
        static Topic dec(String s){String[] a=s.split("~",-1);return a.length==4?new Topic(d(a[0]),d(a[1]),d(a[2]),d(a[3])):null;}
    }
    static class Routine {
        String title,day,start,end,subject,topic,repeat;
        Routine(String t,String d,String s,String e,String sub,String top,String r){title=t;day=d;start=s;end=e;subject=sub;topic=top;repeat=r;}
        String enc(){return e(title)+"~"+e(day)+"~"+e(start)+"~"+e(end)+"~"+e(subject)+"~"+e(topic)+"~"+e(repeat);}
        static Routine dec(String s){String[] a=s.split("~",-1);return a.length==7?new Routine(d(a[0]),d(a[1]),d(a[2]),d(a[3]),d(a[4]),d(a[5]),d(a[6])):null;}
    }
    static class Target {
        String month,title,subject,goal,unit,done;
        Target(String m,String t,String s,String g,String u,String d){month=m;title=t;subject=s;goal=g;unit=u;done=d;}
        String enc(){return e(month)+"~"+e(title)+"~"+e(subject)+"~"+e(goal)+"~"+e(unit)+"~"+e(done);}
        static Target dec(String s){String[] a=s.split("~",-1);return a.length==6?new Target(d(a[0]),d(a[1]),d(a[2]),d(a[3]),d(a[4]),d(a[5])):null;}
    }
    static class Task {
        String subject,topic,name,start,end,status;
        Task(String s,String t,String n,String a,String b,String st){subject=s;topic=t;name=n;start=a;end=b;status=st;}
        String enc(){return e(subject)+"~"+e(topic)+"~"+e(name)+"~"+e(start)+"~"+e(end)+"~"+e(status);}
        static Task dec(String s){String[] a=s.split("~",-1);return a.length==6?new Task(d(a[0]),d(a[1]),d(a[2]),d(a[3]),d(a[4]),d(a[5])):null;}
    }

    static String e(String s){return Base64.encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8),Base64.NO_WRAP);}
    static String d(String s){try{return new String(Base64.decode(s,Base64.NO_WRAP),java.nio.charset.StandardCharsets.UTF_8);}catch(Exception x){return s;}}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        pref=getSharedPreferences("focusflow_v4",0);
        load(); shell(); show(0);
        handler.post(new Runnable(){public void run(){
            if(pom && System.currentTimeMillis()>=pomEnd){pom=false; Toast.makeText(MainActivity.this,"পোমোডোরো সম্পন্ন হয়েছে!",Toast.LENGTH_LONG).show();}
            handler.postDelayed(this,1000);
        }});
    }

    void load(){
        read(pref.getString("topics",""),topics,0);
        read(pref.getString("routines",""),routines,1);
        read(pref.getString("targets",""),targets,2);
        read(pref.getString("tasks",""),tasks,3);
        if(topics.isEmpty()){
            topics.add(new Topic("গণিত","বীজগণিত","অধ্যায় ৩","অসমতা ও সমীকরণ"));
            topics.add(new Topic("পদার্থবিজ্ঞান","গতি","অধ্যায় ২","Numerical Practice"));
            topics.add(new Topic("ইংরেজি","রচনা","অধ্যায় ১","Essay Writing"));
        }
        if(routines.isEmpty()){
            routines.add(new Routine("গণিত অনুশীলন","শনিবার","08:00 AM","10:00 AM","গণিত","বীজগণিত","সাপ্তাহিক"));
            routines.add(new Routine("পদার্থবিজ্ঞান","রবিবার","10:30 AM","12:00 PM","পদার্থবিজ্ঞান","গতি","সাপ্তাহিক"));
        }
        if(targets.isEmpty()) targets.add(new Target("এপ্রিল ২০২৫","মাসিক পড়াশোনা লক্ষ্য","সব বিষয়","80","ঘণ্টা","34"));
        if(tasks.isEmpty()){
            tasks.add(new Task("গণিত","বীজগণিত","অনুশীলনী ৩.১ - ৩.৫","08:00 AM","10:00 AM","সম্পন্ন"));
            tasks.add(new Task("পদার্থবিজ্ঞান","গতি","গাণিতিক অনুশীলন","10:30 AM","12:00 PM","চলছে"));
            tasks.add(new Task("ইংরেজি","রচনা","ভূমিকা লিখুন","01:30 PM","03:00 PM","অপেক্ষমাণ"));
            tasks.add(new Task("রসায়ন","বিক্রিয়া","রিভিশন","04:00 PM","05:00 PM","অপেক্ষমাণ"));
        }
    }

    void read(String raw,ArrayList list,int type){
        if(raw==null||raw.isEmpty())return;
        for(String s:raw.split("\\|",-1)){
            Object o=null;
            if(type==0)o=Topic.dec(s); else if(type==1)o=Routine.dec(s); else if(type==2)o=Target.dec(s); else o=Task.dec(s);
            if(o!=null)list.add(o);
        }
    }

    void save(){
        ArrayList<String> a=new ArrayList<>();for(Topic x:topics)a.add(x.enc());pref.edit().putString("topics",String.join("|",a)).apply();
        a.clear();for(Routine x:routines)a.add(x.enc());pref.edit().putString("routines",String.join("|",a)).apply();
        a.clear();for(Target x:targets)a.add(x.enc());pref.edit().putString("targets",String.join("|",a)).apply();
        a.clear();for(Task x:tasks)a.add(x.enc());pref.edit().putString("tasks",String.join("|",a)).apply();
    }

    int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView tv(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    TextView bold(String s,float z,int c){TextView t=tv(s,z,c);t.setTypeface(android.graphics.Typeface.DEFAULT,1);return t;}
    GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    GradientDrawable outline(int c,float r){GradientDrawable g=bg(c,r);g.setStroke(dp(1),LINE);return g;}
    LinearLayout card(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(14),dp(12),dp(14),dp(12));x.setBackground(outline(CARD,16));return x;}
    LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);x.setGravity(Gravity.CENTER_VERTICAL);return x;}
    void margin(View v,int l,int t,int r,int b){if(v.getLayoutParams()==null)v.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));ViewGroup.MarginLayoutParams m=(ViewGroup.MarginLayoutParams)v.getLayoutParams();m.setMargins(dp(l),dp(t),dp(r),dp(b));}
    EditText input(String hint,String value){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(TEXT);e.setText(value);e.setSingleLine();e.setBackground(outline(CARD2,12));e.setPadding(dp(12),0,dp(12),0);margin(e,0,5,0,5);return e;}

    void shell(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        ScrollView sc=new ScrollView(this);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);sc.addView(content);
        root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setBackgroundColor(Color.rgb(8,21,36));nav.setGravity(Gravity.CENTER);
        String[] n={"⌂\nহোম","▣\nরুটিন","◈\nটপিক","▦\nক্যালেন্ডার","◎\nফোকাস","▤\nনোট","◌\nটার্গেট","⚙\nসেটিংস"};
        for(int i=0;i<n.length;i++){final int p=i;TextView b=tv(n[i],9,MUTED);b.setGravity(Gravity.CENTER);b.setOnClickListener(v->show(p));nav.addView(b,new LinearLayout.LayoutParams(0,dp(60),1));}
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(65)));setContentView(root);
    }

    void show(int p){page=p;content.removeAllViews();if(p==0)home();else if(p==1)routine();else if(p==2)topicsPage();else if(p==3)calendar();else if(p==4)pomodoro();else if(p==5)notes();else if(p==6)targetsPage();else settings();}

    void head(String title,String sub){
        LinearLayout h=row();h.setPadding(dp(20),dp(18),dp(20),dp(8));
        LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.addView(bold(title,24,TEXT));l.addView(tv(sub,11,MUTED));
        h.addView(l,new LinearLayout.LayoutParams(0,-2,1));TextView g=tv("⚙",22,TEXT);g.setGravity(Gravity.CENTER);g.setBackground(bg(CARD,30));h.addView(g,new LinearLayout.LayoutParams(dp(54),dp(54)));content.addView(h);
    }

    void section(String title,String action,View.OnClickListener l){
        LinearLayout h=row();h.setPadding(dp(20),dp(9),dp(20),dp(8));h.addView(bold(title,18,TEXT),new LinearLayout.LayoutParams(0,-2,1));
        TextView a=tv(action,10,CYAN);if(l!=null)a.setOnClickListener(l);h.addView(a);content.addView(h);
    }

    void home(){
        head("ফোকাসফ্লো","পরিকল্পনা  •  পড়াশোনা  •  উন্নতি");
        LinearLayout hi=new LinearLayout(this);hi.setOrientation(LinearLayout.VERTICAL);hi.setPadding(dp(20),dp(8),dp(20),dp(12));
        hi.addView(tv("শুভ সকাল,",14,MUTED));hi.addView(bold("রাফসান 👋",30,TEXT));hi.addView(tv("আজকের লক্ষ্যগুলো শেষ করি!",12,MUTED));content.addView(hi);

        LinearLayout stats=row();stats.setPadding(dp(20),0,dp(12),dp(8));content.addView(stats);
        stat(stats,"আজকের পড়াশোনা","৪ ঘণ্টা ৩০ মিনিট","/ ৮ ঘণ্টা",GREEN);
        stat(stats,"কাজ","৫ / ৮","সম্পন্ন",CYAN);
        stat(stats,"ধারাবাহিকতা","১২ দিন","",ORANGE);
        stat(stats,"মাসিক লক্ষ্য","৩৪ / ৮০ ঘণ্টা","৪২.৫%",PURPLE);

        section("আজকের রুটিন","সব দেখুন →",v->show(1));
        LinearLayout c=card();margin(c,20,0,20,14);for(int i=0;i<tasks.size();i++)task(c,tasks.get(i),i);content.addView(c);

        section("দ্রুত কাজ","",null);
        LinearLayout q=row();q.setPadding(dp(20),0,dp(20),dp(15));content.addView(q);
        quick(q,"＋\nনতুন কাজ",()->taskDialog());
        quick(q,"＋\nনতুন রুটিন",()->routineDialog());
        quick(q,"🎯\nমাসিক লক্ষ্য",()->targetDialog());
    }

    void stat(LinearLayout p,String a,String b,String c,int color){
        LinearLayout x=card();x.setPadding(dp(10),dp(8),dp(10),dp(8));x.setMinimumWidth(dp(132));
        x.addView(tv(a,9,MUTED));x.addView(bold(b,17,TEXT));x.addView(tv(c,9,color));p.addView(x,new LinearLayout.LayoutParams(0,dp(94),1));((ViewGroup.MarginLayoutParams)x.getLayoutParams()).setMargins(0,0,dp(6),0);
    }
    void quick(LinearLayout p,String s,final Runnable r){TextView b=tv(s,11,TEXT);b.setGravity(Gravity.CENTER);b.setBackground(outline(CARD,14));b.setOnClickListener(v->r.run());p.addView(b,new LinearLayout.LayoutParams(0,dp(70),1));((ViewGroup.MarginLayoutParams)b.getLayoutParams()).setMargins(dp(3),0,dp(3),0);}

    void task(LinearLayout p,Task t,int i){
        LinearLayout r=row();r.setPadding(dp(7),dp(6),dp(7),dp(6));r.setBackground(bg(CARD2,10));
        TextView dot=tv(i==0?"✓":"○",17,i==0?GREEN:MUTED);dot.setGravity(Gravity.CENTER);r.addView(dot,new LinearLayout.LayoutParams(dp(30),dp(52)));
        LinearLayout m=new LinearLayout(this);m.setOrientation(LinearLayout.VERTICAL);m.addView(tv(t.start+" - "+t.end,10,TEXT));m.addView(bold(t.subject,13,TEXT));m.addView(tv(t.topic,9,MUTED));r.addView(m,new LinearLayout.LayoutParams(0,dp(52),1));
        r.addView(tv(t.status,9,t.status.equals("সম্পন্ন")?GREEN:(t.status.equals("চলছে")?CYAN:MUTED)),new LinearLayout.LayoutParams(dp(65),dp(45)));
        r.setOnClickListener(v->taskMenu(t));p.addView(r,new LinearLayout.LayoutParams(-1,dp(62)));((ViewGroup.MarginLayoutParams)r.getLayoutParams()).setMargins(0,0,0,dp(5));
    }

    void routine(){
        head("আমার রুটিন","দৈনিক  •  সাপ্তাহিক  •  মাসিক");
        LinearLayout tabs=row();tabs.setPadding(dp(20),dp(7),dp(20),dp(10));content.addView(tabs);
        for(String s:new String[]{"দৈনিক","সাপ্তাহিক","মাসিক"}){TextView b=tv(s,11,TEXT);b.setGravity(Gravity.CENTER);b.setBackground(bg(s.equals("দৈনিক")?BLUE:CARD,18));tabs.addView(b,new LinearLayout.LayoutParams(0,dp(42),1));}
        TextView info=tv("নিজের সময় অনুযায়ী রুটিন বানান। প্রতিদিন বা সপ্তাহের নির্দিষ্ট দিনে আলাদা রুটিন রাখা যাবে।",11,MUTED);margin(info,20,0,20,10);content.addView(info);
        for(int i=0;i<routines.size();i++)routineCard(routines.get(i),i);
        TextView add=tv("＋  নতুন রুটিন তৈরি করুন",14,TEXT);add.setGravity(Gravity.CENTER);add.setBackground(bg(BLUE,15));add.setOnClickListener(v->routineDialog());margin(add,20,10,20,20);add.setPadding(0,dp(14),0,dp(14));content.addView(add);
    }

    void routineCard(Routine r,int idx){
        LinearLayout c=card();margin(c,20,0,20,8);LinearLayout h=row();h.addView(bold(r.title,14,TEXT),new LinearLayout.LayoutParams(0,-2,1));h.addView(tv(r.day,10,CYAN));c.addView(h);
        c.addView(tv(r.start+" - "+r.end+"   •   "+r.subject+"   •   "+r.topic,10,MUTED));
        c.addView(tv("পুনরাবৃত্তি: "+r.repeat,9,ORANGE));
        c.setOnClickListener(v->routineMenu(idx));content.addView(c);
    }

    void topicsPage(){
        head("আমার টপিক","নিজের ইচ্ছামতো বিষয়, অধ্যায় ও টপিক তৈরি করুন");
        TextView info=tv("যে কোনো Subject → Topic → Chapter নিজের মতো লিখে Save করুন।",11,MUTED);margin(info,20,2,20,10);content.addView(info);
        for(int i=0;i<topics.size();i++){final int k=i;Topic x=topics.get(i);LinearLayout c=card();margin(c,20,0,20,8);LinearLayout h=row();h.addView(bold(x.subject,15,TEXT),new LinearLayout.LayoutParams(0,-2,1));TextView del=tv("⋮",20,MUTED);h.addView(del,new LinearLayout.LayoutParams(dp(35),dp(35)));c.addView(h);c.addView(tv("📘 "+x.name+"   •   "+x.chapter,11,CYAN));c.addView(tv(x.note,9,MUTED));c.setOnClickListener(v->topicMenu(k));content.addView(c);}
        TextView add=tv("＋  নিজের টপিক যোগ করুন",14,TEXT);add.setGravity(Gravity.CENTER);add.setBackground(bg(BLUE,15));add.setOnClickListener(v->topicDialog());margin(add,20,10,20,20);add.setPadding(0,dp(14),0,dp(14));content.addView(add);
    }

    void calendar(){
        head("ক্যালেন্ডার","তোমার Daily & Weekly routine একসাথে");
        LinearLayout c=card();margin(c,20,0,20,12);c.addView(bold("এপ্রিল ২০২৫",16,TEXT));c.addView(tv("সোম   মঙ্গল   বুধ   বৃহস্পতি   শুক্র   শনি   রবি",9,MUTED));c.addView(tv("  ২১      ২২      ২৩        ২৪        ২৫      ২৬      ২৭",12,TEXT));content.addView(c);
        for(Routine r:routines){LinearLayout x=card();margin(x,20,0,20,7);x.addView(bold(r.day+"  •  "+r.start+" - "+r.end,12,TEXT));x.addView(tv(r.subject+"  •  "+r.topic,10,CYAN));content.addView(x);}
    }

    void pomodoro(){
        head("পোমোডোরো","ফোকাস  •  ছোট বিরতি  •  দীর্ঘ বিরতি");
        LinearLayout c=card();margin(c,20,12,20,14);c.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView timer=bold(time(),34,TEXT);timer.setGravity(Gravity.CENTER);timer.setBackground(outline(CARD2,120));c.addView(timer,new LinearLayout.LayoutParams(dp(210),dp(210)));
        c.addView(tv(pom?"ফোকাস সেশন চলছে":"ফোকাস সময়",12,MUTED));
        Button b=new Button(this);b.setText(pom?"বিরতি দিন":"ফোকাস শুরু করুন");b.setTextColor(TEXT);b.setBackground(bg(BLUE,14));b.setOnClickListener(v->{if(pom)pom=false;else{pom=true;pomEnd=System.currentTimeMillis()+25*60*1000;}show(4);});c.addView(b,new LinearLayout.LayoutParams(dp(170),dp(50)));content.addView(c);
        String[][] a={{"ফোকাস সময়","২৫ মিনিট"},{"ছোট বিরতি","৫ মিনিট"},{"দীর্ঘ বিরতি","১৫ মিনিট"}};for(String[] z:a){LinearLayout x=card();margin(x,20,0,20,7);LinearLayout r=row();r.addView(tv(z[0],12,TEXT),new LinearLayout.LayoutParams(0,-2,1));r.addView(tv(z[1],10,MUTED));x.addView(r);content.addView(x);}
    }
    String time(){long l=pom?Math.max(0,pomEnd-System.currentTimeMillis()):1500000;return String.format(Locale.US,"%02d:%02d",l/60000,(l/1000)%60);}

    void notes(){
        head("নোট","পড়াশোনা  •  কাজ  •  ব্যক্তিগত");
        TextView add=tv("＋  নতুন নোট",13,TEXT);add.setGravity(Gravity.CENTER);add.setBackground(bg(BLUE,14));add.setOnClickListener(v->noteDialog());margin(add,20,4,20,10);add.setPadding(0,dp(13),0,dp(13));content.addView(add);
        String[] ns={"বীজগণিতের সূত্র","পদার্থবিজ্ঞানের গুরুত্বপূর্ণ প্রশ্ন","ইংরেজি রচনার আইডিয়া","রসায়নের বিক্রিয়া নোট"};for(String n:ns){LinearLayout c=card();margin(c,20,0,20,7);c.addView(bold(n,13,TEXT));c.addView(tv("আজ  •  পড়াশোনা",9,MUTED));content.addView(c);}
    }

    void targetsPage(){
        head("মাসিক লক্ষ্য","এই মাসে কী অর্জন করতে চাও সেট করুন");
        LinearLayout summary=card();margin(summary,20,0,20,12);summary.addView(bold("এপ্রিল ২০২৫",16,TEXT));summary.addView(tv("মোট লক্ষ্য ৮০ ঘণ্টা  •  সম্পন্ন ৩৪ ঘণ্টা",11,MUTED));ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(80);pb.setProgress(34);summary.addView(pb,new LinearLayout.LayoutParams(-1,dp(8)));summary.addView(tv("৪২.৫% সম্পন্ন",10,GREEN));content.addView(summary);
        for(int i=0;i<targets.size();i++){final int k=i;Target t=targets.get(i);LinearLayout c=card();margin(c,20,0,20,8);LinearLayout h=row();h.addView(bold(t.title,14,TEXT),new LinearLayout.LayoutParams(0,-2,1));h.addView(tv(t.done+" / "+t.goal+" "+t.unit,11,CYAN));c.addView(h);c.addView(tv(t.subject+"  •  "+t.month,9,MUTED));ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(Integer.parseInt(t.goal));p.setProgress(Math.min(Integer.parseInt(t.goal),Integer.parseInt(t.done)));c.addView(p,new LinearLayout.LayoutParams(-1,dp(8)));c.setOnClickListener(v->targetMenu(k));content.addView(c);}
        TextView add=tv("＋  নতুন মাসিক লক্ষ্য",14,TEXT);add.setGravity(Gravity.CENTER);add.setBackground(bg(BLUE,15));add.setOnClickListener(v->targetDialog());margin(add,20,10,20,20);add.setPadding(0,dp(14),0,dp(14));content.addView(add);
    }

    void settings(){
        head("সেটিংস","প্রোফাইল ও অ্যাপ সেটিংস");
        String[][] a={{"প্রোফাইল","রাফসান আহমেদ"},{"থিম","ডার্ক মোড"},{"নোটিফিকেশন","চালু"},{"গুগল ক্যালেন্ডার","বন্ধ"},{"ডাটা ব্যাকআপ","আজ সিঙ্ক হয়েছে"},{"অ্যাপ ভার্সন","৪.০"}};for(String[] z:a){LinearLayout c=card();margin(c,20,0,20,7);LinearLayout r=row();r.addView(tv(z[0],12,TEXT),new LinearLayout.LayoutParams(0,-2,1));r.addView(tv(z[1],10,MUTED));c.addView(r);content.addView(c);}
    }

    void topicDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText s=input("বিষয় যেমন: গণিত",""),n=input("টপিক যেমন: বীজগণিত",""),c=input("অধ্যায় যেমন: অধ্যায় ৩",""),note=input("নোট / বর্ণনা","");
        box.addView(s);box.addView(n);box.addView(c);box.addView(note);
        new AlertDialog.Builder(this).setTitle("নিজের টপিক তৈরি করুন").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("টপিক সেভ করুন",(d,w)->{if(n.getText().toString().trim().isEmpty())return;topics.add(new Topic(s.getText().toString().trim(),n.getText().toString().trim(),c.getText().toString().trim(),note.getText().toString().trim()));save();show(2);}).show();
    }

    void topicMenu(int i){
        new AlertDialog.Builder(this).setTitle(topics.get(i).name).setMessage("বিষয়: "+topics.get(i).subject+"\nঅধ্যায়: "+topics.get(i).chapter+"\n"+topics.get(i).note)
        .setPositiveButton("সম্পাদনা",(d,w)->topicEdit(i)).setNegativeButton("মুছে ফেলুন",(d,w)->{topics.remove(i);save();show(2);}).show();
    }
    void topicEdit(int i){
        Topic old=topics.get(i);LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText s=input("বিষয়",old.subject),n=input("টপিক",old.name),c=input("অধ্যায়",old.chapter),note=input("নোট",old.note);box.addView(s);box.addView(n);box.addView(c);box.addView(note);
        new AlertDialog.Builder(this).setTitle("টপিক সম্পাদনা").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("সেভ",(d,w)->{topics.set(i,new Topic(s.getText().toString(),n.getText().toString(),c.getText().toString(),note.getText().toString()));save();show(2);}).show();
    }

    void routineDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText title=input("রুটিনের নাম","সকালের পড়াশোনা"),day=input("দিন: প্রতিদিন / শনিবার / রবিবার","প্রতিদিন"),start=input("শুরুর সময়","08:00 AM"),end=input("শেষের সময়","10:00 AM"),subject=input("বিষয়","গণিত"),topic=input("টপিক","বীজগণিত"),repeat=input("পুনরাবৃত্তি","দৈনিক");
        box.addView(title);box.addView(day);box.addView(start);box.addView(end);box.addView(subject);box.addView(topic);box.addView(repeat);
        new AlertDialog.Builder(this).setTitle("Daily / Weekly রুটিন তৈরি করুন").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("রুটিন সেভ করুন",(d,w)->{routines.add(new Routine(title.getText().toString(),day.getText().toString(),start.getText().toString(),end.getText().toString(),subject.getText().toString(),topic.getText().toString(),repeat.getText().toString()));save();show(1);}).show();
    }
    void routineMenu(int i){new AlertDialog.Builder(this).setTitle(routines.get(i).title).setMessage("দিন: "+routines.get(i).day+"\nসময়: "+routines.get(i).start+" - "+routines.get(i).end+"\n"+routines.get(i).subject+" • "+routines.get(i).topic).setNegativeButton("মুছে ফেলুন",(d,w)->{routines.remove(i);save();show(1);}).setPositiveButton("ঠিক আছে",null).show();}

    void targetDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText month=input("মাস","মে ২০২৫"),title=input("লক্ষ্যের নাম","এই মাসে ৮০ ঘণ্টা পড়াশোনা"),subject=input("বিষয় / সব বিষয়","সব বিষয়"),goal=input("টার্গেট সংখ্যা","80"),unit=input("ইউনিট: ঘণ্টা / টপিক / অধ্যায়","ঘণ্টা"),done=input("ইতিমধ্যে কত হয়েছে","0");
        box.addView(month);box.addView(title);box.addView(subject);box.addView(goal);box.addView(unit);box.addView(done);
        new AlertDialog.Builder(this).setTitle("মাসিক লক্ষ্য সেট করুন").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("লক্ষ্য সেভ করুন",(d,w)->{targets.add(new Target(month.getText().toString(),title.getText().toString(),subject.getText().toString(),goal.getText().toString(),unit.getText().toString(),done.getText().toString()));save();show(6);}).show();
    }
    void targetMenu(int i){Target t=targets.get(i);new AlertDialog.Builder(this).setTitle(t.title).setMessage(t.done+" / "+t.goal+" "+t.unit+"\nবিষয়: "+t.subject).setPositiveButton("অগ্রগতি আপডেট",(d,w)->targetUpdate(i)).setNegativeButton("মুছে ফেলুন",(d,w)->{targets.remove(i);save();show(6);}).show();}
    void targetUpdate(int i){EditText e=input("বর্তমান অগ্রগতি",targets.get(i).done);new AlertDialog.Builder(this).setTitle("অগ্রগতি আপডেট করুন").setView(e).setNegativeButton("বাতিল",null).setPositiveButton("সেভ",(d,w)->{targets.get(i).done=e.getText().toString();save();show(6);}).show();}

    void taskDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);
        EditText s=input("বিষয়","গণিত"),t=input("টপিক","বীজগণিত"),n=input("কাজের নাম","অনুশীলনী সমাধান"),a=input("শুরুর সময়","08:00 AM"),b=input("শেষের সময়","10:00 AM");
        box.addView(s);box.addView(t);box.addView(n);box.addView(a);box.addView(b);
        new AlertDialog.Builder(this).setTitle("নতুন কাজ যোগ করুন").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("সেভ",(d,w)->{tasks.add(new Task(s.getText().toString(),t.getText().toString(),n.getText().toString(),a.getText().toString(),b.getText().toString(),"অপেক্ষমাণ"));save();show(page);}).show();
    }
    void taskMenu(Task t){new AlertDialog.Builder(this).setTitle(t.subject+" • "+t.topic).setMessage(t.name+"\n"+t.start+" - "+t.end).setPositiveButton("সম্পন্ন করুন",(d,w)->{t.status="সম্পন্ন";save();show(page);}).setNegativeButton("মুছে ফেলুন",(d,w)->{tasks.remove(t);save();show(page);}).show();}
    void noteDialog(){EditText e=input("নোট লিখুন","");e.setSingleLine(false);e.setMinLines(5);new AlertDialog.Builder(this).setTitle("নতুন নোট").setView(e).setNegativeButton("বাতিল",null).setPositiveButton("সেভ",(d,w)->Toast.makeText(this,"নোট সেভ হয়েছে",Toast.LENGTH_SHORT).show()).show();}
}
