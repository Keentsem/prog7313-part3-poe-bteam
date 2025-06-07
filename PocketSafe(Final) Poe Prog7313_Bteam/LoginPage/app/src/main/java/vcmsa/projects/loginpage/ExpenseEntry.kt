package vcmsa.projects.loginpage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import android.animation.ObjectAnimator
import android.os.Build
import android.view.ViewOutlineProvider

import androidx.core.content.ContextCompat
import android.widget.ImageView

class ExpenseEntry : Activity() {

    private lateinit var categoryInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var startDateInput: EditText
    private lateinit var endDateInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var photoPreview: ImageView
    private lateinit var expensesDisplayLayout: LinearLayout

    private lateinit var addExpenseSection: LinearLayout
    private lateinit var showExpensesSection: LinearLayout

    private lateinit var filterCategoryInput: EditText
    private lateinit var filterStartDateInput: EditText
    private lateinit var filterEndDateInput: EditText
    private lateinit var filterSection: LinearLayout
    private lateinit var filterToggleButton: Button

    private var selectedPhotoUri: Uri? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F5EBDD")) // light oak
        }

        // Back button permanently placed a little below the top left of the screen
        val backButton = Button(this).apply {
            text = "Back"
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.parseColor("#5C3A21"))
            textSize = 16f
            gravity = Gravity.START
            // Use absolute positioning by setting margins and gravity carefully
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 30  // little below top
                marginStart = 20 // small left margin
                gravity = Gravity.START or Gravity.TOP
            }
            setOnClickListener {
                try {
                    val intent = Intent(this@ExpenseEntry, Class.forName("vcmsa.projects.loginpage.MainMenu"))
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    startActivity(intent)
                    finish()
                } catch (e: ClassNotFoundException) {
                    Toast.makeText(this@ExpenseEntry, "Main menu not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(backButton)

        // Animated coin ImageView (gold, bigger, slower rotation)
        // Container to hold the coin image and the "E" text overlay
        val coinContainer = FrameLayout(this).apply {
            val sizeInDp = 96  // bigger size now
            val scale = resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams((sizeInDp * scale).toInt(), (sizeInDp * scale).toInt())
        }

        // Circular coin background using a simple colored drawable (gold color)
        val coinView = ImageView(this).apply {
            setImageDrawable(ContextCompat.getDrawable(this@ExpenseEntry, android.R.drawable.presence_online))
            setColorFilter(Color.parseColor("#A68B6B")) // gold color

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // Make circular
            clipToOutline = true
            outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        // TextView with letter Money Emoji we wanna add flavour, centered on the coin
        val letterEView = TextView(this).apply {
            text = "\uD83D\uDCB8"
            textSize = 48f  // bigger text for bigger coin
            setTextColor(Color.parseColor("#5C3A21")) // dark brown text color
            gravity = Gravity.CENTER

            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        coinContainer.addView(coinView)
        coinContainer.addView(letterEView)

// Add the container to your layout
        layout.addView(coinContainer)

// Position horizontally centered where it currently is, move it up by 50 pixels
        coinContainer.post {
            val centerX = coinContainer.x + coinContainer.width / 2f
            coinContainer.x = centerX - coinContainer.width / 2f

            coinContainer.y = coinContainer.y - 50f  // move up by 50 pixels
        }

// Slow rotating animation on the whole container
        ObjectAnimator.ofFloat(coinContainer, "rotation", 0f, 360f).apply {
            duration = 8000L
            repeatCount = ObjectAnimator.INFINITE
            start()
        }


        // Add Expense header & toggle
        val addExpenseHeader = TextView(this).apply {
            text = "Add Expense"
            textSize = 24f
            setTextColor(Color.parseColor("#5C3A21"))
            setPadding(0, 0, 0, 10)
        }
        val toggleAddExpenseBtn = Button(this).apply {
            text = "Collapse Add Expense"
            setButtonStyle(this)
        }

        // Input fields
        categoryInput = createEditText("Category")
        descriptionInput = createEditText("Description")
        startDateInput = createDateInput("Start Date")
        endDateInput = createDateInput("End Date")
        amountInput = createEditText("Amount").apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        photoPreview = ImageView(this).apply {
            visibility = View.GONE
        }

        val photoButton = Button(this).apply {
            text = "Upload Receipt"
            setButtonStyle(this)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(intent, 0)
            }
        }

        val submitButton = Button(this).apply {
            text = "Submit Expense"
            setButtonStyle(this)
            setOnClickListener { submitExpense() }
        }

        val clearButton = Button(this).apply {
            text = "Clear Inputs"
            setButtonStyle(this)
            setOnClickListener { clearInputs() }
        }

        addExpenseSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(categoryInput)
            addView(descriptionInput)
            addView(startDateInput)
            addView(endDateInput)
            addView(amountInput)
            addView(photoButton)
            addView(photoPreview)
            addView(submitButton)
            addView(clearButton)
        }

        toggleAddExpenseBtn.setOnClickListener {
            if (addExpenseSection.visibility == View.VISIBLE) {
                addExpenseSection.visibility = View.GONE
                toggleAddExpenseBtn.text = "Expand Add Expense"
            } else {
                addExpenseSection.visibility = View.VISIBLE
                toggleAddExpenseBtn.text = "Collapse Add Expense"
            }
        }

        // Show Expenses header & toggle
        val showExpensesHeader = TextView(this).apply {
            text = "Show Expenses"
            textSize = 24f
            setTextColor(Color.parseColor("#5C3A21"))
            setPadding(0, 30, 0, 10)
        }
        val toggleShowExpensesBtn = Button(this).apply {
            text = "Collapse Expenses"
            setButtonStyle(this)
        }

        showExpensesSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        expensesDisplayLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 20, 10, 20)
        }
        val scrollView = ScrollView(this).apply { addView(expensesDisplayLayout) }

        // Filter section
        filterCategoryInput = createEditText("Filter by Category (optional)")
        filterStartDateInput = createDateInput("Filter Start Date (optional)")
        filterEndDateInput = createDateInput("Filter End Date (optional)")

        val filterButton = Button(this).apply {
            text = "Filter Expenses"
            setButtonStyle(this)
            setOnClickListener { filterExpenses() }
        }

        filterSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(filterCategoryInput)
            addView(filterStartDateInput)
            addView(filterEndDateInput)
            addView(filterButton)
        }

        filterToggleButton = Button(this).apply {
            text = "Collapse Filters"
            setButtonStyle(this)
            setOnClickListener {
                if (filterSection.visibility == View.VISIBLE) {
                    filterSection.visibility = View.GONE
                    text = "Expand Filters"
                } else {
                    filterSection.visibility = View.VISIBLE
                    text = "Collapse Filters"
                }
            }
        }

        val showExpensesButton = Button(this).apply {
            text = "Refresh Expenses"
            setButtonStyle(this)
            setOnClickListener { displayAllExpenses() }
        }

        showExpensesSection.apply {
            addView(filterToggleButton)
            addView(filterSection)
            addView(showExpensesButton)
            addView(scrollView)
        }

        toggleShowExpensesBtn.setOnClickListener {
            if (showExpensesSection.visibility == View.VISIBLE) {
                showExpensesSection.visibility = View.GONE
                toggleShowExpensesBtn.text = "Expand Expenses"
            } else {
                showExpensesSection.visibility = View.VISIBLE
                toggleShowExpensesBtn.text = "Collapse Expenses"
            }
        }

        // Add all to main layout
        layout.apply {
            addView(addExpenseHeader)
            addView(toggleAddExpenseBtn)
            addView(addExpenseSection)
            addView(showExpensesHeader)
            addView(toggleShowExpensesBtn)
            addView(showExpensesSection)
        }

        // Initialize sections collapsed
        addExpenseSection.visibility = View.GONE
        showExpensesSection.visibility = View.GONE
        filterSection.visibility = View.GONE
        toggleAddExpenseBtn.text = "Expand Add Expense"
        toggleShowExpensesBtn.text = "Expand Expenses"
        filterToggleButton.text = "Expand Filters"

        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5EBDD"))
            addView(layout)
        }

        setContentView(container)
    }

    private fun createEditText(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        setTextColor(Color.parseColor("#5C3A21"))
        setHintTextColor(Color.parseColor("#8B6F4A"))
    }

    private fun createDateInput(hintText: String): EditText = EditText(this).apply {
        hint = hintText
        inputType = android.text.InputType.TYPE_NULL
        isFocusable = false
        setTextColor(Color.parseColor("#5C3A21"))
        setHintTextColor(Color.parseColor("#8B6F4A"))
        setOnClickListener { showDatePicker { setText(it) } }
    }

    private fun setButtonStyle(button: Button) {
        button.setBackgroundColor(Color.parseColor("#D6B185"))
        button.setTextColor(Color.parseColor("#5C3A21"))
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply { set(y, m, d) }
                val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(picked.time)
                onDateSelected(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            photoPreview.setImageURI(selectedPhotoUri)
            photoPreview.visibility = View.VISIBLE
        }
    }

    private fun submitExpense() {
        val category = categoryInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val startDate = startDateInput.text.toString().trim()
        val endDate = endDateInput.text.toString().trim()
        val amountText = amountInput.text.toString().trim()

        if (category.isEmpty() || description.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        if (selectedPhotoUri != null) {
            uploadPhotoToFirebase(userId) { imageUrl ->
                if (imageUrl != null) {
                    saveExpenseToFirestore(category, description, startDate, endDate, amount, userId, imageUrl)
                } else {
                    Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveExpenseToFirestore(category, description, startDate, endDate, amount, userId, null)
        }
    }

    private fun uploadPhotoToFirebase(userId: String, callback: (String?) -> Unit) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("expense_photos/$userId/$filename")
        selectedPhotoUri?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri -> callback(uri.toString()) }
                        .addOnFailureListener { callback(null) }
                }
                .addOnFailureListener { callback(null) }
        } ?: callback(null)
    }

    private fun saveExpenseToFirestore(category: String, description: String, startDate: String, endDate: String, amount: Double, userId: String, imageUrl: String?) {
        val expense = hashMapOf(
            "category" to category,
            "description" to description,
            "startDate" to startDate,
            "endDate" to endDate,
            "amount" to amount,
            "userId" to userId,
            "imageUrl" to imageUrl
        )
        firestore.collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        categoryInput.text.clear()
        descriptionInput.text.clear()
        startDateInput.text.clear()
        endDateInput.text.clear()
        amountInput.text.clear()
        photoPreview.visibility = View.GONE
        selectedPhotoUri = null
    }

    private fun displayAllExpenses() {
        val userId = auth.currentUser?.uid ?: return
        expensesDisplayLayout.removeAllViews()
        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val noExpensesText = TextView(this).apply {
                        text = "No expenses found."
                        setTextColor(Color.parseColor("#5C3A21"))
                    }
                    expensesDisplayLayout.addView(noExpensesText)
                    return@addOnSuccessListener
                }
                for (document in documents) {
                    val expenseText = TextView(this).apply {
                        setTextColor(Color.parseColor("#5C3A21"))
                        text = formatExpense(document.data)
                    }
                    expensesDisplayLayout.addView(expenseText)
                }
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show() }
    }

    private fun formatExpense(data: Map<String, Any>): String {
        val category = data["category"] ?: ""
        val description = data["description"] ?: ""
        val startDate = data["startDate"] ?: ""
        val endDate = data["endDate"] ?: ""
        val amount = data["amount"] ?: ""
        val imageUrl = data["imageUrl"] ?: ""
        return "Category: $category\nDescription: $description\nStart: $startDate\nEnd: $endDate\nAmount: $amount\nReceipt: $imageUrl\n"
    }

    private fun filterExpenses() {
        val userId = auth.currentUser?.uid ?: return
        val filterCategory = filterCategoryInput.text.toString().trim().lowercase(Locale.getDefault())
        val filterStartDate = filterStartDateInput.text.toString().trim()
        val filterEndDate = filterEndDateInput.text.toString().trim()

        expensesDisplayLayout.removeAllViews()
        firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val filtered = documents.filter { doc ->
                    val category = doc.getString("category")?.lowercase(Locale.getDefault()) ?: ""
                    val startDate = doc.getString("startDate") ?: ""
                    val endDate = doc.getString("endDate") ?: ""
                    val matchesCategory = filterCategory.isEmpty() || category == filterCategory
                    val matchesStartDate = filterStartDate.isEmpty() || startDate >= filterStartDate
                    val matchesEndDate = filterEndDate.isEmpty() || endDate <= filterEndDate
                    matchesCategory && matchesStartDate && matchesEndDate
                }
                if (filtered.isEmpty()) {
                    val noExpensesText = TextView(this).apply {
                        text = "No expenses match the filter criteria."
                        setTextColor(Color.parseColor("#5C3A21"))
                    }
                    expensesDisplayLayout.addView(noExpensesText)
                    return@addOnSuccessListener
                }
                filtered.forEach { document ->
                    val expenseText = TextView(this).apply {
                        setTextColor(Color.parseColor("#5C3A21"))
                        text = formatExpense(document.data)
                    }
                    expensesDisplayLayout.addView(expenseText)
                }
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to filter expenses", Toast.LENGTH_SHORT).show() }
    }
}
