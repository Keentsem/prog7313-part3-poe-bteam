package com.example.pocketsafe

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.example.pocketsafe.data.Expense
import com.example.pocketsafe.data.Category
import com.example.pocketsafe.data.AppDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ExpenseEntry : Activity() {
    private lateinit var db: AppDatabase
    private lateinit var categorySpinner: Spinner
    private lateinit var amountEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var startDateEditText: TextView
    private lateinit var endDateEditText: TextView
    private lateinit var photoImageView: ImageView
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var photoUri: String? = null
    private val PICK_IMAGE_REQUEST = 1
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase.getDatabase(applicationContext)
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setPadding(30, 30, 30, 30)
        }

        val backButton = Button(this).apply {
            text = "Back to Main Menu"
            setPadding(40, 40, 40, 40)

            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(Color.parseColor("#D2B48C"))
            background = drawable

            setOnClickListener {
                val intent = Intent(this@ExpenseEntry, MainMenu::class.java)
                startActivity(intent)
                finish()
            }
        }

        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 100
            leftMargin = 40
        }

        mainLayout.addView(backButton, layoutParams)

        val space = View(this).apply {
            setLayoutParams(LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 20))
        }
        mainLayout.addView(space)

        setupCategorySpinner(mainLayout)
        setupAmountInput(mainLayout)
        setupDescriptionInput(mainLayout)
        setupDateRangeInput(mainLayout)
        setupPhotoInput(mainLayout)
        setupSaveButton(mainLayout)

        setContentView(mainLayout)

        loadCategories()
    }

    private fun setupCategorySpinner(mainLayout: LinearLayout) {
        val categoryLabel = TextView(this).apply {
            text = "Category:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(categoryLabel)

        categorySpinner = Spinner(this)
        mainLayout.addView(categorySpinner)
    }

    private fun setupAmountInput(mainLayout: LinearLayout) {
        val amountLabel = TextView(this).apply {
            text = "Amount:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(amountLabel)

        amountEditText = EditText(this).apply {
            hint = "Enter amount"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#8B5E3C"))
        }
        mainLayout.addView(amountEditText)
    }

    private fun setupDescriptionInput(mainLayout: LinearLayout) {
        val descriptionLabel = TextView(this).apply {
            text = "Description:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(descriptionLabel)

        descriptionEditText = EditText(this).apply {
            hint = "Enter description"
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setTextColor(Color.parseColor("#8B5E3C"))
        }
        mainLayout.addView(descriptionEditText)
    }

    private fun setupDateRangeInput(mainLayout: LinearLayout) {
        val dateLabel = TextView(this).apply {
            text = "Date Range:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(dateLabel)

        startDateEditText = TextView(this).apply {
            text = "Start Date"
            setPadding(0, 10, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setOnClickListener { showDatePickerDialog(true) }
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(startDateEditText)

        endDateEditText = TextView(this).apply {
            text = "End Date"
            setPadding(0, 10, 0, 20)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setOnClickListener { showDatePickerDialog(false) }
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(endDateEditText)
    }

    private fun setupPhotoInput(mainLayout: LinearLayout) {
        val photoLabel = TextView(this).apply {
            text = "Photo:"
            textSize = 20f
            setPadding(0, 20, 0, 10)
            setBackgroundColor(Color.parseColor("#8B5E3C"))
            setTextColor(Color.parseColor("#D2B48C"))
        }
        mainLayout.addView(photoLabel)

        photoImageView = ImageView(this).apply {
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                200
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }
        mainLayout.addView(photoImageView)

        val selectPhotoButton = Button(this).apply {
            text = "Select Photo"
            setBackgroundColor(Color.parseColor("#D2B48C"))
            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
            }
        }
        mainLayout.addView(selectPhotoButton)
    }

    private fun setupSaveButton(mainLayout: LinearLayout) {
        val saveButton = Button(this).apply {
            text = "Save Expense"
            setPadding(40, 40, 40, 40)
            setBackgroundColor(Color.parseColor("#D2B48C"))
            setOnClickListener { saveExpense() }
        }
        mainLayout.addView(saveButton)
    }

    private fun loadCategories() {
        MainScope().launch(Dispatchers.IO) {
            try {
                // Get the first emission from the Flow
                val categoryList = db.categoryDao().getAllCategories().firstOrNull() ?: emptyList()
                categories = categoryList
                runOnUiThread {
                    val adapter = object : ArrayAdapter<Category>(
                        this@ExpenseEntry,
                        android.R.layout.simple_spinner_item,
                        categories
                    ) {
                        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getView(position, convertView, parent)
                            (view as TextView).setTextColor(Color.parseColor("#D2B48C"))
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                            val view = super.getDropDownView(position, convertView, parent)
                            view.setBackgroundColor(Color.parseColor("#8B5E3C"))
                            (view as TextView).setTextColor(Color.parseColor("#D2B48C"))
                            return view
                        }
                    }

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    categorySpinner.adapter = adapter
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast("Failed to load categories: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(formatted) ?: Date()

                if (isStartDate) {
                    startDate = parsedDate
                    startDateEditText.text = "Start Date: $formatted"
                } else {
                    endDate = parsedDate
                    endDateEditText.text = "End Date: $formatted"
                }
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveExpense() {
        val selectedCategory = categorySpinner.selectedItem as? Category
        val amountText = amountEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (selectedCategory == null) {
            showToast("Please select a category")
            return
        }

        if (amountText.isEmpty()) {
            showToast("Please enter an amount")
            return
        }

        if (startDate == null) {
            showToast("Please select a start date")
            return
        }

        if (endDate == null) {
            showToast("Please select an end date")
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            showToast("Please enter a valid amount")
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expense = Expense(
            categoryId = selectedCategory.id,
            amount = amount,
            description = description,
            startDate = sdf.format(startDate ?: Date()),
            endDate = sdf.format(endDate ?: Date()),
            photoUri = photoUri
        )

        MainScope().launch(Dispatchers.IO) {
            try {
                db.expenseDao().insertExpense(expense)
                runOnUiThread {
                    showToast("Expense saved successfully")
                    val intent = Intent(this@ExpenseEntry, MainMenu::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showToast("Failed to save expense: ${e.localizedMessage}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImage = data.data
            try {
                // Using safer API that doesn't use deprecated getBitmap
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, selectedImage!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                }
                photoImageView.setImageBitmap(bitmap)

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${timeStamp}_"
                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
                )

                val outputStream = FileOutputStream(image)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                photoUri = image.absolutePath
            } catch (e: IOException) {
                showToast("Failed to load image: ${e.localizedMessage}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 