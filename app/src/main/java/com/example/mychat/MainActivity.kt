package com.example.mychat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.User
import com.example.mychat.data.repository.AuthRepository
import com.example.mychat.data.repository.ChatRepository
import com.example.mychat.data.repository.TravelRepository
import com.example.mychat.data.repository.WishlistRepository
import com.example.mychat.service.MyFirebaseMessagingService
import com.example.mychat.service.NotificationHelper
import com.example.mychat.ui.components.AnnouncementDialog
import com.example.mychat.ui.components.ForceUpdateDialog
import com.example.mychat.ui.components.MaintenanceDialog
import com.example.mychat.ui.components.OptionalUpdateDialog
import com.example.mychat.ui.screens.*
import com.example.mychat.ui.theme.MychatTheme
import com.example.mychat.viewmodel.AuthViewModel
import com.example.mychat.viewmodel.ChatViewModel
import com.example.mychat.viewmodel.ConfigViewModel
import com.example.mychat.viewmodel.TravelViewModel
import com.example.mychat.viewmodel.WishlistViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var onGoogleSignInResult: ((com.google.android.gms.auth.api.signin.GoogleSignInAccount?) -> Unit)? = null
    private var authRepositoryRef: AuthRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel for push notifications
        NotificationHelper.createNotificationChannel(this)

        // Handle notification tap intent - extract navigation data
        handleNotificationIntent(intent)

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    onGoogleSignInResult?.invoke(account)
                } catch (e: ApiException) {
                    android.util.Log.e("GoogleSignIn", "Google sign-in failed: ${e.statusCode}")
                    onGoogleSignInResult?.invoke(null)
                }
            } else {
                onGoogleSignInResult?.invoke(null)
            }
        }

        enableEdgeToEdge()
        setContent {
            MychatTheme {
                TravelApp(
                    onGoogleSignIn = {
                        authRepositoryRef?.let { repo ->
                            val googleSignInClient = repo.getGoogleSignInClient(this@MainActivity)
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    },
                    onSetGoogleSignInResultCallback = { callback ->
                        onGoogleSignInResult = callback
                    },
                    onAuthRepositoryReady = { repo ->
                        authRepositoryRef = repo
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("navigate_to_chat", false) == true) {
            val chatUserId = intent.getStringExtra("chat_user_id") ?: return
            android.util.Log.d("MainActivity", "Notification tap: navigate to chat with user: $chatUserId")
            intent.removeExtra("navigate_to_chat")
        }
    }

    override fun onResume() {
        super.onResume()
        // Clear notifications when app is opened
        try {
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.cancel(1001)
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Error clearing notifications: ${e.message}")
        }
    }
}

enum class Screen {
    DASHBOARD,
    LISTING_DETAIL,
    BOOKING,
    CHAT,
    CHAT_LIST,
    WISHLIST,
    MY_BOOKINGS,
    PROFILE
}

@Composable
fun TravelNavigation(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel,
    travelViewModel: TravelViewModel,
    wishlistRepository: WishlistRepository,
    currentUser: User
) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var selectedListing by remember { mutableStateOf<TravelListing?>(null) }
    var chatWithAgency by remember { mutableStateOf<String?>(null) }
    var navigateToBookings by remember { mutableStateOf(false) }
    var chatBackDestination by remember { mutableStateOf(Screen.DASHBOARD) }

    val currentUserState by authViewModel.currentUser.collectAsState()

    val wishlistViewModel: WishlistViewModel = viewModel { WishlistViewModel(wishlistRepository) }

    val listings by travelViewModel.listings.collectAsState()
    val isLoadingListings by travelViewModel.isLoadingListings.collectAsState()
    val selectedListingState by travelViewModel.selectedListing.collectAsState()
    val isCreatingBooking by travelViewModel.isCreatingBooking.collectAsState()
    val bookingResult by travelViewModel.bookingResult.collectAsState()

    val chatMessages by chatViewModel.chatMessages.collectAsState()
    val allMessages by chatViewModel.messages.collectAsState()
    val currentChatUserState by chatViewModel.currentChatUser.collectAsState()
    val isLoadingHistory by chatViewModel.isLoadingHistory.collectAsState()
    val hasMoreHistory by chatViewModel.hasMoreHistory.collectAsState()
    val historyError by chatViewModel.historyError.collectAsState()

    when (currentScreen) {
        Screen.DASHBOARD -> {
            TravelDashboard(
                currentUser = currentUser,
                listings = listings,
                isLoading = isLoadingListings,
                onListingClick = { listing ->
                    travelViewModel.loadListingById(listing.id)
                    currentScreen = Screen.LISTING_DETAIL
                },
                onChatClick = { listing ->
                    chatWithAgency = listing.agencyId
                    val agencyUser = User(
                        id = listing.agencyId,
                        email = "${listing.agencyId}@agency.com",
                        displayName = listing.agencyName,
                        isOnline = true
                    )
                    chatViewModel.setCurrentChatUser(agencyUser)
                    chatBackDestination = Screen.DASHBOARD
                    currentScreen = Screen.CHAT
                },
                onWishlistClick = { listing ->
                    wishlistViewModel.toggleWishlist(listing.id)
                },
                onWishlistNavigate = { currentScreen = Screen.WISHLIST },
                onBookingsNavigate = { currentScreen = Screen.MY_BOOKINGS },
                onChatListNavigate = { currentScreen = Screen.CHAT_LIST },
                onProfileNavigate = { currentScreen = Screen.PROFILE },
                onSignOut = { authViewModel.signOut() },
                wishlistViewModel = wishlistViewModel
            )
        }

        Screen.LISTING_DETAIL -> {
            selectedListingState?.let { listing ->
                EnhancedListingDetailScreen(
                    listing = listing,
                    onBack = {
                        travelViewModel.clearSelectedListing()
                        currentScreen = Screen.DASHBOARD
                    },
                    onChatClick = {
                        chatWithAgency = listing.agencyId
                        val agencyUser = User(
                            id = listing.agencyId,
                            email = "${listing.agencyId}@agency.com",
                            displayName = listing.agencyName,
                            isOnline = true
                        )
                        chatViewModel.setCurrentChatUser(agencyUser)
                        chatBackDestination = Screen.LISTING_DETAIL
                        currentScreen = Screen.CHAT
                    },
                    onBookNow = { currentScreen = Screen.BOOKING },
                    onWishlistToggle = {
                        android.util.Log.d("Wishlist", "Toggled wishlist for ${listing.title}")
                    },
                    isWishlisted = false
                )
            }
        }

        Screen.BOOKING -> {
            selectedListingState?.let { listing ->
                BookingScreen(
                    listing = listing,
                    currentUser = currentUser,
                    onBack = { currentScreen = Screen.LISTING_DETAIL },
                    onBookingComplete = {
                        travelViewModel.clearBookingResult()
                        travelViewModel.clearSelectedListing()
                        currentScreen = Screen.DASHBOARD
                    },
                    isCreatingBooking = isCreatingBooking,
                    bookingResult = bookingResult,
                    onCreateBooking = { booking -> travelViewModel.createBooking(booking) },
                    generateBookingReference = { travelViewModel.generateBookingReference() }
                )
            }
        }

        Screen.CHAT -> {
            currentChatUserState?.let { agencyUser ->
                LaunchedEffect(agencyUser) {
                    chatViewModel.initializeChatHistory()
                }

                LaunchedEffect(chatMessages) {
                    chatViewModel.markAllMessagesFromUserAsRead(agencyUser.id)
                }

                ChatScreen(
                    currentUser = currentUser,
                    chatUser = agencyUser,
                    messages = chatMessages,
                    isLoadingHistory = isLoadingHistory,
                    hasMoreHistory = hasMoreHistory,
                    historyError = historyError,
                    onSendMessage = { content -> chatViewModel.sendMessage(agencyUser.id, content) },
                    onBack = {
                        chatWithAgency = null
                        chatViewModel.clearCurrentChatUser()
                        currentScreen = chatBackDestination
                    },
                    onLoadMoreHistory = { chatViewModel.loadMoreHistory() },
                    onClearHistoryError = { chatViewModel.clearHistoryError() }
                )
            }
        }

        Screen.WISHLIST -> {
            WishlistScreen(
                wishlistViewModel = wishlistViewModel,
                onListingClick = { listing ->
                    travelViewModel.loadListingById(listing.id)
                    currentScreen = Screen.LISTING_DETAIL
                },
                onChatClick = { listing ->
                    chatWithAgency = listing.agencyId
                    val agencyUser = User(
                        id = listing.agencyId,
                        email = "${listing.agencyId}@agency.com",
                        displayName = listing.agencyName,
                        isOnline = true
                    )
                    chatViewModel.setCurrentChatUser(agencyUser)
                    chatBackDestination = Screen.WISHLIST
                    currentScreen = Screen.CHAT
                },
                onBack = { currentScreen = Screen.DASHBOARD }
            )
        }

        Screen.MY_BOOKINGS -> {
            MyBookingsScreen(
                travelViewModel = travelViewModel,
                currentUserId = currentUser.id,
                onBack = { currentScreen = Screen.DASHBOARD },
                onExplorePackages = { currentScreen = Screen.DASHBOARD }
            )
        }

        Screen.CHAT_LIST -> {
            // Ensure listeners are active when viewing the chat list
            LaunchedEffect(currentUser?.id) {
                chatViewModel.ensureListening()
            }
            ChatListScreen(
                currentUser = currentUser,
                messages = allMessages,
                chatViewModel = chatViewModel,
                onBack = { currentScreen = Screen.DASHBOARD },
                onOpenChat = { agencyUser ->
                    chatWithAgency = agencyUser.id
                    chatViewModel.setCurrentChatUser(agencyUser)
                    chatBackDestination = Screen.CHAT_LIST
                    currentScreen = Screen.CHAT
                }
            )
        }

        Screen.PROFILE -> {
            val profileUser = currentUserState ?: currentUser
            ProfileScreen(
                currentUser = profileUser,
                authViewModel = authViewModel,
                onBack = { currentScreen = Screen.DASHBOARD },
                onWishlistNavigate = { currentScreen = Screen.WISHLIST }
            )
        }
    }
}

@Composable
fun TravelApp(
    onGoogleSignIn: () -> Unit,
    onSetGoogleSignInResultCallback: ((com.google.android.gms.auth.api.signin.GoogleSignInAccount?) -> Unit) -> Unit,
    onAuthRepositoryReady: (AuthRepository) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    val authRepository = remember { AuthRepository() }
    val chatRepository = remember { ChatRepository(authRepository) }
    val travelRepository = remember { TravelRepository(authRepository) }
    val wishlistRepository = remember { WishlistRepository(authRepository) }

    val authViewModel: AuthViewModel = viewModel { AuthViewModel(authRepository) }
    val chatViewModel: ChatViewModel = viewModel { ChatViewModel(chatRepository) }
    val travelViewModel: TravelViewModel = viewModel { TravelViewModel(travelRepository) }
    val wishlistViewModel: WishlistViewModel = viewModel { WishlistViewModel(wishlistRepository) }

    val configViewModel: ConfigViewModel = viewModel()
    val appConfig by configViewModel.appConfig.collectAsState()
    val showMaintenance by configViewModel.showMaintenanceDialog.collectAsState()
    val showForceUpdate by configViewModel.showForceUpdateDialog.collectAsState()
    val showOptionalUpdate by configViewModel.showOptionalUpdateDialog.collectAsState()
    val showAnnouncement by configViewModel.showAnnouncement.collectAsState()
    val configLoading by configViewModel.isLoading.collectAsState()

    LaunchedEffect(authRepository) {
        onAuthRepositoryReady(authRepository)
    }

    onSetGoogleSignInResultCallback { account ->
        if (account != null) {
            authViewModel.signInWithGoogle(account)
        }
    }

    val configLoaded by produceState(initialValue = false) {
        configViewModel.appConfig.collect {
            if (!configViewModel.isLoading.value) {
                activity?.let { act -> configViewModel.performStartupChecks(act) }
                value = true
            }
        }
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Request notification permission and register FCM token when user is logged in
    if (currentUser != null) {
        RequestNotificationPermissionAndRegister()
    }

    val mainContent: @Composable () -> Unit = {
        when {
            currentUser != null -> {
                TravelNavigation(
                    authViewModel = authViewModel,
                    chatViewModel = chatViewModel,
                    travelViewModel = travelViewModel,
                    wishlistRepository = wishlistRepository,
                    currentUser = currentUser!!
                )
            }
            else -> {
                LoginScreen(
                    authState = authState,
                    onSignIn = { email, password -> authViewModel.signIn(email, password) },
                    onSignUp = { email, password -> authViewModel.signUp(email, password) },
                    onAnonymousSignIn = { authViewModel.signInAnonymously() },
                    onGoogleSignIn = onGoogleSignIn
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        mainContent()

        if (showMaintenance) {
            activity?.let { act ->
                MaintenanceDialog(
                    config = appConfig,
                    onDismiss = { configViewModel.dismissMaintenance() }
                )
            }
        }

        if (showForceUpdate) {
            activity?.let { act ->
                ForceUpdateDialog(
                    config = appConfig,
                    activity = act,
                    onDismiss = { configViewModel.dismissForceUpdate() },
                    onUpdate = {
                        configViewModel.openPlayStore(act)
                        configViewModel.dismissForceUpdate()
                    }
                )
            }
        }

        if (showOptionalUpdate) {
            activity?.let { act ->
                OptionalUpdateDialog(
                    config = appConfig,
                    activity = act,
                    onDismiss = { configViewModel.dismissOptionalUpdate() },
                    onUpdate = {
                        configViewModel.openPlayStore(act)
                        configViewModel.dismissOptionalUpdate()
                    }
                )
            }
        }

        if (showAnnouncement) {
            AnnouncementDialog(
                config = appConfig,
                onDismiss = { configViewModel.dismissAnnouncement() },
                onLinkClick = {
                    activity?.let { act -> configViewModel.openAnnouncementLink(act) }
                }
            )
        }
    }
}

@Composable
private fun RequestNotificationPermissionAndRegister() {
    val context = androidx.compose.ui.platform.LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+: Need runtime permission
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                android.util.Log.d("FCM", "Notification permission granted")
                MyFirebaseMessagingService().registerFCMToken()
            } else {
                android.util.Log.w("FCM", "Notification permission denied")
            }
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.d("FCM", "Requesting notification permission")
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                android.util.Log.d("FCM", "Permission already granted, registering FCM token")
                MyFirebaseMessagingService().registerFCMToken()
            }
        }
    } else {
        // Pre-Android 13: No runtime permission needed
        LaunchedEffect(Unit) {
            android.util.Log.d("FCM", "Pre-Tiramisu, registering FCM token directly")
            MyFirebaseMessagingService().registerFCMToken()
        }
    }
}