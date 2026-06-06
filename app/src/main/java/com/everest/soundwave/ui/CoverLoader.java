package com.everest.soundwave.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.everest.soundwave.R;

import java.io.File;

public final class CoverLoader {

    private CoverLoader() {}

    public static void into(ImageView view, String coverPath) {
        if (coverPath == null || coverPath.isEmpty()) {
            view.setImageResource(R.drawable.cover_placeholder);
            return;
        }
        if (coverPath.startsWith("http://") || coverPath.startsWith("https://")) {
            Glide.with(view).load(coverPath)
                    .placeholder(R.drawable.cover_placeholder)
                    .into(view);
        } else {
            Glide.with(view).load(new File(coverPath))
                    .placeholder(R.drawable.cover_placeholder)
                    .into(view);
        }
    }
}
