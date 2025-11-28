package com.example.komyuapplication;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToast {

    public static void showSuccess(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.ic_success, R.color.greenKomyu);
    }

    public static void showError(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.error_icon, R.color.RedLogout);
    }

    public static void showWarning(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.ic_warning, R.color.orangeKomyu);
    }

    public static void showInfo(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.ic_info, R.color.blueKomyu);
    }

    public static void showLoading(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.ic_loading, R.color.purpleKomyu);
    }

    // Add this method for regular toasts with default styling
    public static void show(Activity activity, String message) {
        showCustomToast(activity, message, R.drawable.ic_loading, R.color.blueKomyu);
    }

    private static void showCustomToast(Activity activity, String message, int iconRes, int colorRes) {
        if (activity == null || activity.isFinishing()) return;

        activity.runOnUiThread(() -> {
            // Inflate custom layout
            LayoutInflater inflater = activity.getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast_layout,
                    activity.findViewById(R.id.custom_toast_container));

            // Set message
            TextView text = layout.findViewById(R.id.custom_toast_text);
            text.setText(message);

            // Set icon - use your app logo for success, or other icons for different states
            ImageView icon = layout.findViewById(R.id.custom_toast_icon);
            icon.setImageResource(iconRes);

            // Set background color
            View container = layout.findViewById(R.id.custom_toast_container);
            container.setBackgroundColor(activity.getResources().getColor(colorRes));

            // Create and show toast
            Toast toast = new Toast(activity);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        });
    }
}