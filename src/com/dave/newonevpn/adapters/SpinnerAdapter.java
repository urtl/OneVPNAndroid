package com.dave.newonevpn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dave.onevpnfresh.R;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {

	private Context context;
    private List<String> itemList;
    LayoutInflater inflater;
	public SpinnerAdapter(Context context,
                          List<String> objects) {
		super(context, R.layout.spinneritem, objects);
		this.context = context;
		this.itemList = objects;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
 
    // This funtion called for each row ( Called data.size() times )
    public View getCustomView(int position, View convertView, ViewGroup parent) {
 
        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(R.layout.spinneritem, parent, false);

        /***** Get each Model object from Arraylist ********/
        TextView label        = (TextView)row.findViewById(R.id.txtLabel);
        if( itemList != null ){
	        String item = itemList.get(position);
	        if( item != null )
	        	label.setText(item);
        }
        return row;
      }

}
