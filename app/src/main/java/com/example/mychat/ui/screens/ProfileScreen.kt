package com.example.mychat.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.mychat.data.model.User
import com.example.mychat.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onWishlistNavigate: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(currentUser.displayName) { mutableStateOf(currentUser.displayName) }
    var editPhone by remember(currentUser.phone) { mutableStateOf(currentUser.phone) }

    ProfileScreenContent(
        currentUser = currentUser,
        isEditing = isEditing,
        editName = editName,
        editPhone = editPhone,
        onEditNameChange = { editName = it },
        onEditPhoneChange = { editPhone = it },
        onToggleEdit = { isEditing = !isEditing },
        onSave = {
            authViewModel.updateProfile(editName, editPhone)
            isEditing = false
        },
        onCancel = {
            editName = currentUser.displayName
            editPhone = currentUser.phone
            isEditing = false
        },
        onBack = onBack,
        onWishlistNavigate = onWishlistNavigate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    currentUser: User,
    isEditing: Boolean,
    editName: String,
    editPhone: String,
    onEditNameChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onToggleEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onWishlistNavigate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Account", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("\u2190", fontSize = 22.sp)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1C1F26).copy(alpha = 0.05f), Color.Transparent)
                                    )
                                )
                                .padding(top = 32.dp, bottom = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Avatar circle
                                Surface(
                                    modifier = Modifier.size(100.dp),
                                    shape = CircleShape,
                                    color = Color.Transparent
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color(0xFFF97316), Color(0xFFEAB308))
                                                ),
                                                shape = CircleShape
                                            )
                                            .border(4.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser.displayName
                                                .split(" ")
                                                .filter { it.isNotBlank() }
                                                .take(2)
                                                .joinToString("") { it.take(1).uppercase() }
                                                .ifEmpty { "U" },
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = currentUser.displayName.ifEmpty { "User" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = currentUser.email,
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280)
                                )
                                if (currentUser.phone.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentUser.phone,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE5E7EB))

                        // Navigation links
                        Column(modifier = Modifier.padding(12.dp)) {
                            ProfileNavItem(
                                icon = Icons.Default.Person,
                                title = "My Account",
                                isActive = true
                            )
                        ProfileNavItem(
                                icon = Icons.Default.ShoppingCart,
                                title = "My Holiday Cart",
                                onClick = onWishlistNavigate
                            )
                            ProfileNavItem(
                                icon = Icons.Default.Favorite,
                                title = "Wishlist",
                                onClick = onWishlistNavigate
                            )
                        }
                    }
                }
            }

            // Personal Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Personal Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            if (!isEditing) {
                                OutlinedButton(
                                    onClick = onToggleEdit,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2B58C4)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Edit Profile",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2B58C4),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = onSave,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2B58C4)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = onCancel,
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isEditing) {
                            // Display mode
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ProfileDetailRow("Name", currentUser.displayName.ifEmpty { "—" })
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Contact", currentUser.phone.ifEmpty { "—" })
                                Spacer(modifier = Modifier.height(12.dp))
                                ProfileDetailRow("Email ID", currentUser.email)
                            }
                        } else {
                            // Edit mode
                            Column {
                                Text(
                                    text = "Name",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = onEditNameChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2B58C4),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Contact",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editPhone,
                                    onValueChange = onEditPhoneChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                    placeholder = { Text("e.g. +91 932 329 4525", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2B58C4),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Email ID (Cannot be changed)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6B7280),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = currentUser.email,
                                    onValueChange = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color(0xFF9CA3AF)),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor = Color(0xFFF9FAFB),
                                        disabledBorderColor = Color(0xFFE5E7EB),
                                        disabledTextColor = Color(0xFF9CA3AF)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
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
fun ProfileDetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9CA3AF),
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151)
        )
    }
}

@Composable
fun ProfileNavItem(
    icon: ImageVector,
    title: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isActive) Color(0xFF2B58C4).copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isActive) Color(0xFF2B58C4) else Color(0xFF6B7280))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) Color(0xFF2B58C4) else Color(0xFF6B7280)
            )
        }
    }
}
