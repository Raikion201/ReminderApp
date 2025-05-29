package com.example.reminderapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.reminderapp.data.model.Priority
import com.example.reminderapp.ui.theme.PriorityHigh
import com.example.reminderapp.ui.theme.PriorityLow
import com.example.reminderapp.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Returns the color associated with a priority level
 */
@Composable
fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
        Priority.NONE -> MaterialTheme.colorScheme.surfaceVariant
    }
}

/**
 * Creates a small circular indicator for the priority level
 */
@Composable
fun PriorityIndicator(priority: Priority, modifier: Modifier = Modifier) {
    if (priority == Priority.NONE) return
    
    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(getPriorityColor(priority))
    )
}

/**
 * Creates a chip-style indicator for priority
 */
@Composable
fun PriorityChip(priority: Priority) {
    if (priority == Priority.NONE) return
    
    Surface(
        color = getPriorityColor(priority).copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = priority.name,
            style = MaterialTheme.typography.labelSmall,
            color = getPriorityColor(priority),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Get appropriate due date text and color based on timestamp
 */
@Composable
fun DueDateInfo(dueDate: Long): Pair<String, Color> {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    
    // Set up calendars for comparison
    val dueCal = Calendar.getInstance().apply { timeInMillis = dueDate }
    val todayCal = Calendar.getInstance().apply { 
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrowCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    // Check if overdue
    if (dueDate < now) {
        val diff = TimeUnit.MILLISECONDS.toDays(now - dueDate)
        return if (diff == 0L) {
            Pair("Overdue today", Color.Red)
        } else {
            Pair("Overdue by ${diff} day${if (diff > 1) "s" else ""}", Color.Red)
        }
    }
    
    // Same day (today)
    if (dueCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        dueCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            return Pair("Today at ${timeFormat.format(Date(dueDate))}", com.example.reminderapp.ui.theme.DueDateToday)
    }
    
    // Tomorrow
    if (dueCal.get(Calendar.YEAR) == tomorrowCal.get(Calendar.YEAR) &&
        dueCal.get(Calendar.DAY_OF_YEAR) == tomorrowCal.get(Calendar.DAY_OF_YEAR)) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            return Pair("Tomorrow at ${timeFormat.format(Date(dueDate))}", com.example.reminderapp.ui.theme.DueDateUpcoming)
    }
    
    // Within a week
    val daysDiff = TimeUnit.MILLISECONDS.toDays(dueDate - now)
    if (daysDiff < 7) {
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return Pair("${dayFormat.format(Date(dueDate))} at ${timeFormat.format(Date(dueDate))}", com.example.reminderapp.ui.theme.DueDateUpcoming)
    }
    
    // Default format for dates beyond a week
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    return Pair(dateFormat.format(Date(dueDate)), com.example.reminderapp.ui.theme.DueDateUpcoming)
}

/**
 * Due date chip that shows date in appropriate format with color coding
 */
@Composable
fun DueDateChip(dueDate: Long) {
    val (text, color) = DueDateInfo(dueDate)
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * Icon with background appropriate for completion status
 */
@Composable
fun StatusIcon(isCompleted: Boolean, dueDate: Long? = null) {
    val isOverdue = dueDate?.let { it < System.currentTimeMillis() } ?: false
    
    val (icon, color) = when {
        isCompleted -> Pair(Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary)
        isOverdue -> Pair(Icons.Filled.Warning, Color.Red)
        dueDate != null -> Pair(Icons.Filled.Info, com.example.reminderapp.ui.theme.DueDateUpcoming)
        else -> Pair(Icons.Filled.Info, MaterialTheme.colorScheme.surfaceVariant)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = when {
            isCompleted -> "Completed"
            isOverdue -> "Overdue"
            else -> "Scheduled"
        },
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}
