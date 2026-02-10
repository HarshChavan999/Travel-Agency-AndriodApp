package com.example.mychat.ui.screens

import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.PlaceCovered
import org.junit.Test
import org.junit.Assert.*

class EnhancedPackageDisplayTest {

    @Test
    fun testTokyoListingDisplay() {
        // Create a Tokyo listing similar to the one in Firebase
        val tokyoListing = TravelListing(
            id = "krxZhOWJWBBdBxHCzMtN",
            title = "Japan Package", // This should be the fallback title
            description = "Experience the vibrant culture and modern wonders of Tokyo, Japan's bustling capital city.",
            price = 7000.0,
            duration = 7,
            destination = "Tokyo",
            type = "cultural",
            agencyId = "testAgency",
            agencyName = "Test Travel Agency",
            packageType = "international",
            countryName = "Japan",
            placesCovered = listOf(
                PlaceCovered("1", "Tokyo")
            ),
            hotelType = "premium",
            mealPlan = "all-meals",
            cost = 7000.0
        )

        // Test that all enhanced package fields are present
        assertEquals("international", tokyoListing.packageType)
        assertEquals("Japan", tokyoListing.countryName)
        assertEquals(1, tokyoListing.placesCovered.size)
        assertEquals("Tokyo", tokyoListing.placesCovered.first().name)
        assertEquals("premium", tokyoListing.hotelType)
        assertEquals("all-meals", tokyoListing.mealPlan)
        assertEquals(7000.0, tokyoListing.cost)
        
        // Test that the title is properly set
        assertEquals("Japan Package", tokyoListing.title)
        
        // Test that price display logic works
        val displayPrice = if (tokyoListing.cost != null) {
            tokyoListing.cost!!.toInt()
        } else {
            tokyoListing.price.toInt()
        }
        assertEquals(7000, displayPrice)
    }

    @Test
    fun testDisplayLogicForEnhancedFields() {
        val listing = TravelListing(
            id = "test",
            title = "Test Package",
            description = "Test",
            price = 1000.0,
            duration = 3,
            destination = "Test",
            type = "test",
            agencyId = "test",
            agencyName = "Test Agency",
            packageType = "international",
            countryName = "Test Country",
            placesCovered = listOf(PlaceCovered("1", "Test Place")),
            hotelType = "luxury",
            mealPlan = "full-board",
            cost = 2500.0
        )

        // Test package type display logic
        val packageTypeDisplay = when (listing.packageType) {
            "international" -> "🌍 International"
            "domestic" -> "🏠 Domestic"
            else -> "✈️ Package"
        }
        assertEquals("🌍 International", packageTypeDisplay)

        // Test location display logic
        val locationDisplay = when (listing.packageType) {
            "international" -> listing.countryName ?: "Country not specified"
            "domestic" -> listing.stateName ?: "State not specified"
            else -> listing.destination
        }
        assertEquals("Test Country", locationDisplay)

        // Test places covered display
        val placesDisplay = if (listing.placesCovered != null && listing.placesCovered.isNotEmpty()) {
            listing.placesCovered.joinToString(", ") { it.name }
        } else {
            "No specific places mentioned"
        }
        assertEquals("Test Place", placesDisplay)

        // Test hotel type display
        val hotelTypeDisplay = listing.hotelType?.replaceFirstChar { it.uppercase() } ?: "Not specified"
        assertEquals("Luxury", hotelTypeDisplay)

        // Test meal plan display
        val mealPlanDisplay = listing.mealPlan?.replaceFirstChar { it.uppercase() } ?: "Not specified"
        assertEquals("Full-board", mealPlanDisplay)
    }
}