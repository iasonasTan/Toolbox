package com.app.toolbox.tools.notepad;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.List;

public final class NotepadRoot extends ParentPageFragment {
    public static final String STRING_ID = "toolbox.page.NOTEPAD_PAGE";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_root, container, false);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.notepad_icon);
    }

    @Override
    protected List<Fragment> pages() {
        return List.of(
                new NotepadHome(),
                new NotepadEditor()
        );
    }

    @Override
    protected String defaultPageClassName() {
        return NotepadHome.class.getName();
    }

    @Override
    protected int containerId() {
        return R.id.notepad_fragment_container;
    }
}
