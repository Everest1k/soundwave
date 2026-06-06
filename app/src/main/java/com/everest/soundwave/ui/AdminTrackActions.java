package com.everest.soundwave.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.everest.soundwave.R;
import com.everest.soundwave.data.Track;
import com.everest.soundwave.data.TrackRepository;

public final class AdminTrackActions {

    private AdminTrackActions() {}

    public static void show(View anchor, Track t) {
        Context ctx = anchor.getContext();
        PopupMenu pm = new PopupMenu(ctx, anchor);
        pm.getMenu().add(0, 1, 0, R.string.rename);
        pm.getMenu().add(0, 2, 1, R.string.delete);
        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showRename(ctx, t);
                return true;
            } else if (item.getItemId() == 2) {
                new AlertDialog.Builder(ctx)
                        .setMessage(R.string.delete_confirm)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, (d, w) ->
                                TrackRepository.get(ctx).delete(t))
                        .show();
                return true;
            }
            return false;
        });
        pm.show();
    }

    private static void showRename(Context ctx, Track t) {
        EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(t.title);
        input.setHint(R.string.rename_hint);
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.rename)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String s = input.getText().toString().trim();
                    if (!s.isEmpty()) TrackRepository.get(ctx).rename(t.id, s);
                })
                .show();
    }
}
