package com.example.mychat.data.sample

import com.example.mychat.data.model.*

object SampleData {
    
    fun getSampleItinerary(): List<ItineraryDay> {
        return listOf(
            ItineraryDay(
                day = 1,
                placeName = "Chandigarh",
                description = "Arrive in Chandigarh and check-in to your hotel. Enjoy a welcome dinner and get briefed about the tour.",
                activities = listOf("Hotel check-in", "Welcome dinner", "Tour briefing"),
                accommodation = "Hotel Mount View"
            ),
            ItineraryDay(
                day = 2,
                placeName = "Manali",
                description = "Drive to Manali via Kullu Valley. Visit Kullu Shawl Factory and enjoy the scenic beauty of the valley.",
                activities = listOf("Drive to Manali", "Visit Kullu Shawl Factory", "Scenic valley views"),
                accommodation = "Solang Valley Resort"
            ),
            ItineraryDay(
                day = 3,
                placeName = "Manali",
                description = "Full day sightseeing in Manali. Visit Hadimba Temple, Vashisht Hot Springs, and Mall Road.",
                activities = listOf("Hadimba Temple", "Vashisht Hot Springs", "Mall Road shopping"),
                accommodation = "Solang Valley Resort"
            ),
            ItineraryDay(
                day = 4,
                placeName = "Shimla",
                description = "Drive to Shimla. En route visit Rohtang Pass (subject to weather conditions).",
                activities = listOf("Drive to Shimla", "Rohtang Pass visit", "Shimla arrival"),
                accommodation = "Wildflower Hall"
            ),
            ItineraryDay(
                day = 5,
                placeName = "Shimla",
                description = "Explore Shimla city. Visit The Ridge, Mall Road, Christ Church, and Jakhoo Temple.",
                activities = listOf("The Ridge", "Mall Road", "Christ Church", "Jakhoo Temple"),
                accommodation = "Wildflower Hall"
            )
        )
    }
    
    fun getSampleInclusions(): String {
        return """
            |Accommodation in 4-star hotels
            |All meals (Breakfast, Lunch, Dinner)
            |Sightseeing as per itinerary
            |All entrance fees and permits
            |Professional tour guide
            |All transfers and transportation
            |Airport/railway station pickup and drop
        """.trimMargin()
    }
    
    fun getSampleExclusions(): String {
        return """
            |Airfare/train fare to and from destination
            |Personal expenses (laundry, phone calls, etc.)
            |Tips and gratuities
            |Travel insurance
            |Any expenses caused by circumstances beyond our control
            |Additional sightseeing not mentioned in the itinerary
        """.trimMargin()
    }
    
    fun getSampleFAQs(): List<FAQ> {
        return listOf(
            FAQ(
                question = "What is the best time to visit?",
                answer = "The best time to visit is from March to June and September to November when the weather is pleasant and ideal for sightseeing."
            ),
            FAQ(
                question = "Is travel insurance included?",
                answer = "Travel insurance is not included by default but can be added as an optional extra during booking. We recommend all travelers have comprehensive travel insurance."
            ),
            FAQ(
                question = "Can I customize the itinerary?",
                answer = "Yes! We offer flexible itineraries. You can discuss customization options with our travel experts after booking. Additional charges may apply for major changes."
            ),
            FAQ(
                question = "What is the cancellation policy?",
                answer = "Cancellations made 30+ days before departure receive a full refund. 15-30 days: 75% refund. 7-14 days: 50% refund. Less than 7 days: no refund."
            ),
            FAQ(
                question = "Are meals included in the package?",
                answer = "Yes, all meals (Breakfast, Lunch, and Dinner) are included as mentioned in the Tour Inclusion section."
            )
        )
    }
    
    fun getSamplePhotoUrls(): List<String> {
        return listOf(
            "https://example.com/photos/chandigarh-rock-garden.jpg",
            "https://example.com/photos/manali-solang-valley.jpg",
            "https://example.com/photos/shimla-mall-road.jpg",
            "https://example.com/photos/kullu-valley.jpg",
            "https://example.com/photos/rohtang-pass.jpg"
        )
    }
    
    fun createEnhancedTravelListing(): TravelListing {
        return TravelListing(
            id = "pkg_001",
            title = "Himachal Paradise Tour - 5 Days/4 Nights",
            description = "Experience the beauty of Himachal Pradesh with this comprehensive tour covering Chandigarh, Manali, and Shimla. Enjoy scenic drives, cultural experiences, and comfortable accommodation.",
            price = 25000.0,
            duration = 5,
            destination = "Himachal Pradesh",
            type = "family",
            photos = listOf(
                "https://example.com/photos/himachal-tour-main.jpg"
            ),
            rating = 4.8,
            reviewsCount = 127,
            agencyId = "agency_001",
            agencyName = "Himalayan Adventures",
            agencyData = Agency(
                id = "agency_001",
                companyName = "Himalayan Adventures",
                name = "Rajesh Kumar",
                email = "info@himalayanadventures.com",
                approved = true
            ),
            approved = true,
            packageType = "domestic",
            countryName = null,
            stateName = "Himachal Pradesh",
            placesCovered = listOf(
                PlaceCovered(
                    id = "place_001",
                    name = "Chandigarh",
                    imageUrls = listOf("https://example.com/photos/chandigarh.jpg")
                ),
                PlaceCovered(
                    id = "place_002",
                    name = "Manali",
                    imageUrls = listOf("https://example.com/photos/manali.jpg")
                ),
                PlaceCovered(
                    id = "place_003",
                    name = "Shimla",
                    imageUrls = listOf("https://example.com/photos/shimla.jpg")
                )
            ),
            hotelType = "4-star",
            mealPlan = "Full board",
            cost = 25000.0,
            // Enhanced features
            itinerary = getSampleItinerary(),
            inclusions = getSampleInclusions(),
            exclusions = getSampleExclusions(),
            faqs = getSampleFAQs(),
            packageCode = "HP001",
            tourCategories = listOf("Family", "Honeymoon", "Adventure"),
            photoUrls = getSamplePhotoUrls(),
            videoUrl = "https://example.com/videos/himachal-tour.mp4"
        )
    }
    
    fun createBasicTravelListing(): TravelListing {
        return TravelListing(
            id = "pkg_002",
            title = "Goa Beach Getaway - 4 Days/3 Nights",
            description = "Relax and unwind on the beautiful beaches of Goa with this budget-friendly package.",
            price = 15000.0,
            duration = 4,
            destination = "Goa",
            type = "budget",
            photos = listOf(
                "https://example.com/photos/goa-beach.jpg"
            ),
            rating = 4.2,
            reviewsCount = 89,
            agencyId = "agency_002",
            agencyName = "Sunset Travels",
            agencyData = Agency(
                id = "agency_002",
                companyName = "Sunset Travels",
                name = "Anita Desai",
                email = "contact@sunsettravels.com",
                approved = false
            ),
            approved = true,
            packageType = "domestic",
            countryName = null,
            stateName = "Goa",
            placesCovered = listOf(
                PlaceCovered(
                    id = "place_004",
                    name = "Calangute",
                    imageUrls = listOf("https://example.com/photos/calangute.jpg")
                ),
                PlaceCovered(
                    id = "place_005",
                    name = "Baga",
                    imageUrls = listOf("https://example.com/photos/baga.jpg")
                )
            ),
            hotelType = "3-star",
            mealPlan = "Bed & Breakfast",
            cost = 15000.0
            // No enhanced features - will use basic screen
        )
    }
    
    fun createInternationalListing(): TravelListing {
        return TravelListing(
            id = "pkg_003",
            title = "Swiss Alps Adventure - 8 Days/7 Nights",
            description = "Experience the majestic Swiss Alps with this premium tour including Zurich, Interlaken, and Zermatt.",
            price = 2800.0,
            duration = 8,
            destination = "Switzerland",
            type = "luxury",
            photos = listOf(
                "https://example.com/photos/swiss-alps.jpg"
            ),
            rating = 4.9,
            reviewsCount = 56,
            agencyId = "agency_003",
            agencyName = "Euro Luxury Tours",
            agencyData = Agency(
                id = "agency_003",
                companyName = "Euro Luxury Tours",
                name = "Hans Müller",
                email = "info@euroluxury.com",
                approved = true
            ),
            approved = true,
            packageType = "international",
            countryName = "Switzerland",
            stateName = null,
            placesCovered = listOf(
                PlaceCovered(
                    id = "place_006",
                    name = "Zurich",
                    imageUrls = listOf("https://example.com/photos/zurich.jpg")
                ),
                PlaceCovered(
                    id = "place_007",
                    name = "Interlaken",
                    imageUrls = listOf("https://example.com/photos/interlaken.jpg")
                ),
                PlaceCovered(
                    id = "place_008",
                    name = "Zermatt",
                    imageUrls = listOf("https://example.com/photos/zermatt.jpg")
                )
            ),
            hotelType = "5-star",
            mealPlan = "Full board",
            cost = 2800.0,
            // Enhanced features for international package
            itinerary = listOf(
                ItineraryDay(
                    day = 1,
                    placeName = "Zurich",
                    description = "Arrive in Zurich and check-in to your luxury hotel. Evening city tour.",
                    activities = listOf("Hotel check-in", "Zurich city tour", "Welcome dinner"),
                    accommodation = "Baur au Lac"
                ),
                ItineraryDay(
                    day = 2,
                    placeName = "Interlaken",
                    description = "Travel to Interlaken via scenic train journey. Explore the town and surrounding lakes.",
                    activities = listOf("Scenic train journey", "Lake Thun cruise", "Interlaken town tour"),
                    accommodation = "Victoria Jungfrau Grand Hotel"
                )
                // ... more days
            ),
            inclusions = """
                |5-star luxury accommodation
                |All meals including fine dining experiences
                |Private transfers and guided tours
                |All entrance fees and activities
                |Swiss Travel Pass
                |Travel insurance
            """.trimMargin(),
            exclusions = """
                |International airfare
                |Personal shopping expenses
                |Optional activities not mentioned in itinerary
            """.trimMargin(),
            faqs = listOf(
                FAQ(
                    question = "Do I need a visa for Switzerland?",
                    answer = "Yes, most travelers need a Schengen visa to visit Switzerland. We can assist with visa documentation."
                ),
                FAQ(
                    question = "What currency is used?",
                    answer = "Swiss Franc (CHF) is the official currency. Credit cards are widely accepted."
                )
            ),
            packageCode = "SW001",
            tourCategories = listOf("Luxury", "Honeymoon", "Cultural"),
            photoUrls = listOf(
                "https://example.com/photos/swiss-mountains.jpg",
                "https://example.com/photos/zurich-old-town.jpg"
            ),
            videoUrl = "https://example.com/videos/swiss-alps.mp4"
        )
    }
}