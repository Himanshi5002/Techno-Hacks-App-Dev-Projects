package com.example.todoapp

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.adapter.TaskAdapter
import com.example.todoapp.data.Task
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.viewmodel.TaskViewModel
import com.example.todoapp.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adapter with click listener
        taskAdapter = TaskAdapter { task ->
            showEditDeleteDialog(task)
        }

        binding.taskRecycler.layoutManager = LinearLayoutManager(this)
        binding.taskRecycler.adapter = taskAdapter

        // Observe data from ViewModel
        taskViewModel.allTasks.observe(this) {
            taskAdapter.updateList(it)
        }

        // Add button
        binding.addBtn.setOnClickListener {
            val text = binding.taskInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val newTask = Task(title = text)
                taskViewModel.insert(newTask)
                binding.taskInput.text.clear()
                playConfetti()
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            }
        }

        // Features
        startClock()
        animateBackground()
        enableSwipeToDelete()
    }

    private fun showEditDeleteDialog(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit or Delete Task")

        val input = EditText(this)
        input.setText(task.title)
        input.setPadding(32, 16, 32, 16)
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val updatedText = input.text.toString().trim()
            if (updatedText.isNotEmpty()) {
                val updatedTask = task.copy(
                    title = updatedText,
                    timestamp = System.currentTimeMillis()
                )
                taskViewModel.update(updatedTask)
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Delete") { _, _ ->
            taskViewModel.delete(task)
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
        }

        builder.setNeutralButton("Cancel", null)
        builder.show()
    }

    private fun startClock() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
                binding.clockText.text = currentTime
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun playConfetti() {
        binding.confettiView.apply {
            visibility = View.VISIBLE
            playAnimation()
            postDelayed({ visibility = View.GONE }, 2000)
        }
    }

    private fun animateBackground() {
        val colors = listOf(
            "#FFEBEE".toColorInt(),
            "#E8F5E9".toColorInt(),
            "#E3F2FD".toColorInt(),
            "#FFF3E0".toColorInt(),
            "#F3E5F5".toColorInt()
        )

        var colorIndex = 0
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                val nextIndex = (colorIndex + 1) % colors.size
                val animator = ValueAnimator.ofArgb(colors[colorIndex], colors[nextIndex])
                animator.duration = 4000
                animator.addUpdateListener {
                    binding.mainLayout.setBackgroundColor(it.animatedValue as Int)
                }
                animator.start()
                colorIndex = nextIndex
                handler.postDelayed(this, 4000)
            }
        }
        handler.post(runnable)
    }

    private fun enableSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val task = taskAdapter.getTaskAt(position)
                taskViewModel.delete(task)
                Toast.makeText(this@MainActivity, "Task deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.taskRecycler)
    }
}
