package com.example.komyuapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ValidIdFragment extends Fragment {

    private AutoCompleteTextView dropdownValidID;
    private MaterialButton btnUploadFront;
    private MaterialButton btnUploadBack;

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
        View view = inflater.inflate(R.layout.fragment_valid_id, container, false);

        dropdownValidID = view.findViewById(R.id.dropdownValidID);
        btnUploadFront = view.findViewById(R.id.btnUploadFront);
        btnUploadBack = view.findViewById(R.id.btnUploadBack);

        // hidden initially
        btnUploadFront.setVisibility(View.GONE);
        btnUploadBack.setVisibility(View.GONE);

        // load choices
        String[] validIds = getResources().getStringArray(R.array.valid_ids);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                validIds
        );
        dropdownValidID.setAdapter(adapter);

        dropdownValidID.setOnClickListener(v -> dropdownValidID.showDropDown());

        dropdownValidID.setOnItemClickListener((parent, itemView, position, id) -> {
            String selected = dropdownValidID.getText().toString().trim();
            if (listener != null) listener.onValidIdSelected(selected);

            // show buttons when something is selected
            boolean show = selected != null && !selected.isEmpty();
            setUploadButtonsVisible(show);
        });

        // ✅ Handle upload button clicks (mock for now)
        btnUploadFront.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Front ID uploaded (mock)", Toast.LENGTH_SHORT).show();
        });

        btnUploadBack.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Back ID uploaded (mock)", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setUploadButtonsVisible(boolean visible) {
        if (btnUploadFront != null) btnUploadFront.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (btnUploadBack != null) btnUploadBack.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
