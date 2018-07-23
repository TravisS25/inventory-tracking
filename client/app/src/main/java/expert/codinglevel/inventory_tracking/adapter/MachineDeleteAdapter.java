//package expert.codinglevel.hospital_inventory.adapter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
//import MachineDeleteActivity;
//import MachineEditActivity;
//import expert.codinglevel.hospital_inventory.R;
//import ActivityType;
//import Machine;
//
//
//public class MachineDeleteAdapter extends BaseAdapter {
//    public final static String TAG = MachineEditAdapter.class.getSimpleName();
//    private LayoutInflater mInflater;
//    private ArrayList<Machine.MachineProperty> mDataSource;
//
//    public MachineDeleteAdapter(Context context, ArrayList<Machine.MachineProperty> items){
//        mDataSource = items;
//        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    }
//
//    @Override
//    public int getCount() {
//        return mDataSource.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return mDataSource.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(final int position, View convertView, final ViewGroup parent) {
//        ViewHolder holder;
//        Machine.MachineProperty machineProperty = (Machine.MachineProperty) getItem(position);
//
//        // check if the view already exists if so, no need to inflate and findViewById again!
//        if (convertView == null) {
//            // Inflate the custom row layout from your XML.
//            convertView = mInflater.inflate(R.layout.machine_detail_item, parent, false);
//
//            // create a new "Holder" with subviews
//            holder = new ViewHolder();
//            holder.mPropertyTextView = convertView.findViewById(R.id.property_text);
//            holder.mPropertyValueView = convertView.findViewById(R.id.property_value);
//            convertView.setTag(holder);
//        }
//        else {
//            // skip all the expensive inflation/findViewById and just get the holder you already made
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        // Update row view's textviews to display machine information
//        holder.mPropertyTextView.setText(machineProperty.getPropertyText());
//        holder.mPropertyValueView.setText(machineProperty.getPropertyValue());
//
//        return convertView;
//    }
//
//    private static class ViewHolder{
//        private TextView mPropertyTextView;
//        private TextView mPropertyValueView;
//    }
//}
