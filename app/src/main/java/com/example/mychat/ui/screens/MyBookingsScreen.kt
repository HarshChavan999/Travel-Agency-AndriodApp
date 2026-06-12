package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychat.data.model.Booking
import com.example.mychat.viewmodel.TravelViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    travelViewModel: TravelViewModel,
    currentUserId: String,
    onBack: () -> Unit,
    onExplorePackages: () -> Unit
) {
    val userBookings by travelViewModel.userBookings.collectAsState()
    val isLoadingBookings by travelViewModel.isLoadingBookings.collectAsState()

    // Load user bookings when screen appears
    LaunchedEffect(currentUserId) {
        travelViewModel.loadUserBookings(currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Tours & Cancellations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1F26),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        if (isLoadingBookings) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (userBookings.isEmpty()) {
            // Empty State - matches WebApp design
            EmptyBookingsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onExplorePackages = onExplorePackages
            )
        } else {
            // Bookings List with Hero Banner
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Hero Banner with Stats
                item {
                    BookingsHeroBanner(bookings = userBookings)
                }

                // Booking Tickets
                items(userBookings) { booking ->
                    BookingTicketCard(
                        booking = booking,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun BookingsHeroBanner(bookings: List<Booking>) {
    val confirmedCount = bookings.count { it.status == "confirmed" }
    val pendingCount = bookings.count { it.status == "pending" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1C1F26), Color(0xFF2B2F3A))
                )
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            // Label
            Surface(
                color = Color(0xFFF97316),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "MY TRAVEL HISTORY",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "My Tours & Cancellations",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your bookings, view itineraries, and manage your travel plans.",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    label = "Total Bookings",
                    value = "${bookings.size}",
                    valueColor = Color.White
                )
                StatBox(
                    label = "Confirmed",
                    value = "$confirmedCount",
                    valueColor = Color(0xFF4ADE80)
                )
                StatBox(
                    label = "Pending",
                    value = "$pendingCount",
                    valueColor = Color(0xFFFBBF24)
                )
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, valueColor: Color) {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF),
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyBookingsState(
    modifier: Modifier = Modifier,
    onExplorePackages: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Blue gradient top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2B58C4), Color(0xFF407BFF))
                    )
                )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Icon circle
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(0.dp, Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF4F46E5))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Trips Booked Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your travel adventures will appear here.\nExplore our amazing packages and book your first unforgettable trip!",
            fontSize = 15.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onExplorePackages,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF97316)
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .widthIn(min = 200.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Explore Packages",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BookingTicketCard(
    booking: Booking,
    modifier: Modifier = Modifier
) {
    val isConfirmed = booking.status == "confirmed"
    val isPending = booking.status == "pending"
    val isCancelled = booking.status == "cancelled"

    val headerColors = when {
        isConfirmed -> listOf(Color(0xFF0F4C35), Color(0xFF1a6647))
        isPending -> listOf(Color(0xFF7B4F00), Color(0xFFA86800))
        else -> listOf(Color(0xFF6B1616), Color(0xFF8B2020))
    }

    val borderColor = when {
        isConfirmed -> Color(0xFF86EFAC).copy(alpha = 0.5f)
        isPending -> Color(0xFFFCD34D).copy(alpha = 0.5f)
        else -> Color(0xFFFECACA).copy(alpha = 0.5f)
    }

    val isIntl = booking.packageType == "international"
    val currency = if (isIntl) "$" else "₹"

    val createdAtFormatted = if (booking.createdAt > 0) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        sdf.format(Date(booking.createdAt))
    } else "—"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
            // ── TICKET HEADER ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(colors = headerColors)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Icon
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isIntl) Icons.Default.Language else Icons.Default.Terrain,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isIntl) "INTERNATIONAL TOUR" else "DOMESTIC TOUR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = booking.listingTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "by ${booking.agencyName}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Status badge + reference
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = when {
                                isConfirmed -> Color(0xFF4ADE80).copy(alpha = 0.2f)
                                isPending -> Color(0xFFFBBF24).copy(alpha = 0.2f)
                                else -> Color(0xFFF87171).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isConfirmed -> Color(0xFF4ADE80).copy(alpha = 0.4f)
                                    isPending -> Color(0xFFFBBF24).copy(alpha = 0.4f)
                                    else -> Color(0xFFF87171).copy(alpha = 0.4f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isConfirmed -> Color(0xFF4ADE80)
                                                isPending -> Color(0xFFFBBF24)
                                                else -> Color(0xFFF87171)
                                            }
                                        )
                                )
                                Text(
                                    text = when {
                                        isConfirmed -> "CONFIRMED"
                                        isPending -> "PENDING REVIEW"
                                        else -> "CANCELLED"
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isConfirmed -> Color(0xFFBBF7D0)
                                        isPending -> Color(0xFFFDE68A)
                                        else -> Color(0xFFFECACA)
                                    },
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#${booking.bookingReference}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // ── TICKED BODY ──
            Column {
                // Dashed divider
                DashedDivider()

                // Key Details Grid (2x2 for mobile)
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailCardItem(
                            icon = Icons.Default.DateRange,
                            label = "DEPARTURE DATE",
                            value = booking.travelDate ?: "TBD",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DetailCardItem(
                            icon = Icons.Default.People,
                            label = "PASSENGERS",
                            value = "${booking.travelers} ${if (booking.travelers == 1) "Person" else "People"}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DetailCardItem(
                            icon = Icons.Default.Payment,
                            label = "TOTAL FARE",
                            value = "$currency${"%.2f".format(booking.totalAmount)}",
                            valueColor = Color(0xFF059669),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DetailCardItem(
                            icon = Icons.Default.CalendarMonth,
                            label = "BOOKED ON",
                            value = createdAtFormatted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE5E7EB))

                // Passenger + Status Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Passenger Info",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(label = "Name", value = booking.userName)
                        InfoRow(label = "Email", value = booking.userEmail)
                        InfoRow(label = "Mobile", value = booking.userPhone ?: "—")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isConfirmed) "Booking Status" else if (isPending) "Status Update" else "Cancellation",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        when {
                            isConfirmed -> {
                                StatusIndicator(
                                    icon = Icons.Default.CheckCircle,
                                    title = "Booking Confirmed",
                                    subtitle = "Your spot is reserved.",
                                    titleColor = Color(0xFF059669)
                                )
                            }
                            isPending -> {
                                StatusIndicator(
                                    icon = Icons.Default.Schedule,
                                    title = "Under Review by ${booking.agencyName}",
                                    subtitle = "You'll be notified once confirmed.",
                                    titleColor = Color(0xFFD97706)
                                )
                            }
                            isCancelled -> {
                                StatusIndicator(
                                    icon = Icons.Default.Cancel,
                                    title = "Booking Cancelled",
                                    subtitle = "Contact ${booking.agencyName} for refund info.",
                                    titleColor = Color(0xFFDC2626)
                                )
                            }
                        }

                        // Preferences tags
                        if (booking.preferences.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                booking.preferences.take(3).forEach { pref ->
                                    Surface(
                                        color = Color(0xFFF3F4F6),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                    ) {
                                        Text(
                                            text = pref,
                                            fontSize = 9.sp,
                                            color = Color(0xFF6B7280),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── ACTION BAR ──
            Surface(
                color = Color(0xFFF9FAFB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ref: #${booking.bookingReference}",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isConfirmed || isPending) {
                            Button(
                                onClick = { /* View Details - future */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1C1F26)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "View Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
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
fun DetailCardItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111827),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9CA3AF),
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color(0xFF374151),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StatusIndicator(
    icon: ImageVector,
    title: String,
    subtitle: String,
    titleColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = titleColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = titleColor)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun DashedDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6))
                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            thickness = 2.dp,
            color = Color(0xFFE5E7EB)
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6))
                .border(1.dp, Color(0xFFE5E7EB), CircleShape)
        )
    }
}