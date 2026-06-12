package com.example.mychat.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychat.data.model.Booking
import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    listing: TravelListing,
    currentUser: User,
    onBack: () -> Unit,
    onBookingComplete: () -> Unit,
    isCreatingBooking: Boolean,
    bookingResult: Result<String>?,
    onCreateBooking: (Booking) -> Unit,
    generateBookingReference: () -> String
) {
    var currentStep by remember { mutableStateOf(1) }
    var travelers by remember { mutableStateOf(1) }
    var travelDate by remember { mutableStateOf("") }
    var specialRequests by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf(currentUser.displayName) }
    var contactEmail by remember { mutableStateOf(currentUser.email) }
    var contactPhone by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf(setOf<String>()) }
    
    // Enhanced booking fields matching WebApp
    var insurance by remember { mutableStateOf(false) }
    var emergencyContact by remember { mutableStateOf("") }
    var dietaryRestrictions by remember { mutableStateOf("") }
    var accessibilityNeeds by remember { mutableStateOf("") }
    var bookingNotes by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    // Handle booking result
    LaunchedEffect(bookingResult) {
        bookingResult?.fold(
            onSuccess = {
                onBookingComplete()
            },
            onFailure = { }
        )
    }

    val isInternational = listing.packageType == "international"
    val currency = if (isInternational) "$" else "₹"
    val pricePerPerson = listing.price.toDouble()
    val subtotal = pricePerPerson * travelers
    val serviceFee = subtotal * 0.05
    val insuranceFee = if (insurance) travelers * 50.0 else 0.0
    val total = subtotal + serviceFee + insuranceFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Book Your Trip - Step $currentStep of 4",
                        fontSize = 16.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1C1F26),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Booking Progress Indicator
            BookingProgressIndicator(
                currentStep = currentStep,
                totalSteps = 4
            )

            // Package Info Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF2563EB),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isInternational) "Global" else "Local",
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = listing.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "By ${listing.agencyName}",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Text(
                        text = if (isInternational) "$" else "₹",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            when (currentStep) {
                1 -> Step1PackageDetails(
                    listing = listing,
                    travelers = travelers,
                    travelDate = travelDate,
                    specialRequests = specialRequests,
                    onTravelersChange = { travelers = it },
                    onTravelDateChange = { travelDate = it },
                    onSpecialRequestsChange = { specialRequests = it }
                )
                2 -> Step2Preferences(
                    preferences = preferences,
                    onPreferencesChange = { preferences = it }
                )
                3 -> Step3ContactInfo(
                    contactName = contactName,
                    contactEmail = contactEmail,
                    contactPhone = contactPhone,
                    emergencyContact = emergencyContact,
                    dietaryRestrictions = dietaryRestrictions,
                    accessibilityNeeds = accessibilityNeeds,
                    bookingNotes = bookingNotes,
                    onContactNameChange = { contactName = it },
                    onContactEmailChange = { contactEmail = it },
                    onContactPhoneChange = { contactPhone = it },
                    onEmergencyContactChange = { emergencyContact = it },
                    onDietaryRestrictionsChange = { dietaryRestrictions = it },
                    onAccessibilityNeedsChange = { accessibilityNeeds = it },
                    onBookingNotesChange = { bookingNotes = it }
                )
                4 -> Step4Summary(
                    listing = listing,
                    travelers = travelers,
                    travelDate = travelDate,
                    specialRequests = specialRequests,
                    contactName = contactName,
                    contactEmail = contactEmail,
                    contactPhone = contactPhone,
                    preferences = preferences,
                    isCreatingBooking = isCreatingBooking,
                    bookingResult = bookingResult,
                    currency = currency,
                    subtotal = subtotal,
                    serviceFee = serviceFee,
                    insuranceFee = insuranceFee,
                    total = total,
                    insurance = insurance,
                    onInsuranceChange = { insurance = it },
                    termsAccepted = termsAccepted,
                    onTermsAcceptedChange = { termsAccepted = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentStep > 1) currentStep--
                        else onBack()
                    }
                ) {
                    Text(if (currentStep == 1) "Cancel" else "Previous")
                }

                if (currentStep < 4) {
                    Button(onClick = { currentStep++ }) {
                        Text("Next")
                    }
                } else {
                    Button(
                        onClick = {
                            val booking = Booking(
                                id = generateBookingReference(),
                                userId = currentUser.id,
                                userName = contactName,
                                userEmail = contactEmail,
                                userPhone = contactPhone,
                                listingId = listing.id,
                                listingTitle = listing.title,
                                agencyId = listing.agencyId,
                                agencyName = listing.agencyName,
                                travelers = travelers,
                                travelDate = travelDate,
                                specialRequests = specialRequests,
                                preferences = preferences.toList(),
                                totalAmount = total,
                                status = "pending",
                                createdAt = System.currentTimeMillis(),
                                bookingReference = generateBookingReference()
                            )
                            onCreateBooking(booking)
                        },
                        enabled = !isCreatingBooking && termsAccepted
                    ) {
                        if (isCreatingBooking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Confirm Booking")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingProgressIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (step in 1..totalSteps) {
            val isCompleted = step < currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = Color(0xFF10B981),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = if (isCurrent) Color(0xFF2563EB) else Color(0xFFE5E7EB),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = step.toString(),
                                color = if (isCurrent) Color.White else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .background(
                            color = if (step < currentStep) Color(0xFF10B981) else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun Step1PackageDetails(
    listing: TravelListing,
    travelers: Int,
    travelDate: String,
    specialRequests: String,
    onTravelersChange: (Int) -> Unit,
    onTravelDateChange: (String) -> Unit,
    onSpecialRequestsChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Package Details",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row {
                    Icon(Icons.Default.Language, contentDescription = "Package", modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = listing.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "By ${listing.agencyName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = travelers.toString(),
            onValueChange = { onTravelersChange(it.toIntOrNull() ?: 1) },
            label = { Text("Number of Travelers") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.People, contentDescription = "Travelers", modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = travelDate,
            onValueChange = onTravelDateChange,
            label = { Text("Preferred Travel Date") },
            placeholder = { Text("Select date") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = specialRequests,
            onValueChange = onSpecialRequestsChange,
            label = { Text("Special Requests or Notes") },
            placeholder = { Text("Any special requirements, dietary restrictions, or preferences...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun Step2Preferences(
    preferences: Set<String>,
    onPreferencesChange: (Set<String>) -> Unit
) {
    val availablePreferences = listOf(
        "Adventure", "Culture", "Food", "Relaxation",
        "Shopping", "Nightlife"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Travel Preferences",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select your interests (optional)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                availablePreferences.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { preference ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = preferences.contains(preference),
                                    onCheckedChange = { checked ->
                                        val newPrefs = if (checked) {
                                            preferences + preference
                                        } else {
                                            preferences - preference
                                        }
                                        onPreferencesChange(newPrefs)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF2563EB)
                                    )
                                )
                                Text(
                                    text = preference,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun Step3ContactInfo(
    contactName: String,
    contactEmail: String,
    contactPhone: String,
    emergencyContact: String,
    dietaryRestrictions: String,
    accessibilityNeeds: String,
    bookingNotes: String,
    onContactNameChange: (String) -> Unit,
    onContactEmailChange: (String) -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onEmergencyContactChange: (String) -> Unit,
    onDietaryRestrictionsChange: (String) -> Unit,
    onAccessibilityNeedsChange: (String) -> Unit,
    onBookingNotesChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Contact Information",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = contactName,
                    onValueChange = onContactNameChange,
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = onContactEmailChange,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = onContactPhoneChange,
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = onEmergencyContactChange,
                    label = { Text("Emergency Contact") },
                    placeholder = { Text("Emergency contact name and phone") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = "Emergency", modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dietaryRestrictions,
                    onValueChange = onDietaryRestrictionsChange,
                    label = { Text("Dietary Restrictions") },
                    placeholder = { Text("Any dietary restrictions or allergies") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = "Dietary", modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = accessibilityNeeds,
                    onValueChange = onAccessibilityNeedsChange,
                    label = { Text("Accessibility Needs") },
                    placeholder = { Text("Any mobility or accessibility requirements") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Accessible, contentDescription = "Accessibility", modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bookingNotes,
                    onValueChange = onBookingNotesChange,
                    label = { Text("Additional Notes") },
                    placeholder = { Text("Any other special requests or information") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun Step4Summary(
    listing: TravelListing,
    travelers: Int,
    travelDate: String,
    specialRequests: String,
    contactName: String,
    contactEmail: String,
    contactPhone: String,
    preferences: Set<String>,
    isCreatingBooking: Boolean,
    bookingResult: Result<String>?,
    currency: String,
    subtotal: Double,
    serviceFee: Double,
    insuranceFee: Double,
    total: Double,
    insurance: Boolean,
    onInsuranceChange: (Boolean) -> Unit,
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Booking Summary",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Package:", fontWeight = FontWeight.Bold)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(listing.title)
                        Text(
                            text = "${listing.packageType?.replaceFirstChar { it.uppercase() }} Tour",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Travelers:", color = Color(0xFF6B7280))
                    Text("$travelers")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Travel Date:", color = Color(0xFF6B7280))
                    Text(travelDate.ifEmpty { "Not specified" })
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Price per person:", color = Color(0xFF6B7280))
                    Text("$currency ${listing.price}", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pricing Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Price Breakdown",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal ($travelers x $currency${listing.price})", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Text("$currency ${"%.2f".format(subtotal)}", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Service Fee (5%)", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Text("$currency ${"%.2f".format(serviceFee)}", fontSize = 13.sp)
                }

                if (insurance) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Travel Insurance", fontSize = 13.sp, color = Color(0xFF6B7280))
                        Text("$currency ${"%.2f".format(insuranceFee)}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Amount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currency ${"%.2f".format(total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Travel Insurance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = insurance,
                    onCheckedChange = onInsuranceChange,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Travel Insurance",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Covers medical emergencies and trip cancellations",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
                Text(
                    text = "$currency${"%.0f".format(travelers * 50)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Terms and Conditions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onTermsAcceptedChange(!termsAccepted) }
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = onTermsAcceptedChange,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I accept the terms and conditions",
                fontSize = 13.sp,
                color = Color(0xFF374151)
            )
        }

        // Important Notes
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Important Notes:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    "Booking will be confirmed within 24 hours",
                    "Payment details will be shared after confirmation",
                    "You can modify or cancel your booking before payment",
                    "Travel insurance covers medical emergencies up to \$10,000"
                ).forEach { note ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ", color = Color(0xFF1E40AF), fontSize = 13.sp)
                        Text(
                            text = note,
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }
        }

        // Show booking result
        bookingResult?.fold(
            onSuccess = {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Booking Submitted Successfully!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "You will receive a confirmation email shortly.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            },
            onFailure = {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Booking Failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = it.message ?: "Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }
        )
    }
}