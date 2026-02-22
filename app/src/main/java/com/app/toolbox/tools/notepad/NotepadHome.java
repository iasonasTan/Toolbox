package com.app.toolbox.tools.notepad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.tools.notepad.storage.Storage;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.RemovableView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class NotepadHome extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        View messageView = view.findViewById(R.id.message);
        ViewGroup notesLayout = view.findViewById(R.id.notes_layout);
        new ViewManager(requireContext(), notesLayout, messageView).loadNoteViews();
    }

    private void initViews(View view) {
        view.findViewById(R.id.newNote_fab).setOnClickListener(v -> {
            // Show editor
            Intent openEditorIntent = new Intent(ParentPageFragment.actionChangePage(NotepadRoot.STRING_ID)).setPackage(requireContext().getPackageName());
            openEditorIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, NotepadEditor.class.getName());
            requireContext().sendBroadcast(openEditorIntent);

            // Load empty editor
            final Context context = requireContext();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Open Blank Note
                Intent openBlankIntent = new Intent(NotepadEditor.ACTION_LOAD).setPackage(context.getPackageName());
                openBlankIntent.putExtra(NotepadEditor.FILE_NAME_EXTRA, NotepadEditor.NO_FILE_EXTRA);
                context.sendBroadcast(openBlankIntent);
            }, 100);
        });
    }

    private static final class ViewManager {
        private static final long ANIMATION_DURATION = 150L;
        private final ViewGroup mGroup;
        private final View mFallbackView;
        private final Context context;

        public ViewManager(Context context, ViewGroup group, View fallbackMsg) {
            this.context = context;
            mGroup = group;
            mFallbackView = fallbackMsg;
        }

        public void loadNoteViews() {
            final long DELAY_GAP = 90;
            long delay = DELAY_GAP;

            boolean hasNotes = Storage.getInstance().hasNotes();
            mFallbackView.setVisibility(Utils.booleanVisibility(!hasNotes));

            mGroup.removeAllViews();
            for(File noteFile: Storage.getInstance()) {
                addView(noteFile, delay);
                delay += DELAY_GAP;
            }
        }

        public void addView(File noteFile, long delay) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                View view = createNoteView(noteFile);
                mGroup.addView(view);
                animateView(view);
            }, delay);
        }

        public View createNoteView(File noteFile) {
            RemovableView view = new RemovableView(context);
            view.setTitle(noteFile.getName());

            view.setOnClickListener(createOnClickListener(noteFile));
            view.setOnDeleteListener(createOnDeleteListener(noteFile));

            try {
                Scanner scanner = new Scanner(noteFile);
                String line=scanner.nextLine();
                view.setContent(line!=null?line:"");
                scanner.close();
            } catch (FileNotFoundException | NoSuchElementException ignored) {
                // Ignore
            }
            return view;
        }

        public View.OnClickListener createOnDeleteListener(File noteFile) {
            return v -> Storage.askToDelete(context,
                (dialog, which) -> {
                    // noinspection all
                    noteFile.delete();
                    loadNoteViews();
                    dialog.dismiss();
                }
            );
        }

        public View.OnClickListener createOnClickListener(File noteFile) {
            return v -> {
                // Open editor
                Intent changePageIntent = new Intent(ParentPageFragment.actionChangePage(NotepadRoot.STRING_ID)).setPackage(context.getPackageName());
                changePageIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, NotepadEditor.class.getName());
                context.sendBroadcast(changePageIntent);

                // Load data
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent loadFileIntent = new Intent(NotepadEditor.ACTION_LOAD).setPackage(context.getPackageName());
                    loadFileIntent.putExtra(NotepadEditor.FILE_NAME_EXTRA, noteFile.getName());
                    context.sendBroadcast(loadFileIntent);
                }, 100);
            };
        }

        private void animateView(View view) {
            view.animate()
                .scaleX(0.1f)
                .scaleY(0.1f)
                .setDuration(0L)
                .withEndAction(() -> view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(ANIMATION_DURATION)
                    .start())
                .start();
        }
    }
}
