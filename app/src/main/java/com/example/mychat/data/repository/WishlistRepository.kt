package com.example.mychat.data.repository

import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.Wishlist
import com.example.mychat.data.model.WishlistItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class WishlistRepository(private val authRepository: AuthRepository) {

    private val db = FirebaseFirestore.getInstance()

    fun getUserWishlist(): Flow<List<String>> = authRepository.currentUser.flatMapLatest { currentUser ->
        if (currentUser != null) {
            callbackFlow {
                val listener = db.collection("users")
                    .document(currentUser.id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val wishlist = snapshot.get("wishlist") as? List<String> ?: emptyList()
                            trySend(wishlist)
                        } else {
                            // If user document doesn't exist, initialize with empty wishlist
                            trySend(emptyList())
                        }
                    }

                awaitClose {
                    listener.remove()
                }
            }
        } else {
            flowOf(emptyList())
        }
    }

    suspend fun addToWishlist(listingId: String): Result<String> {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        return try {
            val userRef = db.collection("users").document(currentUser.id)
            val userDoc = userRef.get().await()
            
            val currentWishlist = userDoc.get("wishlist") as? List<String> ?: emptyList()
            val updatedWishlist = if (currentWishlist.contains(listingId)) {
                currentWishlist
            } else {
                currentWishlist + listingId
            }

            userRef.update("wishlist", updatedWishlist).await()
            Result.success("Added to wishlist")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromWishlist(listingId: String): Result<String> {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        return try {
            val userRef = db.collection("users").document(currentUser.id)
            val userDoc = userRef.get().await()
            
            val currentWishlist = userDoc.get("wishlist") as? List<String> ?: emptyList()
            val updatedWishlist = currentWishlist.filter { it != listingId }

            userRef.update("wishlist", updatedWishlist).await()
            Result.success("Removed from wishlist")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isListingInWishlist(listingId: String): Boolean {
        val currentUser = authRepository.currentUser.first()
        if (currentUser == null) {
            return false
        }

        return try {
            val userDoc = db.collection("users").document(currentUser.id).get().await()
            val wishlist = userDoc.get("wishlist") as? List<String> ?: emptyList()
            wishlist.contains(listingId)
        } catch (e: Exception) {
            false
        }
    }

    fun getWishlistListings(): Flow<List<TravelListing>> = authRepository.currentUser.flatMapLatest { currentUser ->
        if (currentUser != null) {
            callbackFlow {
                val listener = db.collection("users")
                    .document(currentUser.id)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val wishlist = snapshot.get("wishlist") as? List<String> ?: emptyList()
                            
                            if (wishlist.isEmpty()) {
                                trySend(emptyList())
                            } else {
                                // Fetch the actual listings for the wishlist items
                                // Use FieldPath.documentId() to query by document ID
                                db.collection("listings")
                                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), wishlist)
                                    .whereEqualTo("approved", true)
                                    .addSnapshotListener { listingsSnapshot, listingsError ->
                                        if (listingsError != null) {
                                            close(listingsError)
                                            return@addSnapshotListener
                                        }

                                        val listings = listingsSnapshot?.documents?.mapNotNull { document ->
                                            val data = document.data ?: return@mapNotNull null
                                            try {
                                                val agencyId = data["agencyId"] as? String ?: ""
                                                var agencyName = "Unknown Agency"
                                                var agencyData: com.example.mychat.data.model.Agency? = null

                                                if (agencyId.isNotEmpty()) {
                                                    // Fetch agency data from users collection
                                                    db.collection("users").document(agencyId).get()
                                                        .addOnSuccessListener { agencyDoc ->
                                                            if (agencyDoc.exists()) {
                                                                val agencyDocData = agencyDoc.data
                                                                if (agencyDocData != null) {
                                                                    agencyName = agencyDocData["companyName"] as? String ?: "Unknown Agency"
                                                                    agencyData = com.example.mychat.data.model.Agency(
                                                                        id = agencyDoc.id,
                                                                        companyName = agencyDocData["companyName"] as? String ?: "",
                                                                        name = agencyDocData["name"] as? String ?: "",
                                                                        email = agencyDocData["email"] as? String ?: "",
                                                                        approved = agencyDocData["approved"] as? Boolean ?: false
                                                                    )
                                                                }
                                                            }
                                                        }
                                                }

                                                com.example.mychat.data.model.TravelListing(
                                                    id = document.id,
                                                    title = data["title"] as? String ?: "",
                                                    description = data["description"] as? String ?: "",
                                                    price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                                    duration = (data["duration"] as? Number)?.toInt() ?: 1,
                                                    destination = data["destination"] as? String ?: "",
                                                    type = data["type"] as? String ?: "adventure",
                                                    photos = (data["photos"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                                                    rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                                                    reviewsCount = (data["reviewsCount"] as? Number)?.toInt() ?: 0,
                                                    agencyId = agencyId,
                                                    agencyName = agencyName,
                                                    agencyData = agencyData,
                                                    approved = data["approved"] as? Boolean ?: false,
                                                    // Enhanced package details
                                                    packageType = data["packageType"] as? String,
                                                    countryName = data["countryName"] as? String,
                                                    stateName = data["stateName"] as? String,
                                                    placesCovered = parsePlacesCovered(data["placesCovered"]),
                                                    hotelType = data["hotelType"] as? String,
                                                    mealPlan = data["mealPlan"] as? String,
                                                    cost = (data["cost"] as? Number)?.toDouble(),
                                                    // Enhanced features - parse from Firestore data
                                                    itinerary = parseItinerary(data["itinerary"]),
                                                    inclusions = data["inclusions"] as? String ?: "",
                                                    exclusions = data["exclusions"] as? String ?: "",
                                                    faqs = parseFAQs(data["faqs"]),
                                                    packageCode = data["packageCode"] as? String ?: "",
                                                    tourCategories = parseTourCategories(data["tourCategories"]),
                                                    photoUrls = parsePhotoUrls(data["photoUrls"]),
                                                    videoUrl = data["videoUrl"] as? String ?: ""
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        } ?: emptyList()

                                        trySend(listings)
                                    }
                            }
                        } else {
                            trySend(emptyList())
                        }
                    }

                awaitClose {
                    listener.remove()
                }
            }
        } else {
            flowOf(emptyList())
        }
    }

    private fun parsePlacesCovered(placesData: Any?): List<com.example.mychat.data.model.PlaceCovered> {
        return if (placesData is List<*>) {
            placesData.mapNotNull { placeData ->
                if (placeData is Map<*, *>) {
                    val id = placeData["id"] as? String ?: ""
                    val name = placeData["name"] as? String ?: ""
                    val imageUrls = (placeData["imageUrls"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    if (id.isNotEmpty() && name.isNotEmpty()) {
                        com.example.mychat.data.model.PlaceCovered(id, name, imageUrls)
                    } else null
                } else null
            }
        } else {
            emptyList()
        }
    }

    private fun parseItinerary(itineraryData: Any?): List<com.example.mychat.data.model.ItineraryDay> {
        return if (itineraryData is List<*>) {
            itineraryData.mapNotNull { dayData ->
                if (dayData is Map<*, *>) {
                    val day = (dayData["day"] as? Number)?.toInt() ?: 0
                    val placeName = dayData["placeName"] as? String ?: ""
                    val description = dayData["description"] as? String ?: ""
                    val activities = (dayData["activities"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                    val accommodation = dayData["accommodation"] as? String ?: ""
                    
                    if (day > 0 && placeName.isNotEmpty()) {
                        com.example.mychat.data.model.ItineraryDay(day, placeName, description, activities, accommodation)
                    } else null
                } else null
            }
        } else {
            emptyList()
        }
    }

    private fun parseFAQs(faqsData: Any?): List<com.example.mychat.data.model.FAQ> {
        return if (faqsData is List<*>) {
            faqsData.mapNotNull { faqData ->
                if (faqData is Map<*, *>) {
                    val question = faqData["question"] as? String ?: ""
                    val answer = faqData["answer"] as? String ?: ""
                    
                    if (question.isNotEmpty() && answer.isNotEmpty()) {
                        com.example.mychat.data.model.FAQ(question, answer)
                    } else null
                } else null
            }
        } else {
            emptyList()
        }
    }

    private fun parseTourCategories(categoriesData: Any?): List<String> {
        return if (categoriesData is List<*>) {
            categoriesData.mapNotNull { it as? String }
        } else {
            emptyList()
        }
    }

    private fun parsePhotoUrls(photoUrlsData: Any?): List<String> {
        return if (photoUrlsData is List<*>) {
            photoUrlsData.mapNotNull { it as? String }
        } else {
            emptyList()
        }
    }
}