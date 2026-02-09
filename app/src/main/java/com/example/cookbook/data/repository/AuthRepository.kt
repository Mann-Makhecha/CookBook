package com.example.cookbook.data.repository

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
            emit(Result.Loading)

            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("User creation failed")

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user document in Firestore
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

            emit(Result.Success(user))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    /**
     * Sign in an existing user with email and password.
     */
    fun signIn(email: String, password: String): Flow<Result<User>> = flow {
        try {
            emit(Result.Loading)

            // Sign in with Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: throw Exception("Sign in failed")

            // Get user document from Firestore
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (userDoc.exists()) {
                User.fromMap(userDoc.data ?: emptyMap())
            } else {
                // Create user document if it doesn't exist (edge case)
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
                newUser
            }

            emit(Result.Success(user))
        } catch (e: Exception) {
            emit(Result.Error(e))
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
