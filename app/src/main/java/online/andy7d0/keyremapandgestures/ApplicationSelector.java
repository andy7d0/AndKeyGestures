package online.andy7d0.keyremapandgestures;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import online.andy7d0.keyremapandgestures.R;

public class ApplicationSelector extends ListPreference {
    private Context contexte;
    ArrayList<ApplicationInfo> ret;

    public ApplicationSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        contexte=context;

        ret = new ArrayList<ApplicationInfo>();

        ApplicationInfo dummy = new ApplicationInfo();
        dummy.packageName = "";

        ret.add(dummy);

        List<ApplicationInfo> pkg = this.getInstalledApplication(contexte);
        for(ApplicationInfo ai : pkg) {
            if(
                    (ai.flags & ApplicationInfo.FLAG_INSTALLED) !=0
                            &&
                            ai.enabled
                            &&
                            contexte.getPackageManager()
                                    .getLaunchIntentForPackage(ai.packageName)!=null
                    ) {
                ret.add(ai);
            }
        }

        String[] rets = new String[ret.size()];
        String[] retv = new String[ret.size()];
        for(int i = 0; i < ret.size(); ++i) {
            rets[i] = ret.get(i).packageName;
            retv[i] = i == 0 ? "---" : ret.get(i).loadLabel(contexte.getPackageManager()).toString();
        }

        setEntryValues(rets);
        setEntries(retv);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        //int index = findIndexOfValue(getSharedPreferences().getString(
        //        getKey(), "1"));

        AppliAdaptateur adapter = new AppliAdaptateur(contexte,
                ret , contexte.getPackageManager());

        builder.setAdapter(adapter, this);
        super.onPrepareDialogBuilder(builder);
    }


    public static List<ApplicationInfo> getInstalledApplication(Context c) {
        return c.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public class AppliAdaptateur extends BaseAdapter {
        private Context mContext;
        private List mListAppInfo;
        private PackageManager mPackManager;

        public AppliAdaptateur(Context c, List list, PackageManager pm) {
            mContext = c;
            mListAppInfo = list;
            mPackManager = pm;
        }

        @Override
        public int getCount() {
            return mListAppInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return mListAppInfo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // get the selected entry
            ApplicationInfo entry = (ApplicationInfo) mListAppInfo.get(position);

            // reference to convertView
            View v = convertView;

            // inflate new layout if null
            if (v == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                v = inflater.inflate(R.layout.appselect, null);
            }

            // load controls from layout resources
            ImageView ivAppIcon = (ImageView) v.findViewById(R.id.ivIcon);
            TextView tvAppName = (TextView) v.findViewById(R.id.tvName);
            TextView tvPkgName = (TextView) v.findViewById(R.id.tvPack);

            // set data to display
            if(entry.packageName.equals(""))
                ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            else
                ivAppIcon.setImageDrawable(entry.loadIcon(mPackManager));
            tvAppName.setText(
                    entry.packageName.equals("") ? ""
                    :        entry.loadLabel(mPackManager));
            tvPkgName.setText(entry.packageName);

            // return view
            return v;
        }
    }


}
