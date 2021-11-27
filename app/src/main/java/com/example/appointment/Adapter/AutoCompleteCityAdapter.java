package com.example.appointment.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appointment.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteCityAdapter extends ArrayAdapter<String> {

    private List<String>citylistFull;

    public AutoCompleteCityAdapter(@NonNull Context context, @NonNull List<String> cityList) {
        super(context, 0, cityList);
        citylistFull = new ArrayList<>(cityList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return cityFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.city_autocomplete_row,parent,false);
        }

        TextView textViewName = convertView.findViewById(R.id.city_autocomplete_tv);

        String cityName = getItem(position);

        if(cityName != null)textViewName.setText(cityName);

        return convertView;
    }

    // FIXME: this shit is driving me crazy ffs 10/10/2020
    @SuppressWarnings(value = "unchecked")
    private Filter cityFilter = new Filter()
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<String>suggestions = new ArrayList<>();
            if(constraint == null || constraint.length() == 0)
            {
                suggestions.addAll(citylistFull);
            }else
            {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(String city : citylistFull)
                {
                    if(city.toLowerCase().contains(filterPattern))
                    {
                        suggestions.add(city);
                    }
                }
            }
            filterResults.count = suggestions.size();
            filterResults.values = suggestions;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if(results!= null || results.count>0 && !constraint.toString().equals("")) {
                clear();
                addAll((List) results.values);
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }

        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return super.convertResultToString(resultValue);
        }
    };

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
