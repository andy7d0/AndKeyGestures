package online.andy7d0.keyremapandgestures;

import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class WatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(
                //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                //WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        //);
        Log.d("/KE/svc/wa", "");

        setContentView(R.layout.activity_watch);
        AccService.InitWatchView(getWindow().getDecorView(),
                PreferenceManager.getDefaultSharedPreferences(this));
    }
}
