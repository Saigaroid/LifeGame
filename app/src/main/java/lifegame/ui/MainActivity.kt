package lifegame.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.lifegame.R
import lifegame.constants.CellState.ALIVE
import lifegame.constants.CellState.DEATH
import lifegame.core.Cell
import lifegame.ui.view.GridCanvasView

class MainActivity : ComponentActivity() {
    companion object {
        private const val CELL_SIZE = 50 // px
    }

    private lateinit var cellMap: Array<Array<Cell>>
    private var cellUpdateThread: Thread? = null
    private var canvasView: GridCanvasView? = null
    private var gameOverTxt: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    private fun initView(rootView: View) {
        canvasView = rootView.findViewById<GridCanvasView>(R.id.view_canvas).also { canvas ->
            canvas.post {
                cellMap = Array(canvas.height / CELL_SIZE) {
                    List(canvas.width / CELL_SIZE) {
                        if ((0 .. 5).random() == 0) Cell(ALIVE)
                        else Cell(DEATH)
                    }.toTypedArray()
                }
                canvas.setCellSize(CELL_SIZE)
                redraw(cellMap)

                start()
            }
        }

        gameOverTxt = rootView.findViewById(R.id.txt_game_over)
    }

    private fun start() {
        cellUpdateThread = CellUpdateThread().also {
            it.start()
        }
    }

    private fun stop() {
        cellUpdateThread?.let { thread ->
            thread.join()
            thread.interrupt()
        }
        cellUpdateThread = null

        Handler(Looper.getMainLooper()).post {
            gameOverTxt?.visibility = VISIBLE
        }
    }

    private fun redraw(map: Array<Array<Cell>>) {
        Handler(Looper.getMainLooper()).post {
            canvasView?.update(map.clone())
        }
    }

    private fun updateAllCellState(map: Array<Array<Cell>>) {
        var updated = false

        for (y in map.indices) {
            for (x in map[y].indices) {
                val aroundAliveCnt = getAroundCellNum(map, x, y)
                val cell = map[y][x]
                val curState = cell.state

                if (curState == DEATH) {
                    if (aroundAliveCnt == 3) {
                        cell.state = ALIVE
                        updated = true
                    }
                } else {
                    if (aroundAliveCnt !in 2 .. 3) {
                        cell.state = DEATH
                        updated = true
                    }
                }
            }
        }

        if (!updated) stop()
    }

    private fun getAroundCellNum(map: Array<Array<Cell>>,
                                 x: Int,
                                 y: Int): Int {
        fun top(): Cell = map[y - 1][x]
        fun bottom(): Cell = map[y + 1][x]
        fun left(): Cell = map[y][x - 1]
        fun right(): Cell = map[y][x + 1]
        fun topLeft(): Cell = map[y - 1][x - 1]
        fun bottomLeft(): Cell = map[y + 1][x - 1]
        fun topRight(): Cell = map[y - 1][x + 1]
        fun bottomRight(): Cell = map[y + 1][x + 1]

        fun isAlive(cell: Cell): Boolean = cell.state == ALIVE

        var cnt = 0

        // top
        if (y - 1 in map.indices && isAlive(top())) cnt++

        // bottom
        if (y + 1 in map.indices && isAlive(bottom())) cnt++

        // left
        if (x - 1 in map[y].indices) {
            if (y - 1 in map.indices && isAlive(topLeft())) cnt++
            if (isAlive(left())) cnt++
            if (y + 1 in map.indices && isAlive(bottomLeft())) cnt++
        }

        // right
        if (x + 1 in map[y].indices) {
            if (y - 1 in map.indices && isAlive(topRight())) cnt++
            if (isAlive(right())) cnt++
            if (y + 1 in map.indices && isAlive(bottomRight())) cnt++
        }

        return cnt
    }

    private inner class CellUpdateThread : Thread() {
        override fun run() {
            while (true) {
                try {
                    updateAllCellState(cellMap)
                    redraw(cellMap)
                    sleep(100)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
    }
}