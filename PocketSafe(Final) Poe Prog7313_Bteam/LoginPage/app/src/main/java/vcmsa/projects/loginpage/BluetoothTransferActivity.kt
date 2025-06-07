package vcmsa.projects.loginpage

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BluetoothExpenseTransfer : Activity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_CODE_PERMISSIONS = 200

    private val permissionsToRequest = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 100, 20, 20)
            gravity = Gravity.CENTER
        }

        // Sender and Receiver Icons (App Logo Style)
        val iconsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            val sender = ImageView(this@BluetoothExpenseTransfer).apply {
                setImageResource(android.R.drawable.sym_action_email)
                layoutParams = LinearLayout.LayoutParams(200, 200)
            }
            val receiver = ImageView(this@BluetoothExpenseTransfer).apply {
                setImageResource(android.R.drawable.sym_action_chat)
                layoutParams = LinearLayout.LayoutParams(200, 200)
            }
            addView(sender)
            addView(receiver)
        }

        val transferButton = Button(this).apply {
            text = "Send This Month's Expenses"
            setOnClickListener {
                checkAndRequestPermissions()
            }
        }

        layout.addView(iconsLayout)
        layout.addView(transferButton)
        setContentView(layout)
    }

    private fun checkAndRequestPermissions() {
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            initializeBluetoothTransfer()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeBluetoothTransfer()
            } else {
                Toast.makeText(this, "Permissions denied. Cannot proceed.", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun initializeBluetoothTransfer() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            fetchAndSendExpenses()
        }
    }

    private fun fetchAndSendExpenses() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val currentMonth = dateFormat.format(calendar.time)

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.filter { doc ->
                    val date = doc.getString("startDate") ?: ""
                    date.startsWith(currentMonth)
                }.joinToString(separator = "\n") { doc ->
                    "Category: ${doc.getString("category")}, Amount: ${doc.getString("amount")}, Date: ${doc.getString("startDate")}"
                }

                // Simulate Bluetooth send (in real app use OutputStream to a connected socket)
                Toast.makeText(this, "Sending:\n$expenses", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses.", Toast.LENGTH_SHORT).show()
            }
    }
}
