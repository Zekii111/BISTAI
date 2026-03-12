package com.muzaffer.bistai.domain.repository

import com.google.firebase.auth.AuthResult
import com.muzaffer.bistai.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    
    /**
     * Firebase Auth State flow olarak döner. 
     * Kullanıcı yoksa null, varsa User objesi döner.
     */
    fun getAuthState(): Flow<User?>
    
    /** 
     * Uygulamanın anlık olarak kullanıcısının kim olduğunu döner.
     */
    fun getCurrentUser(): User?

    /**
     * Google Sign In Credential id token i üzerinden Firebase'e giriş yapar.
     */
    suspend fun signInWithGoogleCredential(idToken: String): Result<AuthResult>
    
    /**
     * Çıkış yapar.
     */
    suspend fun signOut()
}
