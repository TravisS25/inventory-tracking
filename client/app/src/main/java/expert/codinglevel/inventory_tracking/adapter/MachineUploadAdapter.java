package expert.codinglevel.inventory_tracking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.model.Machine;

public class MachineUploadAdapter extends BaseAdapter {
    public static final String TAG = MachineListAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private ArrayList<Machine> mDataSource;

    public MachineUploadAdapter(Context context, ArrayList<Machine> items){
        mDataSource = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            convertView = mInflater.inflate(R.layout.machine_detail_item, parent, false);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.propertyTextView = convertView.findViewById(R.id.property_text);
            holder.propertyValueView = convertView.findViewById(R.id.property_value);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        }
        else {

            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Update row view's textviews to display machine information
        holder.propertyTextView.setText("Machine");
        holder.propertyValueView.setText(machine.getMachineName().getText());

        return convertView;
    }

    private static class ViewHolder {
        private TextView propertyTextView;
        private TextView propertyValueView;
    }
}
