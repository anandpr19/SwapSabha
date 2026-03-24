package com.skillswap.app.ui.swaps

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.skillswap.app.R
import com.skillswap.app.databinding.ActivitySwapRequestBinding
import com.skillswap.app.ui.viewmodel.SwapState
import com.skillswap.app.ui.viewmodel.SwapViewModel
import com.skillswap.app.utils.hide
import com.skillswap.app.utils.show
import com.skillswap.app.utils.showToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity to create a swap request — user picks date, duration, location, and message.
 * Launched from StudentProfileActivity when tapping "Request Swap".
 */
class SwapRequestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TEACHER_ID = "extra_teacher_id"
        const val EXTRA_TEACHER_NAME = "extra_teacher_name"
        const val EXTRA_TEACHER_PIC = "extra_teacher_pic"
        const val EXTRA_SKILL_ID = "extra_skill_id"
        const val EXTRA_SKILL_NAME = "extra_skill_name"
    }

    private lateinit var binding: ActivitySwapRequestBinding
    private lateinit var viewModel: SwapViewModel

    private var teacherId = ""
    private var teacherName = ""
    private var teacherPic = ""
    private var skillId = ""
    private var skillName = ""

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySwapRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            title = getString(R.string.title_request_swap)
            setDisplayHomeAsUpEnabled(true)
        }

        teacherId = intent.getStringExtra(EXTRA_TEACHER_ID) ?: run { finish(); return }
        teacherName = intent.getStringExtra(EXTRA_TEACHER_NAME) ?: ""
        teacherPic = intent.getStringExtra(EXTRA_TEACHER_PIC) ?: ""
        skillId = intent.getStringExtra(EXTRA_SKILL_ID) ?: ""
        skillName = intent.getStringExtra(EXTRA_SKILL_NAME) ?: ""

        viewModel = ViewModelProvider(this)[SwapViewModel::class.java]

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvSkillName.text = skillName
        binding.tvTeacherName.text = "with $teacherName"
        binding.tvDurationValue.text = "60 min"
    }

    private fun setupListeners() {
        // Date picker
        binding.etDate.setOnClickListener { showDatePicker() }

        // Duration slider
        binding.sliderDuration.addOnChangeListener { _, value, _ ->
            binding.tvDurationValue.text = "${value.toInt()} min"
        }

        // Submit
        binding.btnSendRequest.setOnClickListener {
            viewModel.createSwapRequest(
                teacherId = teacherId,
                teacherName = teacherName,
                teacherProfilePic = teacherPic,
                skillId = skillId,
                skillName = skillName,
                proposedDate = calendar.timeInMillis,
                duration = binding.sliderDuration.value.toInt(),
                location = binding.etLocation.text.toString(),
                message = binding.etMessage.text.toString()
            )
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                binding.etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.btnSendRequest.isEnabled = !loading
            if (loading) binding.progressBar.show() else binding.progressBar.hide()
        }

        viewModel.locationError.observe(this) { error -> binding.tilLocation.error = error }
        viewModel.messageError.observe(this) { error -> binding.tilMessage.error = error }

        viewModel.swapState.observe(this) { state ->
            when (state) {
                is SwapState.RequestSent -> {
                    showToast(getString(R.string.msg_request_sent))
                    setResult(RESULT_OK)
                    finish()
                }
                is SwapState.Error -> showToast(state.message)
                else -> {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
