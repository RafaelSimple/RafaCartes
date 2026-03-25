package com.example.rafacartes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    public interface OnCardClickListener {
        void onCardClick(Card card);
    }

    private final Context context;
    private final List<Card> cards;
    private final OnCardClickListener listener;

    public CardAdapter(Context context, List<Card> cards, OnCardClickListener listener) {
        this.context = context;
        this.cards = cards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);

        // Description
        String desc = card.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(desc);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Image ou nom
        String imgFile = card.getImageFileName();
        if (imgFile != null && !imgFile.isEmpty()) {
            File f = new File(context.getFilesDir(), imgFile);
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    holder.ivLogo.setImageBitmap(bmp);
                    holder.ivLogo.setVisibility(View.VISIBLE);
                    holder.tvNoLogo.setVisibility(View.GONE);
                } else {
                    showNameFallback(holder, card);
                }
            } else {
                showNameFallback(holder, card);
            }
        } else {
            showNameFallback(holder, card);
        }

        holder.itemView.setOnClickListener(v -> listener.onCardClick(card));
    }

    private void showNameFallback(CardViewHolder holder, Card card) {
        holder.ivLogo.setVisibility(View.GONE);
        holder.tvNoLogo.setVisibility(View.VISIBLE);
        holder.tvNoLogo.setText(card.getEnseigneName());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvNoLogo;
        TextView tvDescription;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.iv_logo);
            tvNoLogo = itemView.findViewById(R.id.tv_no_logo);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }
    }
}
