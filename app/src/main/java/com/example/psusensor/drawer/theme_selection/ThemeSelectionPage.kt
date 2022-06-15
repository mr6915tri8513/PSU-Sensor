package com.example.psusensor.drawer.theme_selection

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.psusensor.Communicator
import com.example.psusensor.R
import com.example.psusensor.databinding.FragmentThemeSelectionPageBinding
import com.example.psusensor.theme.Theme
import com.example.psusensor.theme.ThemeViewModel
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class ThemeSelectionPage: Fragment() {

    private lateinit var communicator: Communicator
    private lateinit var themeViewModel: ThemeViewModel
    private var _binding: FragmentThemeSelectionPageBinding? = null
    private val binding get() = _binding!!
    private val adapter = ThemeListAdapter(this)

    private lateinit var theme: Theme

    override fun onCreateView (
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeSelectionPageBinding.inflate(inflater, container, false)
        val view = binding.root

        communicator = activity as Communicator

        themeViewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setTheme(getTheme())

        binding.themeListRv.adapter = adapter
        binding.themeListRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        themeViewModel.allThemes.observe(viewLifecycleOwner) { themes ->
            adapter.onDataChanged(themes)
        }

        communicator.setDrawerChecked(1, 0, true)

        return view
    }

    private fun getTheme(): Theme {
        requireActivity().getSharedPreferences("themeDatabase", Context.MODE_PRIVATE).apply {
            val backgroundColor = getInt("backgroundColor", Theme.Blue)
            val ledColor = getInt("ledColor", Theme.Blue)
            val textColor = getInt("textColor", Theme.White)
            val themeId = getLong("themeId", 0)
            return Theme(if (themeId == 0L) -4 else themeId, "", null, backgroundColor, ledColor, textColor)
        }
    }

    private fun storeTheme() {
        val sharedPref = requireActivity().getSharedPreferences("themeDatabase", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putLong("themeId", theme.id)
            putInt("backgroundColor", theme.backgroundColor)
            putInt("ledColor", theme.ledColor)
            putInt("textColor", theme.textColor)
            apply()
        }
    }

    private fun setTheme(theme: Theme) {
        this.theme = theme
        binding.background.backgroundTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(theme.backgroundColor)
        )
        binding.led.backgroundTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(theme.ledColor)
        )
        binding.powerTv.setTextColor(theme.textColor)
        communicator.setTheme(theme)
    }

    private fun addTheme(theme: Theme) {
        themeViewModel.addTheme(theme) { themeId ->
            this.theme = Theme(themeId, theme.name, theme.iconUri, theme.backgroundColor, theme.ledColor, theme.textColor)
            adapter.notifyItemChanged(adapter.itemCount - 2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        storeTheme()
        communicator.setDrawerChecked(1, 0, false)
        _binding = null
    }

    private class ThemeListAdapter(private val parent: ThemeSelectionPage): RecyclerView.Adapter<ThemeListAdapter.ThemeViewHolder>() {

        private var themeList = emptyList<Theme>()

        class ThemeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val radioBtn: RadioButton = itemView.findViewById(R.id.radio_btn)
            val themePhotoImg: ImageView = itemView.findViewById(R.id.theme_photo_img)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
            return ThemeViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.theme_row, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return themeList.count() + 4
        }

        override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
            val theme = when (holder.adapterPosition) {
                0 -> Theme(-4, parent.getString(R.string.theme_classic), null, Theme.Blue, Theme.Blue, Theme.White)
                1 -> Theme(-3, parent.getString(R.string.theme_black_and_white), null, Theme.Black, Theme.White, Theme.White)
                2 -> Theme(-2, parent.getString(R.string.theme_hacker), null, Theme.Black, Theme.LightGreen, Theme.LightGreen)
                themeList.count() + 3 -> {
                    holder.radioBtn.buttonDrawable = null
                    Theme(-1, parent.getString(R.string.add_custom_theme), null, Theme.Blue, Theme.Black, Theme.White)
                }
                else -> {
                    holder.radioBtn.setButtonDrawable(androidx.appcompat.R.drawable.abc_btn_radio_material)
                    themeList[holder.adapterPosition - 3]
                }
            }
            holder.radioBtn.isChecked = parent.theme.id == theme.id
            holder.radioBtn.text = theme.name
            if (theme.iconUri != null) {
                Log.e("uri", theme.iconUri)
                Glide.with(holder.itemView)
                    .load(theme.iconUri)
                    .error(R.drawable.ic_baseline_image_not_supported_48)
                    .into(holder.themePhotoImg)
                //holder.themePhotoImg.setImageURI(theme.iconUri.toUri()) //TODO here
                holder.themePhotoImg.visibility = View.VISIBLE
            } else {
                when (holder.adapterPosition) {
                    0 -> holder.themePhotoImg.apply {
                        setImageResource(R.drawable.ic_thinking)
                        visibility = View.VISIBLE
                    }
                    1 -> holder.themePhotoImg.apply {
                        setImageResource(R.drawable.ic_pinched_finger)
                        visibility = View.VISIBLE
                    }
                    2 -> holder.themePhotoImg.apply {
                        setImageResource(R.drawable.ic_hacker)
                        visibility = View.VISIBLE
                    }
                    itemCount - 1 -> holder.themePhotoImg.apply {
                        setImageResource(R.drawable.ic_add_custom_theme)
                        visibility = View.VISIBLE
                    }
                    else -> holder.themePhotoImg.visibility = View.GONE
                }
            }
            val colorStateList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_enabled)),
                intArrayOf(theme.textColor)
            )
            holder.radioBtn.buttonTintList = colorStateList
            holder.radioBtn.setTextColor(theme.textColor)
            holder.itemView.setBackgroundColor(theme.backgroundColor)

            fun onClick() {
                Log.d("themeId", theme.id.toString())
                if (theme.id == -1L) {
                    CustomThemeDialogFragment(parent, itemCount, null)
                        .show(parent.parentFragmentManager, "add_custom_theme")
                } else if (theme.id != parent.theme.id) {
                    val pos =
                        if (parent.theme.id < 0) {
                            (parent.theme.id).toInt() + 4
                        } else {
                            themeList.indexOfFirst {
                                it.id == parent.theme.id
                            } + 3
                        }
                    parent.setTheme(theme)
                    if (pos != -1) {
                        notifyItemChanged(pos)
                    }
                    Log.e("origin", pos.toString())
                    holder.radioBtn.isChecked = true
                    //notifyItemChanged(holder.adapterPosition)
                }
            }

            holder.itemView.setOnClickListener {
                onClick()
            }

            holder.radioBtn.setOnClickListener {
                onClick()
            }

            fun onLongClick() {
                if (theme.id > 0) {
                    val dialog = CustomThemeDialogFragment(parent, itemCount, theme)
                    dialog.show(parent.parentFragmentManager, "edit_custom_theme")
                }
            }

            holder.itemView.setOnLongClickListener {
                onLongClick()
                true
            }

            holder.radioBtn.setOnLongClickListener {
                onLongClick()
                true
            }
        }

        fun onDataChanged(themes: List<Theme>) {
            themeList = themes
            notifyDataSetChanged()
        }
    }

    class CustomThemeDialogFragment(private val parent: ThemeSelectionPage, private val count: Int, private val theme: Theme?): DialogFragment() {

        private var ledColor = Theme.AquaColor       //0xFFFFFFFF
        private var backgroundColor = Theme.AquaBlue //0xFF000000
        private var textColor = Theme.White      //0xFFFFFFFF
        private var iconUri: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return activity?.let { activity ->
                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(activity)
                val view = activity.layoutInflater.inflate(R.layout.fragment_custom_theme, activity.findViewById(R.id.custom_theme_container))

                val nameEt: EditText = view.findViewById(R.id.name_et)
                val led: View = view.findViewById(R.id.led)
                val background: View = view.findViewById(R.id.background)
                val text: TextView = view.findViewById(R.id.text)
                val ledBtn: Button = view.findViewById(R.id.led_btn)
                val textBtn: Button = view.findViewById(R.id.text_btn)
                val backgroundBtn: Button = view.findViewById(R.id.background_btn)
                val ledColorTv: TextView = view.findViewById(R.id.led_color_tv)
                val textColorTv: TextView = view.findViewById(R.id.text_color_tv)
                val backgroundColorTv: TextView = view.findViewById(R.id.background_color_tv)
                val iconImg: ImageView = view.findViewById(R.id.icon_img)
                val selectIconTv: TextView = view.findViewById(R.id.select_icon_tv)

                val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        // There are no request codes
                        val data: Intent? = result.data
                        Log.e("data", data.toString())
                        data?.data?.let { it ->
                            Glide.with(parent)
                                .load(it)
                                .into(iconImg)
                            //iconImg.setImageURI(it)
                            iconUri = it.toString()
                            selectIconTv.visibility = View.GONE
                            Log.e("uri", it.toString())
                        }
                    }
                }

                fun applyColorChange() {
                    led.backgroundTintList = ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_enabled)),
                        intArrayOf(ledColor)
                    )
                    text.setTextColor(textColor)
                    background.backgroundTintList = ColorStateList(
                        arrayOf(intArrayOf(android.R.attr.state_enabled)),
                        intArrayOf(backgroundColor)
                    )
                    ledColorTv.text = String.format("#%06X", ledColor and 0xFFFFFF)
                    textColorTv.text = String.format("#%06X", textColor and 0xFFFFFF)
                    backgroundColorTv.text = String.format("#%06X", backgroundColor and 0xFFFFFF)
                }

                fun showColorPicker(type: Int) {
                    ColorPickerDialog.Builder(parent.requireContext())
                        .setTitle(getString(when (type) {
                            1 -> R.string.select_led_color
                            2 -> R.string.select_text_color
                            3 -> R.string.select_background_color
                            else -> R.string.select_color
                        }))
                        //.setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton(getString(R.string.confirm),
                            object: ColorEnvelopeListener {
                                override fun onColorSelected(
                                    envelope: ColorEnvelope?,
                                    fromUser: Boolean
                                ) {
                                    envelope?.apply{
                                        when(type) {
                                            1 -> ledColor = color
                                            2 -> textColor = color
                                            3 -> backgroundColor = color
                                        }
                                        applyColorChange()
                                    }
                                }
                            })
                        .setNegativeButton(getString(R.string.cancel)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .attachAlphaSlideBar(false) // default is true. If false, do not show the AlphaSlideBar.
                        .attachBrightnessSlideBar(true)  // default is true. If false, do not show the BrightnessSlideBar.
                        .show()
                }

                if (theme != null) {
                    nameEt.setText(theme.name)
                    ledColor = theme.ledColor
                    textColor = theme.textColor
                    backgroundColor = theme.backgroundColor
                    theme.iconUri?.let { uri ->
                        iconUri = uri
                        Glide.with(this)
                            .load(theme.iconUri)
                            .error(R.drawable.ic_baseline_image_not_supported_48)
                            .into(iconImg)
                        selectIconTv.visibility = View.GONE
                    }

                } else {
                    nameEt.setText(getString(R.string.custom_theme_name, count - 3))
                }

                applyColorChange()

                ledBtn.setOnClickListener {
                    showColorPicker(1)
                }

                textBtn.setOnClickListener {
                    showColorPicker(2)
                }

                backgroundBtn.setOnClickListener {
                    showColorPicker(3)
                }

                iconImg.setOnClickListener {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
                    resultLauncher.launch(Intent.createChooser(intent, getString(R.string.select_icon)))
                }

                builder.setView(view)
                    //builder.setMessage(R.string.add_custom_theme)
                    .setPositiveButton(if (theme == null) R.string.add else R.string.edit) { dialog, id ->
                        Log.e("dialog", "save")
                        if (theme == null) {
                            parent.addTheme(
                                Theme(0, nameEt.text.toString(), iconUri, backgroundColor, ledColor, textColor)
                            )
                        } else {
                            parent.themeViewModel.updateTheme(
                                Theme(theme.id, nameEt.text.toString(), iconUri, backgroundColor, ledColor, textColor)
                            )
                        }
                    }
                    .setNegativeButton(R.string.cancel) { dialog, id ->
                        Log.e("dialog", "cancel")
                    }

                if (theme != null) {
                    builder.setNeutralButton(R.string.delete) { dialog, id ->
                        Log.e("dialog", "delete")
                        ConfirmDialogFragment(R.string.confirm_delete) {
                            if (it) {
                                parent.themeViewModel.deleteTheme(theme)
                            }
                        }.show(parentFragmentManager, "confirm_delete_theme")
                    }
                }
                // Create the AlertDialog object and return it
                builder.create()
            } ?: throw IllegalStateException("Activity cannot be null")
        }
    }
}