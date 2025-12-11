package com.prak9.myapplication

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prak9.myapplication.databinding.ItemTaskBinding

class TodoAdapter(
    private val tasks: List<Task>,
    private val onDeleteClick: (Task) -> Unit,
    private val onUpdateClick: (Task) -> Unit,
    private val onCheckClick: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDeadline.text = task.deadline

            // --- NEW: Description Logic ---
            if (task.description.isNullOrEmpty()) {
                binding.tvDescription.visibility = View.GONE
            } else {
                binding.tvDescription.visibility = View.VISIBLE
                binding.tvDescription.text = task.description
            }

            // Checkbox Logic
            binding.cbIsFinished.setOnCheckedChangeListener(null)
            binding.cbIsFinished.isChecked = task.isFinished

            // Strikethrough if finished
            if (task.isFinished) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            binding.cbIsFinished.setOnCheckedChangeListener { _, isChecked ->
                onCheckClick(task, isChecked)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(task)
            }

            binding.root.setOnClickListener {
                onUpdateClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size
}