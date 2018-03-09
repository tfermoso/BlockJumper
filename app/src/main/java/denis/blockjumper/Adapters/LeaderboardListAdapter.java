package denis.blockjumper.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import denis.blockjumper.Firebase.User;
import denis.blockjumper.R;

/**
 * Created by denis.couñago on 06/03/2018.
 */

public class LeaderboardListAdapter extends BaseAdapter {
    private int layoutMolde;
    private List<User> userList;
    private Activity activity;

    public LeaderboardListAdapter(int layoutMolde, List<User> userList, Activity activity) {
        this.layoutMolde = layoutMolde;
        this.userList = userList;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return this.userList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.userList.get(position);
    }

    @Override
    public long getItemId(int position) {
//        return 0;
        return this.userList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String textName = userList.get(position).getName();
        int textPoints = userList.get(position).getPoints();

        View v = convertView;

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(layoutMolde, null);
        }
        TextView name =  v.findViewById(R.id.txt_nameUser);
        TextView points =  v.findViewById(R.id.txt_pointsUser);
        name.setText(textName);
        points.setText(textPoints+"");
        return v;
    }
}
