package com.example.hci_test.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hci_test.R;
import com.example.hci_test.model.Collection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionChoiceAdapter extends BaseAdapter {
    private final Context context;
    private final List<Collection> collections;
    private final Set<String> selectedNames = new HashSet<>();

    public CollectionChoiceAdapter(Context context, List<Collection> collections) {
        this.context = context;
        this.collections = collections;
    }

    @Override
    public int getCount() {
        return collections.size();
    }

    @Override
    public Object getItem(int position) {
        return collections.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_collection_choice, parent, false);

        Collection collection = collections.get(position);

        ImageView imageView = convertView.findViewById(R.id.imageViewThumbnail);
        TextView textView = convertView.findViewById(R.id.textViewName);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        textView.setText(collection.getName());

        checkBox.setOnCheckedChangeListener(null); // prevent callback triggers on recycling
        checkBox.setChecked(selectedNames.contains(collection.getName()));

        // ✅ Keep track when the checkbox is toggled
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedNames.add(collection.getName());
            } else {
                selectedNames.remove(collection.getName());
            }
        });

        // ✅ Also toggle checkbox when the row is clicked
        convertView.setOnClickListener(v -> {
            boolean newState = !checkBox.isChecked();
            checkBox.setChecked(newState); // triggers listener above
        });

        String thumbnail = collection.getThumbnailUrl();
        if ("placeholder".equals(thumbnail)) {
            imageView.setImageResource(R.drawable.placeholder_collection);
        } else {
            Glide.with(context)
                    .load(thumbnail)
                    .placeholder(R.drawable.placeholder_collection)
                    .into(imageView);
        }

        return convertView;
    }

    public Set<String> getSelectedNames() {
        return selectedNames;
    }
}




