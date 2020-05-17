package com.elliot.kim.kotlin.dimcatcamnote.item_touch_helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

const val toLeft = 0
const val toRight = 1

@SuppressLint("ClickableViewAccessibility")
abstract class RecyclerViewTouchHelper(val context: Context, private val recyclerView: RecyclerView,
                      private var leftButtonWidth: Int, private var rightButtonWidth: Int,
                                       private val itemMovedListener: ItemMovedListener
)
    : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var removerQueue: Queue<Int>

    private var leftButtons: MutableList<UnderlayButton>? = null
    private val leftButtonBuffer: MutableMap<Int, MutableList<UnderlayButton>>

    private var rightButtons: MutableList<UnderlayButton>? = null
    private val rightButtonBuffer: MutableMap<Int, MutableList<UnderlayButton>>

    private var swipePosition = -1
    private var swipeThreshold = 0.5F

    abstract fun instantiateRightUnderlayButton(viewHolder: RecyclerView.ViewHolder,
                                           rightButtonBuffer: MutableList<UnderlayButton>)

    abstract fun instantiateLeftUnderlayButton(viewHolder: RecyclerView.ViewHolder,
                                               leftButtonBuffer: MutableList<UnderlayButton>)

    private val gestureListener = object
        : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            for (button in rightButtons!!)
                if (button.onClick(e!!.x, e.y)) break

            for (button in leftButtons!!)
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

        this.rightButtons = ArrayList()
        this.rightButtonBuffer = HashMap()

        this.leftButtons = ArrayList()
        this.leftButtonBuffer = HashMap()

        this.removerQueue = InLinkedList()

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
        val position = viewHolder.adapterPosition
        if (swipePosition != position) removerQueue.add(swipePosition)
        swipePosition = position

        if (rightButtonBuffer.containsKey(swipePosition)) rightButtons =
            rightButtonBuffer[swipePosition]
        else rightButtons!!.clear()
        rightButtonBuffer.clear()

        if (leftButtonBuffer.containsKey(swipePosition)) leftButtons =
            leftButtonBuffer[swipePosition]
        else leftButtons!!.clear()
        leftButtonBuffer.clear()

        // 수정 해야 될듯.if (direction == ItemTouchHelper.START) = swipeThreshold
        // left button 쭉 딸려나오는 문제.

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
                if (!rightButtonBuffer.containsKey(position)) {
                    instantiateRightUnderlayButton(viewHolder, buffer)
                    rightButtonBuffer[position] = buffer
                } else buffer = rightButtonBuffer[position]!!

                translationX = dX * rightButtonWidth / itemView.width
                drawButton(c, itemView, buffer, position, translationX, toLeft)
            } else if (dX > 0){
                var buffer: MutableList<UnderlayButton> = ArrayList()
                if (!leftButtonBuffer.containsKey(position)) {
                    instantiateLeftUnderlayButton(viewHolder, buffer)
                    leftButtonBuffer[position] = buffer
                } else buffer = leftButtonBuffer[position]!!

                translationX = dX * leftButtonWidth / itemView.width
                drawButton(c, itemView, buffer, position, translationX, toRight)
            }

            super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun drawButton(c: Canvas, itemView: View, buffer: MutableList<UnderlayButton>,
                           position: Int, translationX: Float, direction: Int) {
        var left = itemView.left.toFloat()
        var right = itemView.right.toFloat()
        var buttonWidth = 0F

        if (direction == toLeft) buttonWidth = -1 * translationX / buffer.size
        else if (direction == toRight) buttonWidth = translationX / buffer.size

        if (direction == toLeft) {
            for (button in buffer) {
                left = right - buttonWidth
                button.onDraw(
                    c, RectF(left, itemView.top.toFloat(), right, itemView.bottom.toFloat()),
                    position
                )
                right = left
            }
        } else if (direction == toRight) {
            for (button in buffer) {
                right = left + buttonWidth
                button.onDraw(
                    c, RectF(left, itemView.top.toFloat(), right, itemView.bottom.toFloat()),
                    position
                )
                left = right
            }
        }
    }

    override fun isItemViewSwipeEnabled(): Boolean = true
    override fun isLongPressDragEnabled(): Boolean = true

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.2F * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0F * defaultValue
    }


}