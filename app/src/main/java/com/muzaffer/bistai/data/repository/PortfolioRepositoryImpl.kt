package com.muzaffer.bistai.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.muzaffer.bistai.domain.model.PortfolioItem
import com.muzaffer.bistai.domain.repository.PortfolioRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PortfolioRepository {

    override fun getPortfolio(uid: String): Flow<List<PortfolioItem>> = callbackFlow {
        val collectionRef = firestore.collection("users").document(uid).collection("portfolio")
        
        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PortfolioItem::class.java)?.copy(symbol = doc.id)
                }
                trySend(items)
            }
        }
        
        awaitClose { subscription.remove() }
    }

    override suspend fun addOrUpdatePortfolioItem(uid: String, item: PortfolioItem): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .collection("portfolio").document(item.symbol)
                .set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removePortfolioItem(uid: String, symbol: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .collection("portfolio").document(symbol)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
