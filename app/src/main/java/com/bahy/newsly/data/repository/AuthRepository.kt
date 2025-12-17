package com.bahy.newsly.data.repository

import android.content.Context
import com.bahy.newsly.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val _currentUser = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
    
    val currentUser: Flow<User?> = _currentUser

    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            if (email.isEmpty() || password.isEmpty()) {
                return Result.failure(Exception("Email and password are required"))
            }

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user?.toUser()
            
            if (user != null) {
                // Update user data in Firestore (in case profile was updated)
                saveUserToFirestore(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection"
                else -> e.message ?: "An error occurred during sign in"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your internet connection"
                e.message?.contains("socket", ignoreCase = true) == true -> 
                    "Connection error. Please check your internet connection"
                else -> e.message ?: "An error occurred. Please try again"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (email.isEmpty()) {
                return Result.failure(Exception("Email is required"))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Please enter a valid email address"))
            }

            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection"
                else -> e.message ?: "Failed to send reset email"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your internet connection"
                e.message?.contains("socket", ignoreCase = true) == true ->
                    "Connection error. Please check your internet connection"
                else -> e.message ?: "An error occurred. Please try again"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                return Result.failure(Exception("All fields are required"))
            }

            if (password.length < 6) {
                return Result.failure(Exception("Password must be at least 6 characters"))
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(Exception("Please enter a valid email address"))
            }

            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            // Update display name
            result.user?.updateProfile(
                com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )?.await()
            
            val user = result.user?.toUser()
            
            if (user != null) {
                // Save user data to Firestore
                saveUserToFirestore(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Sign up failed"))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            // Handle Firebase Auth specific errors
            val errorMessage = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered"
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection"
                else -> e.message ?: "An error occurred during sign up"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            // Handle other errors
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your internet connection"
                e.message?.contains("socket", ignoreCase = true) == true -> 
                    "Connection error. Please check your internet connection"
                else -> e.message ?: "An error occurred. Please try again"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun signOut(context: Context? = null) {
        firebaseAuth.signOut()
        // Also sign out from Google Sign-In to allow account selection on next login
        context?.let {
            GoogleSignInHelper.signOut(it)
        }
    }

    suspend fun signInWithGoogle(googleSignInAccount: GoogleSignInAccount): Result<User> {
        return try {
            val idToken = googleSignInAccount.idToken
            if (idToken == null) {
                return Result.failure(Exception("Google sign in failed: No ID token"))
            }
            
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toUser()
            
            if (user != null) {
                // Save/update user data to Firestore
                saveUserToFirestore(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign in failed"))
            }
        } catch (e: com.google.firebase.auth.FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection"
                else -> e.message ?: "Google sign in failed"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Please check your internet connection"
                e.message?.contains("socket", ignoreCase = true) == true -> 
                    "Connection error. Please check your internet connection"
                else -> e.message ?: "Google sign in failed. Please try again"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            username = displayName ?: email?.substringBefore("@") ?: "User",
            email = email ?: "",
            profilePictureUrl = photoUrl?.toString()
        )
    }
    
    private suspend fun saveUserToFirestore(user: User) {
        try {
            val userMap = mapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "profilePictureUrl" to (user.profilePictureUrl ?: ""),
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(user.id)
                .set(userMap)
                .await()
        } catch (e: Exception) {
            // Log error but don't fail the sign in process
            android.util.Log.e("AuthRepository", "Error saving user to Firestore: ${e.message}", e)
        }
    }
    
    suspend fun updateUserProfile(user: User) {
        try {
            val userMap = mapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "profilePictureUrl" to (user.profilePictureUrl ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(user.id)
                .set(userMap, com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error updating user profile: ${e.message}", e)
            throw e
        }
    }
}

