data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val status: String = "",
    val timestamp: Long = 0L,
    val description: String = "",
    val userId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "amount" to amount,
            "currency" to currency,
            "status" to status,
            "timestamp" to timestamp,
            "description" to description,
            "userId" to userId
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Transaction {
            return Transaction(
                id = map["id"] as? String ?: "",
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                currency = map["currency"] as? String ?: "",
                status = map["status"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
                description = map["description"] as? String ?: "",
                userId = map["userId"] as? String ?: ""
            )
        }
    }
}
