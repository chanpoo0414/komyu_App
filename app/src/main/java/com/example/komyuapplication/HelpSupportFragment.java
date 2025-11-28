package com.example.komyuapplication;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HelpSupportFragment extends Fragment {

    private EditText etSearchHelp;
    private LinearLayout searchContainer;


//    ill edit this
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_support, container, false);

        // Get the back button and set click listener
        ImageView btnBack = view.findViewById(R.id.btn_back_help_support);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to previous fragment
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // Initialize views
        etSearchHelp = view.findViewById(R.id.et_search_help);
        searchContainer = view.findViewById(R.id.search_container);

        // Setup search functionality
        setupSearchFunctionality();

        // Setup FAQ dropdown functionality
        setupFAQDropdowns(view);

        return view;
    }

    private void setupSearchFunctionality() {
        // Set up editor action listener for search
        etSearchHelp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchQuery = etSearchHelp.getText().toString().trim();
                    performSearch(searchQuery);
                    // Hide keyboard after search
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        // Add click listener to the search container to focus on EditText
        if (searchContainer != null) {
            searchContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    etSearchHelp.requestFocus();
                    showKeyboard();
                }
            });
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null && etSearchHelp != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearchHelp.getWindowToken(), 0);
            }
        }
    }

    private void showKeyboard() {
        if (getActivity() != null && etSearchHelp != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearchHelp, 0);
            }
        }
    }

    private void performSearch(String query) {
        if (!query.isEmpty()) {
            // Implement your search logic here
            // For example, filter FAQs based on the search query
            Toast.makeText(getContext(), "Searching for: " + query, Toast.LENGTH_SHORT).show();

            // You can add logic to filter and show/hide FAQ items based on the search
            filterFAQs(query);
        } else {
            Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterFAQs(String query) {
        // Implement FAQ filtering logic based on the search query
        // This would involve showing/hiding FAQ items that match the search
        // For now, we'll just show a toast
        Toast.makeText(getContext(), "Filtering FAQs for: " + query, Toast.LENGTH_SHORT).show();

        // Example of how you might implement filtering:
        // You would need to get references to all FAQ content and check if they contain the search term
        // Then show/hide the corresponding FAQ sections
    }

    private void setupFAQDropdowns(View view) {
        setupFAQDropdown(view, R.id.faq1_header, R.id.faq1_content, R.id.faq1_arrow);
        setupFAQDropdown(view, R.id.faq2_header, R.id.faq2_content, R.id.faq2_arrow);
        setupFAQDropdown(view, R.id.faq3_header, R.id.faq3_content, R.id.faq3_arrow);
        setupFAQDropdown(view, R.id.faq4_header, R.id.faq4_content, R.id.faq4_arrow);
        setupFAQDropdown(view, R.id.faq5_header, R.id.faq5_content, R.id.faq5_arrow);
        setupFAQDropdown(view, R.id.faq6_header, R.id.faq6_content, R.id.faq6_arrow);
        setupFAQDropdown(view, R.id.faq7_header, R.id.faq7_content, R.id.faq7_arrow);
        setupFAQDropdown(view, R.id.faq8_header, R.id.faq8_content, R.id.faq8_arrow);
        setupFAQDropdown(view, R.id.faq9_header, R.id.faq9_content, R.id.faq9_arrow);
    }

    private void setupFAQDropdown(View view, int headerId, int contentId, int arrowId) {
        View header = view.findViewById(headerId);
        final View content = view.findViewById(contentId);
        final TextView arrow = view.findViewById(arrowId);

        if (header != null && content != null && arrow != null) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (content.getVisibility() == View.VISIBLE) {
                        content.setVisibility(View.GONE);
                        arrow.setText("▼");
                    } else {
                        content.setVisibility(View.VISIBLE);
                        arrow.setText("▲");
                    }
                }
            });
        }
    }
}