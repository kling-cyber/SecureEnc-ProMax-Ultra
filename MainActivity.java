package com.secureenc.ultra;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String PASSWORD = "MySuperSecurePasswordUltra";
    private static final int RANDOMIZATION_LAYERS = 10;
    private Uri selectedFileUri;
    private boolean isEncryptMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnEncrypt = findViewById(R.id.btnEncrypt);
        Button btnDecrypt = findViewById(R.id.btnDecrypt);

        btnEncrypt.setOnClickListener(v -> {
            isEncryptMode = true;
            selectFile();
        });

        btnDecrypt.setOnClickListener(v -> {
            isEncryptMode = false;
            selectFile();
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                selectedFileUri = data.getData();
                if (isEncryptMode) {
                    encryptFile();
                } else {
                    decryptFile();
                }
            }
        }
    }

    private void encryptFile() {
        try {
            byte[] fileData = readBytes(selectedFileUri);
            byte[] encryptedBytes = encryptAES(fileData, PASSWORD);
            String base64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
            String randomized = multiRandomizer(base64, PASSWORD, RANDOMIZATION_LAYERS);

            String fileName = getFileName(selectedFileUri) + ".enc";
            saveToFile(randomized.getBytes(), fileName);

            Toast.makeText(this, "Encrypted and saved: " + fileName, Toast.LENGTH_LONG).show();
            deleteOriginalFile(selectedFileUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Encryption failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void decryptFile() {
        try {
            byte[] encryptedFileData = readBytes(selectedFileUri);
            String randomized = new String(encryptedFileData);
            String reversed = reverseRandomizer(randomized, PASSWORD, RANDOMIZATION_LAYERS);
            byte[] decoded = Base64.decode(reversed, Base64.DEFAULT);
            byte[] decryptedBytes = decryptAES(decoded, PASSWORD);

            String fileName = getFileName(selectedFileUri).replace(".enc", "");
            saveToFile(decryptedBytes, fileName);

            Toast.makeText(this, "Decrypted: " + fileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Decryption failed.", Toast.LENGTH_SHORT).show();
        }
    }

    // Randomization (reversible)
    public static String multiRandomizer(String input, String seed, int layers) {
        String result = input;
        for (int i = 0; i < layers; i++) {
            result = shuffle(result, seed + i);
        }
        return result;
    }

    public static String reverseRandomizer(String input, String seed, int layers) {
        String result = input;
        for (int i = layers - 1; i >= 0; i--) {
            result = unshuffle(result, seed + i);
        }
        return result;
    }

    private static String shuffle(String input, String seed) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) indices.add(i);
        Random rand = new Random(seed.hashCode());
        Collections.shuffle(indices, rand);
        char[] shuffled = new char[input.length()];
        for (int i = 0; i < input.length(); i++)
            shuffled[i] = input.charAt(indices.get(i));
        return new String(shuffled);
    }

    private static String unshuffle(String input, String seed) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) indices.add(i);
        Random rand = new Random(seed.hashCode());
        List<Integer> shuffledIndices = new ArrayList<>(indices);
        Collections.shuffle(shuffledIndices, rand);
        char[] unshuffled = new char[input.length()];
        for (int i = 0; i < input.length(); i++)
            unshuffled[shuffledIndices.get(i)] = input.charAt(i);
        return new String(unshuffled);
    }

    private byte[] encryptAES(byte[] data, String password) throws Exception {
        SecretKeySpec keySpec = getKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    private byte[] decryptAES(byte[] data, String password) throws Exception {
        SecretKeySpec keySpec = getKey(password);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    private SecretKeySpec getKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
    }

    private void deleteOriginalFile(Uri uri) {
        try {
            getContentResolver().delete(uri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readBytes(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1)
            byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }

    private void saveToFile(byte[] data, String filename) throws IOException {
        File outputDir = getExternalFilesDir(null);
        File outFile = new File(outputDir, filename);
        FileOutputStream fos = new FileOutputStream(outFile);
        fos.write(data);
        fos.close();
    }

    private String getFileName(Uri uri) {
        String result = "";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        }
        return result;
    }
}
