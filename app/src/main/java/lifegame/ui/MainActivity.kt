package lifegame.ui

import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.lifegame.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lifegame.constants.CellState.ALIVE
import lifegame.constants.CellState.DEATH
import lifegame.core.Cell
import lifegame.ui.view.GridCanvasView

class MainActivity : ComponentActivity() {
    companion object {
        private const val CELL_SIZE = 50 // px
        private const val INITIALIZATION_PROBABILITY = 0.2 // 20% の確率でセルが生きている状態で初期化される
        private const val CELL_UPDATE_INTERVAL_MS = 100L

        private val DIRECTIONS = listOf(
            -1 to -1, -1 to 0, -1 to 1,
            0 to -1, 0 to 1,
            1 to -1, 1 to 0, 1 to 1
        )
    }

    private lateinit var cellMap: Array<Array<Cell>>
    private var cellUpdateThread: Thread? = null

    private lateinit var canvasView: GridCanvasView
    private lateinit var gameOverTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

    override fun onDestroy() {
        super.onDestroy()
        cellUpdateThread?.interrupt()
    }

    private fun initView(rootView: View) {
        gameOverTxt = rootView.findViewById(R.id.txt_game_over)

        canvasView = rootView.findViewById(R.id.view_canvas)
        canvasView.post {
            CoroutineScope(Dispatchers.Default).launch {
                initializeCellMap()
                runOnUiThread {
                    canvasView.updateCellSize(CELL_SIZE)
                    redraw(cellMap)
                    start()
                }
            }
        }
    }

    private fun initializeCellMap() {
        assert(canvasView.width > 0 && canvasView.height > 0)
        val rows = canvasView.height / CELL_SIZE
        val cols = canvasView.width / CELL_SIZE
        cellMap = Array(rows) {
            Array(cols) {
                Cell(if (Math.random() < INITIALIZATION_PROBABILITY) ALIVE else DEATH)
            }
        }
    }

    private fun start() {
        cellUpdateThread = Thread(CellUpdateRunnable()).apply { start() }
    }

    private fun stop() {
        cellUpdateThread?.interrupt()
        cellUpdateThread = null
        runOnUiThread {
            gameOverTxt.visibility = VISIBLE
        }
    }

    private fun redraw(map: Array<Array<Cell>>) {
        runOnUiThread {
            canvasView.update(map.clone())
        }
    }

    private fun updateAllCellState(map: Array<Array<Cell>>) {
        val newMap = map.map { row -> row.map { it.clone() }.toTypedArray() }.toTypedArray()
        var updated = false
        for (y in map.indices) {
            for (x in map[y].indices) {
                updated = updateCellState(map, newMap, x, y) or updated
            }
        }
        if (!updated) stop()
        cellMap = newMap
    }

    private fun updateCellState(
        oldMap: Array<Array<Cell>>,
        newMap: Array<Array<Cell>>,
        x: Int,
        y: Int
    ): Boolean {
        val aroundAliveCnt = getAroundCellNum(oldMap, x, y)
        val cell = newMap[y][x]
        val curState = oldMap[y][x].state
        return when {
            curState == DEATH && aroundAliveCnt == 3 -> {
                cell.state = ALIVE
                true
            }

            curState == ALIVE && aroundAliveCnt !in 2..3 -> {
                cell.state = DEATH
                true
            }

            else -> false
        }
    }

    private fun getAroundCellNum(map: Array<Array<Cell>>, x: Int, y: Int): Int {
        return DIRECTIONS.count { (dy, dx) ->
            val newY = y + dy
            val newX = x + dx
            newY in map.indices && newX in map[newY].indices && map[newY][newX].state == ALIVE
        }
    }

    private inner class CellUpdateRunnable : Runnable {
        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    updateAllCellState(cellMap)
                    redraw(cellMap)
                    Thread.sleep(CELL_UPDATE_INTERVAL_MS)
                } catch (ex: InterruptedException) {
                    break
                }
            }
        }
    }
}