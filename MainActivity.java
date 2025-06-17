package com.example.secureenc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Uri selectedFileUri;
    private Button encryptButton, decryptButton, chooseFileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.bright_yellow));
        setContentView(R.layout.activity_main);

        encryptButton = findViewById(R.id.encryptButton);
        decryptButton = findViewById(R.id.decryptButton);
        chooseFileButton = findViewById(R.id.chooseFileButton);

        chooseFileButton.setOnClickListener(v -> openFileChooser());

        encryptButton.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                // Encrypt logic here
                Toast.makeText(this, "Encrypting...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please choose a file", Toast.LENGTH_SHORT).show();
            }
        });

        decryptButton.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                // Decrypt logic here
                Toast.makeText(this, "Decrypting...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please choose a file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                selectedFileUri = data.getData();
                Toast.makeText(this, "File selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
