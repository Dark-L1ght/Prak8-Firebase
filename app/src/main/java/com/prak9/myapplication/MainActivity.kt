package com.prak9.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.prak9.myapplication.databinding.ActivityMainBinding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tasksRef: DatabaseReference
    private lateinit var adapter: TodoAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tasksRef = FirebaseDatabase.getInstance("https://pmobfb375-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("tasks")

        binding.rvBooks.layoutManager = LinearLayoutManager(this)

        adapter = TodoAdapter(
            taskList,
            onDeleteClick = { task -> deleteTask(task) },
            onUpdateClick = { task -> showEditDialog(task) },
            onCheckClick = { task, isChecked -> updateTaskStatus(task, isChecked) }
        )
        binding.rvBooks.adapter = adapter
        setupDragAndDrop()
        updateEmptyState()
        fetchData()

        binding.fabAddBooks.setOnClickListener {
            TaskFormDialog(this, tasksRef, null).show()
        }
    }
    private fun setupDragAndDrop() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                Collections.swap(taskList, fromPosition, toPosition)

                adapter.notifyItemMoved(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        })

        // Attach the helper to your RecyclerView
        itemTouchHelper.attachToRecyclerView(binding.rvBooks)
    }

    private fun fetchData() {
        tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val task = data.getValue(Task::class.java)
                        task?.id = data.key
                        task?.let { taskList.add(it) }
                    }
                    // Sort: Unfinished first
                    taskList.sortBy { it.isFinished }
                }
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmptyState() {
        if (taskList.isEmpty()) {
            binding.rvBooks.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvBooks.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
        }
    }

    private fun deleteTask(task: Task) {
        task.id?.let {
            tasksRef.child(it).removeValue()
        }
    }

    private fun showEditDialog(task: Task) {
        TaskFormDialog(this, tasksRef, task).show()
    }

    private fun updateTaskStatus(task: Task, isFinished: Boolean) {
        task.id?.let {
            tasksRef.child(it).child("finished").setValue(isFinished)
        }
    }
}