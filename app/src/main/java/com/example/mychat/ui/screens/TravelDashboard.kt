package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest as CoilImageRequest
import com.example.mychat.R
import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.User
import com.example.mychat.viewmodel.WishlistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Destination(
    val name: String,
    val country: String,
    val imageUrl: String,
    val price: Int,
    val duration: String,
    val rating: Float,
    val reviews: Int,
    val groupSize: String,
    val description: String,
    val featured: Boolean = false,
    val travelListing: TravelListing? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelDashboard(
    currentUser: User,
    listings: List<TravelListing>,
    isLoading: Boolean,
    onListingClick: (TravelListing) -> Unit,
    onChatClick: (TravelListing) -> Unit,
    onWishlistClick: (TravelListing) -> Unit,
    onWishlistNavigate: () -> Unit,
    onBookingsNavigate: () -> Unit = {},
    onProfileNavigate: () -> Unit = {},
    onSignOut: () -> Unit,
    wishlistViewModel: WishlistViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val comparisonList = remember { mutableStateListOf<TravelListing>() }
    var showComparison by remember { mutableStateOf(false) }
    var pincode by remember { mutableStateOf("Pincode 400605") }

    val destinations = remember(listings) {
        listings.map { listing ->
            Destination(
                name = listing.title,
                country = listing.agencyName,
                imageUrl = listing.photos.firstOrNull() ?: "",
                price = listing.price.toInt(),
                duration = "${listing.duration} Days",
                rating = listing.rating.toFloat(),
                reviews = listing.reviewsCount,
                groupSize = "2-10 people",
                description = listing.description,
                featured = false,
                travelListing = listing
            )
        }
    }

    val filteredDestinations = remember(destinations, searchQuery) {
        if (searchQuery.isBlank()) destinations
        else destinations.filter { dest ->
            val query = searchQuery.lowercase()
            dest.name.lowercase().contains(query) ||
            dest.country.lowercase().contains(query) ||
            dest.description.lowercase().contains(query) ||
            dest.travelListing?.let { listing ->
                listing.stateName?.lowercase()?.contains(query) == true ||
                listing.countryName?.lowercase()?.contains(query) == true ||
                listing.packageType?.lowercase()?.contains(query) == true ||
                listing.type?.lowercase()?.contains(query) == true
            } == true
        }
    }

    TravelAgencyTheme {
        Scaffold(
            topBar = {
                CleanHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSignOut = onSignOut,
                    userName = currentUser.displayName
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onWishlistClick = onWishlistNavigate,
                    onBookingsClick = onBookingsNavigate,
                    onProfileClick = onProfileNavigate
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                if (comparisonList.isNotEmpty() && !showComparison) {
                    ComparisonBar(
                        comparisonList = comparisonList,
                        onCompareNow = { showComparison = true },
                        onClear = { comparisonList.clear() }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    item { HeroBanner() }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Popular Destinations",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Explore amazing places",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    } else if (filteredDestinations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "" else "",
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No results found for \"$searchQuery\""
                                               else "No travel packages available yet.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredDestinations) { destination ->
                            ModernDestinationCard(
                                destination = destination,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                onListingClick = { destination.travelListing?.let { onListingClick(it) } },
                                onChatClick = { destination.travelListing?.let { onChatClick(it) } },
                                onWishlistToggle = { destination.travelListing?.let { onWishlistClick(it) } },
                                onCompareToggle = { destination.travelListing?.let { listing ->
                                    if (comparisonList.contains(listing)) {
                                        comparisonList.remove(listing)
                                    } else if (comparisonList.size < 3) {
                                        comparisonList.add(listing)
                                    }
                                } },
                                isWishlisted = destination.travelListing?.let { wishlistViewModel.isListingInWishlist(it.id) } ?: false,
                                isInComparison = destination.travelListing?.let { comparisonList.contains(it) } ?: false,
                                canCompareMore = comparisonList.size < 3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF2B58C4), Color(0xFF407BFF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFFDB813),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(
                    text = "BEST TRAVEL AGENTS AT ONE PLACE",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Text(
                text = "BOOK YOUR TOUR WITH US",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Domestic Tour | International tour",
                fontSize = 11.sp,
                color = Color(0xFFBFDBFE),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSignOut: () -> Unit,
    userName: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1C1F26))
    ) {
        // Simple clean row: Logo + Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "BOM", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp)
                Text(text = "TRA", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF97316), letterSpacing = 1.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sign Out link
                Text(
                    text = "Sign Out",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier
                        .clickable { onSignOut() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                // Profile avatar
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color(0xFF374151)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.take(1).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search your Holiday Destination",
                        color = Color(0xFF9CA3AF),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ComparisonBar(
    comparisonList: List<TravelListing>,
    onCompareNow: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Comparing ${comparisonList.size} of 3 packages", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E40AF))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    comparisonList.forEach { pkg ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Text(
                                text = pkg.title.take(15) + if (pkg.title.length > 15) "..." else "",
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onCompareNow,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Compare", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Clear", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CategoryChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Beach", "City", "Mountain", "Island", "Adventure")
    Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 1.dp) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(text = category, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF3F4F6),
                        labelColor = Color(0xFF374151)
                    )
                )
            }
        }
    }
}

@Composable
fun ModernDestinationCard(
    destination: Destination,
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(isWishlisted) { isFavorite = isWishlisted }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                if (destination.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = CoilImageRequest.Builder(LocalContext.current)
                            .data(destination.imageUrl)
                            .crossfade(true)
                            .size(480, 360)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = destination.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_launcher_background),
                        error = painterResource(R.drawable.ic_launcher_background)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, contentDescription = "No image", modifier = Modifier.size(64.dp).alpha(0.3f))
                    }
                }

                destination.travelListing?.packageType?.let { packageType ->
                    Surface(
                        modifier = Modifier.padding(6.dp).align(Alignment.TopStart),
                        color = if (packageType == "international") Color(0xFF3B82F6) else Color(0xFF10B981),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (packageType == "international") "Intl" else "Dom",
                                fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.padding(6.dp).align(Alignment.BottomStart),
                    color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = destination.travelListing?.agencyName?.take(15) ?: "Unknown",
                        fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    modifier = Modifier.padding(6.dp).align(Alignment.TopEnd),
                    color = Color.White.copy(alpha = 0.9f), shape = CircleShape
                ) {
                    IconButton(onClick = {
                        when {
                            isInComparison -> { compareToastMessage = "Already added!"; showCompareToast = true }
                            !canCompareMore -> { compareToastMessage = "Max 3 allowed!"; showCompareToast = true }
                            else -> { compareToastMessage = "Added to compare!"; showCompareToast = true; onCompareToggle?.invoke() }
                        }
                        scope.launch { delay(1500); showCompareToast = false }
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (isInComparison) Icons.Filled.CheckCircle else Icons.Filled.Add,
                            contentDescription = "Compare",
                            tint = if (isInComparison) Color(0xFF2563EB) else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (showCompareToast) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 6.dp),
                        color = Color(0xFF1F2937), shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = compareToastMessage, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Surface(
                    modifier = Modifier.padding(end = 40.dp, top = 6.dp).align(Alignment.TopEnd).size(28.dp),
                    color = Color.White.copy(alpha = 0.9f), shape = CircleShape
                ) {
                    IconButton(onClick = { isFavorite = !isFavorite; onWishlistToggle?.invoke() }, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF6B7280),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = destination.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(12.dp))
                            val location = destination.travelListing?.let { listing -> listing.countryName ?: listing.stateName ?: destination.country } ?: destination.country
                            Text(text = location, fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = destination.description, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    destination.travelListing?.let { listing ->
                        DetailItem(icon = Icons.Default.Star, text = "${listing.rating} (${listing.reviewsCount})", iconTint = Color(0xFFFBBF24))
                        val itineraryDays = listing.placesCovered?.size ?: listing.duration
                        val nights = if (itineraryDays > 0) itineraryDays - 1 else 0
                        DetailItem(icon = Icons.Default.DateRange, text = "${itineraryDays}D / ${nights}N")
                        if (listing.packageType != null) {
                            DetailItem(
                                icon = Icons.Default.Star,
                                text = listing.packageType.replaceFirstChar { it.uppercase() }.take(5),
                                iconTint = if (listing.packageType == "international") Color(0xFF3B82F6) else Color(0xFF10B981)
                            )
                        }
                    } ?: run {
                        DetailItem(icon = Icons.Default.Star, text = "${destination.rating} (${destination.reviews})", iconTint = Color(0xFFFBBF24))
                        DetailItem(icon = Icons.Default.DateRange, text = destination.duration)
                    }
                }

                destination.travelListing?.let { listing ->
                    if (listing.placesCovered != null && listing.placesCovered.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Places Covered:", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color(0xFF9CA3AF))
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            listing.placesCovered.take(3).forEach { place ->
                                Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                                    Text(text = place.name.trim(), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                            }
                            if (listing.placesCovered.size > 3) {
                                Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                                    Text(text = "+${listing.placesCovered.size - 3} more", fontSize = 9.sp, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFE5E7EB))
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onListingClick, modifier = Modifier.weight(1f)) {
                        Text("View", fontSize = 12.sp)
                    }
                    Button(onClick = onChatClick, modifier = Modifier.weight(1f)) {
                        Text("Chat", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, text: String, iconTint: Color = Color(0xFF6B7280)) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = iconTint)
        Text(text = text, fontSize = 10.sp, color = Color(0xFF6B7280))
    }
}

@Composable
fun TravelAgencyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2563EB), onPrimary = Color.White, primaryContainer = Color(0xFFEFF6FF),
            secondary = Color(0xFF64748B), background = Color(0xFFF9FAFB), surface = Color.White,
            onBackground = Color(0xFF111827), onSurface = Color(0xFF111827)
        ), content = content
    )
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onWishlistClick: () -> Unit,
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Explore", Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Wishlist", Icons.Filled.FavoriteBorder, Icons.Outlined.FavoriteBorder),
        BottomNavItem("Bookings", Icons.Filled.DateRange, Icons.Outlined.DateRange),
        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        items.forEachIndexed { index, item ->
            val onClickAction = when (index) {
                2 -> onWishlistClick
                3 -> onBookingsClick
                4 -> onProfileClick
                else -> { { onTabSelected(index) } }
            }
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = onClickAction,
                icon = { Icon(imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
                label = { Text(text = item.label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color(0xFF6B7280), unselectedTextColor = Color(0xFF6B7280), indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelListingCard(listing: TravelListing, onClick: () -> Unit, onChatClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (listing.photos.isNotEmpty()) {
                AsyncImage(
                    model = CoilImageRequest.Builder(LocalContext.current)
                        .data(listing.photos.first())
                        .crossfade(true)
                        .size(480, 360)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_background),
                    error = painterResource(R.drawable.ic_launcher_background)
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.LightGray).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = "No image", modifier = Modifier.size(64.dp).alpha(0.3f)) }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = listing.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (listing.packageType != null) {
                        Text(text = if (listing.packageType == "international") "International" else "Domestic", fontSize = 12.sp, color = if (listing.packageType == "international") Color(0xFF3B82F6) else Color(0xFF10B981))
                        Text("•", color = Color(0xFF6B7280))
                    }
                    val location = listing.countryName ?: listing.stateName ?: listing.destination
                    Text(text = location, fontSize = 12.sp, color = Color(0xFF6B7280))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = listing.type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Text("•", color = Color.Gray)
                    val itineraryDays = listing.placesCovered?.size ?: listing.duration
                    val nights = if (itineraryDays > 0) itineraryDays - 1 else 0
                    Text(text = "${itineraryDays} days / ${nights} nights", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = listing.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "By ${listing.agencyName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (listing.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Text(text = "${listing.rating} (${listing.reviewsCount})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) { Text("View Details") }
                    Button(onClick = onChatClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.Email, contentDescription = "Chat", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chat")
                    }
                }
            }
        }
    }
}