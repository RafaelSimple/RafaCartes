package com.example.rafacartes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Util {

    private static class ColorItem {
        String name;
        int color;

        ColorItem(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    public static void showColorPickerDialog(Context context, int currentColor, Consumer<Integer> onColorSelected) {
        List<ColorItem> colors = new ArrayList<>();
        colors.add(new ColorItem("Blanc", Color.parseColor("#FFFFFF")));
        colors.add(new ColorItem("Noir", Color.parseColor("#000000")));
        colors.add(new ColorItem("Rose-Armand Thierry", Color.parseColor("#C40160")));
        colors.add(new ColorItem("Rouge-Auchan", Color.parseColor("#FF0015")));
        colors.add(new ColorItem("Rouge-DPAM", Color.parseColor("#E11838")));
        colors.add(new ColorItem("Jaune-Bricorama", Color.parseColor("#FFF101")));
        colors.add(new ColorItem("Jaune-CarteSourire", Color.parseColor("#FFED00")));
        colors.add(new ColorItem("Bleu-Lidl", Color.parseColor("#2250A9")));
        colors.add(new ColorItem("Bleu-Decathlon", Color.parseColor("#3743BA")));
        colors.add(new ColorItem("Bleu-Ikea", Color.parseColor("#0057AD")));
        colors.add(new ColorItem("Bleu-Makro", Color.parseColor("#002470")));
        colors.add(new ColorItem("Bleu-Norauto", Color.parseColor("#002B6F")));
        colors.add(new ColorItem("Or-Fnac", Color.parseColor("#EBB200")));
        colors.add(new ColorItem("Indigo", Color.parseColor("#3949AB")));
        colors.add(new ColorItem("Cyan", Color.parseColor("#00ACC1")));
        colors.add(new ColorItem("Vert clair-DrMax", Color.parseColor("#6EBA3A")));
        colors.add(new ColorItem("VertOlive-YvesRocher", Color.parseColor("#89993E")));
        colors.add(new ColorItem("Jaune", Color.parseColor("#FFB300")));
        colors.add(new ColorItem("Gris", Color.parseColor("#757575")));
        colors.add(new ColorItem("Bordeau-Nicolas", Color.parseColor("#9A1E3A")));
        colors.add(new ColorItem("Rouge-Penny", Color.parseColor("#CD1414")));



        ArrayAdapter<ColorItem> adapter = new ArrayAdapter<ColorItem>(context, 0, colors) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_color_choice, parent, false);
                }

                ColorItem item = getItem(position);
                View circle = convertView.findViewById(R.id.view_color_circle);
                TextView tvName = convertView.findViewById(R.id.tv_color_name);


                circle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(item.color));


                tvName.setText(item.name);

                // gras la couleur actuellement sélectionnée
                if (item.color == currentColor) {
                    tvName.setTypeface(null, Typeface.BOLD);
                } else {
                    tvName.setTypeface(null, Typeface.NORMAL);
                }

                return convertView;
            }
        };

        // liste couleurs
        new MaterialAlertDialogBuilder(context)
                .setTitle("Choisir une couleur")
                .setAdapter(adapter, (dialog, which) -> {
                    ColorItem selectedItem = adapter.getItem(which);
                    if (selectedItem != null) {
                        onColorSelected.accept(selectedItem.color);
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }


    public static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static int getThemeColor(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrRes, typedValue, true)) {
            return typedValue.data;
        }
        return Color.GRAY; // Couleur de secours au cas où
    }
}