package com.snake.cnake6110.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CnakeEngineTest {

    private lateinit var engine: CnakeEngine

    @Before
    fun setUp() {
        engine = CnakeEngine(cols = 10, rows = 20)
    }

    @Test
    fun testInitialState() {
        assertEquals(GameState.READY, engine.state)
        assertEquals(4, engine.snake.size)
        assertEquals(Direction.UP, engine.currentDirection)
    }

    @Test
    fun testWallWrapping_RightToLeft() {
        engine.startPlaying()
        engine.snake.clear()
        engine.snake.add(GridPoint(9, 10))
        engine.snake.add(GridPoint(8, 10))
        engine.snake.add(GridPoint(7, 10))
        engine.currentDirection = Direction.RIGHT

        engine.tick()

        // Head should wrap from col 9 to col 0
        assertEquals(0, engine.snake.first().x)
        assertEquals(10, engine.snake.first().y)
        assertEquals(GameState.PLAYING, engine.state)
    }

    @Test
    fun testWallWrapping_LeftToRight() {
        engine.startPlaying()
        engine.snake.clear()
        engine.snake.add(GridPoint(0, 10))
        engine.snake.add(GridPoint(1, 10))
        engine.snake.add(GridPoint(2, 10))
        engine.currentDirection = Direction.LEFT

        engine.tick()

        // Head should wrap from col 0 to col 9
        assertEquals(9, engine.snake.first().x)
        assertEquals(10, engine.snake.first().y)
        assertEquals(GameState.PLAYING, engine.state)
    }

    @Test
    fun testWallWrapping_TopToBottom() {
        engine.startPlaying()
        engine.snake.clear()
        engine.snake.add(GridPoint(5, 0))
        engine.snake.add(GridPoint(5, 1))
        engine.snake.add(GridPoint(5, 2))
        engine.currentDirection = Direction.UP

        engine.tick()

        // Head should wrap from row 0 to row 19
        assertEquals(5, engine.snake.first().x)
        assertEquals(19, engine.snake.first().y)
        assertEquals(GameState.PLAYING, engine.state)
    }

    @Test
    fun testWallWrapping_BottomToTop() {
        engine.startPlaying()
        engine.snake.clear()
        engine.snake.add(GridPoint(5, 19))
        engine.snake.add(GridPoint(5, 18))
        engine.snake.add(GridPoint(5, 17))
        engine.currentDirection = Direction.DOWN

        engine.tick()

        // Head should wrap from row 19 to row 0
        assertEquals(5, engine.snake.first().x)
        assertEquals(0, engine.snake.first().y)
        assertEquals(GameState.PLAYING, engine.state)
    }

    @Test
    fun testOppositeDirectionIgnored() {
        engine.startPlaying()
        assertEquals(Direction.UP, engine.currentDirection)

        // Try to reverse into DOWN while moving UP
        engine.setDirection(Direction.DOWN)
        engine.tick()

        // Should still be moving UP
        assertEquals(Direction.UP, engine.currentDirection)
    }

    @Test
    fun testFoodEatingAndGrowth() {
        engine.startPlaying()
        val initialHead = engine.snake.first()
        val foodPos = GridPoint(initialHead.x, (initialHead.y - 1).mod(20))
        engine.food = foodPos

        val initialLength = engine.snake.size
        engine.currentDirection = Direction.UP
        engine.tick()

        // Snake ate food: grew by 1
        assertEquals(initialLength + 1, engine.snake.size)
        assertEquals(foodPos, engine.snake.first())
        assertNotEquals(foodPos, engine.food) // Food respawned elsewhere
    }

    @Test
    fun testSelfCollision_CausesDeath() {
        engine.startPlaying()
        // Snake moving LEFT with body forming a loop:
        // Head at (2, 2), body at (3, 2), (3, 3), (2, 3), (1, 3)
        engine.snake.clear()
        engine.snake.add(GridPoint(2, 2)) // Head
        engine.snake.add(GridPoint(3, 2))
        engine.snake.add(GridPoint(3, 3))
        engine.snake.add(GridPoint(2, 3))
        engine.snake.add(GridPoint(1, 3))
        engine.currentDirection = Direction.LEFT

        // Turn DOWN from LEFT into (2, 3) where body segment is!
        engine.setDirection(Direction.DOWN)
        engine.tick()

        assertEquals(GameState.DEAD, engine.state)
    }

    @Test
    fun testRestartAfterDeath() {
        engine.startPlaying()
        engine.state = GameState.DEAD

        assertEquals(GameState.DEAD, engine.state)

        engine.restart()

        assertEquals(GameState.PLAYING, engine.state)
        assertEquals(4, engine.snake.size)
        assertEquals(Direction.UP, engine.currentDirection)
    }

    @Test
    fun testDeadSnakeTouchDetection() {
        engine.startPlaying()
        val head = engine.snake.first()

        assertTrue(engine.isTouchOnDeadSnake(head.x, head.y))
        assertTrue(engine.isTouchNearDeadSnake(head.x.toFloat(), head.y.toFloat()))
        assertFalse(engine.isTouchOnDeadSnake(0, 0))
    }
}
