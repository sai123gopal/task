package com.college.task;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Objects;

class Connection {

    static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            if (info.isConnected()) {
                return true;
            } else {
                return true;
            }

        }
    }
}
