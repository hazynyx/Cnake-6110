package com.snake.cnake6110.game

import java.util.ArrayDeque
import kotlin.random.Random

class CnakeEngine(
    val cols: Int = 10,
    val rows: Int = 20
) {
    var state: GameState = GameState.READY
        private set

    val snake = mutableListOf<GridPoint>()
    var currentDirection: Direction = Direction.UP
        private set

    private val directionQueue = ArrayDeque<Direction>()

    var food: GridPoint = GridPoint(4, 5)
        private set

    init {
        initGame()
    }

    fun initGame() {
        state = GameState.READY
        directionQueue.clear()
        currentDirection = Direction.UP
        snake.clear()

        // Initial snake: 4 segments centered vertically
        val startX = cols / 2
        val startY = rows / 2
        snake.add(GridPoint(startX, startY))
        snake.add(GridPoint(startX, startY + 1))
        snake.add(GridPoint(startX, startY + 2))
        snake.add(GridPoint(startX, startY + 3))

        spawnFood()
    }

    fun startPlaying() {
        if (state == GameState.READY) {
            state = GameState.PLAYING
        }
    }

    fun restart() {
        directionQueue.clear()
        currentDirection = Direction.UP
        snake.clear()

        val startX = cols / 2
        val startY = rows / 2
        snake.add(GridPoint(startX, startY))
        snake.add(GridPoint(startX, startY + 1))
        snake.add(GridPoint(startX, startY + 2))
        snake.add(GridPoint(startX, startY + 3))

        spawnFood()
        state = GameState.PLAYING
    }

    fun setDirection(newDirection: Direction) {
        if (state == GameState.READY) {
            // If swiped in ready state, start game with that direction
            if (!newDirection.isOpposite(currentDirection)) {
                currentDirection = newDirection
            }
            state = GameState.PLAYING
            return
        }

        if (state != GameState.PLAYING) return

        val lastPending = if (directionQueue.isNotEmpty()) directionQueue.last else currentDirection
        if (!newDirection.isOpposite(lastPending) && newDirection != lastPending) {
            if (directionQueue.size < 2) {
                directionQueue.addLast(newDirection)
            }
        }
    }

    fun tick(): Boolean {
        if (state != GameState.PLAYING) return false

        if (directionQueue.isNotEmpty()) {
            currentDirection = directionQueue.removeFirst()
        }

        val head = snake.first()
        val nextX = (head.x + currentDirection.dx).mod(cols)
        val nextY = (head.y + currentDirection.dy).mod(rows)
        val newHead = GridPoint(nextX, nextY)

        val willEat = (newHead == food)

        // Collision check with snake body
        // If not eating, tail moves away, so collision is against body excluding tail
        val checkBody = if (willEat) snake else snake.subList(0, snake.size - 1)
        if (checkBody.contains(newHead)) {
            // Collision! Snake dies
            state = GameState.DEAD
            return true
        }

        // Move snake
        snake.add(0, newHead)
        if (willEat) {
            spawnFood()
        } else {
            snake.removeAt(snake.size - 1)
        }

        return true
    }

    private fun spawnFood() {
        val freePositions = ArrayList<GridPoint>(cols * rows)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val pt = GridPoint(x, y)
                if (!snake.contains(pt)) {
                    freePositions.add(pt)
                }
            }
        }

        food = if (freePositions.isNotEmpty()) {
            freePositions[Random.nextInt(freePositions.size)]
        } else {
            GridPoint(0, 0)
        }
    }

    fun isTouchOnDeadSnake(gx: Int, gy: Int): Boolean {
        val pt = GridPoint(gx, gy)
        return snake.contains(pt)
    }

    fun isTouchNearDeadSnake(gx: Float, gy: Float): Boolean {
        for (seg in snake) {
            val dx = seg.x - gx
            val dy = seg.y - gy
            if (dx * dx + dy * dy <= 1.8f) {
                return true
            }
        }
        return false
    }
}
