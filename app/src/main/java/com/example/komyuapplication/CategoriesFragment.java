package com.example.komyuapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CategoriesFragment extends Fragment {

    private AutoCompleteTextView dropdownCategory;
    private SignUpPage4Listener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SignUpPage4Listener) {
            listener = (SignUpPage4Listener) context;
        } else {
            throw new RuntimeException(context + " must implement SignUpPage4Listener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);
        dropdownCategory = view.findViewById(R.id.dropdownCategory);

        String[] categories = getResources().getStringArray(R.array.categories);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        dropdownCategory.setAdapter(adapter);

        dropdownCategory.setOnClickListener(v -> dropdownCategory.showDropDown());

        dropdownCategory.setOnItemClickListener((parent, itemView, position, id) -> {
            String selected = dropdownCategory.getText().toString().trim();
            if (listener != null) listener.onRoleSelected(selected);
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // allow activity to set category programmatically
    public void setCategory(String category) {
        if (dropdownCategory != null) {
            dropdownCategory.setText(category, false);
            if (listener != null) listener.onRoleSelected(category);
        }
    }
}
