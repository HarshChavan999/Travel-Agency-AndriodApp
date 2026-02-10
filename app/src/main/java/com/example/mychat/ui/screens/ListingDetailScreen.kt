package com.example.mychat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: TravelListing,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onBookNow: () -> Unit,
    onWishlistToggle: (() -> Unit)? = null,
    isWishlisted: Boolean = false
) {
    // Use enhanced screen if enhanced features are available
    if (listing.itinerary.isNotEmpty() || 
        listing.inclusions.isNotEmpty() || 
        listing.exclusions.isNotEmpty() || 
        listing.faqs.isNotEmpty() ||
        listing.packageCode.isNotEmpty() ||
        listing.photoUrls.isNotEmpty()) {
        
        EnhancedListingDetailScreen(
            listing = listing,
            onBack = onBack,
            onChatClick = onChatClick,
            onBookNow = onBookNow,
            onWishlistToggle = onWishlistToggle ?: {},
            isWishlisted = isWishlisted
        )
    } else {
        // Fallback to basic screen for listings without enhanced data
        BasicListingDetailScreen(
            listing = listing,
            onBack = onBack,
            onChatClick = onChatClick,
            onBookNow = onBookNow
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicListingDetailScreen(
    listing: TravelListing,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onBookNow: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listing.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onChatClick) {
                        Icon(Icons.Filled.Email, contentDescription = "Chat with Agency")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Gallery
            if (listing.photos.isNotEmpty()) {
                AsyncImage(
                    model = listing.photos.first(),
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏖️", style = MaterialTheme.typography.displayLarge)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title and Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (listing.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${listing.rating}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " (${listing.reviewsCount})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Agency info
                Text(
                    text = "By ${listing.agencyName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Package Type and Basic Info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Package Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Package Type with Country/State
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (listing.packageType) {
                                    "international" -> "🌍 International"
                                    "domestic" -> "🏠 Domestic"
                                    else -> "✈️ Package"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (listing.packageType) {
                                    "international" -> listing.countryName ?: "Country not specified"
                                    "domestic" -> listing.stateName ?: "State not specified"
                                    else -> listing.destination
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Basic Package Info
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = listing.type.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Type",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${listing.duration}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val displayPrice = if (listing.cost != null) {
                                    listing.cost!!.toInt()
                                } else {
                                    listing.price.toInt()
                                }
                                Text(
                                    text = "$${displayPrice}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Price",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Places Covered
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Places Covered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (listing.placesCovered != null && listing.placesCovered.isNotEmpty()) {
                            Text(
                                text = listing.placesCovered.joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Text(
                                text = "No specific places mentioned",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Accommodation Details
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Accommodation Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hotel Type
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏨 Hotel Type:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = listing.hotelType?.replaceFirstChar { it.uppercase() } ?: "Not specified",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (listing.hotelType != null) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (listing.hotelType != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Meal Plan
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🍽️ Meal Plan:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = listing.mealPlan?.replaceFirstChar { it.uppercase() } ?: "Not specified",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (listing.mealPlan != null) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (listing.mealPlan != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Destination
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Destination",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = listing.destination,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = listing.description,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onBookNow,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        val displayPrice = if (listing.cost != null) {
                            listing.cost!!.toInt()
                        } else {
                            listing.price.toInt()
                        }
                        Text(
                            "Book Now - $${displayPrice}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedButton(
                        onClick = onChatClick,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = "Chat",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Chat with ${listing.agencyName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
