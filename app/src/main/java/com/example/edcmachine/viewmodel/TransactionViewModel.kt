package com.example.edcmachine.viewmodel

import Transaction
import TransactionRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TransactionState {
    object WaitingForTransaction : TransactionState()
    data class NewDataDetected(val transaction: Transaction) : TransactionState()
    data class PaymentSuccessful(val transaction: Transaction) : TransactionState()
    data class ShowingSummary(val transaction: Transaction) : TransactionState()
}

class TransactionViewModel(
    private val repository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<TransactionState>(TransactionState.WaitingForTransaction)
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private var lastProcessedTransactionId: String? = null
    private var currentTransactionId: String? = null

    init {
        listenToTransactions()
    }

    private fun listenToTransactions() {
        viewModelScope.launch {
            repository.listenToTransactions().collect { transactions ->
                val latestTransaction = transactions.firstOrNull()

                if (latestTransaction != null && latestTransaction.id != lastProcessedTransactionId) {
                    // New transaction detected
                    currentTransactionId = latestTransaction.id
                    _state.value = TransactionState.NewDataDetected(latestTransaction)

                    // Start listening to this specific transaction for status updates
                    listenToTransactionStatus(latestTransaction.id)
                }
            }
        }
    }

    private fun listenToTransactionStatus(transactionId: String) {
        viewModelScope.launch {
            repository.listenToSpecificTransaction(transactionId).collect { transaction ->
                if (transaction != null && transaction.id == currentTransactionId) {
                    when (transaction.status.lowercase()) {
                        "success" -> {
                            _state.value = TransactionState.PaymentSuccessful(transaction)

                            // Wait 2 seconds then show summary
                            delay(2000)
                            _state.value = TransactionState.ShowingSummary(transaction)

                            // Wait a bit more then reset to waiting state
                            delay(3000)
                            lastProcessedTransactionId = transaction.id
                            currentTransactionId = null
                            _state.value = TransactionState.WaitingForTransaction
                        }
                        "failed" -> {
                            // Handle failed transaction if needed
                            lastProcessedTransactionId = transaction.id
                            currentTransactionId = null
                            _state.value = TransactionState.WaitingForTransaction
                        }
                        // For other statuses (pending, processing), keep showing "waiting for status"
                    }
                }
            }
        }
    }

    fun resetToWaiting() {
        lastProcessedTransactionId = currentTransactionId
        currentTransactionId = null
        _state.value = TransactionState.WaitingForTransaction
    }
}