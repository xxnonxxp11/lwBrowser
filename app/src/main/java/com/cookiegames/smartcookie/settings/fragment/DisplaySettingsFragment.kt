package com.cookiegames.smartcookie.settings.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cookiegames.smartcookie.AppTheme
import com.cookiegames.smartcookie.MainActivity
import com.cookiegames.smartcookie.R
import com.cookiegames.smartcookie.browser.ChooseNavbarCol
import com.cookiegames.smartcookie.di.injector
import com.cookiegames.smartcookie.dialog.BrowserDialog
import com.cookiegames.smartcookie.extensions.resizeAndShow
import com.cookiegames.smartcookie.extensions.withSingleChoiceItems
import com.cookiegames.smartcookie.preference.UserPreferences
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

/**
 * Display, theme and visual customization settings.
 */
class DisplaySettingsFragment : AbstractSettingsFragment() {

    @Inject internal lateinit var userPreferences: UserPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_display)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injector.inject(this)

        // Theme selection
        clickableDynamicPreference(
            preference = SETTINGS_THEME,
            summary = userPreferences.useTheme.toDisplayString(),
            onClick = ::showThemePicker
        )

        // Navbar color
        clickablePreference(
            preference = SETTINGS_NAVBAR_COL,
            onClick = ::showColorPicker
        )

        // Dark mode web
        switchPreference(
            preference = SETTINGS_DARK_MODE,
            isChecked = userPreferences.darkModeExtension,
            onCheckChange = {
                userPreferences.darkModeExtension = it
                Toast.makeText(activity, R.string.please_restart, Toast.LENGTH_LONG).show()
            }
        )

        // Black status bar (AMOLED)
        switchPreference(
            preference = SETTINGS_BLACK_STATUS,
            isChecked = userPreferences.useBlackStatusBar,
            onCheckChange = { userPreferences.useBlackStatusBar = it }
        )

        // Color mode
        switchPreference(
            preference = SETTINGS_COLOR_MODE,
            isChecked = userPreferences.colorModeEnabled,
            onCheckChange = { userPreferences.colorModeEnabled = it }
        )

        // Bottom toolbar
        switchPreference(
            preference = SETTINGS_BOTTOM_BAR,
            isChecked = userPreferences.bottomBar,
            onCheckChange = {
                userPreferences.bottomBar = it
                Toast.makeText(activity, R.string.please_restart, Toast.LENGTH_LONG).show()
            }
        )

        // Hide status bar
        switchPreference(
            preference = SETTINGS_HIDESTATUSBAR,
            isChecked = userPreferences.hideStatusBarEnabled,
            onCheckChange = { userPreferences.hideStatusBarEnabled = it }
        )

        // Hide toolbar while scrolling
        switchPreference(
            preference = SETTINGS_FULLSCREEN,
            isChecked = userPreferences.fullScreenEnabled,
            onCheckChange = { userPreferences.fullScreenEnabled = it }
        )

        // Text size
        clickablePreference(
            preference = SETTINGS_TEXTSIZE,
            onClick = ::showTextSizePicker
        )

        // Wide viewport
        switchPreference(
            preference = SETTINGS_VIEWPORT,
            isChecked = userPreferences.useWideViewPortEnabled,
            onCheckChange = { userPreferences.useWideViewPortEnabled = it }
        )

        // Overview mode
        switchPreference(
            preference = SETTINGS_OVERVIEWMODE,
            isChecked = userPreferences.overviewModeEnabled,
            onCheckChange = { userPreferences.overviewModeEnabled = it }
        )

        // Force zoom
        switchPreference(
            preference = SETTINGS_FORCE_ZOOM,
            isChecked = userPreferences.forceZoom,
            onCheckChange = { userPreferences.forceZoom = it }
        )
    }

    private fun AppTheme.toDisplayString(): String = getString(when (this) {
        AppTheme.LIGHT -> R.string.light_theme
        AppTheme.DARK -> R.string.dark_theme
        AppTheme.BLACK -> R.string.black_theme
    })

    private fun showThemePicker(summaryUpdater: SummaryUpdater) {
        val currentTheme = userPreferences.useTheme
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(resources.getString(R.string.theme))
            val values = AppTheme.values().map { Pair(it, it.toDisplayString()) }
            withSingleChoiceItems(values, userPreferences.useTheme) {
                userPreferences.useTheme = it
                summaryUpdater.updateSummary(it.toDisplayString())
            }
            setPositiveButton(resources.getString(R.string.action_ok)) { _, _ ->
                if (currentTheme != userPreferences.useTheme) {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            setOnCancelListener {
                if (currentTheme != userPreferences.useTheme) {
                    activity?.onBackPressed()
                }
            }
        }.resizeAndShow()
    }

    private fun showColorPicker() {
        BrowserDialog.showCustomDialog(activity) {
            setTitle(R.string.navbar_col)
            val stringArray = resources.getStringArray(R.array.navbar_col)
            val values = ChooseNavbarCol.values().map {
                Pair(it, when (it) {
                    ChooseNavbarCol.NONE -> stringArray[0]
                    ChooseNavbarCol.COLOR -> stringArray[1]
                })
            }
            withSingleChoiceItems(values, userPreferences.navbarColChoice) {
                userPreferences.navbarColChoice = it
            }
            setPositiveButton(R.string.action_ok) { _, _ ->
                updateNavbarCol(userPreferences.navbarColChoice)
            }
        }
    }

    private fun updateNavbarCol(choice: ChooseNavbarCol) {
        if (choice == ChooseNavbarCol.COLOR) {
            showNavbarColPicker()
        }
        userPreferences.navbarColChoice = choice
    }

    private fun showNavbarColPicker() {
        var initColor = userPreferences.colorNavbar
        if (userPreferences.navbarColChoice == ChooseNavbarCol.NONE) {
            initColor = Color.WHITE
        }
        ColorPickerDialogBuilder
            .with(activity)
            .setTitle("Choose color")
            .initialColor(initColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(12)
            .setOnColorSelectedListener { }
            .setPositiveButton("ok") { _, selectedColor, _ -> userPreferences.colorNavbar = selectedColor }
            .setNegativeButton("cancel") { _, _ -> }
            .build()
            .show()
    }

    private fun showTextSizePicker() {
        val maxValue = 7
        MaterialAlertDialogBuilder(requireContext()).apply {
            val layoutInflater = activity?.layoutInflater
            val customView = (layoutInflater?.inflate(R.layout.dialog_seek_bar, null) as LinearLayout).apply {
                val text = TextView(activity).apply {
                    setText(R.string.untitled)
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                addView(text)
                val size = TextView(activity).apply {
                    setText(R.string.untitled)
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                    gravity = Gravity.CENTER_HORIZONTAL
                }
                addView(size)

                findViewById<SeekBar>(R.id.text_size_seekbar).apply {
                    setOnSeekBarChangeListener(TextSeekBarListener(text, size))
                    max = maxValue
                    progress = maxValue - userPreferences.textSize
                }
            }
            setView(customView)
            setTitle(R.string.title_text_size)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val seekBar = customView.findViewById<SeekBar>(R.id.text_size_seekbar)
                userPreferences.textSize = maxValue - seekBar.progress
            }
        }.resizeAndShow()
    }

    private class TextSeekBarListener(
        private val sampleText: TextView,
        private val sizeText: TextView
    ) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(view: SeekBar, size: Int, user: Boolean) {
            this.sampleText.textSize = getTextSize(size)
            this.sizeText.text = (size * 15 + 40).toString() + "%"
        }
        override fun onStartTrackingTouch(arg0: SeekBar) {}
        override fun onStopTrackingTouch(arg0: SeekBar) {}
    }

    companion object {
        private const val SETTINGS_THEME = "app_theme"
        private const val SETTINGS_NAVBAR_COL = "navbar_col"
        private const val SETTINGS_DARK_MODE = "dark_mode"
        private const val SETTINGS_BLACK_STATUS = "black_status_bar"
        private const val SETTINGS_COLOR_MODE = "cb_colormode"
        private const val SETTINGS_BOTTOM_BAR = "bottom_bar"
        private const val SETTINGS_HIDESTATUSBAR = "fullScreenOption"
        private const val SETTINGS_FULLSCREEN = "fullscreen"
        private const val SETTINGS_TEXTSIZE = "text_size"
        private const val SETTINGS_VIEWPORT = "wideViewPort"
        private const val SETTINGS_OVERVIEWMODE = "overViewMode"
        private const val SETTINGS_FORCE_ZOOM = "force_zoom"

        private fun getTextSize(size: Int): Float = when (size) {
            0 -> 10f
            1 -> 12f
            2 -> 14f
            3 -> 16f
            4 -> 18f
            5 -> 20f
            6 -> 22f
            7 -> 24f
            else -> 16f
        }
    }
}
