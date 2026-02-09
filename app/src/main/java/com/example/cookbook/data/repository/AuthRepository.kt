package com.example.cookbook.data.repository

import android.util.Log
import com.example.cookbook.data.model.User
import com.example.cookbook.util.Constants
import com.example.cookbook.util.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling Firebase Authentication operations.
 * Provides methods for sign up, sign in, sign out, and password reset.
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Get the currently authenticated user as a Flow.
     * Emits updates whenever the auth state changes.
     */
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)

        // Send initial value
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * Check if a user is currently signed in.
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Get the current user's ID.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Sign up a new user with email and password.
     * Also creates a user document in Firestore.
     */
    fun signUp(email: String, password: String, name: String): Flow<Result<User>> = flow {
        try {
            Log.d(TAG, "Starting signUp for email: $email")
            emit(Result.Loading)

            // Create user in Firebase Auth with timeout
            Log.d(TAG, "Creating user with Firebase Auth...")
            val authResult = kotlinx.coroutines.withTimeout(30000) { // 30 second timeout
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            Log.d(TAG, "Auth result received")

            val firebaseUser = authResult.user
                ?: throw Exception("User creation failed")
            Log.d(TAG, "Firebase user created with UID: ${firebaseUser.uid}")

            // Update display name
            Log.d(TAG, "Updating display name...")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            Log.d(TAG, "Display name updated successfully")

            // Create user document in Firestore
            Log.d(TAG, "Creating Firestore document...")
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                favorites = emptyList()
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()
            Log.d(TAG, "Firestore document created successfully")

            emit(Result.Success(user))
            Log.d(TAG, "Sign up completed successfully")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Sign up timeout", e)
            emit(Result.Error(Exception("Connection timeout. Please check your internet connection and Firebase setup.")))
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed", e)
            emit(Result.Error(Exception("Sign up failed: ${e.message ?: e.toString()}")))
        }
    }

    /**
     * Sign in an existing user with email and password.
     */
    fun signIn(email: String, password: String): Flow<Result<User>> = flow {
        try {
            Log.d(TAG, "Starting signIn for email: $email")
            emit(Result.Loading)

            // Sign in with Firebase Auth with timeout
            Log.d(TAG, "Signing in with Firebase Auth...")
            val authResult = kotlinx.coroutines.withTimeout(30000) { // 30 second timeout
                auth.signInWithEmailAndPassword(email, password).await()
            }
            Log.d(TAG, "Auth result received")

            val firebaseUser = authResult.user
                ?: throw Exception("Sign in failed")
            Log.d(TAG, "User signed in with UID: ${firebaseUser.uid}")

            // Get user document from Firestore
            Log.d(TAG, "Fetching user document from Firestore...")
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()
            Log.d(TAG, "Firestore document fetched, exists: ${userDoc.exists()}")

            val user = if (userDoc.exists()) {
                User.fromMap(userDoc.data ?: emptyMap())
            } else {
                // Create user document if it doesn't exist (edge case)
                Log.d(TAG, "User document doesn't exist, creating new one...")
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email,
                    favorites = emptyList()
                )
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(newUser.toMap())
                    .await()
                Log.d(TAG, "New user document created")
                newUser
            }

            emit(Result.Success(user))
            Log.d(TAG, "Sign in completed successfully")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Sign in timeout", e)
            emit(Result.Error(Exception("Connection timeout. Please check your internet connection and Firebase setup.")))
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            emit(Result.Error(Exception("Sign in failed: ${e.message ?: e.toString()}")))
        }
    }

    /**
     * Sign out the current user.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Send a password reset email to the specified email address.
     */
    fun resetPassword(email: String): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)
            auth.sendPasswordResetEmail(email).await()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Delete the current user's account.
     * Also deletes their Firestore data.
     */
    fun deleteAccount(): Flow<Result<Unit>> = flow {
        try {
            emit(Result.Loading)

            val user = auth.currentUser
                ?: throw Exception("No user signed in")

            // Delete Firestore document
            firestore.collection(Constants.USERS_COLLECTION)
                .document(user.uid)
                .delete()
                .await()

            // Delete Firebase Auth account
            user.delete().await()

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
