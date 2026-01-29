package com.app.toolbox.tools.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.view.storing.StoringLinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public final class HomeFragment extends Fragment {
    static final String NOTES_DIR_NAME = "notes";
    private StoringLinearLayout mNotesList;
    private TextView noNotesFound_textview;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        NotepadFragment.usedNames = new ArrayList<>();
        mNotesList.setNamesList(NotepadFragment.usedNames);
    }

    private void initViews(View view) {
        noNotesFound_textview = view.findViewById(R.id.message);
        mNotesList = view.findViewById(R.id.notes_layout);
        view.findViewById(R.id.newNote_fab).setOnClickListener(v -> {
            Log.d("notepad_debug", "Sending intent to open blank editor");
            Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent1.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_EDITOR);
            requireContext().sendBroadcast(intent1);

            Intent intent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(requireContext().getPackageName());
            intent.putExtra(EditorFragment.FILE_PATH_EXTRA, EditorFragment.PATH_NONE_EXTRA);
            requireContext().sendBroadcast(intent);
        });
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
        FileViewFactory factory = FileViewFactory.newInstance(requireContext(), this::updateViews);
        for (File file : files)
            mNotesList.addView(factory.createNoteView(file));
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_home, container, false);
    }
}
