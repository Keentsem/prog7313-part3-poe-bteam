package vcmsa.projects.loginpage.ExpenseElements

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ExpenseFirebaseRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val expensesCollection = firestore.collection("expenses")

    suspend fun addExpenseToFirebase(expense: Expense): String {
        val docRef = expensesCollection.add(expense).await()
        return docRef.id
    }

    suspend fun updateExpenseInFirebase(expense: Expense) {
        val firebaseId = expense.firebaseId
        if (firebaseId != null) {
            expensesCollection.document(firebaseId).set(expense).await()
        } else {
            throw IllegalArgumentException("Firebase ID is null, cannot update expense")
        }
    }

    suspend fun getAllExpensesFromFirebase(): List<Expense> {
        val snapshot = expensesCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
    }

    suspend fun getFilteredExpensesFirebase(
        category: String,
        filterStartDate: String,
        filterEndDate: String
    ): List<Expense> {
        var query: Query = expensesCollection

        if (category != "All Categories") {
            query = query.whereEqualTo("categoryName", category)
        }

        query = query.whereLessThanOrEqualTo("startDate", filterEndDate)
        val snapshot = query.get().await()

        return snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
            .filter { expense -> expense.endDate >= filterStartDate }
    }

    suspend fun deleteExpenseFromFirebase(firebaseId: String) {
        expensesCollection.document(firebaseId).delete().await()
    }
}
