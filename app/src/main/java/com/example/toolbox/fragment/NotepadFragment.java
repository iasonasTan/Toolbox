package com.example.toolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.toolbox.MainActivity;
import com.example.toolbox.view.ItemView;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class NotepadFragment extends ToolFragment {
    public final HomeFragment home=new HomeFragment();
    public final EditorFragment editor=new EditorFragment();

    public NotepadFragment(Context context) {
        super(new NavigationItemView(context, R.drawable.notepad_icon));
    }

    public void setFragment(Fragment fragment) {
        FragmentManager manager=getChildFragmentManager();

        final Consumer<Fragment> hideFrag=f->{
            manager.beginTransaction()
                    .hide(f)
                    .commit();
        };

        hideFrag.accept(home);
        hideFrag.accept(editor);

        manager.beginTransaction()
                .show(fragment)
                .commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentManager man=getChildFragmentManager();

        final Consumer<Fragment> addFrag= f->{
            man.beginTransaction()
                    .add(R.id.notepad_fragment_container, f)
                    .commit();
        };

        addFrag.accept(home);
        addFrag.accept(editor);

        setFragment(home);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_notepad_root, container, false);
    }

    public static class HomeFragment extends Fragment {
        private static final String NOTES_DIR_NAME ="notes";
        private NotepadFragment parent;
        private final List<ItemView> note_views=new ArrayList<>();
        private LinearLayout notesList;
        private TextView textView;


        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            parent = (NotepadFragment) getParentFragment();
            view.findViewById(R.id.newNote_fab).setOnClickListener(v->{
                parent.editor.newFile();
                parent.setFragment(parent.editor);
            });

            textView = view.findViewById(R.id.message);
            notesList = view.findViewById(R.id.notes_layout);
        }

        public File newFile(String fileName) {
            File notes_dir=new File(requireContext().getFilesDir(), NOTES_DIR_NAME);
            if (!notes_dir.isDirectory())
                notes_dir.mkdir();
            return new File(notes_dir, fileName);
        }

        public void updateGUI() {
            note_views.clear();
            notesList.removeAllViews();

            // loads the saved notes
            File notes_dir=new File(requireContext().getFilesDir(), NOTES_DIR_NAME);
            if (!notes_dir.isDirectory()) {
                notes_dir.mkdir();
            }
            File[] files=Objects.requireNonNull(notes_dir.listFiles());
            if(files.length==0) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
            for (File file: files) {
                ItemView nv = getNoteView(file);
                notesList.addView(nv);
                note_views.add(nv);
            }
        }

        @Override
        public void onHiddenChanged(boolean hidden) {
            super.onHiddenChanged(hidden);
            updateGUI();
        }

        private ItemView getNoteView(File file) {
            ItemView nv=new ItemView(getContext());
            nv.setClickable(true);
            nv.setOnClickListener(v -> {
                // switch to editor with it's data
                parent.editor.loadFile(file);
                parent.setFragment(parent.editor);
            });
            nv.setOnDeleteListener(v -> {
                file.delete();
                updateGUI();
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
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_notepad_home, container, false);
        }

        public boolean nameConflicts(String name) {
            for (ItemView itemView : note_views) {
                if (itemView.getTitle().equals(name))
                    return true;
            }
            return false;
        }

    }

    public static class EditorFragment extends Fragment {
        private NotepadFragment parent;
        private EditText title_view, main_edittext;
        private boolean newFile;

        public void newFile () {
            newFile=true;
            title_view.setText("");
            main_edittext.setText("");
        }

        public void loadFile (File file) {
            newFile=false;
            title_view.setText("");
            main_edittext.setText("");
            try {
                BufferedReader reader=new BufferedReader(new FileReader(file));
                String line;
                while ((line=reader.readLine())!=null) {
                    main_edittext.append(line);
                    main_edittext.append("\n");
                }
                title_view.setText(file.getName());
                file.delete();
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            requireActivity().getOnBackPressedDispatcher()
                    .addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true){
                @Override
                public void handleOnBackPressed() {
                    exitEditor();
                }
            });

            parent=(NotepadFragment) getParentFragment();
            view.findViewById(R.id.back_button).setOnClickListener(v -> {
                exitEditor();
            });
            title_view=view.findViewById(R.id.title_view);
            main_edittext=view.findViewById(R.id.main_edittext);

            main_edittext.setHorizontallyScrolling(false);
            main_edittext.setSingleLine(false);
            main_edittext.setVerticalScrollBarEnabled(true);
            main_edittext.setMovementMethod(new ScrollingMovementMethod());

        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_notepad_editor, container, false);
        }

        public void exitEditor () {
            if (saveFile())
                parent.setFragment(parent.home);
        }

        public boolean saveFile () {
            String name=title_view.getText().toString();
            if (name.isBlank()&&main_edittext.getText().toString().isBlank()) {
                return true;
            } else if (name.isBlank()||parent.home.nameConflicts(name)&&newFile) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Invalid Name")
                        .setMessage("The name you entered is conflict with another file " +
                                "or invalid, please change the note name and try again")
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
                return false;
            } else {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(parent.home.newFile(name)));
                    writer.write(main_edittext.getText().toString());
                    writer.close();
                    Toast.makeText(requireContext(), "Note saved!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
    }

}
