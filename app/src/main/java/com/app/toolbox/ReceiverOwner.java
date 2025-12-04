package com.app.toolbox;

import android.content.Context;

/**
 * Represents a class that has broadcast receivers that need to
 * work even when the component is not visible.
 */

public interface ReceiverOwner {
    /**
     * Unregisters all receiver from {@code this}.
     * @param context context used to unregister receivers.
     */
    void unregisterReceivers(Context context);
}
