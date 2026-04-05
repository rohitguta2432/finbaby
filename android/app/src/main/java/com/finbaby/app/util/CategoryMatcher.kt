package com.finbaby.app.util

object CategoryMatcher {

    private val keywordMap = mapOf(
        // Food Ordering
        "swiggy" to 2L, "zomato" to 2L, "dominos" to 2L, "pizza" to 2L,
        "burger" to 2L, "kfc" to 2L, "mcdonalds" to 2L, "uber eats" to 2L,

        // Groceries
        "bigbasket" to 1L, "blinkit" to 1L, "zepto" to 1L, "dmart" to 1L,
        "bigbazaar" to 1L, "reliance" to 1L, "grocery" to 1L, "vegetables" to 1L,
        "milk" to 1L, "fruits" to 1L,

        // Transport
        "uber" to 12L, "ola" to 12L, "rapido" to 12L, "auto" to 12L,
        "metro" to 12L, "bus" to 12L, "train" to 12L, "irctc" to 12L,

        // Petrol
        "petrol" to 3L, "diesel" to 3L, "cng" to 3L, "fuel" to 3L,
        "hp" to 3L, "indian oil" to 3L, "bharat petroleum" to 3L,

        // Bills
        "electricity" to 5L, "water bill" to 5L, "gas bill" to 5L,
        "internet" to 5L, "broadband" to 5L, "wifi" to 5L,

        // Recharge
        "airtel" to 14L, "jio" to 14L, "vi" to 14L, "bsnl" to 14L,
        "recharge" to 14L,

        // Entertainment
        "netflix" to 11L, "hotstar" to 11L, "prime" to 11L, "spotify" to 11L,
        "movie" to 11L, "theatre" to 11L, "youtube" to 11L,

        // Medical
        "pharmacy" to 6L, "medicine" to 6L, "doctor" to 6L, "hospital" to 6L,
        "apollo" to 6L, "medplus" to 6L, "1mg" to 6L, "pharmeasy" to 6L,

        // Shopping
        "amazon" to 7L, "flipkart" to 7L, "myntra" to 7L, "ajio" to 7L,
        "meesho" to 7L, "nykaa" to 7L,

        // Education
        "school" to 8L, "college" to 8L, "tuition" to 8L, "coaching" to 8L,
        "books" to 8L, "udemy" to 8L, "coursera" to 8L,

        // EMI
        "emi" to 10L, "loan" to 10L, "credit card" to 10L,
    )

    fun suggestCategoryId(note: String): Long? {
        val lower = note.lowercase()
        return keywordMap.entries
            .firstOrNull { lower.contains(it.key) }
            ?.value
    }
}
