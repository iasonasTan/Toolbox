package com.app.toolbox.fragment.stopwatch;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public enum UIState implements UIStateConstants, Parcelable {
    ERROR(ERROR_STR),
    BEGINNING(BEGINNING_STR),
    PAUSED(PAUSED_STR),
    RUNNING(RUNNING_STR);

    final String string;

    UIState(String name) {
        this.string = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UIState> CREATOR = new Creator<>() {
        @Override
        public UIState createFromParcel(Parcel source) {
            String requiredName = source.readString();
            for (UIState value : UIState.values()) {
                if (value.string.equals(requiredName)) {
                    return value;
                }
            }
            return ERROR;
        }

        @Override
        public UIState[] newArray(int size) {
            return new UIState[0];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(string);
    }
}
