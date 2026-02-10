package com.example.mychat.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing
import com.example.mychat.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    wishlistViewModel: WishlistViewModel,
    onListingClick: (TravelListing) -> Unit,
    onChatClick: (TravelListing) -> Unit,
    onBack: () -> Unit
) {
    val wishlistListings by wishlistViewModel.wishlistListings.collectAsState()
    val isLoading by wishlistViewModel.isLoading.collectAsState()
    val error by wishlistViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Your Wishlist",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { /* Retry logic */ }) {
                            Text("Retry")
                        }
                    }
                }
            } else if (wishlistListings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Empty Wishlist",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your wishlist is empty",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add travel packages to your wishlist by clicking the heart icon on any listing.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Browse Packages")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Saved for later",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(wishlistListings) { listing ->
                        WishlistItemCard(
                            listing = listing,
                            onListingClick = { onListingClick(listing) },
                            onChatClick = { onChatClick(listing) },
                            onWishlistToggle = {
                                wishlistViewModel.toggleWishlist(listing.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    listing: TravelListing,
    onListingClick: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onListingClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (listing.photos.isNotEmpty()) {
                    AsyncImage(
                        model = listing.photos.first(),
                        contentDescription = listing.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏖️", style = MaterialTheme.typography.displayLarge)
                    }
                }

                // Package Type Badge
                listing.packageType?.let { packageType ->
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        color = if (packageType == "international") Color(0xFF3B82F6) else Color(0xFF10B981),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (packageType == "international") "🌍 International" else "🏠 Domestic",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Remove from Wishlist Button
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    IconButton(
                        onClick = onWishlistToggle,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Remove from wishlist",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title and Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Package Type and Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Package Type
                            if (listing.packageType != null) {
                                Text(
                                    text = if (listing.packageType == "international") 
                                        "🌍 International" 
                                        else "🏠 Domestic",
                                    fontSize = 12.sp,
                                    color = if (listing.packageType == "international") Color(0xFF3B82F6) else Color(0xFF10B981)
                                )
                                Text("•", color = Color(0xFF6B7280))
                            }
                            
                            // Country/State
                            val location = listing.countryName ?: listing.stateName ?: listing.destination
                            Text(
                                text = location,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val displayPrice = run {
                            val price = listing.cost ?: listing.price
                            val currency = if (listing.packageType == "international") "$" else "₹"
                            "${currency}${price.toInt()}"
                        }
                        
                        Text(
                            text = displayPrice,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "per person",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = listing.description,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Rating
                    DetailInfo(
                        icon = Icons.Default.Star,
                        text = "${listing.rating} (${listing.reviewsCount})",
                        iconTint = Color(0xFFFBBF24)
                    )
                    
                    // Duration
                    val itineraryDays = listing.placesCovered?.size ?: listing.duration
                    val nights = if (itineraryDays > 0) itineraryDays - 1 else 0
                    DetailInfo(
                        icon = Icons.Default.DateRange,
                        text = "${itineraryDays} days / ${nights} nights"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color(0xFFE5E7EB))

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onListingClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Details")
                    }

                    Button(
                        onClick = onChatClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Chat",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    iconTint: Color = Color(0xFF6B7280)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = iconTint
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}