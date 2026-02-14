package com.app.toolbox.tools.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.tools.notepad.storage.Storage;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class NotepadEditor extends Fragment {
    public static final String ACTION_LOAD     = "toolbox.notepad.openFile";
    public static final String FILE_NAME_EXTRA = "toolbox.notepad.filePath";
    public static final String NO_FILE_EXTRA   = "toolbox.notepad.noText";
    public static final String TEXT_EXTRA      = "toolbox.notepad.appendText";

    private final char[] FORBIDDEN_CHARACTERS = {'/', '<', '>', ':', '"', '|', '?', '*', '\\'};
    private EditText mTitleEditText, mContentEditText;
    private File mCurrentFile;

    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fileName = intent.getStringExtra(FILE_NAME_EXTRA);
            if(fileName!=null)
                handleFile(fileName);

            String content = intent.getStringExtra(TEXT_EXTRA);
            if(content!=null)
                handleContent(content);
        }

        private void handleFile(String fileName) {
            if (fileName.equals(NO_FILE_EXTRA)) {
                Log.d("notepad", "Opening blank editor");
                openBlank();
            } else {
                mCurrentFile = Storage.getInstance().getNote(fileName);
                loadFile();
            }
        }

        private void handleContent(String content) {
            openBlank();
            mContentEditText.setText(content);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // register receiver
        Log.d("notepad", "Registering receiver...");
        ContextCompat.registerReceiver(requireContext(), mCommandReceiver, new IntentFilter(ACTION_LOAD), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // on back pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                exitEditor();
            }
        });

        // disable user input (no horizontal scroll)
        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.ENABLE_SCROLL_EXTRA, false);
        requireContext().sendBroadcast(intent);

        initViews(view, savedInstanceState);
    }

    private void initViews(View view, Bundle inState) {
        mTitleEditText = view.findViewById(R.id.title_view);
        mContentEditText = view.findViewById(R.id.main_edittext);

        // Restore State
        if(inState!=null) {
            mTitleEditText.setText(inState.getCharSequence("title"));
            mContentEditText.setText(inState.getCharSequence("editor"));
        }

        // Add listeners
        view.findViewById(R.id.back_button).setOnClickListener(v -> exitEditor());
        view.findViewById(R.id.share_button).setOnClickListener(new NoteListener(NoteListener.ACTION_SHARE));
        view.findViewById(R.id.delete_button).setOnClickListener(new NoteListener(NoteListener.ACTION_DELETE));
    }

    private void openBlank() {
        mCurrentFile=null;
        mTitleEditText.setText("");
        mContentEditText.setText("");
    }

    private void loadFile() {
        mTitleEditText.setText(mCurrentFile.getName());
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(mCurrentFile))) {
            byte[] buffer = new byte[8192]; // 8KB
            int bytesRead;
            mContentEditText.setText("");
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                mContentEditText.append(new String(buffer, 0, bytesRead));
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_editor, container, false);
    }

    public void exitEditor() {
        String name = mTitleEditText.getText().toString();
        boolean saved = trySavingBuffer(name);
        int toastTextId = saved?R.string.note_saved:R.string.could_not_save_note;
        Toast.makeText(requireContext(), toastTextId, Toast.LENGTH_SHORT).show();

        // hide on-screen keyboard
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(mContentEditText.getWindowToken(), 0);

        if(saved) {
            Intent intent = new Intent(NotepadRoot.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(NotepadRoot.PAGE_ID, NotepadRoot.FRAGMENT_HOME);
            requireContext().sendBroadcast(intent);
        }
    }

    public boolean trySavingBuffer(String name) {
        // If title is already used or contains forbidden symbols
        if(mCurrentFile==null&&Storage.getInstance().noteFileExists(name)||
                mCurrentFile!=null&&!mCurrentFile.getName().equals(name)&&Storage.getInstance().noteFileExists(name) ||
                !isNameValid(name)) {

            // Don't save and warn user
            showWrongNameDialog();
            return false;
        }
        String title = mTitleEditText.getText().toString();
        String contents = mContentEditText.getText().toString();
        if(mCurrentFile==null)
            Storage.getInstance().createNote(title, contents);
        else
            Storage.getInstance().saveNote(contents, mCurrentFile);
        return true; // Not always right!
    }

    private boolean isNameValid(@NonNull String name) {
        for(char c: FORBIDDEN_CHARACTERS) {
            if (name.contains(String.valueOf(c)))
                return false;
        }
        return !(name.isBlank() && mCurrentFile != null);
    }

    private void showWrongNameDialog() {
        String descriptionBuilder = requireContext().getString(R.string.invalid_name_desc) +
                String.valueOf(FORBIDDEN_CHARACTERS);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(ContextCompat.getString(requireContext(), R.string.invalid_name))
                .setMessage(descriptionBuilder)
                .setPositiveButton(requireContext().getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mCommandReceiver);

        // enable user input (no horizontal scroll)
        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.ENABLE_SCROLL_EXTRA, true);
        requireContext().sendBroadcast(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("title", mTitleEditText.getText());
        outState.putCharSequence("editor", mContentEditText.getText());
    }

    private final class NoteListener implements View.OnClickListener {
        public static final int ACTION_DELETE = 0;
        public static final int ACTION_SHARE  = 1;

        private final int mAction;

        private NoteListener(int action) {
            this.mAction = action;
        }

        @Override
        public void onClick(View ignored) {
            doFileAction();
        }

        private void doFileAction() {
            switch(mAction){
                case ACTION_DELETE -> deleteCurrentFile();
                case ACTION_SHARE -> shareCurrentFile();
            }
        }

        private void deleteCurrentFile() {
            if(mCurrentFile==null)
                return;
            Storage.askToDelete(requireContext(), (dialog, which) -> {
                if(mCurrentFile.delete())
                    Toast.makeText(requireContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
                Intent showHomeIntent = new Intent(NotepadRoot.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
                showHomeIntent.putExtra(NotepadRoot.PAGE_ID, NotepadRoot.FRAGMENT_HOME);
                requireContext().sendBroadcast(showHomeIntent);
            });
        }

        private void shareCurrentFile() {
            String title = mTitleEditText.getText().toString();
            String contents = mContentEditText.getText().toString();
            String textToShare = String.format("Title: %s\n%s", title, contents);

            trySavingBuffer(title);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_this_note));
            requireContext().startActivity(shareIntent);
        }
    }
}
