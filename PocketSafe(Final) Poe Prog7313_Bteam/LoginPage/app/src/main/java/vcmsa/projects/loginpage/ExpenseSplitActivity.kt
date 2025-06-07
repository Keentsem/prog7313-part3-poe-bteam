package vcmsa.projects.loginpage

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.OutputStream
import java.util.*

class ExpenseSplitActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private var permissionGranted by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (!isGranted) {
            Toast.makeText(this, "Storage permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionGranted = isStoragePermissionGranted()

        if (!permissionGranted && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        setContent {
            AppTheme {
                ExpenseSplitScreen(firestore, this, permissionGranted)
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val LightBrown = Color(0xFFA67B5B)
    val CreamOak = Color(0xFFF5F1E9)
    val DarkBrown = Color(0xFF5C4033)

    val colorScheme = lightColorScheme(
        primary = LightBrown,
        onPrimary = CreamOak,
        secondary = DarkBrown,
        onSecondary = CreamOak,
        background = CreamOak,
        onBackground = DarkBrown,
        surface = CreamOak,
        onSurface = DarkBrown,
        error = Color(0xFFB00020),
        onError = CreamOak,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSplitScreen(firestore: FirebaseFirestore, context: Context, permissionGranted: Boolean) {
    var occasion by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberAmount by remember { mutableStateOf("") }
    val members = remember { mutableStateListOf<Pair<String, Double>>() }
    val downloadCodeInput = remember { mutableStateOf("") }

    val creamColor = Color(0xFFF5F1E9)
    val oakColor = Color(0xFFA67B5B)
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Back",
                fontSize = 18.sp,          // bigger font size
                color = creamColor,        // cream color for the text
                modifier = Modifier
                    .padding(top = 38.dp, end = 12.dp)  // moved 1 cm lower and padding on right
                    .clickable {
                        val intent = Intent(context, MainMenu::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }


            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .padding(top = 72.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("My Pocket Splitter", style = MaterialTheme.typography.headlineLarge, color = creamColor, fontWeight = FontWeight.Bold)
            Text("Divide the bill with your friends and family!", style = MaterialTheme.typography.bodyLarge, color = creamColor)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Split Expense Setup", style = MaterialTheme.typography.headlineMedium, color = creamColor)

            OutlinedTextField(
                value = occasion,
                onValueChange = { occasion = it },
                label = { Text("Occasion (e.g., Night Out)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = creamColor)
            )

            OutlinedTextField(
                value = newMemberName,
                onValueChange = { newMemberName = it },
                label = { Text("Member Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = creamColor)
            )

            OutlinedTextField(
                value = newMemberAmount,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() || ch == '.' }) newMemberAmount = it
                },
                label = { Text("Amount Paid", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = creamColor)
            )

            Button(onClick = {
                val amount = newMemberAmount.toDoubleOrNull()
                if (newMemberName.isBlank() || amount == null) {
                    Toast.makeText(context, "Enter valid member name and amount", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                members.add(newMemberName to amount)
                newMemberName = ""
                newMemberAmount = ""
            }) {
                Text("Add Member")
            }

            Text("Members & Amounts:", style = MaterialTheme.typography.titleMedium, color = creamColor)

            LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                itemsIndexed(members) { _, member ->
                    Text("${member.first}: R${"%.2f".format(member.second)}", color = creamColor)
                }
            }

            Button(
                onClick = {
                    if (occasion.isBlank() || members.isEmpty()) {
                        Toast.makeText(context, "Please enter occasion and members", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val generatedCode = UUID.randomUUID().toString().take(8)
                    val data = hashMapOf(
                        "occasion" to occasion,
                        "members" to members.map { mapOf("name" to it.first, "amount" to it.second) },
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("expenseSplits")
                        .document(generatedCode)
                        .set(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Saved! Share this code: $generatedCode", Toast.LENGTH_LONG).show()
                            code = generatedCode
                            generatePdfReceipt(context, occasion, members.map { mapOf("name" to it.first, "amount" to it.second) }, generatedCode)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to save: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Split Expense")
            }

            Divider()

            Text("Download Receipt by Code", style = MaterialTheme.typography.headlineSmall, color = creamColor)

            OutlinedTextField(
                value = downloadCodeInput.value,
                onValueChange = { downloadCodeInput.value = it },
                label = { Text("Enter Code", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(containerColor = creamColor)
            )

            Button(
                onClick = {
                    if (!permissionGranted) {
                        Toast.makeText(context, "Storage permission is required to save PDFs", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val enteredCode = downloadCodeInput.value.trim()
                    if (enteredCode.isBlank()) {
                        Toast.makeText(context, "Please enter a code", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    firestore.collection("expenseSplits").document(enteredCode).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val occasion = doc.getString("occasion") ?: "Unknown Occasion"
                                val membersList = doc.get("members") as? List<Map<String, Any>> ?: emptyList()
                                generatePdfReceipt(context, occasion, membersList, enteredCode)
                            } else {
                                Toast.makeText(context, "Code not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download Receipt")
            }
        }
    }
}

fun generatePdfReceipt(
    context: Context,
    occasion: String,
    members: List<Map<String, Any>>,
    code: String
) {
    val fileName = "Receipt-$occasion.pdf"

    val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/ExpenseSplitter")
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        uri?.let { context.contentResolver.openOutputStream(it) }
    } else {
        null
    }

    if (outputStream == null) {
        Toast.makeText(context, "Failed to create PDF file.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("Expense Split Receipt").setFontSize(20f).setBold())
        document.add(Paragraph("Occasion: $occasion").setFontSize(16f))
        document.add(Paragraph("Code: $code").setFontSize(14f))
        document.add(Paragraph(""))

        val total = members.sumOf { it["amount"]?.toString()?.toDoubleOrNull() ?: 0.0 }

        members.forEach {
            val name = it["name"] ?: "Unknown"
            val amount = it["amount"]?.toString()?.toDoubleOrNull() ?: 0.0
            document.add(Paragraph("$name : R${"%.2f".format(amount)}"))
        }

        document.add(Paragraph(""))
        document.add(Paragraph("Total: R${"%.2f".format(total)}").setBold())

        document.close()

        Toast.makeText(context, "PDF saved as $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
