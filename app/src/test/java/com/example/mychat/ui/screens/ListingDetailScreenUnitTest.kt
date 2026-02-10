package com.example.mychat.ui.screens

import com.example.mychat.data.model.TravelListing
import com.example.mychat.data.model.PlaceCovered
import org.junit.Test
import org.junit.Assert.*

class ListingDetailScreenUnitTest {

    @Test
    fun travelListing_hasCorrectPackageTypeDisplay() {
        // Test international package
        val internationalListing = TravelListing(
            id = "1",
            title = "Japan Tour Package",
            description = "Amazing tour of Japan",
            price = 7000.0,
            duration = 2,
            destination = "Tokyo",
            type = "adventure",
            agencyId = "agency1",
            agencyName = "Adventure Travels",
            packageType = "international",
            countryName = "Japan",
            placesCovered = listOf(
                PlaceCovered("1", "Tokyo"),
                PlaceCovered("2", "Kyoto")
            ),
            hotelType = "premium",
            mealPlan = "all-meals",
            cost = 7000.0
        )

        // Verify package type is international
        assertEquals("international", internationalListing.packageType)
        assertEquals("Japan", internationalListing.countryName)
        assertEquals(2, internationalListing.placesCovered.size)
        assertEquals("premium", internationalListing.hotelType)
        assertEquals("all-meals", internationalListing.mealPlan)
    }

    @Test
    fun travelListing_hasCorrectDomesticPackageDisplay() {
        // Test domestic package
        val domesticListing = TravelListing(
            id = "2",
            title = "Maharashtra Tour",
            description = "Tour of Maharashtra",
            price = 25000.0,
            duration = 5,
            destination = "Mumbai",
            type = "cultural",
            agencyId = "agency2",
            agencyName = "Cultural Tours",
            packageType = "domestic",
            stateName = "Maharashtra",
            placesCovered = listOf(
                PlaceCovered("1", "Mumbai"),
                PlaceCovered("2", "Pune")
            ),
            hotelType = "standard",
            mealPlan = "breakfast-only",
            cost = 25000.0
        )

        // Verify package type is domestic
        assertEquals("domestic", domesticListing.packageType)
        assertEquals("Maharashtra", domesticListing.stateName)
        assertEquals(2, domesticListing.placesCovered.size)
        assertEquals("standard", domesticListing.hotelType)
        assertEquals("breakfast-only", domesticListing.mealPlan)
    }

    @Test
    fun travelListing_handlesMissingDataGracefully() {
        // Test with missing optional fields
        val basicListing = TravelListing(
            id = "3",
            title = "Basic Package",
            description = "Basic tour package",
            price = 1000.0,
            duration = 1,
            destination = "Local",
            type = "budget",
            agencyId = "agency3",
            agencyName = "Local Agency",
            packageType = null,
            countryName = null,
            stateName = null,
            placesCovered = emptyList(),
            hotelType = null,
            mealPlan = null,
            cost = null
        )

        // Verify basic fields are still present
        assertEquals("Basic Package", basicListing.title)
        assertEquals(1, basicListing.duration)
        assertEquals("budget", basicListing.type)
        assertEquals("Local Agency", basicListing.agencyName)
        
        // Verify optional fields are null
        assertNull(basicListing.packageType)
        assertNull(basicListing.countryName)
        assertNull(basicListing.stateName)
        assertTrue(basicListing.placesCovered.isEmpty())
        assertNull(basicListing.hotelType)
        assertNull(basicListing.mealPlan)
        assertNull(basicListing.cost)
    }

    @Test
    fun travelListing_placesCoveredJoinsCorrectly() {
        val listing = TravelListing(
            id = "1",
            title = "Test Package",
            description = "Test",
            price = 1000.0,
            duration = 3,
            destination = "Test",
            type = "test",
            agencyId = "agency4",
            agencyName = "Test Agency",
            placesCovered = listOf(
                PlaceCovered("1", "Tokyo"),
                PlaceCovered("2", "Kyoto"),
                PlaceCovered("3", "Osaka")
            )
        )

        // Test that places are joined correctly
        val placesString = listing.placesCovered.joinToString(", ") { it.name }
        assertEquals("Tokyo, Kyoto, Osaka", placesString)
    }
}