package com.app.toolbox.tools.stopwatch;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public enum StopwatchState implements Parcelable {
    ERROR("toolbox.stopwatch.stateError"),
    BEGINNING("toolbox.stopwatch.stateBeginning"),
    PAUSED("toolbox.stopwatch.statePaused"),
    RUNNING("toolbox.stopwatch.stateRunning");

    public final String string;

    StopwatchState(String name) {
        this.string = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StopwatchState> CREATOR = new Creator<>() {
        @Override public StopwatchState createFromParcel(Parcel source) {
            String requiredName = source.readString();
            for (StopwatchState value : StopwatchState.values()) {
                if (value.string.equals(requiredName)) {
                    return value;
                }
            }
            return ERROR;
        }

        @Override public StopwatchState[] newArray(int size) {
            return new StopwatchState[0];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(string);
    }
}
