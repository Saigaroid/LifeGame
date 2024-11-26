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
    companion object {
        private const val DEFAULT_CELL_SIZE = 10 // px
    }

    private val paint = Paint()
    private var cellGrid: Array<Array<Cell>>? = null
        set(value) {
            field = value
            invalidate()
        }
    private var cellSize = DEFAULT_CELL_SIZE

    fun updateCellSize(size: Int) {
        cellSize = size
    }

    fun update(cellGrid: Array<Array<Cell>>) {
        this.cellGrid = cellGrid
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cellGrid?.let { grid ->
            for (y in grid.indices) {
                for (x in grid[y].indices) {
                    paint.color = getColorForCell(grid[y][x])
                    drawCell(canvas, x, y)
                }
            }
        }
    }

    private fun getColorForCell(cell: Cell) = if (cell.state == ALIVE) Color.BLACK else Color.WHITE

    private fun drawCell(canvas: Canvas, x: Int, y: Int) {
        canvas.drawRect(
            (x * cellSize).toFloat(),
            (y * cellSize).toFloat(),
            ((x + 1) * cellSize).toFloat(),
            ((y + 1) * cellSize).toFloat(),
            paint
        )
    }
}