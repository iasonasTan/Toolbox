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
import com.app.toolbox.ReceiverOwner;
import com.app.toolbox.utils.Utils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public final class EditorFragment extends Fragment implements ReceiverOwner {
    // used with shortcuts
    public static final String FILE_PATH_EXTRA  = "toolbox.notepad.filePath";
    public static final String ACTION_OPEN_FILE = "toolbox.notepad.openFile";
    public static final String TEXT_EXTRA       = "toolbox.notepad.appendText";
    public static final String PATH_NONE_EXTRA  = "toolbox.notepad.noText";

    private boolean mIsNewFile;
    private String mTextToAppend;

    private EditText mTitleEditText, mMainEditText;
    private File mCurrentFile, mFileToOpen;

    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Utils.checkIntent(intent, FILE_PATH_EXTRA);
            String filePath = intent.getStringExtra(FILE_PATH_EXTRA);
            Log.d("notepad_open", "Opening notepad editor.");
            if(PATH_NONE_EXTRA.equals(filePath)) {
                String text = intent.getStringExtra(TEXT_EXTRA);
                Log.d("notepad_open", "Creating new note. Appending text "+text);
                mFileToOpen = null;
                mTextToAppend = text;
            } else {
                // noinspection all
                mFileToOpen = new File(filePath);
            }
        }
    };

    private void openBlank() {
        mIsNewFile = true;
        mTitleEditText.setText("");
        mMainEditText.setText("");
    }

    private void loadFile(File file) {
        mIsNewFile = false;
        mCurrentFile = file;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            mMainEditText.setText("");
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                mMainEditText.append(new String(buffer, 0, bytesRead));
                Log.d("notepad_load", "Append to content: "+new String(buffer, 0, bytesRead));
            }
            mTitleEditText.setText(file.getName());
            Log.d("notepad_load", "Setting title as "+file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(!hidden) {
            if (mFileToOpen == null)
                openBlank();
            else
                loadFile(mFileToOpen);

            if (mMainEditText != null && mTextToAppend != null && mIsNewFile) {
                mMainEditText.append(mTextToAppend);
                mTitleEditText.append(ContextCompat.getString(requireContext(), R.string.untitled_note));
                Log.d("notepad_open", "Appending text to main editor...");
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // on back pressed
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPress());

        // register receiver
        ContextCompat.registerReceiver(requireContext(), mCommandReceiver, new IntentFilter(ACTION_OPEN_FILE), ContextCompat.RECEIVER_NOT_EXPORTED);
        MainActivity.sReceiverOwners.add(this);

        view.findViewById(R.id.delete_button).setOnClickListener(v ->
                FileViewFactory.askToDelete((dialog, which) -> {
                    Intent showHomeIntent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
                    showHomeIntent.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_HOME);
                    requireContext().sendBroadcast(showHomeIntent);
                    boolean deleted = mCurrentFile.delete();
                    if(deleted)
                        Toast.makeText(requireContext(), "Note is deleted", Toast.LENGTH_LONG).show();
        }, requireContext()));

        // disable user input (no horizontal scroll)
        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.USER_INPUT_EXTRA, false);
        requireContext().sendBroadcast(intent);

        initViews(view, savedInstanceState);
    }

    @Override
    public void unregisterReceivers(Context context) {
        context.unregisterReceiver(mCommandReceiver);
    }

    private final class OnBackPress extends OnBackPressedCallback {
        public OnBackPress() { super(true); }

        @Override public void handleOnBackPressed() {
            exitEditor();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        String nameInEditor = mTitleEditText.getText().toString();
        String name = nameInEditor.isBlank() ? "Untitled Note" : nameInEditor;
        boolean result = saveBuffer(name);
        if(!result)
            Toast.makeText(requireContext(), R.string.could_not_save_note, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("title", mTitleEditText.getText());
        outState.putCharSequence("editor", mMainEditText.getText());
    }

    private void initViews(View view, Bundle inState) {
        mTitleEditText = view.findViewById(R.id.title_view);
        mMainEditText = view.findViewById(R.id.main_edittext);
        if(inState!=null) {
            mTitleEditText.setText(inState.getCharSequence("title"));
            mMainEditText.setText(inState.getCharSequence("editor"));
        }
        view.findViewById(R.id.back_button).setOnClickListener(v -> exitEditor());
        view.findViewById(R.id.share_button).setOnClickListener(v -> {
            String name = mTitleEditText.getText().toString();
            if (!name.isBlank() && !mMainEditText.getText().toString().isBlank() && isFileNameValid(name)) {
                saveBuffer(name);
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mMainEditText.getText());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_this_note));
            requireContext().startActivity(shareIntent);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_editor, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.USER_INPUT_EXTRA, true);
        requireContext().sendBroadcast(intent);
    }

    public void exitEditor() {
        String name = mTitleEditText.getText().toString();
        boolean saved;
        if (!name.isBlank() && isFileNameValid(name)) {
            // if document exists, save it.
            saved = saveBuffer(name);
            System.out.println("Buffer saved: " + saved);
        } else {
            saved = false;
        }
        // hide on-screen keyboard
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(mMainEditText.getWindowToken(), 0);

        if(!saved)
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.exit_for_sure)
                .setCancelable(false)
                .setNegativeButton(R.string.ok, (dialog, which) -> {
                    Log.d("doc_saving", "User exited editor without saving file");
                    dialog.dismiss();
                    Log.d("broadcast_sent", "Sending intent to change fragment...");
                    Intent intent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
                    intent.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_HOME);
                    requireContext().sendBroadcast(intent);
                }).setPositiveButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    Log.d("doc_saving", "User saved document");
                }).show();
        else {
            Log.d("broadcast_sent", "Sending intent to change fragment...");
            Intent intent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_HOME);
            requireContext().sendBroadcast(intent);
        }
    }

    public boolean isFileNameValid(String name) {
        if (name.isBlank() || NotepadFragment.usedNames.contains(name) && mIsNewFile) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(ContextCompat.getString(requireContext(), R.string.invalid_name))
                    .setMessage(ContextCompat.getString(requireContext(), R.string.invalid_name_desc))
                    .setPositiveButton(ContextCompat.getString(requireContext(), R.string.ok), (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        }
        return true;
    }

    @SuppressWarnings("all")
    public File openBlank(String fileName) {
        File notes_dir = new File(requireContext().getFilesDir(), HomeFragment.NOTES_DIR_NAME);
        if (!notes_dir.isDirectory()) notes_dir.mkdir();
        return new File(notes_dir, fileName);
    }

    @SuppressWarnings("all")
    public boolean saveBuffer(String name) {
        if(mCurrentFile==null)
            return true;
        try {
            if (!mIsNewFile)
                mCurrentFile.delete();
            BufferedWriter writer = new BufferedWriter(new FileWriter(openBlank(name)));
            writer.write(mMainEditText.getText().toString());
            writer.close();
            Toast.makeText(requireContext(), requireContext().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            Log.d("warning", "Cannot save file..." + e);
            return false;
        }
    }
}
