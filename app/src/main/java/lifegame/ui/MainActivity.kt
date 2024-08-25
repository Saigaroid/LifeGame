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
    private val handler = Handler(Looper.getMainLooper())

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
                        if ((0..5).random() == 0) Cell(ALIVE)
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
        cellUpdateThread?.interrupt()
        cellUpdateThread = null

        handler.post {
            gameOverTxt?.visibility = VISIBLE
        }
    }

    private fun redraw(map: Array<Array<Cell>>) {
        handler.post {
            canvasView?.update(map.clone())
        }
    }

    private fun updateAllCellState(map: Array<Array<Cell>>) {
        val newMap = map.map { row -> row.map { it.clone() }.toTypedArray() }.toTypedArray()
        var updated = false

        for (y in map.indices) {
            for (x in map[y].indices) {
                val aroundAliveCnt = getAroundCellNum(map, x, y)
                val cell = newMap[y][x]
                val curState = map[y][x].state

                if (curState == DEATH) {
                    if (aroundAliveCnt == 3) {
                        cell.state = ALIVE
                        updated = true
                    }
                } else {
                    if (aroundAliveCnt !in 2..3) {
                        cell.state = DEATH
                        updated = true
                    }
                }
            }
        }

        if (!updated) stop()
        cellMap = newMap
    }

    private fun getAroundCellNum(map: Array<Array<Cell>>, x: Int, y: Int): Int {
        val directions = listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )

        var cnt = 0
        for ((dy, dx) in directions) {
            val newY = y + dy
            val newX = x + dx
            if (newY in map.indices && newX in map[newY].indices && map[newY][newX].state == ALIVE) {
                cnt++
            }
        }
        return cnt
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        cellUpdateThread?.interrupt()
    }

    private inner class CellUpdateThread : Thread() {
        override fun run() {
            while (!isInterrupted) {
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