package com.example

import com.example.data.model.Addendum
import com.example.data.model.MemoryEntity
import com.example.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryProcessingTest {

    @Test
    fun testTimeFormatting12Hour() {
        val formatted = TimeUtils.formatTime12Hour(1700000000000L)
        // Verify 12-hour format without leading zero
        assertTrue(formatted.contains("AM") || formatted.contains("PM"))
        assertTrue(!formatted.startsWith("0"))
    }

    @Test
    fun testWordCountAndTags() {
        val memory = MemoryEntity(
            id = 1L,
            text = "This is a test memory captured from Slack chat discussing product roadmap launch.",
            appName = "Slack",
            packageName = "com.Slack",
            appCategory = "Chats & Messages",
            title = "Roadmap Discussion",
            tags = listOf("roadmap", "slack", "launch"),
            wordCount = 12,
            timestamp = System.currentTimeMillis()
        )

        assertEquals(12, memory.wordCount)
        assertEquals(3, memory.tags.size)
        assertEquals("Slack", memory.appName)
    }

    @Test
    fun testAddendumsSupport() {
        val addendum = Addendum(
            id = "addendum_123",
            content = "Follow-up note: Launch postponed by 2 days.",
            timestamp = System.currentTimeMillis()
        )

        val memory = MemoryEntity(
            id = 2L,
            text = "Initial meeting memo.",
            appName = "Notion",
            packageName = "notion.id",
            appCategory = "Productivity & Work",
            title = "Meeting Memo",
            addendums = listOf(addendum)
        )

        assertEquals(1, memory.addendums.size)
        assertEquals("Follow-up note: Launch postponed by 2 days.", memory.addendums[0].content)
    }
}
