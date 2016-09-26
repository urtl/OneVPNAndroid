package com.dave.newonevpn.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dave.newonevpn.model.DNSItem;
import com.dave.onevpnfresh.R;

import java.util.List;


public class ChooseServerListAdapter extends ArrayAdapter<DNSItem> {
	  private final Context context;
	  private final List<DNSItem> values;

	  public ChooseServerListAdapter(Context context, List<DNSItem> values) {
	    super(context, -1, values);
	    this.context = context;
	    this.values = values;
	  }


	@Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = null;
	    ViewGroup viewGroup = null;
	    final View view;
	    final ViewHolder holder;
	    if( convertView == null ){
	    	viewGroup = (ViewGroup) LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.list_server_item, null);
	    	holder = new ViewHolder();

			holder.imgCountryFlag = (ImageView)viewGroup.findViewById(R.id.imgCountryFlag);
			holder.txtServerName = (TextView)viewGroup.findViewById(R.id.txtServerName);
			holder.imgSelectedStatus = (ImageView)viewGroup.findViewById(R.id.imgSelectStatus);
			view = viewGroup;
			view.setTag(holder);
			convertView = view;
	    }else{
	    	holder = (ViewHolder) convertView.getTag();
	    	view = convertView;
	    }
		  DNSItem info = values.get(position);
		  holder.txtServerName.setText(info.name);
		  int id = getResourceId(info.country.toLowerCase(), "drawable", context.getPackageName());
			if( id != -1 )
		  		holder.imgCountryFlag.setImageDrawable(context.getResources().getDrawable(id));

		  if( info.bSelected ){
			  holder.imgSelectedStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.png_radio_select));
		  }else{
			  holder.imgSelectedStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.png_radio_unselect));
		  }
		return convertView;
	  }
	  
	  
	  private static class ViewHolder {
		  ImageView imgCountryFlag;
		  TextView txtServerName;
		  ImageView imgSelectedStatus;
	  }
	public int getResourceId(String pVariableName, String pResourcename, String pPackageName)
	{
		try {
			return context.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
} 
