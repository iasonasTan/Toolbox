package com.app.toolbox.tools.notepad.storage;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class Storage implements Iterable<File> {
    private static Storage sInstance;

    public static void destroy() {
        if(sInstance==null)
            throw new IllegalStateException("Storage is already destroyed!");
        sInstance=null;
    }

    public static boolean initIfNotInitialized(Context context, String dirName) {
        if(sInstance!=null)
            return false;
        init(context, dirName);
        return true;
    }

    public static void init(Context context, String dirName) {
        if(sInstance!=null)
            throw new IllegalStateException("Storage is already initialized.");
        File notes_dir = new File(context.getFilesDir(), dirName);
        sInstance = new Storage(notes_dir);
    }

    public static Storage getInstance() {
        if(sInstance==null)
            throw new IllegalStateException("Storage is not initialized.");
        return sInstance;
    }

    private final File mNotesDir;

    public Storage(@NonNull File notesDir) {
        this.mNotesDir = notesDir;
    }

    public static void askToDelete(Context context, DialogInterface.OnClickListener action) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(ContextCompat.getString(context, R.string.delete_note))
                .setMessage(ContextCompat.getString(context, R.string.delete_note_desc))
                .setPositiveButton(ContextCompat.getString(context, R.string.delete), action)
                .setNegativeButton(ContextCompat.getString(context, R.string.cancel), null)
                .show();
    }

    public boolean noteFileExists(@NonNull String name) {
        String[] fileNames = mNotesDir.list();
        if(fileNames == null)
            return false;
        for(String fileName: fileNames) {
            if(fileName.equals(name))
                return true;
        }
        return false;
    }

    public boolean hasNotes() {
        File[] files = mNotesDir.listFiles();
        return files != null && files.length != 0;
    }

    public File getNote(@NonNull String fileName) {
        File file = new File(mNotesDir, fileName);
        try {
            if(!file.exists()||file.isDirectory())
                // noinspection all
                file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public void createNote(String title, String contents) {
        File file = getNote(title);
        saveNote(contents, file);
    }

    public void saveNote(String contents, File file) {
        if(contents==null||file.isDirectory())
            return;
        try (BufferedOutputStream outputStream=new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] bytes = contents.getBytes();
            outputStream.write(bytes);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    @NonNull
    @Override
    public Iterator<File> iterator() {
        File[] files = Objects.requireNonNull(mNotesDir.listFiles());
        return new Iter(files);
    }

    private static class Iter implements Iterator<File> {
        private final File[] mFiles;
        private final int mLength;
        private int mPointer;

        public Iter(@NonNull File[] files) {
            mFiles = files;
            mLength = files.length;
            mPointer = 0;
        }

        @Override
        public boolean hasNext() {
            // Position in in-array-bounds
            return mPointer < mLength;
        }

        @Override
        public File next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return mFiles[mPointer++];
        }
    }
}
