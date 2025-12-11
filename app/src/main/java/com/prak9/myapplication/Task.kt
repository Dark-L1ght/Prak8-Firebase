package com.prak9.myapplication

data class Task(
    var id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val deadline: String? = null,
    var isFinished: Boolean = false
)