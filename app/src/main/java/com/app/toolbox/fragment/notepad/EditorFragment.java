package com.app.toolbox.fragment.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class EditorFragment extends Fragment {
    // used with shortcuts
    public static final String FILE_PATH_EXTRA  = "toolbox.notepad.filePath";
    public static final String ACTION_OPEN_FILE = "toolbox.notepad.openFile";
    public static final String TEXT_EXTRA       = "toolbox.notepad.appendText";
    public static final String PATH_NONE_EXTRA  = "toolbox.notepad.noText";

    private boolean mIsNewFile;
    private String mTextToAppend;

    private EditText mTitleEditor, mMainEditor;
    private File mCurrentFile, mFileToOpen;

    // listens on action NotepadFragment.ACTION_OPEN_FILE
    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Utils.checkIntent(intent, FILE_PATH_EXTRA);
            String filePath = Objects.requireNonNull(intent.getStringExtra(FILE_PATH_EXTRA));
            Log.d("notepad_open", "Opening notepad editor.");
            if(filePath.equals(PATH_NONE_EXTRA)) {
                String text = intent.getStringExtra(TEXT_EXTRA);
                Log.d("notepad_open", "Creating new note. Appending text "+text);
                mFileToOpen = null;
                mTextToAppend = text;
            } else {
                mFileToOpen = new File(filePath);
            }
        }
    };

    private void newFile() {
        mIsNewFile = true;
        mTitleEditor.setText("");
        mMainEditor.setText("");
    }

    private void loadFile(File file) {
        mIsNewFile = false;
        mTitleEditor.setText("");
        mMainEditor.setText("");
        mCurrentFile = file;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                mMainEditor.append(new String(buffer, 0, bytesRead));
            }
            mTitleEditor.setText(file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFileToOpen == null)
            newFile();
        else
            loadFile(mFileToOpen);

        if(mMainEditor!=null&&mTextToAppend!=null&&mIsNewFile) {
            mMainEditor.append(mTextToAppend);
            mTitleEditor.append(ContextCompat.getString(requireContext(), R.string.untitled_note));
            Log.d("notepad_open", "Appending text to main editor...");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        exitEditor();
                    }
                });
        mTitleEditor = view.findViewById(R.id.title_view);
        view.findViewById(R.id.back_button).setOnClickListener(v -> exitEditor());
        mMainEditor = view.findViewById(R.id.main_edittext);
        ContextCompat.registerReceiver(requireContext(), mCommandReceiver, new IntentFilter(ACTION_OPEN_FILE), ContextCompat.RECEIVER_NOT_EXPORTED);

        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.ENABLE_USER_INPUT, false);
        requireContext().sendBroadcast(intent);

        view.findViewById(R.id.share_button).setOnClickListener(v -> {
            String name = mTitleEditor.getText().toString();
            if (!name.isBlank() && !mMainEditor.getText().toString().isBlank() && isFileNameValid(name)) {
                saveBuffer(name);
            }
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mMainEditor.getText());
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
        //requireContext().unregisterReceiver(mCommandReceiver);
        Intent intent = new Intent(MainActivity.CONFIG_VIEW_PAGER).setPackage(requireContext().getPackageName());
        intent.putExtra(MainActivity.ENABLE_USER_INPUT, true);
        requireContext().sendBroadcast(intent);
    }

    public void exitEditor() {
        String name = mTitleEditor.getText().toString();
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
        imm.hideSoftInputFromWindow(mMainEditor.getWindowToken(), 0);

        if(!saved)
            new AlertDialog.Builder(requireContext())
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.exit_for_sure)
                .setCancelable(false)
                .setNegativeButton(R.string.ok, (dialog, which) -> {
                    Log.d("doc_saving", "User exited editor without saving file");
                    dialog.dismiss();
                    Log.d("broadcast_sent", "Sending intent to change fragment...");
                    Intent intent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
                    intent.putExtra(NotepadFragment.STRING_ID, NotepadFragment.FRAGMENT_HOME);
                    requireContext().sendBroadcast(intent);
                }).setPositiveButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    Log.d("doc_saving", "User saved document");
                }).show();
        else {
            Log.d("broadcast_sent", "Sending intent to change fragment...");
            Intent intent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(NotepadFragment.STRING_ID, NotepadFragment.FRAGMENT_HOME);
            requireContext().sendBroadcast(intent);
        }
    }

    public boolean isFileNameValid(String name) {
        if (name.isBlank() || NotepadFragment.usedNames.contains(name) && mIsNewFile) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(ContextCompat.getString(requireContext(), R.string.invalid_name))
                    .setMessage(ContextCompat.getString(requireContext(), R.string.invalid_name_desc))
                    .setPositiveButton(ContextCompat.getString(requireContext(), R.string.ok), (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        }
        return true;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("action_spoil", this + " Is attached to context " + context);
    }

    @SuppressWarnings("all")
    public File newFile(String fileName) {
        File notes_dir = new File(requireContext().getFilesDir(), HomeFragment.NOTES_DIR_NAME);
        if (!notes_dir.isDirectory()) notes_dir.mkdir();
        return new File(notes_dir, fileName);
    }

    @SuppressWarnings("all")
    public boolean saveBuffer(String name) {
        try {
            if (!mIsNewFile) mCurrentFile.delete();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile(name)));
            writer.write(mMainEditor.getText().toString());
            writer.close();
            Toast.makeText(requireContext(), requireContext().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            Log.d("warning", "Cannot save file..." + e);
            return false;
        }
    }
}
