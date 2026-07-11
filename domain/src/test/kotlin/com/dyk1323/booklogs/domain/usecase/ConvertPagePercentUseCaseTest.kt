package com.dyk1323.booklogs.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertPagePercentUseCaseTest {

    @Test
    fun `percentToPage rounds to the nearest page`() {
        // 62% of 320 = 198.4 -> rounds to 198
        assertEquals(198, ConvertPagePercentUseCase.percentToPage(62, 320))
    }

    @Test
    fun `pageToPercent rounds to the nearest whole percent`() {
        // 198 / 320 = 61.875% -> rounds to 62
        assertEquals(62, ConvertPagePercentUseCase.pageToPercent(198, 320))
    }

    @Test
    fun `0 percent and 100 percent are exact boundaries`() {
        assertEquals(0, ConvertPagePercentUseCase.percentToPage(0, 320))
        assertEquals(320, ConvertPagePercentUseCase.percentToPage(100, 320))
        assertEquals(0, ConvertPagePercentUseCase.pageToPercent(0, 320))
        assertEquals(100, ConvertPagePercentUseCase.pageToPercent(320, 320))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `percentToPage rejects a nonpositive totalPages`() {
        ConvertPagePercentUseCase.percentToPage(50, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `percentToPage rejects a percent outside 0 to 100`() {
        ConvertPagePercentUseCase.percentToPage(150, 320)
    }
}
