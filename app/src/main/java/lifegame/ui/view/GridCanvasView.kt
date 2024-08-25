package lifegame.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import lifegame.constants.CellState.ALIVE
import lifegame.core.Cell

class GridCanvasView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private var map: Array<Array<Cell>>? = null
        set(value) {
            field = value
            invalidate()
        }
    private var cellSize = 10 // px

    fun setCellSize(size: Int) {
        cellSize = size
    }

    fun update(map: Array<Array<Cell>>) {
        this.map = map
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        map?.let { map ->
            for (y in map.indices) {
                for (x in map[y].indices) {
                    paint.color =
                        if (map[y][x].state == ALIVE) Color.BLACK
                        else Color.WHITE
                    canvas.drawRect(
                        (x * cellSize).toFloat(),
                        (y * cellSize).toFloat(),
                        ((x + 1) * cellSize).toFloat(),
                        ((y + 1) * cellSize).toFloat(),
                        paint
                    )
                }
            }
        }
    }
}