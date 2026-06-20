package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest as CoilImageRequest
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Image tab types for photo gallery
enum class ImageTab {
    Sightseeing, Hotel, Video
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedListingDetailScreen(
    listing: TravelListing,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onBookNow: () -> Unit,
    onWishlistToggle: () -> Unit,
    isWishlisted: Boolean = false
) {
    val scrollState = rememberScrollState()
    var activeImageTab by remember { mutableStateOf<ImageTab>(ImageTab.Sightseeing) }
    var expandedDays by remember { mutableStateOf(mutableSetOf<Int>()) }
    var expandedFAQs by remember { mutableStateOf(mutableSetOf<Int>()) }
    var showAllPhotos by remember { mutableStateOf(false) }

    val isInternational = listing.packageType == "international"
    val currency = if (isInternational) "$" else "₹"

    Scaffold(
        topBar = {
            EnhancedTopAppBar(
                listing = listing,
                onBack = onBack,
                onChatClick = onChatClick,
                onWishlistToggle = onWishlistToggle,
                isWishlisted = isWishlisted
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Photo Gallery Section
            PhotoGallerySection(
                listing = listing,
                activeImageTab = activeImageTab,
                onImageTabChange = { activeImageTab = it },
                onShowAllPhotos = { showAllPhotos = true }
            )

            // Package Summary Bar with Pricing
            ModernPackageSummaryBar(
                listing = listing,
                currency = currency
            )

            // Main Content
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Itinerary Section
                if (listing.itinerary.isNotEmpty()) {
                    ExpandableSection(
                        title = "Itinerary",
                        icon = Icons.Default.DateRange,
                        content = {
                            ItinerarySection(
                                itinerary = listing.itinerary,
                                expandedDays = expandedDays,
                                onDayToggle = { day ->
                                    expandedDays = expandedDays.toMutableSet().apply {
                                        if (contains(day)) remove(day) else add(day)
                                    }
                                }
                            )
                        }
                    )
                }

                // Accommodation Details
                AccommodationSection(listing = listing)

                // Tour Inclusions
                if (listing.inclusions.isNotEmpty()) {
                    ExpandableSection(
                        title = "Tour Inclusion Details",
                        icon = Icons.Default.CheckCircle,
                        content = {
                            InclusionsSection(inclusions = parseList(listing.inclusions))
                        }
                    )
                }

                // Tour Exclusions
                if (listing.exclusions.isNotEmpty()) {
                    ExpandableSection(
                        title = "Tour Exclusion Details",
                        icon = Icons.Default.Close,
                        content = {
                            ExclusionsSection(exclusions = parseList(listing.exclusions))
                        }
                    )
                }

                // FAQ Section
                if (listing.faqs.isNotEmpty()) {
                    ExpandableSection(
                        title = "More Frequent Questions",
                        icon = Icons.Default.Info,
                        content = {
                            FAQSection(
                                faqs = listing.faqs,
                                expandedFAQs = expandedFAQs,
                                onFAQToggle = { index ->
                                    expandedFAQs = expandedFAQs.toMutableSet().apply {
                                        if (contains(index)) remove(index) else add(index)
                                    }
                                }
                            )
                        }
                    )
                }

                // Package Highlights
                PackageHighlightsSection()

                // Agency Info
                AgencyInfoSection(listing = listing, onChatClick = onChatClick)

                // Action Buttons
                ModernActionButtonsSection(
                    listing = listing,
                    onChatClick = onChatClick,
                    onBack = onBack,
                    currency = currency
                )
            }
        }
    }

    // Photo Gallery Modal
    if (showAllPhotos) {
        PhotoGalleryModal(
            listing = listing,
            onDismiss = { showAllPhotos = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedTopAppBar(
    listing: TravelListing,
    onBack: () -> Unit,
    onChatClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    isWishlisted: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1F26))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button and title
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = listing.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    if (listing.packageCode.isNotEmpty()) {
                        Text(
                            text = "Code: ${listing.packageCode}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Share
                IconButton(onClick = { /* Share functionality */ }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Compare
                IconButton(onClick = { /* Compare functionality */ }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Compare",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Wishlist
                IconButton(onClick = onWishlistToggle) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Breadcrumb and Rating
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val location = listing.countryName ?: listing.stateName ?: listing.destination
                Text(
                    text = location,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Rating
            if (listing.rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${listing.rating} (${listing.reviewsCount})",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun PhotoGallerySection(
    listing: TravelListing,
    activeImageTab: ImageTab,
    onImageTabChange: (ImageTab) -> Unit,
    onShowAllPhotos: () -> Unit
) {
    val allImages = getAllImages(listing)
    val mainImage = allImages.firstOrNull()

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Main Photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (mainImage != null) {
                AsyncImage(
                    model = CoilImageRequest.Builder(LocalContext.current)
                        .data(mainImage)
                        .crossfade(true)
                        .size(1080, 720)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    error = painterResource(R.drawable.ic_launcher_background)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "No image", modifier = Modifier.size(64.dp).alpha(0.3f))
                }
            }

            // Agency badge on image
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = listing.agencyName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            if (allImages.size > 1) {
                Surface(
                    onClick = onShowAllPhotos,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "+${allImages.size - 1} Photos",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernPackageSummaryBar(
    listing: TravelListing,
    currency: String
) {
    val itineraryDays = listing.placesCovered?.size ?: listing.duration
    val nights = if (itineraryDays > 0) itineraryDays - 1 else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = listing.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Package Type and other tags
                Surface(
                    color = if (listing.packageType == "international") Color(0xFFDBEAFE) else Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (listing.packageType == "international") "International" else "Domestic",
                        fontSize = 11.sp,
                        color = if (listing.packageType == "international") Color(0xFF1E40AF) else Color(0xFF065F46),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Duration
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${itineraryDays}D / ${nights}N",
                        fontSize = 11.sp,
                        color = Color(0xFF374151),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Places Count
                if (listing.placesCovered.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${listing.placesCovered.size} Places",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ItinerarySection(
    itinerary: List<com.example.mychat.data.model.ItineraryDay>,
    expandedDays: Set<Int>,
    onDayToggle: (Int) -> Unit
) {
    itinerary.forEach { day ->
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDayToggle(day.day) }
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D${day.day}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF2563EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Day ${day.day}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = day.placeName,
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (expandedDays.contains(day.day)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }

            if (expandedDays.contains(day.day)) {
                Column(
                    modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = day.description,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF4B5563)
                    )
                    if (day.accommodation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(14.dp)
                    )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Staying at: ${day.accommodation}",
                                    color = Color(0xFF6B7280),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccommodationSection(listing: TravelListing) {
    ExpandableSection(
        title = "Accommodation Details",
        icon = Icons.Default.Star
    ) {
        listing.placesCovered.forEachIndexed { index, place ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(4) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "4 Star Properties",
                            color = Color(0xFF6B7280),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Text(
                    text = "1 Night",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }
            if (index < listing.placesCovered.size - 1) {
                Divider(modifier = Modifier.padding(start = 36.dp))
            }
        }
    }
}

@Composable
fun InclusionsSection(inclusions: List<String>) {
    inclusions.forEach { item ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = item, fontSize = 13.sp)
        }
    }
}

@Composable
fun ExclusionsSection(exclusions: List<String>) {
    exclusions.forEach { item ->
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = item, fontSize = 13.sp)
        }
    }
}

@Composable
fun FAQSection(
    faqs: List<com.example.mychat.data.model.FAQ>,
    expandedFAQs: Set<Int>,
    onFAQToggle: (Int) -> Unit
) {
    faqs.forEachIndexed { index, faq ->
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFAQToggle(index) }
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (expandedFAQs.contains(index)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            }

            if (expandedFAQs.contains(index)) {
                Text(
                    text = faq.answer,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF4B5563)
                )
            }
        }
    }
}

@Composable
fun PackageHighlightsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Package Highlights",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                "Guided tours with expert local guides",
                "Premium accommodation throughout",
                "All transfers and transportation",
                "24/7 customer support"
            ).forEach { highlight ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = highlight, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AgencyInfoSection(listing: TravelListing, onChatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFDBEAFE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Agency",
                    fontSize = 24.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.agencyName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (listing.agencyData?.approved == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Verified",
                                color = Color(0xFF065F46),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onChatClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Chat",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Chat", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun PricingBreakdownSection(
    listing: TravelListing,
    currency: String
) {
    val price = listing.price.toDouble()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Pricing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Base Price", fontSize = 13.sp, color = Color(0xFF6B7280))
                Text("$currency${listing.price}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Service Fee (5%)", fontSize = 13.sp, color = Color(0xFF6B7280))
                Text("$currency${"%.2f".format(price * 0.05)}", fontSize = 13.sp, color = Color(0xFF6B7280))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total per person",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$currency${"%.2f".format(price * 1.05)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
fun ModernActionButtonsSection(
    listing: TravelListing,
    onChatClick: () -> Unit,
    onBack: () -> Unit,
    currency: String
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // Chat
        OutlinedButton(
            onClick = onChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Filled.Email,
                contentDescription = "Chat",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Chat with ${listing.agencyName}",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Back
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back to Listings", color = Color(0xFF6B7280))
        }
    }
}

@Composable
fun PhotoGalleryModal(
    listing: TravelListing,
    onDismiss: () -> Unit
) {
    val allImages = getAllImages(listing)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Photo Gallery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(allImages) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = CoilImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .size(1080, 1080)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_launcher_background),
                                error = painterResource(R.drawable.ic_launcher_background)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper functions
fun getAllImages(listing: TravelListing): List<String> {
    val images = mutableListOf<String>()

    // Add main photos
    images.addAll(listing.photos)

    // Add photo URLs
    images.addAll(listing.photoUrls)

    // Add place covered images
    listing.placesCovered.forEach { place ->
        images.addAll(place.imageUrls)
    }

    return images.distinct()
}

fun parseList(text: String): List<String> {
    if (text.isEmpty()) return emptyList()
    return text.split('\n', ',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}