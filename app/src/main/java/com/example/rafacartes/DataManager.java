package com.example.rafacartes;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private static final String PREFS_NAME = "fidelity_prefs";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_CARDS = "cards";
    private static final String KEY_ACTIVE_PROFILE = "active_profile_id";

    private final SharedPreferences prefs;

    public DataManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── PROFILES ──────────────────────────────────────────────────────────────

    public List<Profile> getProfiles() {
        List<Profile> profiles = new ArrayList<>();
        try {
            String json = prefs.getString(KEY_PROFILES, "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Profile p = new Profile(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getInt("color")
                );
                profiles.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return profiles;
    }

    public void saveProfiles(List<Profile> profiles) {
        try {
            JSONArray arr = new JSONArray();
            for (Profile p : profiles) {
                JSONObject obj = new JSONObject();
                obj.put("id", p.getId());
                obj.put("name", p.getName());
                obj.put("color", p.getColor());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_PROFILES, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addProfile(Profile profile) {
        List<Profile> profiles = getProfiles();
        profiles.add(profile);
        saveProfiles(profiles);
    }

    public void updateProfile(Profile updated) {
        List<Profile> profiles = getProfiles();
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(updated.getId())) {
                profiles.set(i, updated);
                break;
            }
        }
        saveProfiles(profiles);
    }

    public void deleteProfile(String profileId) {
        List<Profile> profiles = getProfiles();
        profiles.removeIf(p -> p.getId().equals(profileId));
        saveProfiles(profiles);
        // Supprimer aussi les cartes associées
        List<Card> cards = getCards(profileId);
        for (Card c : cards) deleteCard(profileId, c.getId());
        // Si le profil actif est supprimé, changer de profil actif
        if (profileId.equals(getActiveProfileId())) {
            List<Profile> remaining = getProfiles();
            if (!remaining.isEmpty()) setActiveProfileId(remaining.get(0).getId());
            else setActiveProfileId(null);
        }
    }

    public String getActiveProfileId() {
        return prefs.getString(KEY_ACTIVE_PROFILE, null);
    }

    public void setActiveProfileId(String id) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE, id).apply();
    }

    public Profile getActiveProfile() {
        String id = getActiveProfileId();
        if (id == null) return null;
        for (Profile p : getProfiles()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public String generateProfileId() {
        return UUID.randomUUID().toString();
    }

    // ─── CARDS ─────────────────────────────────────────────────────────────────

    private String cardKey(String profileId) {
        return KEY_CARDS + "_" + profileId;
    }

    public List<Card> getCards(String profileId) {
        List<Card> cards = new ArrayList<>();
        try {
            String json = prefs.getString(cardKey(profileId), "[]");
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Card c = new Card(
                        obj.getString("id"),
                        obj.getString("enseigneName"),
                        obj.getString("barcodeNumber"),
                        obj.optString("imageFileName", ""),
                        obj.optString("description", "")
                );
                c.setUsageCount(obj.optInt("usageCount", 0));
                c.setDateAdded(obj.optLong("dateAdded", System.currentTimeMillis()));
                cards.add(c);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cards;
    }

    public void saveCards(String profileId, List<Card> cards) {
        try {
            JSONArray arr = new JSONArray();
            for (Card c : cards) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("enseigneName", c.getEnseigneName());
                obj.put("barcodeNumber", c.getBarcodeNumber());
                obj.put("imageFileName", c.getImageFileName() != null ? c.getImageFileName() : "");
                obj.put("description", c.getDescription() != null ? c.getDescription() : "");
                obj.put("usageCount", c.getUsageCount());
                obj.put("dateAdded", c.getDateAdded());
                arr.put(obj);
            }
            prefs.edit().putString(cardKey(profileId), arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addCard(String profileId, Card card) {
        if (card.getId() == null) card.setId(UUID.randomUUID().toString());
        List<Card> cards = getCards(profileId);
        cards.add(card);
        saveCards(profileId, cards);
    }

    public void updateCard(String profileId, Card updated) {
        List<Card> cards = getCards(profileId);
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(updated.getId())) {
                cards.set(i, updated);
                break;
            }
        }
        saveCards(profileId, cards);
    }

    public void deleteCard(String profileId, String cardId) {
        List<Card> cards = getCards(profileId);
        cards.removeIf(c -> c.getId().equals(cardId));
        saveCards(profileId, cards);
    }

    // ─── IMPORT / EXPORT JSON (cartes uniquement) ──────────────────────────────

    /**
     * Exporte uniquement les cartes du profil actif au format JSON.
     * Format : { "cards": [ ... ] }
     */
    public String exportToJson(String profileId) {
        try {
            JSONObject root = new JSONObject();
            JSONArray cardsArr = new JSONArray();
            for (Card c : getCards(profileId)) {
                JSONObject cObj = new JSONObject();
                cObj.put("id", c.getId());
                cObj.put("enseigneName", c.getEnseigneName());
                cObj.put("barcodeNumber", c.getBarcodeNumber());
                cObj.put("imageFileName", c.getImageFileName() != null ? c.getImageFileName() : "");
                cObj.put("description", c.getDescription() != null ? c.getDescription() : "");
                cObj.put("usageCount", c.getUsageCount());
                cObj.put("dateAdded", c.getDateAdded());
                cardsArr.put(cObj);
            }
            root.put("cards", cardsArr);
            return root.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Résultat d'un import : nombre de cartes ajoutées et liste des doublons refusés.
     */
    public static class ImportResult {
        public final int added;
        public final List<String> duplicates; // noms des enseignes refusées

        public ImportResult(int added, List<String> duplicates) {
            this.added = added;
            this.duplicates = duplicates;
        }
    }

    /**
     * Importe des cartes depuis un JSON dans le profil donné.
     * - N'efface pas les cartes existantes.
     * - Si même enseigne + même numéro de code-barres → doublon refusé (toast géré dans MenuActivity).
     * - Si même enseigne mais numéro différent → ajout avec "new" en description (seulement si description vide).
     * Retourne un ImportResult, ou null si le JSON est invalide.
     */
    public ImportResult importFromJson(String profileId, String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);

            // Compatibilité : accepte aussi l'ancien format avec "profiles"
            JSONArray cardsArr;
            if (root.has("cards")) {
                cardsArr = root.getJSONArray("cards");
            } else if (root.has("profiles")) {
                // Ancien format : on prend toutes les cartes de tous les profils
                cardsArr = new JSONArray();
                JSONArray profilesArr = root.getJSONArray("profiles");
                for (int i = 0; i < profilesArr.length(); i++) {
                    JSONArray pc = profilesArr.getJSONObject(i).optJSONArray("cards");
                    if (pc != null) {
                        for (int j = 0; j < pc.length(); j++) cardsArr.put(pc.get(j));
                    }
                }
            } else {
                return null;
            }

            List<Card> existing = getCards(profileId);
            List<Card> toAdd = new ArrayList<>();
            List<String> duplicates = new ArrayList<>();

            for (int i = 0; i < cardsArr.length(); i++) {
                JSONObject cObj = cardsArr.getJSONObject(i);
                String enseigneName = cObj.getString("enseigneName");
                String barcodeNumber = cObj.getString("barcodeNumber");
                String description = cObj.optString("description", "");
                String imageFileName = cObj.optString("imageFileName", "");
                int usageCount = cObj.optInt("usageCount", 0);
                long dateAdded = cObj.optLong("dateAdded", System.currentTimeMillis());

                // Vérifier doublon : même enseigne ET même numéro dans les cartes existantes OU déjà dans toAdd
                boolean isDuplicate = false;
                for (Card ec : existing) {
                    if (ec.getEnseigneName().equalsIgnoreCase(enseigneName)
                            && ec.getBarcodeNumber().equals(barcodeNumber)) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    for (Card tc : toAdd) {
                        if (tc.getEnseigneName().equalsIgnoreCase(enseigneName)
                                && tc.getBarcodeNumber().equals(barcodeNumber)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                }

                if (isDuplicate) {
                    duplicates.add(enseigneName);
                    continue;
                }

                // Vérifier si même enseigne (numéro différent) → ajouter "new" si description vide
                boolean sameEnseigne = false;
                for (Card ec : existing) {
                    if (ec.getEnseigneName().equalsIgnoreCase(enseigneName)) {
                        sameEnseigne = true;
                        break;
                    }
                }
                if (!sameEnseigne) {
                    for (Card tc : toAdd) {
                        if (tc.getEnseigneName().equalsIgnoreCase(enseigneName)) {
                            sameEnseigne = true;
                            break;
                        }
                    }
                }

                if (sameEnseigne && description.isEmpty()) {
                    description = "new";
                }

                Card newCard = new Card(
                        UUID.randomUUID().toString(), // nouvel ID pour éviter les collisions
                        enseigneName,
                        barcodeNumber,
                        imageFileName,
                        description
                );
                newCard.setUsageCount(usageCount);
                newCard.setDateAdded(dateAdded);
                toAdd.add(newCard);
            }

            // Persister
            existing.addAll(toAdd);
            saveCards(profileId, existing);

            return new ImportResult(toAdd.size(), duplicates);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}