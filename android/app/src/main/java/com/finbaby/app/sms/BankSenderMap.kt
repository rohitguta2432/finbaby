package com.finbaby.app.sms

/**
 * Step 1: Sender ID Lookup (HashMap)
 * Maps ALL known financial SMS sender IDs — banks, UPI, wallets, e-commerce,
 * food delivery, subscriptions, recharge, insurance, mutual funds, etc.
 */
object BankSenderMap {

    data class BankInfo(
        val bankName: String,
        val defaultAccountType: String = "bank", // "bank", "upi", "credit_card", "wallet"
        val defaultCategoryId: Long? = null // auto-assign category if known
    )

    private val senderMap = hashMapOf(
        // ==================== BANKS ====================
        // SBI
        "SBIINB" to BankInfo("SBI"),
        "SBIPSG" to BankInfo("SBI"),
        "SBIBNK" to BankInfo("SBI"),
        "SBISMS" to BankInfo("SBI"),
        "SBIUPI" to BankInfo("SBI", "upi"),

        // HDFC
        "HDFCBK" to BankInfo("HDFC"),
        "HDFCBN" to BankInfo("HDFC"),
        "HDFCCC" to BankInfo("HDFC", "credit_card"),
        "HDFCUP" to BankInfo("HDFC", "upi"),

        // ICICI
        "ICICIB" to BankInfo("ICICI"),
        "ICICIS" to BankInfo("ICICI"),
        "ICICCC" to BankInfo("ICICI", "credit_card"),

        // Axis
        "AXISBK" to BankInfo("Axis"),
        "AXISBN" to BankInfo("Axis"),

        // Kotak
        "KOTAKB" to BankInfo("Kotak"),
        "KOTKBK" to BankInfo("Kotak"),

        // PNB
        "PNBSMS" to BankInfo("PNB"),

        // BOB
        "BOBTXN" to BankInfo("BOB"),
        "BOBSMS" to BankInfo("BOB"),

        // Canara
        "CANBKK" to BankInfo("Canara"),

        // Union Bank
        "UBISMS" to BankInfo("Union"),

        // IndusInd
        "INDBNK" to BankInfo("IndusInd"),

        // Yes Bank
        "YESBKL" to BankInfo("Yes Bank"),

        // Federal
        "FEDBNK" to BankInfo("Federal"),

        // IDFC First
        "IDFCFB" to BankInfo("IDFC First"),

        // Bandhan
        "BANDBK" to BankInfo("Bandhan"),

        // RBL
        "RBLBNK" to BankInfo("RBL", "credit_card"),

        // ==================== UPI & WALLETS ====================
        // Google Pay
        "GPAYCC" to BankInfo("Google Pay", "upi"),
        "GPAYTM" to BankInfo("Google Pay", "upi"),
        "GOOGLP" to BankInfo("Google Pay", "upi"),

        // Paytm
        "PAYTMB" to BankInfo("Paytm", "wallet"),
        "PYTMUP" to BankInfo("Paytm", "upi"),
        "PAYTMS" to BankInfo("Paytm", "wallet"),
        "PAYTMK" to BankInfo("Paytm", "wallet"),

        // PhonePe
        "PHONEPE" to BankInfo("PhonePe", "upi"),
        "PHNEPE" to BankInfo("PhonePe", "upi"),
        "PHNPAY" to BankInfo("PhonePe", "upi"),

        // Amazon Pay
        "AMZNPY" to BankInfo("Amazon Pay", "wallet"),
        "AMAZNP" to BankInfo("Amazon Pay", "wallet"),

        // Mobikwik
        "MOBIKW" to BankInfo("Mobikwik", "wallet"),

        // Freecharge
        "FRECHR" to BankInfo("Freecharge", "wallet"),

        // CRED
        "CREDAP" to BankInfo("CRED", "credit_card"),

        // Slice
        "SLICEP" to BankInfo("Slice", "credit_card"),

        // ==================== FOOD DELIVERY ====================
        "SWIGGY" to BankInfo("Swiggy", "wallet", defaultCategoryId = 2),
        "SWIGYY" to BankInfo("Swiggy", "wallet", defaultCategoryId = 2),
        "ZOMATO" to BankInfo("Zomato", "wallet", defaultCategoryId = 2),
        "ZMTORD" to BankInfo("Zomato", "wallet", defaultCategoryId = 2),

        // ==================== E-COMMERCE ====================
        "AMAZON" to BankInfo("Amazon", "bank", defaultCategoryId = 7),
        "AMZNIN" to BankInfo("Amazon", "bank", defaultCategoryId = 7),
        "FLPKRT" to BankInfo("Flipkart", "bank", defaultCategoryId = 7),
        "FLIPKT" to BankInfo("Flipkart", "bank", defaultCategoryId = 7),
        "MYNTRA" to BankInfo("Myntra", "bank", defaultCategoryId = 7),
        "MEESHO" to BankInfo("Meesho", "bank", defaultCategoryId = 7),
        "AJIOAP" to BankInfo("Ajio", "bank", defaultCategoryId = 7),

        // ==================== CAB / TRANSPORT ====================
        "UBERIN" to BankInfo("Uber", "upi", defaultCategoryId = 12),
        "OLACAB" to BankInfo("Ola", "upi", defaultCategoryId = 12),
        "RAPIDO" to BankInfo("Rapido", "upi", defaultCategoryId = 12),
        "IRCTCW" to BankInfo("IRCTC", "bank", defaultCategoryId = 12),

        // ==================== RECHARGE / TELECOM ====================
        "AIRTEL" to BankInfo("Airtel", "bank", defaultCategoryId = 14),
        "JIOINF" to BankInfo("Jio", "bank", defaultCategoryId = 14),
        "ABORTI" to BankInfo("Airtel", "bank", defaultCategoryId = 14),
        "VIINFO" to BankInfo("Vi", "bank", defaultCategoryId = 14),

        // ==================== SUBSCRIPTIONS / OTT ====================
        "NFLXIN" to BankInfo("Netflix", "bank", defaultCategoryId = 11),
        "HTSTAR" to BankInfo("Hotstar", "bank", defaultCategoryId = 11),
        "SPTIFO" to BankInfo("Spotify", "bank", defaultCategoryId = 11),

        // ==================== INSURANCE ====================
        "LICIND" to BankInfo("LIC", "bank"),
        "SBIGEN" to BankInfo("SBI General", "bank"),
        "HDFCLF" to BankInfo("HDFC Life", "bank"),
        "ICICPR" to BankInfo("ICICI Pru", "bank"),

        // ==================== MUTUAL FUNDS / INVESTMENTS ====================
        "GROWWI" to BankInfo("Groww", "bank"),
        "ZERODH" to BankInfo("Zerodha", "bank"),
        "KUVERA" to BankInfo("Kuvera", "bank"),

        // ==================== UTILITIES ====================
        "BESCOM" to BankInfo("BESCOM", "bank", defaultCategoryId = 5),
        "TATAPO" to BankInfo("Tata Power", "bank", defaultCategoryId = 5),

        // ==================== FUEL ====================
        "HPCL00" to BankInfo("HP Petrol", "bank", defaultCategoryId = 3),
        "INDOIL" to BankInfo("Indian Oil", "bank", defaultCategoryId = 3),
        "BPCL00" to BankInfo("BPCL", "bank", defaultCategoryId = 3),

        // ==================== MEDICAL ====================
        "APLLPH" to BankInfo("Apollo", "bank", defaultCategoryId = 6),
        "ONEMGR" to BankInfo("1mg", "bank", defaultCategoryId = 6),
        "PHARME" to BankInfo("PharmEasy", "bank", defaultCategoryId = 6),
        "MEDPLS" to BankInfo("MedPlus", "bank", defaultCategoryId = 6),

        // ==================== GROCERIES ====================
        "BIGBSK" to BankInfo("BigBasket", "bank", defaultCategoryId = 1),
        "BLNKIT" to BankInfo("Blinkit", "bank", defaultCategoryId = 1),
        "ZEPNOW" to BankInfo("Zepto", "bank", defaultCategoryId = 1),
        "DMARTD" to BankInfo("DMart", "bank", defaultCategoryId = 1),

        // Credit Cards (Generic)
        "AMEXIN" to BankInfo("Amex", "credit_card"),
        "CITIBK" to BankInfo("Citi", "credit_card"),
        "SCBANK" to BankInfo("SC", "credit_card"),
    )

    /**
     * Lookup sender ID. Handles formats like:
     * "AD-HDFCBK", "VM-SBIINB", "JD-ICICIB", "HDFCBK", etc.
     */
    fun lookup(senderId: String): BankInfo? {
        val cleaned = senderId
            .uppercase()
            .replace(Regex("^[A-Z]{2}-"), "") // Remove AD-, VM-, JD- prefixes
            .trim()

        // Direct match
        senderMap[cleaned]?.let { return it }

        // Partial match — check if sender contains any known key
        return senderMap.entries
            .firstOrNull { cleaned.contains(it.key) }
            ?.value
    }

    fun isFinancialSms(senderId: String): Boolean {
        return lookup(senderId) != null
    }
}
