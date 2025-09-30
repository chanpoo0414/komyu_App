package com.example.komyuapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.komyuapplication.R;

public class TransactionDetailsFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DATETIME = "arg_datetime";
    private static final String ARG_AMOUNT = "arg_amount";
    private static final String ARG_COUNTER = "arg_counter";
    private static final String ARG_TXN_NUMBER = "arg_txn_number";
    private static final String ARG_ICON = "arg_icon";

    public static TransactionDetailsFragment newInstance(
            String title, String datetime, String amount,
            String counter, String txnNumber, int iconRes) {

        TransactionDetailsFragment frag = new TransactionDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DATETIME, datetime);
        args.putString(ARG_AMOUNT, amount);
        args.putString(ARG_COUNTER, counter);
        args.putString(ARG_TXN_NUMBER, txnNumber);
        args.putInt(ARG_ICON, iconRes);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_transaction_details, container, false);

        ImageView btnBack = v.findViewById(R.id.btn_back_transactionDetails);
        ImageView icon = v.findViewById(R.id.icon_transactionDetails);
        TextView title = v.findViewById(R.id.tv_transactionDetails_mainTitle);
        TextView datetime = v.findViewById(R.id.tv_transactionDetails_datetime);
        TextView amount = v.findViewById(R.id.tv_transactionDetails_amount);
        TextView counter = v.findViewById(R.id.tv_transactionDetails_counter);
        TextView txnNumber = v.findViewById(R.id.tv_transactionDetails_number);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString(ARG_TITLE));
            datetime.setText(args.getString(ARG_DATETIME));
            amount.setText(args.getString(ARG_AMOUNT));
            counter.setText(args.getString(ARG_COUNTER));
            txnNumber.setText("Transaction Number: " + args.getString(ARG_TXN_NUMBER));
            int iconRes = args.getInt(ARG_ICON, 0);
            if (iconRes != 0) icon.setImageResource(iconRes);
        }

        btnBack.setOnClickListener(vw -> requireActivity().getSupportFragmentManager().popBackStack());

        return v;
    }
}
