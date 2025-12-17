package com.bahy.newsly.data.repository

import android.content.Context
import com.bahy.newsly.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.tasks.await

object GoogleSignInHelper {
    fun getGoogleSignInClient(context: Context): GoogleSignInClient? {
        // Get Web Client ID from strings.xml
        // You need to add your Web Client ID from Firebase Console
        // Firebase Console > Project Settings > Your Apps > Web App > Web Client ID
        val webClientId = context.getString(R.string.default_web_client_id)
        
        if (webClientId == "YOUR_WEB_CLIENT_ID" || webClientId.isEmpty()) {
            // Web Client ID not configured
            return null
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        return GoogleSignIn.getClient(context, gso)
    }
    
    fun getLastSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    suspend fun signOut(context: Context) {
        val googleSignInClient = getGoogleSignInClient(context)
        googleSignInClient?.signOut()?.await()
    }
}

