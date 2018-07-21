package expert.codinglevel.hospital_inventory.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import expert.codinglevel.hospital_inventory.MachineDeleteActivity;
import expert.codinglevel.hospital_inventory.MachineEditActivity;
import expert.codinglevel.hospital_inventory.R;
import expert.codinglevel.hospital_inventory.enums.ActivityType;
import expert.codinglevel.hospital_inventory.model.Machine;

public class MachineListAdapter extends BaseAdapter {
    public static final String TAG = MachineListAdapter.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Machine> mDataSource;

    public MachineListAdapter(Context context, ArrayList<Machine> items){
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        Machine machine = (Machine) getItem(position);

        // check if the view already exists if so, no need to inflate and findViewById again!
        if (convertView == null) {
            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.machine_list_item, parent, false);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.mMachineNameTextView = convertView.findViewById(R.id.machine_name);
            holder.mEditMachineImageView = convertView.findViewById(R.id.edit_machine);
            holder.mEditMachineImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Machine machine = mDataSource.get(position);
                    Intent intent = new Intent(mContext, MachineEditActivity.class);
                    Log.i(TAG, "+++ onclick edit  +++" + machine);
                    intent.putExtra("machine", machine);
//                    intent.putExtra("activityType", ActivityType.EDIT);
                    mContext.startActivity(intent);
                }
            });
            holder.mDeleteMachineImageView = convertView.findViewById(R.id.delete_machine);
            holder.mDeleteMachineImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Machine machine = mDataSource.get(position);
                    Intent intent = new Intent(mContext, MachineDeleteActivity.class);
                    intent.putExtra("machine", machine);
                    mContext.startActivity(intent);
                }
            });

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        }
        else {
            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Update row view's textviews to display machine information
        holder.mMachineNameTextView.setText(machine.getMachineName().getText());

        return convertView;
    }

    private static class ViewHolder {
        private TextView mMachineNameTextView;
        private ImageView mEditMachineImageView;
        private ImageView mDeleteMachineImageView;
    }
}
