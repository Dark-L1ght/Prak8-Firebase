package com.prak9.myapplication

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.prak9.myapplication.databinding.DialogTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskFormDialog(
    private val context: Context,
    private val tasksRef: DatabaseReference,
    private val existingTask: Task? = null
) {

    fun show() {
        val binding = DialogTaskBinding.inflate(LayoutInflater.from(context))

        if (existingTask != null) {
            binding.etTitle.setText(existingTask.title)
            binding.etDescription.setText(existingTask.description)
            binding.etDeadline.setText(existingTask.deadline)
        }

        binding.etDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    binding.etDeadline.setText(format.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        val dialogTitle = if (existingTask == null) "New Task" else "Edit Task"
        val btnText = if (existingTask == null) "Add" else "Update"

        MaterialAlertDialogBuilder(context)
            .setTitle(dialogTitle)
            .setView(binding.root)
            .setPositiveButton(btnText) { _, _ ->
                val title = binding.etTitle.text.toString().trim()
                val desc = binding.etDescription.text.toString().trim()
                val deadline = binding.etDeadline.text.toString().trim()

                if (title.isEmpty() || deadline.isEmpty()) {
                    Toast.makeText(context, "Title and Deadline are required", Toast.LENGTH_SHORT).show()
                } else {
                    if (existingTask == null) {
                        saveData(null, title, desc, deadline, false, "Task added")
                    } else {
                        saveData(existingTask.id, title, desc, deadline, existingTask.isFinished, "Task updated")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveData(id: String?, title: String, desc: String, deadline: String, isFinished: Boolean, successMessage: String) {
        val key = id ?: tasksRef.push().key ?: return
        val task = Task(key, title, desc, deadline, isFinished)

        tasksRef.child(key).setValue(task)
            .addOnSuccessListener {
                // Here is your specific Toast
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}