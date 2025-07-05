import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun listenToTransactions(): Flow<List<Transaction>> = callbackFlow {

        println("Listening for new transactions ...")

        val listener = firestore.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {

                    println("Error while listening for new transactions: $error")

                    close(error)
                    return@addSnapshotListener
                }

                println("Transaction found!")

                val transactions = snapshot?.documents?.map { doc ->
                    Transaction.fromMap(doc.data ?: emptyMap()).copy(id = doc.id)
                } ?: emptyList()

                println("Transaction: $transactions")

                trySend(transactions)
            }

        awaitClose { listener.remove() }
    }

    fun listenToSpecificTransaction(transactionId: String): Flow<Transaction?> = callbackFlow {

        println("Listening to document: $transactionId")

        val listener = firestore.collection("transactions")
            .document(transactionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {

                    println("There is an error: $error")

                    close(error)
                    return@addSnapshotListener
                }

                println("Document data changed!")

                val transaction = snapshot?.data?.let { data ->
                    Transaction.fromMap(data).copy(id = snapshot.id)
                }

                trySend(transaction)
            }

        awaitClose { listener.remove() }
    }
}
