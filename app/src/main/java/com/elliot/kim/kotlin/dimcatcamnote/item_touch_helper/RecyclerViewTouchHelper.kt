package com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("ClickableViewAccessibility")
abstract class RecyclerViewTouchHelper(context: Context, private val recyclerView: RecyclerView,
                      private var buttonWidth: Int, private val itemMovedListener: ItemMovedListener
)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private var buttons: MutableList<UnderlayButton>? = null
    private val buttonBuffer: MutableMap<Int, MutableList<UnderlayButton>>

    private lateinit var gestureDetector: GestureDetector
    private lateinit var removerQueue: Queue<Int>

    private var swipePosition = -1
    private var swipeThreshold = 0.5F

    lateinit var itemTouchHelper: ItemTouchHelper

    abstract fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder,
                                           buttonBuffer: MutableList<UnderlayButton>)

    private val gestureListener = object
        : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            for (button in buttons!!)
                if (button.onClick(e!!.x, e.y)) break

            return true
        }
    }

    private val onTouchListener = View.OnTouchListener { _, event ->
            if (swipePosition < 0) return@OnTouchListener false

            val point = Point(event?.rawX!!.toInt(), event.rawY.toInt())
            val rect = Rect()

            val swipeViewHolder = recyclerView
                .findViewHolderForAdapterPosition(swipePosition)
            val swipedItem = swipeViewHolder?.itemView
            swipedItem?.getGlobalVisibleRect(rect)

            if (event.action == MotionEvent.ACTION_DOWN ||
                event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(event)
                else {
                    removerQueue.add(swipePosition)
                    swipePosition = -1
                    recoverSwipedItem()
                }
            }

            false
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!removerQueue.isEmpty()) {
            val position = removerQueue.poll()!!.toInt()
            if (position > -1)
                recyclerView.adapter!!.notifyItemChanged(position)
        }
    }

    init {
        this.recyclerView.setOnTouchListener(onTouchListener)
        this.gestureDetector = GestureDetector(context, gestureListener)
        this.buttons = ArrayList()
        this.buttonBuffer = HashMap()
        this.removerQueue =
            InLinkedList()

        attachRecyclerView()
    }

    class InLinkedList: LinkedList<Int> () {
        override fun contains(element: Int): Boolean {
            return false
        }

        override fun lastIndexOf(element: Int): Int {
            return element
        }

        override fun remove(element: Int): Boolean {
            return false
        }

        override fun indexOf(element: Int): Int {
            return element
        }

        override fun add(element: Int): Boolean {
            return if (contains(element)) false
            else super.add(element)
        }
    }


    private fun attachRecyclerView() {
        itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getMovementFlags(recyclerView: RecyclerView,
                                  viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.DOWN or ItemTouchHelper.UP
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean {
        itemMovedListener.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //listener.onItemSwiped(viewHolder.adapterPosition)

        val position = viewHolder.adapterPosition
        if (swipePosition != position) removerQueue.add(swipePosition)
        swipePosition = position

        if (buttonBuffer.containsKey(swipePosition)) buttons = buttonBuffer[swipePosition]
        else buttons!!.clear()
        buttonBuffer.clear()

        swipeThreshold = 0.5F * buttons!!.size.toFloat() * buttonWidth.toFloat()

        recoverSwipedItem()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val position = viewHolder.adapterPosition
        var translationX = dX
        val itemView = viewHolder.itemView

        if (position < 0) {
            swipePosition = position
            return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX < 0) {
                var buffer: MutableList<UnderlayButton> = ArrayList()
                if (!buttonBuffer.containsKey(position)) {
                    instantiateUnderlayButton(viewHolder, buffer)
                    buttonBuffer[position] = buffer
                } else buffer = buttonBuffer[position]!!

                translationX = dX * buttonBuffer.size * buttonWidth / itemView.width // 비슷하게 반대방향도 제약 가능.
                drawButton(c, itemView, buffer, position, translationX)
            }

            super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun drawButton(c: Canvas, itemView: View, buffer: MutableList<UnderlayButton>,
                           position: Int, translationX: Float) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * translationX / buffer.size

        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(c, RectF(left, itemView.top.toFloat(), right, itemView.bottom.toFloat()),
                position)
            right = left
        }
    }

    override fun isItemViewSwipeEnabled(): Boolean = true
    override fun isLongPressDragEnabled(): Boolean = true

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1F * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0F * defaultValue
    }
}