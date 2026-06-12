# Task: Make Android App UI Same as WebApp

## Analysis

The WebApp (Travel-Agency-WebApp) has a much more polished, modern UI compared to the Android app. Key differences:

### WebApp Features Missing/Inferior in Android App:

1. **Header/Branding**: "BOMTRA" logo with orange accent, dark theme header
2. **Hero Banner**: Gradient blue hero section with "BOOK YOUR TOUR WITH US"
3. **Search Bar**: Pill-shaped, integrated in header
4. **Location/Pincode Display**: With modal for changing
5. **Package Comparison**: Scale icon, comparison bar, can compare up to 3
6. **Booking Flow**: Multi-step wizard with progress indicator (steps 1-4)
7. **Booking History**: Ticket-style design with status colors
8. **User Profile**: Avatar, co-travellers, edit profile
9. **Wishlist**: Heart icon toggle with animation
10. **Category Filter**: WebApp has cleaner category chips
11. **Card Design**: Better pricing display, action buttons layout
12. **Detail View**: Enhanced with itinerary, inclusions/exclusions
13. **Color Scheme**: Dark header gradient, orange accents

## Files to Modify
1. TravelDashboard.kt - Main listings page
2. ListingDetailScreen.kt / EnhancedListingDetailScreen.kt - Detail view
3. BookingScreen.kt - Booking flow
4. WishlistScreen.kt - Wishlist page