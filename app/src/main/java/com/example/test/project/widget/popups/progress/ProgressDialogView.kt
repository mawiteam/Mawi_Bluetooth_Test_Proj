package com.example.test.project.widget.popups.progress

import android.content.Context
import android.graphics.Canvas
import android.text.Layout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ViewGroup
import com.example.test.project.widget.color
import com.example.test.project.widget.dip
import com.example.test.project.widget.sp
import com.facebook.fbui.textlayoutbuilder.TextLayoutBuilder

class ProgressDialogView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val messageTextLayoutBuilder: TextLayoutBuilder
    private var messageLayout: Layout? = null
    private var message: String? = null
    private var messageLeft: Float = 0f
    private var messageTop: Float = 0f

    private var radialProgressView: RadialProgressView = RadialProgressView(context)

    private val dimen44dp: Int = context.dip(44f)
    private val dimen24dp: Int = context.dip(24f)

    private var inLayout: Boolean = false

    init {
        setWillNotDraw(false)

        addView(radialProgressView, LayoutParams(dimen44dp, dimen44dp))

        messageTextLayoutBuilder = TextLayoutBuilder()
                .setTextColor(color(android.R.color.black))
                .setTextSize(context.sp(16f))
                .setSingleLine(true)
                .setEllipsize(TextUtils.TruncateAt.END)
    }

    fun setMessage(resId: Int) {
        setMessage(resources.getString(resId))
    }

    fun setMessage(message: String?) {
        if (this.message.equals(message)) {
            return
        }
        messageLayout = null
        this.message = message

        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        inLayout = true

        val width = MeasureSpec.getSize(widthMeasureSpec)

        radialProgressView.measure(
                MeasureSpec.makeMeasureSpec(radialProgressView.layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(radialProgressView.layoutParams.height, MeasureSpec.EXACTLY)
        )

        val height = radialProgressView.measuredHeight + dimen24dp * 2 + paddingTop + paddingBottom
        val messageMaxWidth = width - radialProgressView.measuredWidth - dimen24dp * 2 - dimen24dp

        if (messageLayout == null || messageLayout!!.text != message) {
            messageLayout = messageTextLayoutBuilder
                    .setText(message)
                    .setWidth(messageMaxWidth)
                    .build()
        }

        setMeasuredDimension(width, height)
        inLayout = false
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val parentLeft = paddingLeft + dimen24dp
        val parentTop = paddingTop + dimen24dp
        val parentBottom = bottom - top - paddingBottom - dimen24dp

        radialProgressView.layout(
                parentLeft,
                parentTop,
                parentLeft + radialProgressView.measuredWidth,
                parentTop + radialProgressView.measuredHeight
        )

        messageLayout?.let {
            messageLeft = parentLeft + radialProgressView.measuredWidth + dimen24dp.toFloat()
            messageTop = parentTop + (parentBottom - parentTop - it.height) / 2f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        messageLayout?.let {
            canvas.save()
            canvas.translate(messageLeft, messageTop)
            it.draw(canvas)
            canvas.restore()
        }
    }

    override fun requestLayout() {
        if (inLayout) {
            return
        }
        super.requestLayout()
    }

    override fun hasOverlappingRendering(): Boolean = false
}