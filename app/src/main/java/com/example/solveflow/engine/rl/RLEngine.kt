package com.example.solveflow.engine.rl

import com.example.solveflow.data.model.RlPolicyEntry
import com.example.solveflow.data.model.RlStrategy
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Reinforcement Learning stats and policy recommendations.
 */
data class RlPolicySnapshot(
    val stateKey: String,
    val recommendedStrategy: RlStrategy,
    val explorationRate: Double,
    val strategyQValues: Map<RlStrategy, Double>,
    val episodeCount: Int,
    val averageReward: Double
)

/**
 * Q-learning Reinforcement Learning Engine for autonomous code synthesis optimization.
 * Updates state-action policy based on user thumbs up/down, error mitigation edits,
 * and fine-tuning corrections.
 */
object RLEngine {

    const val DEFAULT_LEARNING_RATE = 0.25
    const val DEFAULT_EXPLORATION_RATE = 0.15 // Epsilon for epsilon-greedy policy
    const val DISCOUNT_FACTOR = 0.90

    /**
     * Determines the state key from language and user prompt keywords.
     */
    fun deriveStateKey(languageId: String, prompt: String): String {
        val lowerPrompt = prompt.lowercase()
        val category = when {
            lowerPrompt.contains("network") || lowerPrompt.contains("http") || lowerPrompt.contains("api") || lowerPrompt.contains("fetch") -> "network"
            lowerPrompt.contains("database") || lowerPrompt.contains("sql") || lowerPrompt.contains("room") || lowerPrompt.contains("dao") -> "database"
            lowerPrompt.contains("async") || lowerPrompt.contains("thread") || lowerPrompt.contains("coroutine") || lowerPrompt.contains("channel") -> "concurrency"
            lowerPrompt.contains("ui") || lowerPrompt.contains("compose") || lowerPrompt.contains("screen") || lowerPrompt.contains("view") -> "ui"
            lowerPrompt.contains("sort") || lowerPrompt.contains("search") || lowerPrompt.contains("algorithm") || lowerPrompt.contains("tree") -> "algorithm"
            lowerPrompt.contains("test") || lowerPrompt.contains("assert") || lowerPrompt.contains("mock") -> "testing"
            else -> "general"
        }
        return "${languageId.lowercase()}:$category"
    }

    /**
     * Selects an action (generation strategy) using epsilon-greedy policy.
     */
    fun selectStrategy(
        stateKey: String,
        policyEntries: List<RlPolicyEntry>,
        epsilon: Double = DEFAULT_EXPLORATION_RATE
    ): Pair<RlStrategy, Boolean> {
        val allStrategies = RlStrategy.values()

        // Exploration: choose random action with probability epsilon
        if (Random.nextDouble() < epsilon) {
            val randomChoice = allStrategies.random()
            return Pair(randomChoice, true) // isExploration = true
        }

        // Exploitation: choose action with highest Q-value for this state
        val stateEntries = policyEntries.filter { it.stateKey == stateKey }
        val qMap = stateEntries.associate { it.strategyName to it.qValue }

        val bestStrategy = allStrategies.maxByOrNull { strategy ->
            qMap[strategy.name] ?: 0.0
        } ?: RlStrategy.CONCISE

        return Pair(bestStrategy, false) // isExploration = false
    }

    /**
     * Calculates Levenshtein edit distance between original generated code and edited code.
     */
    fun computeLevenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Computes similarity ratio [0.0 .. 1.0] based on edit distance.
     */
    fun computeEditSimilarity(original: String, edited: String): Double {
        val maxLength = max(original.length, edited.length)
        if (maxLength == 0) return 1.0
        val dist = computeLevenshteinDistance(original, edited)
        return (1.0 - (dist.toDouble() / maxLength)).coerceIn(0.0, 1.0)
    }

    /**
     * Calculates the Reinforcement Learning reward for error mitigation and fine-tuning.
     * @param originalCode The generated code
     * @param editedCode The user-corrected code
     * @param mitigationType Categorized error fixed (Syntax, Logic, Import, Performance)
     */
    fun calculateFineTuningReward(
        originalCode: String,
        editedCode: String,
        mitigationType: String?
    ): Double {
        val similarity = computeEditSimilarity(originalCode, editedCode)
        return when {
            // Minor bug fix / error mitigation: rewarding the fine-tuning
            similarity >= 0.75 -> 0.60
            // Moderate adjustments: neutral reward
            similarity >= 0.45 -> 0.10
            // Complete rewrite because generation was unsatisfactory
            else -> -0.90
        }
    }

    /**
     * Updates policy entry via Q-learning Bellman equation:
     * Q(s, a) <- Q(s, a) + alpha * (Reward - Q(s, a))
     */
    fun updateQValue(
        currentEntry: RlPolicyEntry?,
        stateKey: String,
        strategy: RlStrategy,
        reward: Double,
        learningRate: Double = DEFAULT_LEARNING_RATE
    ): RlPolicyEntry {
        val oldQ = currentEntry?.qValue ?: 0.0
        val oldUpdates = currentEntry?.updateCount ?: 0
        val oldTotalReward = currentEntry?.totalReward ?: 0.0

        val newQ = oldQ + learningRate * (reward - oldQ)

        return RlPolicyEntry(
            stateKey = stateKey,
            strategyName = strategy.name,
            qValue = newQ,
            updateCount = oldUpdates + 1,
            totalReward = oldTotalReward + reward,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Generates an analytical snapshot of RL policy for a given state.
     */
    fun getPolicySnapshot(stateKey: String, entries: List<RlPolicyEntry>): RlPolicySnapshot {
        val stateEntries = entries.filter { it.stateKey == stateKey }
        val qMap = RlStrategy.values().associateWith { strat ->
            stateEntries.find { it.strategyName == strat.name }?.qValue ?: 0.0
        }

        val bestStrat = qMap.maxByOrNull { it.value }?.key ?: RlStrategy.CONCISE
        val totalUpdates = stateEntries.sumOf { it.updateCount }
        val avgReward = if (totalUpdates > 0) stateEntries.sumOf { it.totalReward } / totalUpdates else 0.0

        return RlPolicySnapshot(
            stateKey = stateKey,
            recommendedStrategy = bestStrat,
            explorationRate = DEFAULT_EXPLORATION_RATE,
            strategyQValues = qMap,
            episodeCount = totalUpdates,
            averageReward = avgReward
        )
    }
}
