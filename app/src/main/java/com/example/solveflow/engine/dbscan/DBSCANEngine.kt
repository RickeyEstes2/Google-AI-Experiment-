package com.example.solveflow.engine.dbscan

import kotlin.math.sqrt

/**
 * Data point representing a code snippet or knowledge item in vector space.
 */
data class DataPoint(
    val id: Long,
    val title: String,
    val textContent: String,
    val languageId: String,
    val category: String,
    val isSnippet: Boolean,
    val vector: Map<String, Double>
)

/**
 * Result of DBSCAN clustering computation.
 */
data class DBSCANResult(
    val clusters: Map<Int, List<DataPoint>>,
    val noisePoints: List<DataPoint>,
    val corePointIds: Set<Long>,
    val borderPointIds: Set<Long>,
    val clusterKeywords: Map<Int, List<String>>,
    val epsilon: Double,
    val minPts: Int
)

/**
 * Pure Kotlin Density-Based Spatial Clustering of Applications with Noise (DBSCAN) Engine.
 * Vectorizes text & code using TF-IDF / term-frequency representations and groups
 * code patterns into dense clusters while filtering noise/outliers.
 */
object DBSCANEngine {

    // Domain vocabulary for programming and software engineering features
    val VOCABULARY = listOf(
        "network", "http", "api", "fetch", "url", "rest", "client", "retry", "endpoint", "request",
        "response", "json", "parse", "serialize", "pydantic", "gson", "serialization",
        "database", "db", "sql", "room", "sqlite", "query", "insert", "select", "table", "dao",
        "repository", "entity", "upsert", "cache", "store",
        "concurrency", "async", "await", "coroutine", "thread", "channel", "mutex", "lock",
        "rwlock", "arc", "sync", "flow", "stateflow", "worker", "pool", "job",
        "ui", "compose", "view", "screen", "button", "dialog", "scaffold", "padding", "card",
        "text", "layout", "modifier", "state", "hoist", "theme", "color", "animation",
        "algorithm", "sort", "search", "binary", "tree", "graph", "dp", "recursion", "array",
        "list", "map", "hash", "set", "queue", "stack", "complexity",
        "security", "auth", "token", "jwt", "crypto", "hash", "validate", "guard", "permission",
        "safe", "nullability", "exception", "try", "catch", "error", "timeout", "abort",
        "file", "io", "export", "read", "write", "stream", "pdf", "html", "zip", "directory",
        "github", "actions", "ci", "cd", "workflow", "apk", "gradle", "build", "pipeline",
        "test", "assert", "mock", "unit", "verify", "suite", "rule", "benchmark"
    )

    /**
     * Vectorizes text content by counting term frequencies across domain vocabulary.
     */
    fun vectorize(text: String): Map<String, Double> {
        val cleanText = text.lowercase().replace(Regex("[^a-z0-9_\\-\\s]"), " ")
        val tokens = cleanText.split(Regex("\\s+")).filter { it.isNotBlank() }
        val totalTokens = tokens.size.coerceAtLeast(1).toDouble()

        val frequencies = mutableMapOf<String, Double>()
        for (vocabWord in VOCABULARY) {
            val count = tokens.count { it == vocabWord || it.contains(vocabWord) }
            if (count > 0) {
                // Term Frequency (TF) normalized
                frequencies[vocabWord] = count.toDouble() / totalTokens
            }
        }
        return frequencies
    }

    /**
     * Computes Cosine Distance between two sparse term vectors: 1.0 - CosineSimilarity.
     * Distance is bounded between 0.0 (identical) and 1.0 (orthogonal).
     */
    fun cosineDistance(v1: Map<String, Double>, v2: Map<String, Double>): Double {
        if (v1.isEmpty() || v2.isEmpty()) return 1.0

        var dotProduct = 0.0
        for ((word, val1) in v1) {
            val val2 = v2[word] ?: 0.0
            dotProduct += val1 * val2
        }

        var norm1Sq = 0.0
        for (value in v1.values) {
            norm1Sq += value * value
        }

        var norm2Sq = 0.0
        for (value in v2.values) {
            norm2Sq += value * value
        }

        val norm1 = sqrt(norm1Sq)
        val norm2 = sqrt(norm2Sq)

        if (norm1 == 0.0 || norm2 == 0.0) return 1.0

        val similarity = (dotProduct / (norm1 * norm2)).coerceIn(0.0, 1.0)
        return 1.0 - similarity
    }

    /**
     * Executes DBSCAN clustering on given list of DataPoints.
     * @param points Input data points
     * @param eps Epsilon distance threshold (default 0.45)
     * @param minPts Minimum points to form a dense core region (default 2)
     */
    fun cluster(points: List<DataPoint>, eps: Double = 0.45, minPts: Int = 2): DBSCANResult {
        if (points.isEmpty()) {
            return DBSCANResult(
                clusters = emptyMap(),
                noisePoints = emptyList(),
                corePointIds = emptySet(),
                borderPointIds = emptySet(),
                clusterKeywords = emptyMap(),
                epsilon = eps,
                minPts = minPts
            )
        }

        val visited = mutableSetOf<Long>()
        val corePointIds = mutableSetOf<Long>()
        val clusterAssignments = mutableMapOf<Long, Int>()
        val noisePointIds = mutableSetOf<Long>()

        var clusterCounter = 0

        // Find neighbors function
        fun getNeighbors(target: DataPoint): List<DataPoint> {
            return points.filter { other ->
                cosineDistance(target.vector, other.vector) <= eps
            }
        }

        for (point in points) {
            if (visited.contains(point.id)) continue
            visited.add(point.id)

            val neighbors = getNeighbors(point)

            if (neighbors.size < minPts) {
                noisePointIds.add(point.id)
            } else {
                // Point is a Core Point: create new cluster
                val currentClusterId = clusterCounter++
                corePointIds.add(point.id)
                clusterAssignments[point.id] = currentClusterId
                noisePointIds.remove(point.id)

                val seedQueue = ArrayDeque<DataPoint>()
                for (neighbor in neighbors) {
                    if (neighbor.id != point.id) {
                        seedQueue.add(neighbor)
                    }
                }

                while (seedQueue.isNotEmpty()) {
                    val current = seedQueue.removeFirst()

                    if (noisePointIds.contains(current.id)) {
                        // Was noise, now border point
                        clusterAssignments[current.id] = currentClusterId
                        noisePointIds.remove(current.id)
                    }

                    if (!visited.contains(current.id)) {
                        visited.add(current.id)
                        clusterAssignments[current.id] = currentClusterId

                        val currentNeighbors = getNeighbors(current)
                        if (currentNeighbors.size >= minPts) {
                            corePointIds.add(current.id)
                            for (n in currentNeighbors) {
                                if (!visited.contains(n.id) && !seedQueue.any { it.id == n.id }) {
                                    seedQueue.add(n)
                                }
                            }
                        }
                    } else if (!clusterAssignments.containsKey(current.id)) {
                        clusterAssignments[current.id] = currentClusterId
                    }
                }
            }
        }

        // Group into clusters map
        val clusterMap = mutableMapOf<Int, MutableList<DataPoint>>()
        val noiseList = mutableListOf<DataPoint>()
        val borderPointIds = mutableSetOf<Long>()

        for (p in points) {
            val cId = clusterAssignments[p.id]
            if (cId != null) {
                clusterMap.getOrPut(cId) { mutableListOf() }.add(p)
                if (!corePointIds.contains(p.id)) {
                    borderPointIds.add(p.id)
                }
            } else {
                noiseList.add(p)
            }
        }

        // Extract defining keywords for each cluster
        val clusterKeywords = mutableMapOf<Int, List<String>>()
        for ((cId, clusterPoints) in clusterMap) {
            val aggregatedTerms = mutableMapOf<String, Double>()
            for (cp in clusterPoints) {
                for ((word, score) in cp.vector) {
                    aggregatedTerms[word] = (aggregatedTerms[word] ?: 0.0) + score
                }
            }
            val topKeywords = aggregatedTerms.entries
                .sortedByDescending { it.value }
                .take(4)
                .map { it.key }
            clusterKeywords[cId] = if (topKeywords.isNotEmpty()) topKeywords else listOf("General")
        }

        return DBSCANResult(
            clusters = clusterMap,
            noisePoints = noiseList,
            corePointIds = corePointIds,
            borderPointIds = borderPointIds,
            clusterKeywords = clusterKeywords,
            epsilon = eps,
            minPts = minPts
        )
    }

    /**
     * Given a user prompt, retrieves the best matching cluster and its exemplars.
     */
    fun findRelevantExemplars(
        prompt: String,
        dbscanResult: DBSCANResult,
        maxExemplars: Int = 3
    ): Pair<Int?, List<DataPoint>> {
        val promptVector = vectorize(prompt)
        if (promptVector.isEmpty() || dbscanResult.clusters.isEmpty()) {
            return Pair(null, emptyList())
        }

        var bestClusterId: Int? = null
        var minClusterDistance = Double.MAX_VALUE
        var bestExemplars = emptyList<DataPoint>()

        for ((clusterId, points) in dbscanResult.clusters) {
            // Find minimum distance from prompt to any point in this cluster
            val scored = points.map { point ->
                point to cosineDistance(promptVector, point.vector)
            }.sortedBy { it.second }

            val closestDistanceInCluster = scored.firstOrNull()?.second ?: 1.0

            if (closestDistanceInCluster < minClusterDistance && closestDistanceInCluster <= 0.85) {
                minClusterDistance = closestDistanceInCluster
                bestClusterId = clusterId
                bestExemplars = scored.take(maxExemplars).map { it.first }
            }
        }

        return Pair(bestClusterId, bestExemplars)
    }
}
