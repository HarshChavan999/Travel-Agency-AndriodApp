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

data class CategoryConfig(
    val id: String,
    val title: String,
    val subcategories: List<String>,
    val linkText: String
)

data class CategoryChipItem(
    val id: String,
    val label: String,
    val type: String, // "categories" or "all"
    val filterId: String? = null,
    val filterSub: String? = null,
    val filterTitle: String? = null
)

data class CategoryFilter(
    val categoryId: String,
    val subcategory: String? = null,
    val title: String
)

val categoriesConfig = listOf(
    CategoryConfig(
        id = "tourCategory",
        title = "Tour by Category",
        subcategories = listOf("Family Tour", "Group Tour", "Fix Departure Tour", "Honeymoon Tour"),
        linkText = "See more"
    ),
    CategoryConfig(
        id = "domestic",
        title = "Domestic Packages",
        subcategories = listOf("Kashmir", "Himachal", "South", "Rajasthan"),
        linkText = "See more"
    ),
    CategoryConfig(
        id = "international",
        title = "International Packages",
        subcategories = listOf("Dubai", "Europe", "Bali", "Turkey"),
        linkText = "Shop now"
    ),
    CategoryConfig(
        id = "trending",
        title = "Trending Destinations",
        subcategories = listOf("Baku", "Singapore", "Leh Ladakh", "Manali"),
        linkText = "See more"
    ),
    CategoryConfig(
        id = "seasons",
        title = "Seasonal Escapes",
        subcategories = listOf("Summer Retreats", "Monsoon Magic", "Winter Wonderland", "Spring Getaways"),
        linkText = "See more"
    ),
    CategoryConfig(
        id = "events",
        title = "Festive & Event Specials",
        subcategories = listOf("New Year & Christmas", "Diwali Specials", "Summer Vacations", "Long Weekend Escapes"),
        linkText = "See more"
    ),
    CategoryConfig(
        id = "experiences",
        title = "Experience Travel",
        subcategories = listOf("Trekking", "Snow Enjoyment", "Adventure", "Water Sports"),
        linkText = "Explore all"
    )
)

val subcategoryDescriptions = mapOf(
    "Family Tour" to "Create Memories with family",
    "Group Tour" to "Bring your group together to travel!",
    "Fix Departure Tour" to "Join groups, make friends!",
    "Honeymoon Tour" to "Make honeymoon memories!",
    "Kashmir" to "Paradise on Earth",
    "Himachal" to "Queen of Hills",
    "South" to "Backwaters & Temples",
    "Rajasthan" to "Land of Kings",
    "Dubai" to "Modern Oasis",
    "Europe" to "Classic Romance",
    "Bali" to "Tropical Heaven",
    "Turkey" to "East meets West",
    "Baku" to "Flame Towers & Caspian Sea",
    "Singapore" to "Lion City Adventure",
    "Leh Ladakh" to "High Mountain Passes",
    "Manali" to "Snowy Peak Escapes",
    "50% Off" to "Super Saver Deals",
    "10% Off" to "Special Season Discount",
    "Packages under 10K" to "Budget friendly tours",
    "Flash Deals" to "Limited time offers",
    "Trekking" to "Mountain Trails",
    "Snow Enjoyment" to "Winter Wonderland",
    "Adventure" to "Thrill seeker choice",
    "Water Sports" to "Beaches & Oceans",
    "Summer Retreats" to "Hill stations & cool escapes",
    "Monsoon Magic" to "Lush green scenic tours",
    "Winter Wonderland" to "Snow peaks & desert camps",
    "Spring Getaways" to "Pleasant sightseeing trips",
    "New Year & Christmas" to "Beach sides & year-end parties",
    "Diwali Specials" to "Heritage tours & palace stays",
    "Summer Vacations" to "Family beach & theme parks",
    "Long Weekend Escapes" to "Quick 2-3 day getaways"
)

fun getSubcategoryImage(
    categoryId: String,
    subcategory: String,
    listings: List<TravelListing>
): String {
    val matched = listings.filter { listing ->
        if (!listing.approved) return@filter false
        when (categoryId) {
            "tourCategory" -> {
                val cats = listing.tourCategories
                when (subcategory) {
                    "Family Tour" -> cats.any { it.equals("Family", ignoreCase = true) }
                    "Group Tour" -> cats.any { it.equals("Friends", ignoreCase = true) || it.equals("Group", ignoreCase = true) }
                    "Fix Departure Tour" -> cats.any { it.equals("Fix Departure", ignoreCase = true) }
                    "Honeymoon Tour" -> cats.any { it.equals("Honeymoon", ignoreCase = true) }
                    else -> false
                }
            }
            "domestic" -> {
                if (listing.packageType != "domestic") return@filter false
                val state = listing.stateName?.lowercase() ?: ""
                when (subcategory) {
                    "Kashmir" -> state.contains("kashmir") || state.contains("jammu")
                    "Himachal" -> state.contains("himachal")
                    "South" -> state.contains("kerala") || state.contains("karnataka") || state.contains("tamil") || state.contains("south") || state.contains("goa") || state.contains("andhra")
                    "Rajasthan" -> state.contains("rajasthan")
                    else -> false
                }
            }
            "international" -> {
                if (listing.packageType != "international") return@filter false
                val country = listing.countryName?.lowercase() ?: ""
                when (subcategory) {
                    "Dubai" -> country.contains("dubai") || country.contains("emirates") || country.contains("uae")
                    "Europe" -> country.contains("europe") || country.contains("switzerland") || country.contains("france") || country.contains("italy") || country.contains("germany") || country.contains("united kingdom") || country.contains("london")
                    "Bali" -> country.contains("bali") || country.contains("indonesia")
                    "Turkey" -> country.contains("turkey")
                    else -> false
                }
            }
            "trending" -> {
                val dest = ((listing.countryName ?: "") + " " + (listing.stateName ?: "") + " " + listing.title).lowercase()
                when (subcategory) {
                    "Baku" -> dest.contains("baku") || dest.contains("azerbaijan")
                    "Singapore" -> dest.contains("singapore")
                    "Leh Ladakh" -> dest.contains("ladakh") || dest.contains("leh")
                    "Manali" -> dest.contains("manali")
                    else -> false
                }
            }
            "seasons" -> {
                val desc = listing.description.lowercase()
                when (subcategory) {
                    "Summer Retreats" -> desc.contains("summer") || desc.contains("cool")
                    "Monsoon Magic" -> desc.contains("monsoon") || desc.contains("rain")
                    "Winter Wonderland" -> desc.contains("winter") || desc.contains("snow")
                    "Spring Getaways" -> desc.contains("spring") || desc.contains("flower")
                    else -> false
                }
            }
            "events" -> {
                val desc = listing.description.lowercase()
                val title = listing.title.lowercase()
                when (subcategory) {
                    "New Year & Christmas" -> desc.contains("new year") || desc.contains("christmas") || title.contains("new year") || title.contains("christmas")
                    "Diwali Specials" -> desc.contains("diwali") || title.contains("diwali")
                    "Summer Vacations" -> desc.contains("summer") || title.contains("summer")
                    "Long Weekend Escapes" -> desc.contains("weekend") || title.contains("weekend")
                    else -> false
                }
            }
            "experiences" -> {
                val desc = listing.description.lowercase()
                val type = listing.type.lowercase()
                when (subcategory) {
                    "Trekking" -> type.contains("trekking") || desc.contains("trek")
                    "Snow Enjoyment" -> type.contains("snow") || desc.contains("snow") || desc.contains("ski")
                    "Adventure" -> type.contains("adventure") || desc.contains("adventure")
                    "Water Sports" -> type.contains("water") || desc.contains("beach") || desc.contains("rafting") || desc.contains("scuba")
                    else -> false
                }
            }
            else -> false
        }
    }

    val matchedImg = matched.firstOrNull()?.let { listing ->
        listing.placesCovered.firstOrNull()?.imageUrls?.firstOrNull() ?: listing.photos.firstOrNull()
    }
    if (matchedImg != null) return matchedImg

    return when (subcategory) {
        "Family Tour" -> "https://images.unsplash.com/photo-1543039625-14cbd3802e7d?auto=format&fit=crop&q=80&w=400"
        "Group Tour" -> "https://images.unsplash.com/photo-1539635278303-d4002c07eae3?auto=format&fit=crop&q=80&w=400"
        "Fix Departure Tour" -> "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&q=80&w=400"
        "Honeymoon Tour" -> "https://images.unsplash.com/photo-1510312305653-8ed496efae75?auto=format&fit=crop&q=80&w=400"
        "Kashmir" -> "https://images.unsplash.com/photo-1566228015668-4c45dbc4e2f5?auto=format&fit=crop&q=80&w=400"
        "Himachal" -> "https://images.unsplash.com/photo-1605649487212-47bdab064df7?auto=format&fit=crop&q=80&w=400"
        "South" -> "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&q=80&w=400"
        "Rajasthan" -> "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&q=80&w=400"
        "Dubai" -> "https://images.unsplash.com/photo-1512453979798-5ea266f8880c?auto=format&fit=crop&q=80&w=400"
        "Europe" -> "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&q=80&w=400"
        "Bali" -> "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&q=80&w=400"
        "Turkey" -> "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&q=80&w=400"
        "Baku" -> "https://images.unsplash.com/photo-1618083707368-b3823daa2726?auto=format&fit=crop&q=80&w=400"
        "Singapore" -> "https://images.unsplash.com/photo-1525625293386-3f8f99389edd?auto=format&fit=crop&q=80&w=400"
        "Leh Ladakh" -> "https://images.unsplash.com/photo-1621415263409-2259bdd2ac0d?auto=format&fit=crop&q=80&w=400"
        "Manali" -> "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&q=80&w=400"
        "Trekking" -> "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&q=80&w=400"
        "Snow Enjoyment" -> "https://images.unsplash.com/photo-1482862549707-f63cb32c5fd9?auto=format&fit=crop&q=80&w=400"
        "Adventure" -> "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?auto=format&fit=crop&q=80&w=400"
        "Water Sports" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=400"
        "Summer Retreats" -> "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&q=80&w=400"
        "Monsoon Magic" -> "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&q=80&w=400"
        "Winter Wonderland" -> "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?auto=format&fit=crop&q=80&w=400"
        "Spring Getaways" -> "https://images.unsplash.com/photo-1492496913980-501348b61469?auto=format&fit=crop&q=80&w=400"
        "New Year & Christmas" -> "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&q=80&w=400"
        "Diwali Specials" -> "https://images.unsplash.com/photo-1582650625119-3a31f8fa2699?auto=format&fit=crop&q=80&w=400"
        "Summer Vacations" -> "https://images.unsplash.com/photo-1506929562872-bb421503ef21?auto=format&fit=crop&q=80&w=400"
        "Long Weekend Escapes" -> "https://images.unsplash.com/photo-1501555088652-021faa106b9b?auto=format&fit=crop&q=80&w=400"
        else -> "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&q=80&w=400"
    }
}

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
    onChatListNavigate: () -> Unit = {},
    onProfileNavigate: () -> Unit = {},
    onSignOut: () -> Unit,
    wishlistViewModel: WishlistViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var dashboardViewMode by remember { mutableStateOf("categories") } // "categories" or "all"
    var selectedCategoryFilter by remember { mutableStateOf<CategoryFilter?>(null) }
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

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && dashboardViewMode == "categories") {
            dashboardViewMode = "all"
        }
    }

    val filteredDestinations = remember(destinations, searchQuery, selectedCategoryFilter, dashboardViewMode) {
        val baseList = if (searchQuery.isBlank()) {
            val filter = selectedCategoryFilter
            if (filter != null) {
                destinations.filter { dest ->
                    val listing = dest.travelListing ?: return@filter false
                    if (!listing.approved) return@filter false
                    
                    val category = filter.categoryId
                    val subcategory = filter.subcategory
                    
                    when (category) {
                        "tourCategory" -> {
                            val cats = listing.tourCategories
                            if (subcategory != null) {
                                when (subcategory) {
                                    "Family Tour" -> cats.any { it.equals("Family", ignoreCase = true) }
                                    "Group Tour" -> cats.any { it.equals("Friends", ignoreCase = true) || it.equals("Group", ignoreCase = true) }
                                    "Fix Departure Tour" -> cats.any { it.equals("Fix Departure", ignoreCase = true) }
                                    "Honeymoon Tour" -> cats.any { it.equals("Honeymoon", ignoreCase = true) }
                                    else -> false
                                }
                            } else {
                                cats.isNotEmpty()
                            }
                        }
                        "domestic" -> {
                            if (listing.packageType != "domestic") return@filter false
                            if (subcategory != null) {
                                val state = listing.stateName?.lowercase() ?: ""
                                when (subcategory) {
                                    "Kashmir" -> state.contains("kashmir") || state.contains("jammu")
                                    "Himachal" -> state.contains("himachal")
                                    "South" -> state.contains("kerala") || state.contains("karnataka") || state.contains("tamil") || state.contains("south") || state.contains("goa") || state.contains("andhra")
                                    "Rajasthan" -> state.contains("rajasthan")
                                    else -> false
                                }
                            } else true
                        }
                        "international" -> {
                            if (listing.packageType != "international") return@filter false
                            if (subcategory != null) {
                                val country = listing.countryName?.lowercase() ?: ""
                                when (subcategory) {
                                    "Dubai" -> country.contains("dubai") || country.contains("emirates") || country.contains("uae")
                                    "Europe" -> country.contains("europe") || country.contains("switzerland") || country.contains("france") || country.contains("italy") || country.contains("germany") || country.contains("united kingdom") || country.contains("london")
                                    "Bali" -> country.contains("bali") || country.contains("indonesia")
                                    "Turkey" -> country.contains("turkey")
                                    else -> false
                                }
                            } else true
                        }
                        "trending" -> {
                            val destName = ((listing.countryName ?: "") + " " + (listing.stateName ?: "") + " " + listing.title).lowercase()
                            if (subcategory != null) {
                                when (subcategory) {
                                    "Baku" -> destName.contains("baku") || destName.contains("azerbaijan")
                                    "Singapore" -> destName.contains("singapore")
                                    "Leh Ladakh" -> destName.contains("ladakh") || destName.contains("leh")
                                    "Manali" -> destName.contains("manali")
                                    else -> false
                                }
                            } else {
                                listing.rating >= 4.5 || listing.reviewsCount > 50
                            }
                        }
                        "seasons" -> {
                            val desc = listing.description.lowercase()
                            if (subcategory != null) {
                                when (subcategory) {
                                    "Summer Retreats" -> desc.contains("summer") || desc.contains("cool")
                                    "Monsoon Magic" -> desc.contains("monsoon") || desc.contains("rain")
                                    "Winter Wonderland" -> desc.contains("winter") || desc.contains("snow")
                                    "Spring Getaways" -> desc.contains("spring") || desc.contains("flower")
                                    else -> false
                                }
                            } else true
                        }
                        "events" -> {
                            val desc = listing.description.lowercase()
                            val title = listing.title.lowercase()
                            if (subcategory != null) {
                                when (subcategory) {
                                    "New Year & Christmas" -> desc.contains("new year") || desc.contains("christmas") || title.contains("new year") || title.contains("christmas")
                                    "Diwali Specials" -> desc.contains("diwali") || title.contains("diwali")
                                    "Summer Vacations" -> desc.contains("summer") || title.contains("summer")
                                    "Long Weekend Escapes" -> desc.contains("weekend") || title.contains("weekend")
                                    else -> false
                                }
                            } else true
                        }
                        "experiences" -> {
                            val desc = listing.description.lowercase()
                            val type = listing.type.lowercase()
                            if (subcategory != null) {
                                when (subcategory) {
                                    "Trekking" -> type.contains("trekking") || desc.contains("trek")
                                    "Snow Enjoyment" -> type.contains("snow") || desc.contains("snow") || desc.contains("ski")
                                    "Adventure" -> type.contains("adventure") || desc.contains("adventure")
                                    "Water Sports" -> type.contains("water") || desc.contains("beach") || desc.contains("rafting") || desc.contains("scuba")
                                    else -> false
                                }
                            } else true
                        }
                        else -> true
                    }
                }
            } else {
                destinations
            }
        } else {
            destinations.filter { dest ->
                val query = searchQuery.lowercase()
                val basicMatch = dest.name.lowercase().contains(query) ||
                        dest.country.lowercase().contains(query) ||
                        dest.description.lowercase().contains(query)

                val listingMatch = dest.travelListing?.let { listing ->
                    listing.stateName?.lowercase()?.contains(query) == true ||
                    listing.countryName?.lowercase()?.contains(query) == true ||
                    listing.packageType?.lowercase()?.contains(query) == true ||
                    listing.type.lowercase().contains(query) ||
                    listing.packageCode.lowercase().contains(query) ||
                    listing.hotelType?.lowercase()?.contains(query) == true ||
                    listing.mealPlan?.lowercase()?.contains(query) == true ||
                    listing.inclusions.lowercase().contains(query) ||
                    listing.exclusions.lowercase().contains(query) ||
                    listing.placesCovered.any { it.name.lowercase().contains(query) } ||
                    listing.itinerary.any { day ->
                        day.placeName.lowercase().contains(query) ||
                        day.description.lowercase().contains(query) ||
                        day.accommodation.lowercase().contains(query) ||
                        day.activities.any { it.lowercase().contains(query) }
                    } ||
                    listing.faqs.any { faq ->
                        faq.question.lowercase().contains(query) ||
                        faq.answer.lowercase().contains(query)
                    }
                } == true

                basicMatch || listingMatch
            }
        }
        baseList
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
                    onChatListClick = onChatListNavigate,
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
                    dashboardViewMode = dashboardViewMode,
                    selectedCategoryFilter = selectedCategoryFilter,
                    onChipSelected = { viewMode, filter ->
                        dashboardViewMode = viewMode
                        selectedCategoryFilter = filter
                    }
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
                    if (dashboardViewMode == "categories" && selectedCategoryFilter == null && searchQuery.isBlank()) {
                        item { HeroBanner() }
                        
                        categoriesConfig.forEach { config ->
                            item {
                                CategoryCard(
                                    config = config,
                                    listings = listings,
                                    onSubcategoryClick = { subcategory ->
                                        selectedCategoryFilter = CategoryFilter(config.id, subcategory, "${config.title} - $subcategory")
                                        dashboardViewMode = "all"
                                    },
                                    onSeeMoreClick = {
                                        selectedCategoryFilter = CategoryFilter(config.id, null, config.title)
                                        dashboardViewMode = "all"
                                    }
                                )
                            }
                        }
                    } else {
                        if (selectedCategoryFilter != null) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                    border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "FILTERED CATEGORY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC2410C)
                                            )
                                            Text(
                                                text = selectedCategoryFilter!!.title + if (selectedCategoryFilter!!.subcategory != null) " • ${selectedCategoryFilter!!.subcategory}" else "",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937)
                                            )
                                        }
                                        TextButton(
                                            onClick = { 
                                                selectedCategoryFilter = null
                                                dashboardViewMode = "categories"
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC2410C))
                                        ) {
                                            Text("Clear Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (searchQuery.isNotBlank()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "SEARCH RESULTS FOR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1D4ED8)
                                            )
                                            Text(
                                                text = "\"$searchQuery\"",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937)
                                            )
                                        }
                                        TextButton(
                                            onClick = { searchQuery = "" },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF1D4ED8))
                                        ) {
                                            Text("Clear Search", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Search Results" else selectedCategoryFilter?.title ?: "All Packages",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Showing results for \"$searchQuery\"" else "Explore amazing places",
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
                                            text = "",
                                            style = MaterialTheme.typography.displayLarge
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "No results found for \"$searchQuery\""
                                                   else "No travel packages available in this category yet.",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredDestinations) { destination ->
                                ModernListingCard(
                                    listing = destination.travelListing ?: return@items,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChips(
    dashboardViewMode: String,
    selectedCategoryFilter: CategoryFilter?,
    onChipSelected: (viewMode: String, filter: CategoryFilter?) -> Unit
) {
    val items = listOf(
        CategoryChipItem("all_categories", "🎛️ Categories", "categories"),
        CategoryChipItem("all_packages", "🏖️ All Packages", "all"),
        CategoryChipItem("domestic_tab", "🏔️ Domestic", "all", "domestic", null, "Domestic Packages"),
        CategoryChipItem("intl_tab", "🌍 International", "all", "international", null, "International Packages"),
        CategoryChipItem("trending_tab", "🔥 Trending", "all", "trending", null, "Trending Destinations"),
        CategoryChipItem("experience_tab", "🎒 Adventure", "all", "experiences", "Adventure", "Experience Travel - Adventure"),
        CategoryChipItem("honeymoon_tab", "🍯 Honeymoon", "all", "tourCategory", "Honeymoon Tour", "Tour by Category - Honeymoon Tour")
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        LazyRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                val isCategoriesActive = item.type == "categories" && dashboardViewMode == "categories" && selectedCategoryFilter == null
                val isAllActive = item.type == "all" && dashboardViewMode == "all" && selectedCategoryFilter == null && item.filterId == null
                val isFilterActive = item.filterId != null && selectedCategoryFilter != null &&
                        selectedCategoryFilter.categoryId == item.filterId &&
                        selectedCategoryFilter.subcategory == item.filterSub

                val isActive = isCategoriesActive || isAllActive || isFilterActive

                FilterChip(
                    selected = isActive,
                    onClick = {
                        if (item.type == "categories") {
                            onChipSelected("categories", null)
                        } else {
                            val filter = if (item.filterId != null) {
                                CategoryFilter(item.filterId, item.filterSub, item.filterTitle ?: "")
                            } else null
                            onChipSelected("all", filter)
                        }
                    },
                    label = { 
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF97316),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF3F4F6),
                        labelColor = Color(0xFF374151)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isActive,
                        borderColor = if (isActive) Color(0xFFF97316) else Color(0xFFE5E7EB),
                        borderWidth = 1.dp
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

@Composable
fun SubcategoryItem(
    subcategory: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6))
        ) {
            AsyncImage(
                model = CoilImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = subcategory,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subcategory,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryCard(
    config: CategoryConfig,
    listings: List<TravelListing>,
    onSubcategoryClick: (String) -> Unit,
    onSeeMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = config.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val subs = config.subcategories
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (subs.isNotEmpty()) {
                        val sub = subs[0]
                        Box(modifier = Modifier.weight(1f)) {
                            SubcategoryItem(
                                subcategory = sub,
                                imageUrl = getSubcategoryImage(config.id, sub, listings),
                                onClick = { onSubcategoryClick(sub) }
                            )
                        }
                    }
                    if (subs.size > 1) {
                        val sub = subs[1]
                        Box(modifier = Modifier.weight(1f)) {
                            SubcategoryItem(
                                subcategory = sub,
                                imageUrl = getSubcategoryImage(config.id, sub, listings),
                                onClick = { onSubcategoryClick(sub) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (subs.size > 2) {
                        val sub = subs[2]
                        Box(modifier = Modifier.weight(1f)) {
                            SubcategoryItem(
                                subcategory = sub,
                                imageUrl = getSubcategoryImage(config.id, sub, listings),
                                onClick = { onSubcategoryClick(sub) }
                            )
                        }
                    }
                    if (subs.size > 3) {
                        val sub = subs[3]
                        Box(modifier = Modifier.weight(1f)) {
                            SubcategoryItem(
                                subcategory = sub,
                                imageUrl = getSubcategoryImage(config.id, sub, listings),
                                onClick = { onSubcategoryClick(sub) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = config.linkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0284C7),
                modifier = Modifier
                    .clickable(onClick = onSeeMoreClick)
                    .padding(vertical = 4.dp)
            )
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
    onChatListClick: () -> Unit = {},
    onWishlistClick: () -> Unit,
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Explore", Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("Chat", Icons.Filled.Chat, Icons.Outlined.ChatBubbleOutline),
        BottomNavItem("Wishlist", Icons.Filled.FavoriteBorder, Icons.Outlined.FavoriteBorder),
        BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        items.forEachIndexed { index, item ->
            val onClickAction = when (index) {
                2 -> onChatListClick
                3 -> onWishlistClick
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