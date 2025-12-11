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

        // Pre-fill data if we are Editing
        existingTask?.let {
            binding.etTitle.setText(it.title)
            binding.etDescription.setText(it.description)
            binding.etDeadline.setText(it.deadline)
        }

        // Date Picker setup
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
                    Toast.makeText(context, "Title and Deadline are required!", Toast.LENGTH_SHORT).show()
                } else {
                    if (existingTask == null) {
                        addNewTask(title, desc, deadline)
                    } else {
                        updateTask(existingTask.id!!, title, desc, deadline, existingTask.isFinished)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewTask(title: String, desc: String, deadline: String) {
        val id = tasksRef.push().key ?: return
        val task = Task(id, title, desc, deadline, false)

        tasksRef.child(id).setValue(task)
            .addOnSuccessListener {
                Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->

                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("FirebaseError", "Error adding task", e)
            }
    }

    private fun updateTask(id: String, title: String, desc: String, deadline: String, isFinished: Boolean) {
        val task = Task(id, title, desc, deadline, isFinished)
        tasksRef.child(id).setValue(task)
            .addOnSuccessListener { Toast.makeText(context, "Updated!", Toast.LENGTH_SHORT).show() }
    }
}