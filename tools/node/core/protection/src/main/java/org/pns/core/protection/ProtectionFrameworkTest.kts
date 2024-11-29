package org.pns.core.protection

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class ProtectionFrameworkTest {
    private lateinit var framework: ProtectionFramework

    @Before
    fun setup() {
        framework = ProtectionFramework(monitoringInterval = 1L)
    }

    @After
    fun cleanup() {
        framework.close()
    }

    @Test
    fun testNodeDistributionValidation() = runBlocking {
        val validDistribution = mapOf(
            "node1" to 0.03f,
            "node2" to 0.04f
        )
        assertTrue("Valid distribution should be accepted",
            framework.validateNodeDistribution(validDistribution))

        val invalidDistribution = mapOf(
            "node1" to 0.06f  // Exceeds 5% limit
        )
        assertFalse("Invalid distribution should be rejected",
            framework.validateNodeDistribution(invalidDistribution))
    }

    @Test
    fun testEnvironmentalImpactValidation() = runBlocking {
        val validMetrics = mapOf(
            "restoration" to 0.8f,
            "biodiversity" to 0.7f
        )
        assertTrue("Valid environmental metrics should be accepted",
            framework.validateEnvironmentalImpact(validMetrics, 0.02f, 1.0f))
    }

    @Test
    fun testResourceDistributionValidation() = runBlocking {
        val validDistribution = mapOf(
            "region1" to 0.02f,
            "region2" to 0.02f
        )
        assertTrue("Valid resource distribution should be accepted",
            framework.validateResourceDistribution(validDistribution))
    }
}