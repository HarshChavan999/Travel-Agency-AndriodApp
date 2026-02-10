package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing
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

            // Package Summary Bar
            PackageSummaryBar(listing = listing)

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
                ActionButtonsSection(
                    listing = listing,
                    onBookNow = onBookNow,
                    onChatClick = onChatClick,
                    onBack = onBack
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
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        // Breadcrumb Navigation
        BreadcrumbNavigation(listing = listing)

        Spacer(modifier = Modifier.height(8.dp))

        // Title and Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Package Code
                if (listing.packageCode.isNotEmpty()) {
                    Text(
                        text = "Code: ${listing.packageCode}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Share functionality */ }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { /* Compare functionality */ }) {
                    Icon(
                        Icons.Filled.List,
                        contentDescription = "Compare",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onWishlistToggle) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Color(0xFFFFC107) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rating and Agency
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (listing.rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${listing.rating} (${listing.reviewsCount})",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
            
            Text(
                text = "By ${listing.agencyName}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun BreadcrumbNavigation(listing: TravelListing) {
    val breadcrumb = buildBreadcrumb(listing)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        breadcrumb.forEachIndexed { index, (text, isLast) ->
            Text(
                text = text,
                fontSize = 12.sp,
                color = if (isLast) Color.White else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
            )
            if (!isLast) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun buildBreadcrumb(listing: TravelListing): List<Pair<String, Boolean>> {
    val parts = mutableListOf("Home")
    
    if (listing.packageType == "domestic") {
        parts.add("Domestic")
        if (listing.stateName != null) parts.add(listing.stateName)
    } else {
        parts.add("International")
        if (listing.countryName != null) parts.add(listing.countryName)
    }
    
    parts.add(listing.title)
    
    return parts.mapIndexed { index, text ->
        text to (index == parts.size - 1)
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
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (mainImage != null) {
                AsyncImage(
                    model = mainImage,
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
            
            if (allImages.size > 1) {
                Button(
                    onClick = onShowAllPhotos,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f))
                ) {
                    Text("+${allImages.size - 1} Photos")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ImageTab.values().forEach { tab ->
                val isSelected = tab == activeImageTab
                val iconColor = if (isSelected) Color.White else Color.Gray
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onImageTabChange(tab) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when(tab) {
                                ImageTab.Sightseeing -> Icons.Default.Star
                                ImageTab.Hotel -> Icons.Default.Star
                                ImageTab.Video -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when(tab) {
                                ImageTab.Sightseeing -> "Sightseeing"
                                ImageTab.Hotel -> "Hotel"
                                ImageTab.Video -> "Video"
                            },
                            fontSize = 12.sp,
                            color = iconColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (tab == ImageTab.Sightseeing) {
                            Text(
                                text = "${allImages.size} photos",
                                fontSize = 10.sp,
                                color = iconColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackageSummaryBar(listing: TravelListing) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryItem(
                icon = Icons.Default.DateRange,
                label = "Duration",
                value = "${listing.duration}D / ${listing.duration - 1}N"
            )
            SummaryItem(
                icon = Icons.Default.Place,
                label = "Places",
                value = "${listing.placesCovered.size} Cities"
            )
            SummaryItem(
                icon = Icons.Default.Star,
                label = "Hotel Type",
                value = listing.hotelType ?: "Standard"
            )
            SummaryItem(
                icon = Icons.Default.Star,
                label = "Starting From",
                value = "${if (listing.packageType == "international") "$" else "₹"}${(listing.cost ?: listing.price).toInt()}"
            )
        }
    }
}

@Composable
fun SummaryItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
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
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    fontSize = 18.sp,
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
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "D${day.day}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Day ${day.day}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = day.placeName,
                            color = Color.Gray
                        )
                    }
                }
                Icon(
                    imageVector = if (expandedDays.contains(day.day)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            if (expandedDays.contains(day.day)) {
                Column(modifier = Modifier.padding(start = 44.dp, top = 8.dp, bottom = 12.dp)) {
                    Text(
                        text = day.description,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
                    )
                    if (day.accommodation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Staying at: ${day.accommodation}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
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
                    .padding(vertical = 8.dp),
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
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(4) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "4 Star Properties",
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Text(
                    text = "1 Night",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            if (index < listing.placesCovered.size - 1) {
                Divider(modifier = Modifier.padding(horizontal = 36.dp))
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item)
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
                tint = Color(0xEF4444),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item)
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
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expandedFAQs.contains(index)) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            if (expandedFAQs.contains(index)) {
                Text(
                    text = faq.answer,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                        .fillMaxWidth(),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
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
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Package Highlights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
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
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = highlight)
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
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏢",
                    fontSize = 24.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.agencyName,
                    fontWeight = FontWeight.Bold
                )
                if (listing.agencyData?.approved == true) {
                    Text(
                        text = "✅ Verified",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp
                    )
                }
            }
            Button(
                onClick = onChatClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Filled.Email,
                    contentDescription = "Chat",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chat with Agency")
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    listing: TravelListing,
    onBookNow: () -> Unit,
    onChatClick: () -> Unit,
    onBack: () -> Unit
) {
    val price = listing.cost ?: listing.price
    val currency = if (listing.packageType == "international") "$" else "₹"
    
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Starting from",
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "${currency}${price.toInt()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "per person",
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onBookNow,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Book Now")
                }
            }
        }
        
        OutlinedButton(
            onClick = onChatClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                Icons.Filled.Email,
                contentDescription = "Chat",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with ${listing.agencyName}")
        }
        
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Listings")
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
                .fillMaxHeight(0.8f)
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
                        fontSize = 20.sp,
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
                                .height(200.dp)
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
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