package lifegame.core

import lifegame.constants.CellState

class Cell(var state: CellState): Cloneable {
    public override fun clone(): Cell = Cell(this.state)
}