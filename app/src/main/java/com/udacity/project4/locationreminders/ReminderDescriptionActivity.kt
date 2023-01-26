package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: com.udacity.project4.databinding.ActivityReminderDescriptionBinding
    companion object {
        private const val ReminderDataItem = "Reminder Data Item"
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(ReminderDataItem, reminderDataItem)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )
        binding.doneButton.setOnClickListener {
            val toList=Intent(this,RemindersActivity::class.java)
            startActivity(toList)
        }


        if (!intent.hasExtra(ReminderDataItem)) return
        val remind_Dataitem:ReminderDataItem=intent.getSerializableExtra(ReminderDataItem)as ReminderDataItem
        binding.reminderDataItem=remind_Dataitem
        binding.executePendingBindings()

    }
}
