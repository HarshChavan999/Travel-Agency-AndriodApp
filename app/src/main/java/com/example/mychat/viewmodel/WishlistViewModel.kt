package com.example.mychat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.Wishlist
import com.example.mychat.data.repository.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishlistViewModel(private val wishlistRepository: WishlistRepository) : ViewModel() {

    private val _wishlistItems = MutableStateFlow<List<String>>(emptyList())
    val wishlistItems: StateFlow<List<String>> = _wishlistItems.asStateFlow()

    private val _wishlistListings = MutableStateFlow<List<TravelListing>>(emptyList())
    val wishlistListings: StateFlow<List<TravelListing>> = _wishlistListings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadWishlist()
        loadWishlistListings()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            wishlistRepository.getUserWishlist().collect { wishlist ->
                _wishlistItems.value = wishlist
            }
        }
    }

    private fun loadWishlistListings() {
        viewModelScope.launch {
            wishlistRepository.getWishlistListings().collect { listings ->
                _wishlistListings.value = listings
            }
        }
    }

    fun toggleWishlist(listingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val isInWishlist = wishlistItems.value.contains(listingId)
                
                if (isInWishlist) {
                    val result = wishlistRepository.removeFromWishlist(listingId)
                    if (result.isFailure) {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to remove from wishlist"
                    }
                } else {
                    val result = wishlistRepository.addToWishlist(listingId)
                    if (result.isFailure) {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to add to wishlist"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isListingInWishlist(listingId: String): Boolean {
        return wishlistItems.value.contains(listingId)
    }

    fun clearError() {
        _error.value = null
    }
}