package com.example.komyuapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ValidIdFragment extends Fragment {

    private AutoCompleteTextView dropdownValidID;
    private ImageButton frontImageBtn;
    private ImageButton backImageBtn;

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
        frontImageBtn = view.findViewById(R.id.frontImagebtn);
        backImageBtn = view.findViewById(R.id.backImagebtn);

        // hidden initially
        frontImageBtn.setVisibility(View.GONE);
        backImageBtn.setVisibility(View.GONE);

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

            // show image buttons when something is selected
            boolean show = selected != null && !selected.isEmpty();
            setUploadButtonsVisible(show);
        });

        // click listeners for image buttons
        frontImageBtn.setOnClickListener(v -> {
            if (getActivity() instanceof FinalSignupPage4) {
                ((FinalSignupPage4) getActivity()).openImagePicker("front");
            }
        });

        backImageBtn.setOnClickListener(v -> {
            if (getActivity() instanceof FinalSignupPage4) {
                ((FinalSignupPage4) getActivity()).openImagePicker("back");
            }
        });

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setUploadButtonsVisible(boolean visible) {
        if (frontImageBtn != null) frontImageBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (backImageBtn != null) backImageBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    // --- new methods to update previews after selection ---
    public void updateFrontImage(Uri imageUri) {
        if (frontImageBtn != null && imageUri != null) {
            frontImageBtn.setImageURI(imageUri);
        }
    }

    public void updateBackImage(Uri imageUri) {
        if (backImageBtn != null && imageUri != null) {
            backImageBtn.setImageURI(imageUri);
        }
    }
}
