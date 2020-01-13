package com.juliusbaer.premarket

import com.juliusbaer.premarket.helpers.HardwareIdProvider
import org.junit.Assert
import org.junit.Test

class HardwareIdProviderTest {
    @Test
    fun uuid_isCorrect() {
        //length issues
        Assert.assertFalse(HardwareIdProvider.isValidUUID(""))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-826c-4a8d-9645-5dc53c64721"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-826c-4a8d-9645-5dc53c64721aa"))

        //format issues
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0b%72c-826c-4a8d-9645-5dc53c64721a"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c%826c-4a8d-9645-5dc53c64721a"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-82%c-4a8d-9645-5dc53c64721a"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-826c-4a%d-9645-5dc53c64721a"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-82%c-4a8d-96%5-5dc53c64721a"))
        Assert.assertFalse(HardwareIdProvider.isValidUUID("ee0be72c-82%c-4a8d-9645-5dc53%64721a"))

        Assert.assertTrue(HardwareIdProvider.isValidUUID("ee0be72c-826c-4a8d-9645-5dc53c64721a"))
        Assert.assertTrue(HardwareIdProvider.isValidUUID("EE0BE72C-826C-4A8D-9645-5DC53C64721A"))
    }
}
