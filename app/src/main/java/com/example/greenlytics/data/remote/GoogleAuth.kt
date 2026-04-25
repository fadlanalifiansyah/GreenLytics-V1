package com.example.greenlytics.data.remote

import android.content.Context
import com.example.greenlytics.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

object GoogleAuth {
    fun getGoogleIdOption(context: Context): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.webclientid))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
    }
}