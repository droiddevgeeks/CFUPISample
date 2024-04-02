package com.cashfree.cashfreetestupi

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cashfree.cashfreetestupi.databinding.ActivityMainBinding
import com.cashfree.cashfreetestupi.model.EntitySimulation
import com.cashfree.cashfreetestupi.model.SimulateRequest
import com.cashfree.cashfreetestupi.network.ApiClient
import com.cashfree.cashfreetestupi.network.NetworkConnectivityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedReason: String = "FAILED"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(binding.root)
        initUI()
        setToolbar()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.title = ""
        }
    }

    private fun initUI() {
        intent?.let {
            val data = it.data
            data?.let { uri ->
                val amount = uri.getQueryParameter("am") ?: uri.getQueryParameter("amount") ?: ""
                val payeeVPA = uri.getQueryParameter("pa") ?: ""
                val payeeName = uri.getQueryParameter("pn") ?: ""
                val txnNote = uri.getQueryParameter("tn") ?: ""
                val txnRef = uri.getQueryParameter("tr") ?: ""
                val txnId = uri.getQueryParameter("tid") ?: uri.getQueryParameter("txnId") ?: txnRef

                binding?.let { view ->
                    view.tieUpiVpa.setText(payeeVPA)
                    view.tieUpiAmount.setText(amount)
                    view.tieUpiVpn.setText(payeeName)
                    view.tieUpiTxn.setText(txnNote)
                    view.tieUpiTr.setText(txnRef)
                    view.tieUpiTid.setText(txnId)
                    binding.done.setOnClickListener { v -> simulatePayment() }
                    binding.rgUpi.setOnCheckedChangeListener { group, checkedId ->
                        val radioButton: View = binding.rgUpi.findViewById(checkedId)
                        when (binding.rgUpi.indexOfChild(radioButton)) {
                            0 -> {
                                binding.failureSpinner.visibility = View.INVISIBLE
                            }

                            1 -> {
                                binding.failureSpinner.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        setFailureUI()
    }

    private fun setFailureUI() {
        val failureReason = arrayOf(
            "ISSUER_NOT_AVAILABLE",
            "DECLINED_BY_ISSUER_BANK",
            "INCORRECT_PID",
            "DEBIT_FAILED",
            "FAILED",
            "TRANSACTION_NOT_AUTHORIZED",
            "INCORRECT_PIN",
            "COLLECT_EXPIRED",
            "TRANSACTION_DECLINED_BY_CUSTOMER",
            "INSUFFICIENT_FUND"
        )

        val adapter = ArrayAdapter<String>(
            this@MainActivity,
            android.R.layout.simple_spinner_item,
            failureReason
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.failureSpinner.adapter = adapter
        binding.failureSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedReason = failureReason[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun simulatePayment() {
        val radioButtonID: Int = binding.rgUpi.checkedRadioButtonId
        val radioButton: View = binding.rgUpi.findViewById(radioButtonID)
        when (binding.rgUpi.indexOfChild(radioButton)) {
            0 -> updateSuccessStatus()
            1 -> updateFailedStatus()
            else -> updateSuccessStatus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        createCloseAlert()
    }

    private fun createCloseAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancel Transaction")
        builder.setMessage("Are you sure want to cancel this transaction?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Yes") { _, _ ->
            if (binding.tieUpiTid.text.toString().isEmpty()) {
                sendStatus("FAILED")
            } else {
                selectedReason = "TRANSACTION_DECLINED_BY_CUSTOMER"
                updateFailedStatus()
            }

        }

        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.cancel()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun sendStatus(status: String) {
        val intent = Intent()
        val bundle = Bundle().apply {
            putString("status", status)
            putString("status", status)
            putString("txnId", binding.tieUpiTid.text.toString())
            putString("txnRef", binding.tieUpiTr.text.toString())
        }
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun updateSuccessStatus() {
        if (NetworkConnectivityUtil.isNetworkConnected(this)) {
            handleLoaderUI(isLoading = true)
            val service = ApiClient.apiService
            lifecycleScope.launch(Dispatchers.IO) {
                service.simulateStatus(getHeaders(), getSimulateSuccessRequest()).let { result ->
                    if (result.isSuccessful) {
                        result.body()?.let { _ ->
                            handleLoaderUI(isLoading = false)
                            sendStatus("SUCCESS")
                        }
                    } else {
                        handleLoaderUI(isLoading = false)
                        result.errorBody()?.let {
                            val message = it.charStream().readText()
                            showToast(message)
                            sendStatus("FAILED")
                        }
                    }
                }
            }
        } else {
            showToast("No internet connection")
        }
    }

    private fun updateFailedStatus() {
        if (NetworkConnectivityUtil.isNetworkConnected(this)) {
            handleLoaderUI(isLoading = true)
            val service = ApiClient.apiService
            lifecycleScope.launch(Dispatchers.IO) {
                service.simulateStatus(getHeaders(), getSimulateFailureRequest()).let { result ->
                    if (result.isSuccessful) {
                        result.body()?.let { _ ->
                            handleLoaderUI(isLoading = false)
                            sendStatus("FAILED")
                        }
                    } else {
                        handleLoaderUI(isLoading = false)
                        result.errorBody()?.let {
                            val message = it.charStream().readText()
                            showToast(message)
                            sendStatus("FAILED")
                        }
                    }
                }
            }
        } else {
            showToast("No internet connection")
        }
    }

    private fun handleLoaderUI(isLoading: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isLoading) binding.progress.visibility = View.VISIBLE
            else binding.progress.visibility = View.GONE
        }
    }

    private fun getHeaders(): HashMap<String, String> {
        val headers = HashMap<String, String>()
        return headers
    }

    private fun getSimulateSuccessRequest(): SimulateRequest {
        return SimulateRequest(
            entity = "PAYMENTS",
            entityId = binding.tieUpiTid.text.toString(),
            entitySimulation = EntitySimulation(paymentStatus = "SUCCESS")
        )
    }

    private fun getSimulateFailureRequest(): SimulateRequest {
        return SimulateRequest(
            entity = "PAYMENTS",
            entityId = binding.tieUpiTid.text.toString(),
            entitySimulation = EntitySimulation(
                paymentStatus = "FAILED",
                paymentErrorCode = selectedReason
            )
        )
    }

    private fun showToast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

}