package com.app.toolbox.fragment.notepad;

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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class EditorFragment extends Fragment {
    private EditText mTitleView, mMainEditor;
    private boolean mIsNewFile;
    private File mCurrentFile;
    private File mFileToOpen;

    // listens on action NotepadFragment.ACTION_OPEN_FILE
    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String filePath = intent.getStringExtra("filePath");
            if(filePath==null) {
                mFileToOpen = null;
            } else {
                mFileToOpen = new File(filePath);
            }
        }
    };

    private void newFile() {
        mIsNewFile = true;
        mTitleView.setText("");
        mMainEditor.setText("");
    }

    private void loadFile(File file) {
        mIsNewFile = false;
        mTitleView.setText("");
        mMainEditor.setText("");
        mCurrentFile = file;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                mMainEditor.append(new String(buffer, 0, bytesRead));
            }
            mTitleView.setText(file.getName());
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
        mTitleView = view.findViewById(R.id.title_view);
        view.findViewById(R.id.back_button).setOnClickListener(v -> exitEditor());
        mMainEditor = view.findViewById(R.id.main_edittext);
        ContextCompat.registerReceiver(requireContext(), mCommandReceiver, new IntentFilter(NotepadFragment.ACTION_OPEN_FILE), ContextCompat.RECEIVER_NOT_EXPORTED);
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
    }

    public void exitEditor() {
        String name = mTitleView.getText().toString();
        if (!name.isBlank() && !mMainEditor.getText().toString().isBlank() && isFileNameValid(name)) {
            // if document exists, save it.
            boolean saved = saveBuffer(name);
            System.out.println("Buffer saved: " + saved);
        }
        // hide on-screen keyboard
        InputMethodManager imm = requireContext().getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(mMainEditor.getWindowToken(), 0);

        Log.d("broadcast_stats", "Sending intent to change fragment...");
        Intent intent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
        intent.putExtra("fragmentName", NotepadFragment.FRAGMENT_HOME);
        requireContext().sendBroadcast(intent);
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
