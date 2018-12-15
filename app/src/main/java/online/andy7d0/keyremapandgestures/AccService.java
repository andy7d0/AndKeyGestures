package online.andy7d0.keyremapandgestures;

import android.accessibilityservice.AccessibilityService;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.drm.DrmStore;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccService extends AccessibilityService {

    long mPrevUp = 0;
    SharedPreferences mPrefs;
    private ImageButton homeButton;
    private ImageButton searchButton;
    private View watchOnButton;
    private WindowManager wm;

    private View watchView;

    private static int[] keycodes = {
            KeyEvent.KEYCODE_POWER
            , KeyEvent.KEYCODE_VOLUME_UP
            , KeyEvent.KEYCODE_VOLUME_DOWN
            , KeyEvent.KEYCODE_APP_SWITCH
            , KeyEvent.KEYCODE_HOME
            , KeyEvent.KEYCODE_BACK
    };

    private static final String[] keypfx = {
            "power"
            ,"volume_up"
            ,"volume_down"
            ,"recent"
            ,"home"
            ,"back"
    };

    public static String[] app_shortcuts = new String[5];

    private static long[] down_time = new long[10];
    private static long[] down_time2 = new long[10];

    public AccService() {
    }

    @Override
    public void onServiceConnected() {
        // your code...
        Log.d("/KE/sc","sc");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        final AccService service = this;

        homeButton = new ImageButton(this);
        homeButton.setImageResource(R.drawable.ic_home_black_24dp);
        homeButton.setPadding(200,50,200,50);
        homeButton.setBackgroundColor(Color.BLACK);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                service.performGlobalAction(service.GLOBAL_ACTION_HOME);
            }
        });
        homeButton.setVisibility(View.GONE);

        searchButton = new ImageButton(this);
        searchButton.setImageResource(R.drawable.ic_search_black_24dp);
        searchButton.setPadding(200,50,200,50);
        searchButton.setBackgroundColor(Color.BLACK);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        searchButton.setVisibility(View.GONE);

        watchOnButton = new View(this);
        watchOnButton.setPadding(0,0,0,0);
        final GestureDetector gestureDetector =
                new GestureDetector(this,
                        new GestureDetector.SimpleOnGestureListener() {

                            private static final int SWIPE_THRESHOLD = 100;
                            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }

                            @Override
                            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                                boolean result = false;
                                try {
                                    float diffY = e2.getY() - e1.getY();
                                    float diffX = e2.getX() - e1.getX();
                                    if (Math.abs(diffX) > Math.abs(diffY)) {
                                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                            if (diffX > 0) {
                                                onSwipeRight();
                                            } else {
                                                onSwipeLeft();
                                            }
                                            result = true;
                                        }
                                    }
                                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                                        if (diffY > 0) {
                                            onSwipeBottom();
                                        } else {
                                            onSwipeTop();
                                        }
                                        result = true;
                                    }
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                                return result;
                            }
                            public void onSwipeRight() {
                                Log.d("/KE/swipe","R");
                            }

                            public void onSwipeLeft() {
                                Log.d("/KE/swipe","L");
                            }

                            public void onSwipeTop() {
                                Log.d("/KE/swipe","T");
                            }

                            public void onSwipeBottom() {
                                Log.d("/KE/swipe","B");
                                InitWatchView(watchView, mPrefs);
                            }
                        }
                );
        watchOnButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
                return gestureDetector.onTouchEvent(event);
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                //| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                ,
                PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;

        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this))
                wm.addView(homeButton, params);
        }
        params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this))
                wm.addView(searchButton, params);
        }


        WindowManager.LayoutParams paramsView =
                new WindowManager.LayoutParams();
        paramsView.copyFrom(params);
        paramsView.gravity = Gravity.LEFT | Gravity.TOP;
        paramsView.width = WindowManager.LayoutParams.MATCH_PARENT;
        paramsView.height = WindowManager.LayoutParams.MATCH_PARENT;

        watchView = new FrameLayout(this);
        watchView.setVisibility(View.GONE);
        watchView.setBackgroundColor(Color.TRANSPARENT);
        watchView.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        watchView.setVisibility(View.GONE);
                    }
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this))
                wm.addView(watchView, paramsView);
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        inflater.inflate(R.layout.activity_watch, (FrameLayout)watchView);


        ((ImageButton)watchView.findViewById(R.id.alarmButton)).setImageResource(R.drawable.ic_alarm_black_24dp);


        params.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.width = 410;
        params.height = 130;
        params.x = -10;
        params.y = -10;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.canDrawOverlays(this))
                wm.addView(watchOnButton, params);
        }
        Log.d("/KE/sc", "end");
    }

    public static void InitWatchView(View watchView, SharedPreferences mPrefs) {
        watchView.setVisibility(View.VISIBLE);
        final View rl = (watchView.findViewById(R.id.watch_content));
        rl.setTranslationY(-rl.getHeight());
        rl.animate()
                .setDuration(100)
                .translationY(0);
        AlarmManager alarmManager = (AlarmManager)
                watchView.getContext().getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarm = alarmManager.getNextAlarmClock();
        final TextView alarmView = ((TextView)watchView.findViewById(R.id.alarm));
        final TextView alarmView2 = ((TextView)watchView.findViewById(R.id.alarm2));
        Log.d("/KE/alarm/", alarm != null ? alarm.toString() : "null");
        if(alarm == null) {
            alarmView.setText("");
            alarmView2.setText("");
            ((ViewGroup)watchView.findViewById(R.id.alarmGrp)).setVisibility(View.GONE);
        } else {
            long tt = alarm.getTriggerTime();
            String ts = android.text.format.DateUtils
                    .formatDateTime(watchView.getContext()
                            , tt
                            , android.text.format.DateUtils.FORMAT_SHOW_TIME);
            alarmView.setText(ts);
            if (tt >= System.currentTimeMillis() + 24 * 60 * 60 * 1000) {
                ts = android.text.format.DateUtils
                        .formatDateTime(watchView.getContext()
                                , tt
                                , android.text.format.DateUtils.FORMAT_SHOW_DATE
                                        |android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
                                        |android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY
                                        |android.text.format.DateUtils.FORMAT_ABBREV_MONTH
                                        |android.text.format.DateUtils.FORMAT_ABBREV_ALL
                        );
                alarmView2.setText(ts);
            } else
                alarmView2.setText("");
            ((ViewGroup)watchView.findViewById(R.id.alarmGrp)).setVisibility(View.VISIBLE);
        }
        Log.d("/KE/short", "0");
        boolean sChanged = false;
        for(int i =0; i < app_shortcuts.length; ++i) {
            if(app_shortcuts[i] == null || !app_shortcuts[i].equals(
                    mPrefs.getString(
                            String.format("apps_slot_%d", i+1)
                            , ""))) {
                sChanged = true;
                app_shortcuts[i] = mPrefs.getString(
                        String.format("apps_slot_%d", i+1)
                        , "");
            }
        }

        final Context ctx = watchView.getContext();
        final View ww = watchView;

        final View.OnClickListener callApp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ww.setVisibility(View.GONE);
                String app = (String)v.getTag();
                Log.d("/KE/short/call/", app);
                Intent i = app.equals("") ? null :
                        ctx.getPackageManager()
                                .getLaunchIntentForPackage(app);
                if(i != null)
                    try {
                        ctx.startActivity(i);
                    } catch(ActivityNotFoundException e) {
                        Toast t = Toast.makeText(ctx
                                , "No installed app", Toast.LENGTH_LONG);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                    }
                    else {
                    Toast t = Toast.makeText(ctx
                            , "No installed app", Toast.LENGTH_LONG);
                    t.setGravity(Gravity.CENTER, 0, 0);
                    t.show();

                }
            }
        };
        if(sChanged) {
            ViewGroup g = ((ViewGroup)watchView.findViewById(R.id.shortcuts));
            g.removeAllViews();
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(240,240,1);
            lp.setMargins(10,0,10,0);
            for(int i = 0; i < app_shortcuts.length; ++i) {
                if(app_shortcuts[i] == null || app_shortcuts[i].equals("")) continue;
                ImageButton b = new ImageButton(watchView.getContext());
                b.setBackgroundColor(Color.TRANSPARENT);
                b.setLayoutParams(lp);

                try {
                    Drawable icon =
                            ctx.getPackageManager().getApplicationIcon(app_shortcuts[i]);
                    if (icon != null)
                        b.setImageDrawable(icon);
                    else
                        b.setImageResource(R.drawable.ic_app_black_24dp);
                } catch (PackageManager.NameNotFoundException e) {
                    b.setImageResource(R.drawable.ic_app_black_24dp);
                }
                b.setScaleType(ImageButton.ScaleType.FIT_CENTER);
                b.setTag(app_shortcuts[i]);
                b.setOnClickListener(callApp);
                g.addView(b);
                Log.d("/KE/short/view/", app_shortcuts[i]);
            }
        }

        final View.OnClickListener setAlarm = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ww.setVisibility(View.GONE);
                ctx.startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS));
            }
        };

        ((ImageButton)watchView.findViewById(R.id.alarmButton)).setOnClickListener(setAlarm);
        ((TextView)watchView.findViewById(R.id.alarm)).setOnClickListener(setAlarm);
        ((TextView)watchView.findViewById(R.id.alarm2)).setOnClickListener(setAlarm);
        ((TextView)watchView.findViewById(R.id.clock)).setOnClickListener(setAlarm);
        ((TextView)watchView.findViewById(R.id.clock)).setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ww.setVisibility(View.GONE);
                        try {
                            String app = PreferenceManager.getDefaultSharedPreferences(ctx)
                                    .getString("apps_timer", "");
                            Intent i = app.equals("") ? null :
                                    ctx.getPackageManager()
                                            .getLaunchIntentForPackage(app);
                            if(i == null)
                                i =  new Intent(AlarmClock.ACTION_SHOW_TIMERS);
                            ctx.startActivity(i);
                        } catch(ActivityNotFoundException e) {
                            Toast t = Toast.makeText(ctx
                                    , "No installed timer app", Toast.LENGTH_LONG);
                            t.setGravity(Gravity.CENTER, 0, 0);
                            t.show();
                        }
                        return true;
                    }
                }
        );

        final View.OnClickListener setCal = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ww.setVisibility(View.GONE);
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, System.currentTimeMillis());
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(builder.build());
                ctx.startActivity(intent);
            }
        };
        ((TextView)watchView.findViewById(R.id.date)).setOnClickListener(setCal);
        ((TextView)watchView.findViewById(R.id.month_year)).setOnClickListener(setCal);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // your code...
        Log.d("/KE/ae",event.toString());
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            int vis = View.GONE;
            boolean sysUi = false;
            AccessibilityNodeInfo node = this.getRootInActiveWindow();
            if(node!=null) {
                    if (node.getPackageName().toString().equals("com.android.systemui")) {
                        AccessibilityWindowInfo wi = node.getWindow();
                        //Log.d("/KE/wc/sys", this.getRootInActiveWindow().toString());
                        //Log.d("/KE/wc/wnd", wi.toString());
                        if(wi != null && wi.getType() == AccessibilityWindowInfo.TYPE_APPLICATION)
                            vis = View.VISIBLE;
                        sysUi = true;
                    }
            }
            Log.d("/KE/sh/", mPrefs.getBoolean("show_home", false)? "SH" : "NSH");
            homeButton.setVisibility(mPrefs.getBoolean("show_home", false) ? vis : View.GONE);
            Log.d("/KE/sh/", mPrefs.getBoolean("show_search", false)? "SS" : "NSS");
            searchButton.setVisibility(mPrefs.getBoolean("show_search", false) ? vis : View.GONE);
            if(sysUi) {
                watchOnButton.setVisibility(View.GONE);
                watchView.setVisibility(View.GONE);
            } else {
                watchOnButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /* for key filtering
    <?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="0"
    android:canRequestFilterKeyEvents="true"
    android:canRetrieveWindowContent="true"
    android:accessibilityFlags="flagRetrieveInteractiveWindows|flagRequestFilterKeyEvents|flagIncludeNotImportantViews"
    android:description="@string/message_accessibility_service_details"
    android:settingsActivity="online.andy7d0.keyremapandgestures.ServiceSettingsActivity"
    />
     */

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        // your code...
        int cc = event.getKeyCode();
        int p;
        int pf = -1;
        for(p = 0; p < keycodes.length; ++p) {
            if(keycodes[p] == cc)
                pf = p;
        }
        Log.d("/KE/kk/in", String.valueOf(cc));
        Log.d("/KE/kk/pf", String.valueOf(pf));
        if(false && pf >= 0) {
            String kp = keypfx[pf];
            boolean has_op =
                    !mPrefs.getString(kp+"_action","Default")
                        .equals("Default")
                    ||
                    !mPrefs.getString(kp+"_action2","Default")
                            .equals("Default")
                    ||
                    !mPrefs.getString(kp+"_action_l","Default")
                            .equals("Default")
                    ;
            Log.d("/KE/k/",kp);
            Log.d("/KE/k/a",mPrefs.getString(kp+"_action","Default"));
            Log.d("/KE/k/a2",mPrefs.getString(kp+"_action2","Default"));
            Log.d("/KE/k/al",mPrefs.getString(kp+"_action_l","Default"));

            boolean canPreventDefault =
                    pf != 1 && pf != 2;

            if(has_op) {
                int tmo = Integer.parseInt(mPrefs.getString("double_click_time", "200"));
                long et = event.getEventTime();
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    down_time2[pf] = down_time[pf];
                    down_time[pf] = et;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (down_time[pf] - down_time2[pf] < tmo
                            && et - down_time[pf] < tmo) {
                        // double click
                        Log.d("/KE/k/op","DCL");
                        if(DoAction(kp, "_action2") && canPreventDefault) return true;
                    } else if (et - down_time[pf] >= tmo) {
                        // long press
                        Log.d("/KE/k/op","LCL");
                        if(DoAction(kp, "_action_l") && canPreventDefault) return true;
                    } else {
                        // click
                        Log.d("/KE/k/op","CL");
                        if(DoAction(kp, "_action") && canPreventDefault) return true;
                    }
                }
            }
        }
        return super.onKeyEvent(event);
    }

    public void DoHome() {
        performGlobalAction(AccService.GLOBAL_ACTION_HOME);
    }
    public void DoBack() {
        performGlobalAction(AccService.GLOBAL_ACTION_BACK);
    }
    public void DoRecents() {
        performGlobalAction(AccService.GLOBAL_ACTION_RECENTS);
    }
    public  void DoNotifiations() {
        performGlobalAction(AccService.GLOBAL_ACTION_NOTIFICATIONS);
    }
    public void DoLAS() {
        //TODO: implement last app switch
    }
    public void  DoVibrateMode() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }
    public void  DoSilentMode() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }
    public void  DoNormalMode() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }

    public void DoAction(String a) {
        switch(a) {
        case "Default": break;
            case "Do nothing": break;
            case "Ringer: normal": DoNormalMode(); break;
            case "Ringer: vibrate": DoVibrateMode(); break;
            case "Ringer: silent": DoSilentMode(); break;
            case "Go back": DoBack(); break;
            case "Show home screen": DoHome(); break;
            case "Show recent apps": DoRecents(); break;
            case "Show notifications": DoNotifiations(); break;
            case "Previous app": DoLAS(); break;
        }
    }
    public boolean DoAction(String kp, String a) {
        String d = mPrefs.getString(kp+a,"Default");
        if(d.equals("Default")) return false;
        Log.d("/KE/do/",kp);
        Log.d("/KE/do/",d);
        DoAction(d);
        return true;
    }


    @Override
    public void onInterrupt() {
    }

}
