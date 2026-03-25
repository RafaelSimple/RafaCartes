package com.example.rafacartes;

public class Card {
    private String id;
    private String enseigneName;
    private String barcodeNumber;
    private String imageFileName; // ex: "carrefour.png"
    private String description;
    private int usageCount;
    private long dateAdded;

    public Card() {}

    public Card(String id, String enseigneName, String barcodeNumber,
                String imageFileName, String description) {
        this.id = id;
        this.enseigneName = enseigneName;
        this.barcodeNumber = barcodeNumber;
        this.imageFileName = imageFileName;
        this.description = description;
        this.usageCount = 0;
        this.dateAdded = System.currentTimeMillis();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEnseigneName() { return enseigneName; }
    public void setEnseigneName(String enseigneName) { this.enseigneName = enseigneName; }

    public String getBarcodeNumber() { return barcodeNumber; }
    public void setBarcodeNumber(String barcodeNumber) { this.barcodeNumber = barcodeNumber; }

    public String getImageFileName() { return imageFileName; }
    public void setImageFileName(String imageFileName) { this.imageFileName = imageFileName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }
    public void incrementUsage() { this.usageCount++; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
}
