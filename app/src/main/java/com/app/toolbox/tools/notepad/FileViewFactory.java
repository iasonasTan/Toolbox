package com.app.toolbox.tools.notepad;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.view.RemovableView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class FileViewFactory {
    private final Context context;
    private final Runnable mUpdate;

    private FileViewFactory(Context context, Runnable update){
        this.context = context;
        mUpdate = update;
    }

    static FileViewFactory newInstance(Context context, Runnable update) {
        return new FileViewFactory(context, update);
    }

    static void askToDelete(DialogInterface.OnClickListener action, Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(ContextCompat.getString(context, R.string.delete_note))
                .setMessage(ContextCompat.getString(context, R.string.delete_note_desc))
                .setPositiveButton(ContextCompat.getString(context, R.string.delete), action)
                .setNegativeButton(ContextCompat.getString(context, R.string.cancel), null)
                .show();
    }

    public View.OnClickListener createClickListener(File file) {
        return v -> askToDelete((dialog, which) -> {
            // noinspection all
            file.delete();
            mUpdate.run();
            dialog.dismiss();
        }, context);
    }

    public RemovableView createNoteView(File file) {
        RemovableView nv = new RemovableView(context);
        nv.setClickable(true);
        Log.d("intent_stuff", "creating note with file path "+file.getAbsolutePath());
        nv.setOnClickListener(v -> {
            // switch to editor with it's data
            Log.d("broadcast_stats", "Sending intent to open file "+file.getAbsolutePath());
            Intent intent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(context.getPackageName());
            intent.putExtra(EditorFragment.FILE_PATH_EXTRA, file.getAbsolutePath());
            context.sendBroadcast(intent);

            Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(context.getPackageName());
            intent1.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_EDITOR);
            context.sendBroadcast(intent1);
        });
        nv.setOnDeleteListener(createClickListener(file));

        String title = file.getName();
        nv.setTitle(title);
        try {
            // set content preview
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String firstLine = reader.readLine();
            if (firstLine != null) {
                nv.setContent(firstLine);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return nv;
    }
}
