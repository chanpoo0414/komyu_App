package com.example.komyuapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpPage4 extends AppCompatActivity implements SignUpPage4Listener {

    // Request codes
    private SignUPData data;
    private static final int REQ_PICK_GALLERY_FRONT = 1001;
    private static final int REQ_PICK_GALLERY_BACK  = 1002;
    private static final int REQ_READ_EXTERNAL = 3001;

    // Cloudinary settings
    private static final String CLOUD_NAME = "dovtg1cls";
    private static final String UPLOAD_PRESET = "my_app_uploads"; // unsigned preset
    private static final String CLOUDINARY_UPLOAD_URL =
            "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload";

    // UI references
    private Button btnSubmit;
    private TextView tvFrontStatus, tvBackStatus;
    private ImageButton btnFrontId, btnBackId;

    // State
    private String frontImageUrl = "";
    private String backImageUrl = "";
    private File frontImageFile = null;
    private File backImageFile = null;
    private String selectedValidId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page4);

        data = (SignUPData) getIntent().getSerializableExtra("data");
        if (data == null) data = new SignUPData();

        // Bind views
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFrontStatus = findViewById(R.id.tvFrontStatus);
        tvBackStatus = findViewById(R.id.tvBackStatus);
        btnFrontId = findViewById(R.id.btnFrontId);
        btnBackId = findViewById(R.id.btnBackId);

        // Initially disable submit
        btnSubmit.setEnabled(false);
        btnSubmit.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.grey)
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_valid_id_container, new ValidIdFragment())
                    .commit();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_categories_container, new CategoriesFragment())
                    .commit();
        }

        // Upload buttons (double as preview)
        btnFrontId.setOnClickListener(v -> {
            if (frontImageUrl == null || frontImageUrl.isEmpty()) {
                openImagePicker("front");
            } else {
                showPreview(true);
            }
        });

        btnBackId.setOnClickListener(v -> {
            if (backImageUrl == null || backImageUrl.isEmpty()) {
                openImagePicker("back");
            } else {
                showPreview(false);
            }
        });

        // Submit handler
        btnSubmit.setOnClickListener(v -> {
            if (frontImageUrl.isEmpty() || backImageUrl.isEmpty()) {
                Toast.makeText(SignUpPage4.this, "Please upload both front and back ID.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add the Page 4 fields into the object
            data.typeOfId = selectedValidId;
            data.frontId  = frontImageUrl;
            data.backId   = backImageUrl;

            // Send to server via AuthRepository
            AuthRepository repo = new AuthRepository();
            repo.register(data, new retrofit2.Callback<APIResponse>() {
                @Override
                public void onResponse(retrofit2.Call<APIResponse> call,
                                       retrofit2.Response<APIResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success == 1) {
                        Toast.makeText(SignUpPage4.this, "Signup successful!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignUpPage4.this, LoginFirstPage.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpPage4.this, "Signup failed: " +
                                        (response.body() != null ? response.body().message : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<APIResponse> call, Throwable t) {
                    Toast.makeText(SignUpPage4.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // Called from ValidIdFragment when user picks an ID type
    public void setSelectedValidId(String idType) {
        selectedValidId = idType;
        validateForm();
    }

    // Open gallery (called from fragment buttons)
    public void openImagePicker(String which) {
        if (hasGalleryPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            int req = "front".equalsIgnoreCase(which) ? REQ_PICK_GALLERY_FRONT : REQ_PICK_GALLERY_BACK;
            startActivityForResult(intent, req);
        } else {
            requestGalleryPermission();
        }
    }

    private boolean hasGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQ_READ_EXTERNAL);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_READ_EXTERNAL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_READ_EXTERNAL) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                Toast.makeText(this, "Permission granted — try again", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gallery permission required to upload ID", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            boolean isFront = requestCode == REQ_PICK_GALLERY_FRONT;

            try {
                // Copy to cache file
                InputStream is = getContentResolver().openInputStream(imageUri);
                File outFile = new File(getCacheDir(), (isFront ? "front_temp.jpg" : "back_temp.jpg"));
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
                }

                // Update state + UI
                if (isFront) {
                    frontImageFile = outFile;
                    tvFrontStatus.setText("Front ID selected — uploading...");
                } else {
                    backImageFile = outFile;
                    tvBackStatus.setText("Back ID selected — uploading...");
                }

                // Upload
                uploadToCloudinary(outFile, isFront);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error reading image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadToCloudinary(File file, boolean isFront) {
        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/*"));
        RequestBody reqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build();

        Request req = new Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL)
                .post(reqBody)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(SignUpPage4.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (isFront) tvFrontStatus.setText("Front upload failed");
                    else tvBackStatus.setText("Back upload failed");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpPage4.this, "Upload error: " + response.code(), Toast.LENGTH_LONG).show();
                        if (isFront) tvFrontStatus.setText("Front upload error");
                        else tvBackStatus.setText("Back upload error");
                    });
                    return;
                }

                try {
                    JSONObject json = new JSONObject(body);
                    final String secureUrl = json.optString("secure_url", json.optString("url", ""));
                    runOnUiThread(() -> {
                        if (isFront) {
                            frontImageUrl = secureUrl;
                            tvFrontStatus.setText("Front uploaded");
                            Glide.with(SignUpPage4.this).load(secureUrl).into(btnFrontId);
                        } else {
                            backImageUrl = secureUrl;
                            tvBackStatus.setText("Back uploaded");
                            Glide.with(SignUpPage4.this).load(secureUrl).into(btnBackId);
                        }
                        validateForm();
                    });
                } catch (Exception ex) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignUpPage4.this, "Parse error", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void validateForm() {
        boolean ready = !frontImageUrl.isEmpty() && !backImageUrl.isEmpty() && !selectedValidId.isEmpty();
        btnSubmit.setEnabled(ready);
        btnSubmit.setBackgroundTintList(
                ContextCompat.getColorStateList(this, ready ? R.color.lightGreenKomyu : R.color.grey)
        );
    }

    // Show preview in dialog
    private void showPreview(boolean isFront) {
        String url = isFront ? frontImageUrl : backImageUrl;
        if (url == null || url.isEmpty()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_preview, null);
        ImageView img = view.findViewById(R.id.imgPreview);
        Glide.with(this).load(url).into(img);
        builder.setView(view);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onValidIdSelected(String idType) {
        setSelectedValidId(idType); // reuse your existing helper
    }

    @Override
    public void onRoleSelected(String role) {
        data.categories = role; // save chosen category into SignUPData
        validateForm();
    }

}
