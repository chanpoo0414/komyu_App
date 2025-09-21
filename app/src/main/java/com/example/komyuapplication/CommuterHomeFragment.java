package com.example.komyuapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CommuterHomeFragment extends Fragment {

    public CommuterHomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_commuter_home, container, false);

        // Recycler view for recent activity
        RecyclerView rv = root.findViewById(R.id.recycler_recent_activity);
        rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        // Build sample data (replace with your real data source)



        List<RecentActivityAdapter.Item> data = new ArrayList<>();
        RecentActivityAdapter.Item it1 = new RecentActivityAdapter.Item();
        it1.title = "Trip to SM City";
        it1.subtitle = "15 Sep 2025 · 11:11 AM";
        it1.amount = "-35.00";
        data.add(it1);

        RecentActivityAdapter.Item it2 = new RecentActivityAdapter.Item();
        it2.title = "Top-up";
        it2.subtitle = "14 Sep 2025 · 09:00 AM";
        it2.amount = "+100.00";
        data.add(it2);

        RecentActivityAdapter adapter = new RecentActivityAdapter(data);
        rv.setAdapter(adapter);

        // Example buttons in the fragment (IDs must match fragment_commuter_home.xml)
        Button scanBtn = root.findViewById(R.id.btn_scan_pay);
        scanBtn.setOnClickListener(v -> {
            // Open scanner fragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ScannerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button terminalsBtn = root.findViewById(R.id.btn_terminals);
        terminalsBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommuterLogsFragment())
                    .addToBackStack(null)
                    .commit();
        });
//        @Override
//        public void onResume() {
//            super.onResume();
//            // Start ZXing scan (for fragment)
//            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//            integrator.setPrompt("Scan commuter QR");
//            integrator.setBeepEnabled(true);
//            integrator.setOrientationLocked(false);
//            integrator.initiateScan();
//        }

        // other buttons (buy load etc.) can be wired similarly
        return root;
    }
}
