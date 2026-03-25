package com.example.rafacartes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity {

    private static final int REQ_IMPORT = 100;
    private static final int REQ_EXPORT = 101;

    private DataManager dataManager;
    private List<Card> currentCards = new ArrayList<>();
    private CardAdapter cardAdapter;
    private TextView tvBonjour;
    private TextView tvTrier;
    private LinearLayout btnAjouter;

    // Tri actuel : 0=alpha, 1=date, 2=usage
    private int sortMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dataManager = new DataManager(this);

        tvBonjour = findViewById(R.id.tv_bonjour);
        tvTrier = findViewById(R.id.tv_trier);
        RecyclerView recyclerView = findViewById(R.id.rv_cards);
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabAdd = findViewById(R.id.fab_add_card);
        ImageButton btnMenu = findViewById(R.id.btn_menu);

        // Grid 2 colonnes
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        cardAdapter = new CardAdapter(this, currentCards, card -> openCard(card));
        recyclerView.setAdapter(cardAdapter);

        // Vérifier si profil existant
        List<Profile> profiles = dataManager.getProfiles();
        if (profiles.isEmpty()) {
            showCreateProfileDialog(true);
        } else {
            if (dataManager.getActiveProfileId() == null) {
                dataManager.setActiveProfileId(profiles.get(0).getId());
            }
            refreshUI();
        }

        // Bouton menu hamburger
        btnMenu.setOnClickListener(v -> showMenuPopup(v));

        // Bouton ajouter carte
        fabAdd.setOnClickListener(v -> showAddCardDialog());

        // Bouton trier
        tvTrier.setOnClickListener(v -> showSortDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        Profile active = dataManager.getActiveProfile();
        if (active != null) {
            tvBonjour.setText("Salut " + active.getName() + " !");
            currentCards.clear();
            currentCards.addAll(getSortedCards(dataManager.getCards(active.getId())));
            cardAdapter.notifyDataSetChanged();
        }
    }

    private List<Card> getSortedCards(List<Card> cards) {
        List<Card> sorted = new ArrayList<>(cards);
        switch (sortMode) {
            case 0: // Alphabétique
                sorted.sort(Comparator.comparing(c -> c.getEnseigneName().toLowerCase()));
                break;
            case 1: // Date d'ajout
                sorted.sort((a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
                break;
            case 2: // Fréquence
                sorted.sort((a, b) -> Integer.compare(b.getUsageCount(), a.getUsageCount()));
                break;
        }
        return sorted;
    }

    private void openCard(Card card) {
        // Incrémenter l'usage
        card.incrementUsage();
        dataManager.updateCard(dataManager.getActiveProfileId(), card);

        Intent intent = new Intent(this, CarteActivity.class);
        intent.putExtra("card_id", card.getId());
        intent.putExtra("profile_id", dataManager.getActiveProfileId());
        startActivity(intent);
    }

    // ─── POPUP MENU ────────────────────────────────────────────────────────────

    private void showMenuPopup(View anchor) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_menu, null);
        PopupWindow popup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popup.setElevation(8f);

        popupView.findViewById(R.id.menu_import).setOnClickListener(v -> {
            popup.dismiss();
            openFilePicker();
        });
        popupView.findViewById(R.id.menu_export).setOnClickListener(v -> {
            popup.dismiss();
            exportCards();
        });
        popupView.findViewById(R.id.menu_change_profile).setOnClickListener(v -> {
            popup.dismiss();
            showChangeProfileDialog();
        });

        popup.showAsDropDown(anchor, 0, 0);
    }

    // ─── SORT DIALOG ───────────────────────────────────────────────────────────

    private void showSortDialog() {
        String[] options = {"Alphabétique", "Date d'ajout", "Les + utilisées"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Trier les cartes")
                .setSingleChoiceItems(options, sortMode, (dialog, which) -> {
                    sortMode = which;
                    dialog.dismiss();
                    refreshUI();
                })
                .show();
    }

    // ─── ADD CARD DIALOG ───────────────────────────────────────────────────────

    private void showAddCardDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null);
        EditText etName = view.findViewById(R.id.et_enseigne);
        EditText etBarcode = view.findViewById(R.id.et_barcode);
        EditText etDesc = view.findViewById(R.id.et_description);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Ajouter une carte")
                .setView(view)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String barcode = etBarcode.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (name.isEmpty() || barcode.isEmpty()) {
                        Toast.makeText(this, "Nom et code-barres requis", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Card card = new Card(null, name, barcode, "", desc);
                    dataManager.addCard(dataManager.getActiveProfileId(), card);
                    refreshUI();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // ─── PROFILE DIALOGS ───────────────────────────────────────────────────────

    private void showChangeProfileDialog() {
        List<Profile> profiles = dataManager.getProfiles();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_profile, null);
        LinearLayout profilesContainer = view.findViewById(R.id.profiles_container);

        for (Profile p : profiles) {
            View item = LayoutInflater.from(this).inflate(R.layout.item_profile_choice, null);
            View colorCircle = item.findViewById(R.id.view_color_circle);
            TextView tvName = item.findViewById(R.id.tv_profile_name);

            colorCircle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(p.getColor()));
            tvName.setText(p.getName());

            // Cocher le profil actif
            if (p.getId().equals(dataManager.getActiveProfileId())) {
                item.setBackgroundResource(R.drawable.bg_selected_profile);
            }

            item.setOnClickListener(v -> {
                dataManager.setActiveProfileId(p.getId());
                refreshUI();
            });

            profilesContainer.addView(item);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Changer de profil")
                .setView(view)
                .setNeutralButton("✏ Modifier les profils", null)
                .setPositiveButton("OK", (d, w) -> refreshUI())
                .show();


        LinearLayout btnAjouter = view.findViewById(R.id.btn_add_profile);

        btnAjouter.setOnClickListener(view1 -> {
            dialog.dismiss();
            showCreateProfileDialog(false);
        });


        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            dialog.dismiss();
            showEditProfilesDialog();
        });
    }

    private void showCreateProfileDialog(boolean isFirst) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_create_profile, null);
        EditText etName = view.findViewById(R.id.et_profile_name);

        // Palette de couleurs
        int[] colors = {
                Color.parseColor("#E53935"), // Rouge
                Color.parseColor("#8E24AA"), // Violet
                Color.parseColor("#1E88E5"), // Bleu
                Color.parseColor("#00897B"), // Vert
                Color.parseColor("#F4511E"), // Orange
                Color.parseColor("#3949AB"), // Indigo
                Color.parseColor("#00ACC1"), // Cyan
                Color.parseColor("#7CB342"), // Vert clair
                Color.parseColor("#FFB300"), // Jaune
                Color.parseColor("#757575"), // Gris
        };

        final int[] selectedColor = {colors[0]};
        LinearLayout colorContainer = view.findViewById(R.id.color_container);

        List<View> colorViews = new ArrayList<>();
        for (int c : colors) {
            View circle = new View(this);
            int size = dpToPx(40);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            circle.setLayoutParams(lp);
            circle.setBackgroundResource(R.drawable.circle_color);
            circle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(c));

            final int thisColor = c;
            circle.setOnClickListener(v -> {
                selectedColor[0] = thisColor;
                // Mettre à jour les bordures
                for (View cv : colorViews) {
                    cv.setAlpha(0.5f);
                    cv.setScaleX(1f);
                    cv.setScaleY(1f);
                }
                circle.setAlpha(1f);
                circle.setScaleX(1.2f);
                circle.setScaleY(1.2f);
            });

            colorViews.add(circle);
            colorContainer.addView(circle);
        }
        colorViews.get(0).setScaleX(1.2f);
        colorViews.get(0).setScaleY(1.2f);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("Créer un profil")
                .setView(view)
                .setPositiveButton("Créer", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Entrez un nom", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Profile> existing = dataManager.getProfiles();
                    if (existing.size() >= 3) {
                        Toast.makeText(this, "Maximum 3 profils", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Profile p = new Profile(dataManager.generateProfileId(), name, selectedColor[0]);
                    dataManager.addProfile(p);
                    dataManager.setActiveProfileId(p.getId());
                    refreshUI();
                });

        if (!isFirst) {
            builder.setNegativeButton("Annuler", null);
        } else {
            builder.setCancelable(false);
        }

        builder.show();
    }

    private void showEditProfilesDialog() {
        List<Profile> profiles = dataManager.getProfiles();
        String[] names = profiles.stream().map(Profile::getName).toArray(String[]::new);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Modifier les profils")
                .setItems(names, (dialog, which) -> {
                    Profile selected = profiles.get(which);
                    showEditSingleProfileDialog(selected);
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void showEditSingleProfileDialog(Profile profile) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = view.findViewById(R.id.et_profile_name_edit);
        etName.setText(profile.getName());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Modifier " + profile.getName())
                .setView(view)
                .setPositiveButton("Sauvegarder", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        profile.setName(newName);
                        dataManager.updateProfile(profile);
                        refreshUI();
                    }
                })
                .setNeutralButton("Supprimer", (dialog, which) -> {
                    if (dataManager.getProfiles().size() <= 1) {
                        Toast.makeText(this, "Impossible de supprimer le dernier profil", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Supprimer le profil ?")
                            .setMessage("Toutes les cartes de ce profil seront supprimées.")
                            .setPositiveButton("Supprimer", (d2, w2) -> {
                                dataManager.deleteProfile(profile.getId());
                                refreshUI();
                            })
                            .setNegativeButton("Annuler", null)
                            .show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // ─── IMPORT / EXPORT ───────────────────────────────────────────────────────

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        startActivityForResult(intent, REQ_IMPORT);
    }

    private void exportCards() {
        String profileId = dataManager.getActiveProfileId();
        if (profileId == null) return;

        String json = dataManager.exportToJson(profileId);
        if (json == null) {
            Toast.makeText(this, "Erreur lors de l'export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String filename = "fidelity_cards_export.json";
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(json.getBytes());
            fos.close();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    new java.io.File(getFilesDir(), filename)
            );
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Exporter les cartes"));
        } catch (Exception e) {
            Toast.makeText(this, "Erreur export: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMPORT && resultCode == Activity.RESULT_OK && data != null) {
            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String profileId = dataManager.getActiveProfileId();
                DataManager.ImportResult result = dataManager.importFromJson(profileId, sb.toString());

                if (result == null) {
                    Toast.makeText(this, "Fichier JSON invalide", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Toasts pour les doublons refusés
                for (String enseigne : result.duplicates) {
                    Toast.makeText(this,
                            "La carte " + enseigne + " n'a pas été ajoutée (doublon)",
                            Toast.LENGTH_SHORT).show();
                }

                // Toast de confirmation
                if (result.added > 0) {
                    Toast.makeText(this,
                            result.added + " carte(s) importée(s) avec succès !",
                            Toast.LENGTH_SHORT).show();
                } else if (result.duplicates.isEmpty()) {
                    Toast.makeText(this, "Aucune carte à importer", Toast.LENGTH_SHORT).show();
                }

                refreshUI();

            } catch (Exception e) {
                Toast.makeText(this, "Erreur import: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}