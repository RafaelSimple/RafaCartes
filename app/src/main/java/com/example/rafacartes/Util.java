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
        colors.add(new ColorItem("Rouge", Color.parseColor("#E53935")));
        colors.add(new ColorItem("Violet", Color.parseColor("#8E24AA")));
        colors.add(new ColorItem("Bleu", Color.parseColor("#1E88E5")));
        colors.add(new ColorItem("Vert foncé", Color.parseColor("#00897B")));
        colors.add(new ColorItem("Orange", Color.parseColor("#F4511E")));
        colors.add(new ColorItem("Indigo", Color.parseColor("#3949AB")));
        colors.add(new ColorItem("Cyan", Color.parseColor("#00ACC1")));
        colors.add(new ColorItem("Vert clair", Color.parseColor("#7CB342")));
        colors.add(new ColorItem("Jaune", Color.parseColor("#FFB300")));
        colors.add(new ColorItem("Gris", Color.parseColor("#757575")));


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