package com.app.toolbox.tools.notepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.List;
import java.util.Objects;

public class NotepadFragment extends PageFragment {
    public static final String FRAGMENT_EDITOR        = "toolbox.notepad.EDITOR_FRAGMENT";
    public static final String ACTION_CHANGE_FRAGMENT = "toolbox.notepad.CHANGE_FRAGMENT";
    public static final String STRING_ID              = "toolbox.page.NOTEPAD_PAGE";
    public static final String FRAGMENT_HOME          = "toolbox.notepad.HOME_FRAGMENT";

    static List<String> usedNames;
    private final HomeFragment   mHome   = new HomeFragment();
    private final EditorFragment mEditor = new EditorFragment();

    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Fragment fragment = switch(
                    Objects.requireNonNull(intent.getStringExtra(STRING_ID))) {
                case FRAGMENT_HOME -> mHome;
                case FRAGMENT_EDITOR -> mEditor;
                default -> null;
            };
            if(fragment != null&&!isStateSaved()) {
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.notepad_fragment_container, fragment)
                        .commit();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //requireContext().unregisterReceiver(mCommandReceiver);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getChildFragmentManager().beginTransaction().add(R.id.notepad_fragment_container, mEditor).show(mEditor).commit();
        getChildFragmentManager().beginTransaction().hide(mEditor).replace(R.id.notepad_fragment_container, mHome).commit();
        ContextCompat.registerReceiver(requireContext(), mCommandReceiver, new IntentFilter(NotepadFragment.ACTION_CHANGE_FRAGMENT), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notepad_root, container, false);
    }

}
