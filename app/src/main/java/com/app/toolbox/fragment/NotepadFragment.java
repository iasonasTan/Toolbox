package com.app.toolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.ItemView;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

public class NotepadFragment extends ToolFragment {
    public final HomeFragment home=new HomeFragment();
    public final EditorFragment editor=new EditorFragment();

    @Override
    protected String fragmentName() {
        return "NOTEPAD_FRAGMENT";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.notepad_icon);
    }

    public void setFragment(Fragment fragment) {
        FragmentManager manager=getChildFragmentManager();
        manager.beginTransaction().replace(R.id.notepad_fragment_container, fragment).commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFragment(home);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_root, container, false);
    }

    public static class HomeFragment extends Fragment {
        static final String NOTES_DIR_NAME ="notes";
        private NotepadFragment parent;
        private LinearLayout notesList;
        private TextView noNotesFound_textview;

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            parent = (NotepadFragment) getParentFragment();
            view.findViewById(R.id.newNote_fab).setOnClickListener(v->{
                parent.editor.openFile(() -> null);
                parent.setFragment(parent.editor);
            });
            noNotesFound_textview = view.findViewById(R.id.message);
            notesList = view.findViewById(R.id.notes_layout);
        }

        public void updateViews() {
            notesList.removeAllViews();
            // load saved notes
            File notes_dir=new File(requireContext().getFilesDir(), NOTES_DIR_NAME);
            if (!notes_dir.isDirectory())
                // noinspection all
                notes_dir.mkdir();
            File[] files=Objects.requireNonNull(notes_dir.listFiles());
            noNotesFound_textview.setVisibility(files.length==0&&isVisible()?View.VISIBLE:View.GONE);
            for (File file: files) {
                ItemView nv = getNoteView(file);
                notesList.addView(nv);
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
            parent.setFragment(this);
        }

        private ItemView getNoteView(File file) {
            ItemView nv=new ItemView(getContext());
            nv.setClickable(true);
            nv.setOnClickListener(v -> {
                // switch to editor with it's data
                parent.editor.openFile(() -> file);
                parent.setFragment(parent.editor);
            });
            // noinspection all
            nv.setOnDeleteListener(v -> {
                new AlertDialog.Builder(requireContext()) // use getContext() if in activity
                        .setTitle(ContextCompat.getString(requireContext(), R.string.delete_note))
                        .setMessage(ContextCompat.getString(requireContext(), R.string.delete_note_desc))
                        .setPositiveButton(ContextCompat.getString(requireContext(), R.string.delete), (dialog, which) -> {
                            // noinspection all
                            file.delete();
                            updateViews();
                        })
                        .setNegativeButton(ContextCompat.getString(requireContext(), R.string.cancel), null)
                        .show();
            });
            final int LIMIT=20;
            String string=file.getName();
            int n=string.length();
            nv.setTitle(string.substring(0, Math.min(n, LIMIT)));
            try {
                // set content preview
                BufferedReader reader=new BufferedReader(new FileReader(file));
                string=reader.readLine();
                if (string!=null) {
                    n = string.length();
                    nv.setContent(string.substring(0, Math.min(n, LIMIT)));
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

        public boolean nameConflicts(String name) {
            for (int i = 0; i < notesList.getChildCount(); i++) {
                ItemView itemView= (ItemView) notesList.getChildAt(i);
                if (itemView.getTitle().equals(name)) return true;
            }
            return false;
        }

    }

    public static class EditorFragment extends Fragment {
        private NotepadFragment mParentFragment;
        private EditText mTitle_view, mMain_edittext;
        private boolean mNewFile;
        private File mCurrentFile;
        private Supplier<File> mFileSupplier;

        private void newFile () {
            mNewFile =true;
            mTitle_view.setText("");
            mMain_edittext.setText("");
        }

        public void openFile(Supplier<File> fileSupplier) {
            this.mFileSupplier=fileSupplier;
        }

        private void openFile(File file) {
            mNewFile =false;
            mTitle_view.setText("");
            mMain_edittext.setText("");
            mCurrentFile =file;
            try (BufferedInputStream bufferedInputStream=new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer=new byte[8192];
                int bytesRead;
                while((bytesRead=bufferedInputStream.read(buffer)) != -1) {
                    mMain_edittext.append(new String(buffer, 0, bytesRead));
                }
                mTitle_view.setText(file.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            File file=mFileSupplier.get();
            if(file==null)
                newFile();
            else
                openFile(file);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                    new OnBackPressedCallback(true){
                        @Override
                        public void handleOnBackPressed() {
                            exitEditor();
                        }
                    });
            mParentFragment =(NotepadFragment) getParentFragment();
            mTitle_view =view.findViewById(R.id.title_view);
            view.findViewById(R.id.back_button).setOnClickListener(v -> exitEditor());
            mMain_edittext =view.findViewById(R.id.main_edittext);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_notepad_editor, container, false);
        }

        public void exitEditor () {
            String name= mTitle_view.getText().toString();
            if (!name.isBlank() && !mMain_edittext.getText().toString().isBlank() && isFileNameValid(name)) {
                // if document exists, save it.
                boolean saved=saveBuffer(name);
                System.out.println("Buffer saved: "+saved);
            }
            // hide on-screen keyboard
            InputMethodManager imm=requireContext().getSystemService(InputMethodManager.class);
            imm.hideSoftInputFromWindow(mMain_edittext.getWindowToken(), 0);
            // show home fragment
            mParentFragment.setFragment(mParentFragment.home);
        }

        public boolean isFileNameValid(String name) {
            if (name.isBlank()|| mParentFragment.home.nameConflicts(name)&& mNewFile) {
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
            Log.d("action_spoil", this+" Is attached to context "+context);
        }

        @SuppressWarnings("all")
        public File newFile(String fileName) {
            File notes_dir=new File(requireContext().getFilesDir(), HomeFragment.NOTES_DIR_NAME);
            if (!notes_dir.isDirectory()) notes_dir.mkdir();
            return new File(notes_dir, fileName);
        }

        @SuppressWarnings("all")
        public boolean saveBuffer(String name) {
            try {
                if(!mNewFile) mCurrentFile.delete();
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile(name)));
                writer.write(mMain_edittext.getText().toString());
                writer.close();
                Toast.makeText(requireContext(), requireContext().getString(R.string.note_saved), Toast.LENGTH_SHORT).show();
                return true;
            } catch (IOException e) {
                Log.d("warning", "Cannot save file..."+e);
                return false;
            }
        }
    }

}
