package com.app.toolbox.fragment.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.view.ItemView;
import com.app.toolbox.view.storing.StoringLinearLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {
    static final String NOTES_DIR_NAME = "notes";
    private StoringLinearLayout mNotesList;
    private TextView noNotesFound_textview;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.newNote_fab).setOnClickListener(v -> {
            Log.d("broadcast_stats", "Sending intent to open blank editor");
            Intent intent = new Intent(NotepadFragment.ACTION_OPEN_FILE).setPackage(requireContext().getPackageName());
            intent.putExtra("filePath", (String)null);
            requireContext().sendBroadcast(intent);

            Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent1.putExtra("fragmentName", NotepadFragment.FRAGMENT_EDITOR);
            requireContext().sendBroadcast(intent1);
        });
        noNotesFound_textview = view.findViewById(R.id.message);
        mNotesList = view.findViewById(R.id.notes_layout);
        NotepadFragment.usedNames = new ArrayList<>();
        mNotesList.setNamesList(NotepadFragment.usedNames);

    }

    public void updateViews() {
        mNotesList.removeAllViews();
        // load saved notes
        File notes_dir = new File(requireContext().getFilesDir(), NOTES_DIR_NAME);
        if (!notes_dir.isDirectory())
            // noinspection all
            notes_dir.mkdir();
        File[] files = Objects.requireNonNull(notes_dir.listFiles());
        noNotesFound_textview.setVisibility(files.length == 0 && isVisible() ? View.VISIBLE : View.GONE);
        for (File file : files) {
            ItemView nv = getNoteView(file);
            mNotesList.addView(nv);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        updateViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();
    }

    private View.OnClickListener getClickListener(File file) {
        return v -> new AlertDialog.Builder(requireContext())
                .setTitle(ContextCompat.getString(requireContext(), R.string.delete_note))
                .setMessage(ContextCompat.getString(requireContext(), R.string.delete_note_desc))
                .setPositiveButton(ContextCompat.getString(requireContext(), R.string.delete), (dialog, which) -> {
                    // noinspection all
                    file.delete();
                    updateViews();
                })
                .setNegativeButton(ContextCompat.getString(requireContext(), R.string.cancel), null)
                .show();
    }

    private ItemView getNoteView(File file) {
        ItemView nv = new ItemView(getContext());
        nv.setClickable(true);
        Log.d("intent_stuff", "creating note with file path "+file.getAbsolutePath());
        nv.setOnClickListener(v -> {
            // switch to editor with it's data
            Log.d("broadcast_stats", "Sending intent to open file "+file.getAbsolutePath());
            Intent intent = new Intent(NotepadFragment.ACTION_OPEN_FILE).setPackage(requireContext().getPackageName());
            intent.putExtra("filePath", file.getAbsolutePath());
            requireContext().sendBroadcast(intent);

            Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent1.putExtra("fragmentName", NotepadFragment.FRAGMENT_EDITOR);
            requireContext().sendBroadcast(intent1);
        });
        nv.setOnDeleteListener(getClickListener(file));

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_home, container, false);
    }
}
