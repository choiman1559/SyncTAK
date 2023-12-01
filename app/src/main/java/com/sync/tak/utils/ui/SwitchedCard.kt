package com.sync.tak.utils.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.switchmaterial.SwitchMaterial

import com.sync.tak.R
import kotlin.properties.Delegates

class SwitchedCard(context: Context?, attrs: AttributeSet?) : LinearLayoutCompat(context!!, attrs) {

    private lateinit var title: String
    private lateinit var description: String
    private lateinit var switchCompat: SwitchMaterial
    private lateinit var itemParent: LinearLayoutCompat
    private var checked by Delegates.notNull<Boolean>()

        init {
        if (attrs != null) {
            initAttrs(attrs)
        }
        initView()
    }

    private fun initAttrs(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SwitchedCard,
            0, 0
        ).apply {
            try {
                title = getString(R.styleable.SwitchedCard_title) ?: ""
                description = getString(R.styleable.SwitchedCard_description) ?: ""
                checked = getBoolean(R.styleable.SwitchedCard_checked, false)
            } finally {
                recycle()
            }
        }
    }

    private fun initView() {
        inflate(context, R.layout.item_switch_card, this)

        val titleTextView = findViewById<TextView>(R.id.itemTitle)
        val descriptionTextView = findViewById<TextView>(R.id.itemDescription)
        switchCompat = findViewById(R.id.switchCompat)
        itemParent = findViewById(R.id.itemParent)

        titleTextView.text = title
        descriptionTextView.text = description
        switchCompat.isChecked = checked
    }

    fun setSwitchChecked(checked: Boolean) {
        switchCompat.isChecked = checked
        requestLayout()
    }

    fun isChecked() : Boolean {
        return switchCompat.isChecked
    }

    override fun setOnClickListener(l: OnClickListener?) {
        itemParent.setOnClickListener(l)
    }
}
