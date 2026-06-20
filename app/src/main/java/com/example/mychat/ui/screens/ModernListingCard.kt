package com.example.mychat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest as CoilImageRequest
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ModernListingCard(
    listing: TravelListing,
    modifier: Modifier = Modifier,
    onListingClick: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistToggle: (() -> Unit)? = null,
    onCompareToggle: (() -> Unit)? = null,
    isWishlisted: Boolean = false,
    isInComparison: Boolean = false,
    canCompareMore: Boolean = true
) {
    var isFavorite by remember(isWishlisted) { mutableStateOf(isWishlisted) }
    var showCompareToast by remember { mutableStateOf(false) }
    var compareToastMessage by remember { mutableStateOf("") }
    var currentImageIndex by remember { mutableIntStateOf(0) }
    var imageLoaded by remember { mutableStateOf(false) }
    var imageError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isWishlisted) { isFavorite = isWishlisted }

    // Compute derived values
    val duration = listing.itinerary.size
    val nights = if (duration > 1) duration - 1 else 0
    val price = listing.cost ?: listing.price
    val packageType = if (listing.packageType == "international") "International" else "Domestic"
    val currencySymbol = if (listing.packageType == "international") "$" else "₹"
    val location = if (listing.packageType == "international")
        (listing.countryName ?: "Country not specified")
    else
        (listing.stateName ?: "State not specified")

    val packageCode = if (listing.packageCode.isNotEmpty()) listing.packageCode
    else if (listing.id.length >= 4) listing.id.takeLast(4).uppercase()
    else "1045"

    val pickupLocation = if (listing.placesCovered.isNotEmpty()) listing.placesCovered[0].name.trim()
    else listing.stateName ?: "Delhi"
    val dropLocation = if (listing.placesCovered.size > 1) listing.placesCovered.last().name.trim()
    else listing.stateName ?: "Delhi"

    val cardTitle = (if (listing.packageType == "international") listing.countryName else listing.stateName)
        ?: listing.title

    val placesText = if (listing.placesCovered.isNotEmpty())
        listing.placesCovered.joinToString(" | ") { it.name.trim() }
    else location

    // Collect all images
    val allImages = remember(listing) {
        buildList {
            listing.placesCovered.forEach { place ->
                place.imageUrls.forEach { url -> add(url) }
            }
            listing.photos.forEach { url ->
                if (!contains(url)) add(url)
            }
            listing.photoUrls.forEach { url ->
                if (!contains(url)) add(url)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // === FULL WIDTH IMAGE ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                when {
                    allImages.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "📸", fontSize = 32.sp)
                        }
                    }
                    allImages.size > 1 -> {
                        // Carousel mode
                        AsyncImage(
                            model = CoilImageRequest.Builder(LocalContext.current)
                                .data(allImages[currentImageIndex])
                                .crossfade(300)
                                .size(480, 360)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "$cardTitle photo ${currentImageIndex + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Left arrow
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .align(Alignment.CenterStart)
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                .clickable(enabled = true) {
                                    currentImageIndex = if (currentImageIndex == 0) allImages.size - 1 else currentImageIndex - 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", modifier = Modifier.size(16.dp), tint = Color(0xFF374151))
                        }

                        // Right arrow
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .align(Alignment.CenterEnd)
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                .clickable(enabled = true) {
                                    currentImageIndex = if (currentImageIndex == allImages.size - 1) 0 else currentImageIndex + 1
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next", modifier = Modifier.size(16.dp), tint = Color(0xFF374151))
                        }

                        // Dot indicators
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            allImages.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .height(5.dp)
                                        .width(if (idx == currentImageIndex) 12.dp else 5.dp)
                                        .background(
                                            if (idx == currentImageIndex) Color.White else Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        }
                    }
                    else -> {
                        AsyncImage(
                            model = CoilImageRequest.Builder(LocalContext.current)
                                .data(allImages[0])
                                .crossfade(300)
                                .size(480, 360)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = cardTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Compare Toast overlay
                if (showCompareToast) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1F2937),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = compareToastMessage,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Package type badge on image
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = Color(0xFFBEE5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = packageType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF084298),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Verified badge on image
                if (listing.agencyData?.approved != null && listing.agencyData!!.approved) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Text(
                            text = "✅ Verified",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // === CONTENT BELOW IMAGE ===
            Column(modifier = Modifier.padding(16.dp)) {
                // Badges row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tour category badge
                    if (listing.tourCategories.isNotEmpty()) {
                        val catColor = if (listing.tourCategories.any { it.contains("Luxury", ignoreCase = true) })
                            Pair(Color(0xFFE2E3E5), Color(0xFF4F4F4F))
                        else
                            Pair(Color(0xFFFFE0B2), Color(0xFFE65100))

                        Surface(color = catColor.first, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = listing.tourCategories.first(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = catColor.second,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(color = Color(0xFFFFE0B2), shape = RoundedCornerShape(12.dp)) {
                            Text(text = "Family Tour", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                        Surface(color = Color(0xFFE2E3E5), shape = RoundedCornerShape(12.dp)) {
                            Text(text = "Luxury", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F4F4F), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }

                    // Package code badge
                    Surface(color = Color(0xFFCFD8DC), shape = RoundedCornerShape(12.dp)) {
                        Text(text = "$packageCode", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = cardTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Places text
                Text(
                    text = placesText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Star Ratings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (s in 1..5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (s <= (if (listing.rating.isNaN()) 5 else listing.rating.toInt()))
                                Color(0xFF0D6EFD) else Color(0xFFE5E7EB)
                        )
                    }
                    Text(text = "Google Rating", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444), modifier = Modifier.padding(start = 6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Icons row - SightSeeing, Transport, Hotel Stay, Meal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconBadgeItem(icon = "📸", label = "SightSeeing")
                    IconBadgeItem(icon = "🚌", label = "Transport")
                    IconBadgeItem(icon = "🛏️", label = "Hotel Stay")
                    IconBadgeItem(icon = "🍽️", label = "Meal")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Divider
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                Spacer(modifier = Modifier.height(10.dp))

                // 3-column grid: Stay, Pick-up, Drop
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Stay", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                            Text(text = "${duration}D | ${nights}N", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Pick-up", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                            Text(text = pickupLocation, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "Drop", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                            Text(text = dropLocation, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Highlight Box (Blue)
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // EMI & Pricing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Interest free EMI", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                                Text(text = "Available", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                            }
                            if (price != null && price > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Starting Price", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                                    Text(text = "$currencySymbol${price.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D6EFD))
                                }
                            } else {
                                Text(text = "Contact Agent\nfor Pricing", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0D6EFD), textAlign = TextAlign.End)
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Compare button
                            Box(modifier = Modifier.weight(1f)
                                .border(1.dp, Color(0xFF0D6EFD), RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable {
                                    when {
                                        isInComparison -> { compareToastMessage = "Already added!"; showCompareToast = true }
                                        !canCompareMore -> { compareToastMessage = "Max 3 allowed!"; showCompareToast = true }
                                        else -> { compareToastMessage = "Added to compare!"; onCompareToggle?.invoke() }
                                    }
                                    scope.launch { delay(2000); showCompareToast = false }
                                }
                                .padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = if (isInComparison) Icons.Filled.CheckCircle else Icons.Filled.Scale,
                                        contentDescription = "Compare", modifier = Modifier.size(13.dp), tint = Color(0xFF0D6EFD))
                                    Text(text = if (isInComparison) "Added" else "Compare", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D6EFD))
                                }
                            }

                            // Wishlist button
                            Box(modifier = Modifier.weight(1f)
                                .border(1.dp, Color(0xFF0D6EFD), RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .clickable { isFavorite = !isFavorite; onWishlistToggle?.invoke() }
                                .padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(imageVector = Icons.Filled.FavoriteBorder, contentDescription = "Wishlist",
                                        modifier = Modifier.size(13.dp), tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF0D6EFD))
                                    Text(text = "Wishlist", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D6EFD))
                                }
                            }

                            // View Itinerary button (amber)
                            Box(modifier = Modifier.weight(1.5f)
                                .background(Color(0xFFFFA000), RoundedCornerShape(12.dp))
                                .clickable { onListingClick() }
                                .padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                                Text(text = "View Itinerary", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconBadgeItem(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFFFF1F2), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
    }
}