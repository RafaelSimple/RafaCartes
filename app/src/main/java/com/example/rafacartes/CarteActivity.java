package com.example.rafacartes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.example.rafacartes.DataManager;

public class CarteActivity extends AppCompatActivity {

    private DataManager dataManager;
    private Card currentCard;
    private String profileId;
    private ImageView ivBarcode;
    private TextView tvBarcode;
    private LinearLayout llDescription;
    private TextView tvDescription;
    private TextView tvTitle;

    private ActivityResultLauncher<String> imagePickerLauncher;

    // Image choisie temporairement
    private Uri pendingImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carte);

        dataManager = new DataManager(this);

        String cardId = getIntent().getStringExtra("card_id");
        profileId = getIntent().getStringExtra("profile_id");

        // Trouver la carte
        for (Card c : dataManager.getCards(profileId)) {
            if (c.getId().equals(cardId)) {
                currentCard = c;
                break;
            }
        }

        ivBarcode = findViewById(R.id.iv_barcode);
        tvBarcode = findViewById(R.id.tv_barcode);
        llDescription = findViewById(R.id.ll_description);
        tvDescription = findViewById(R.id.tv_description);
        tvTitle = findViewById(R.id.tv_title);
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnSupp = findViewById(R.id.btn_supp);
        ImageButton btnEdit = findViewById(R.id.btn_edit);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnSupp.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_rm_card, null);
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Voulez-vous supprimer la carte ?")
                    .setView(view)
                    .setNegativeButton("Non", null)
                    .setPositiveButton("Oui", (d, w) -> {
                        dataManager.deleteCard(profileId, currentCard.getId());
                        finish();
                    })
                    .show();
        });


        // Launcher pour choisir une image
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pendingImageUri = uri;
                    }
                }
        );

        if (currentCard != null) {
            renderCard();
        } else {
            Toast.makeText(this, "Carte introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void renderCard() {
        tvTitle.setText(currentCard.getEnseigneName());

        String desc = currentCard.getDescription();
        if (desc != null && !desc.isEmpty()) {
            llDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(desc);
        } else {
            llDescription.setVisibility(View.GONE);
        }

        generateBarcode(currentCard.getBarcodeNumber());
        tvBarcode.setText(currentCard.getBarcodeNumber());
    }

    private void generateBarcode(String content) {
        if (content == null || content.isEmpty()) return;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            // On essaie CODE_128, format universel compatible avec tous les codes de fidélité
            BitMatrix bitMatrix = writer.encode(content.replace(" ", ""), BarcodeFormat.CODE_128, 900, 300);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            ivBarcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Impossible de générer le code-barres", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditDialog() {
        pendingImageUri = null;
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_card, null);
        EditText etName = view.findViewById(R.id.et_enseigne_edit);
        EditText etDesc = view.findViewById(R.id.et_description_edit);
        EditText etBarcode = view.findViewById(R.id.et_barcode_edit);
        TextView tvImageStatus = view.findViewById(R.id.tv_image_status);
        View btnChooseImage = view.findViewById(R.id.btn_choose_image);

        etName.setText(currentCard.getEnseigneName());
        etDesc.setText(currentCard.getDescription());
        etBarcode.setText(currentCard.getBarcodeNumber());

        String img = currentCard.getImageFileName();
        tvImageStatus.setText((img != null && !img.isEmpty()) ? img : "Aucune image");

        btnChooseImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        new MaterialAlertDialogBuilder(this)
                .setTitle("Modifier la carte")
                .setView(view)
                .setPositiveButton("Sauvegarder", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newDesc = etDesc.getText().toString().trim();
                    String newBarcode = etBarcode.getText().toString().trim();

                    if (newName.isEmpty() || newBarcode.isEmpty()) {
                        Toast.makeText(this, "Nom et code-barres requis", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentCard.setEnseigneName(newName);
                    currentCard.setDescription(newDesc);
                    currentCard.setBarcodeNumber(newBarcode);

                    // Sauvegarder l'image si sélectionnée
                    if (pendingImageUri != null) {
                        String savedFilename = saveImageFromUri(pendingImageUri, newName);
                        if (savedFilename != null) {
                            currentCard.setImageFileName(savedFilename);
                        }
                    }

                    dataManager.updateCard(profileId, currentCard);
                    renderCard();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private String saveImageFromUri(Uri uri, String enseigneName) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            String filename = enseigneName.toLowerCase().replaceAll("\\s+", "_") + ".png";
            File outFile = new File(getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) fos.write(buffer, 0, len);
            fos.close();
            is.close();
            return filename;
        } catch (IOException e) {
            Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
