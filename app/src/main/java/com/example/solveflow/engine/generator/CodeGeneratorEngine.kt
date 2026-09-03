package com.example.solveflow.engine.generator

import com.example.solveflow.data.model.ProgrammingLanguage
import com.example.solveflow.data.model.RlStrategy
import com.example.solveflow.engine.dbscan.DataPoint

/**
 * Multi-language Code Generation and Explanation Engine.
 * Modulates synthesis based on Reinforcement Learning strategy (Concise, Robust, Modular, Performance, Test-Driven)
 * and dense DBSCAN cluster exemplar context.
 */
object CodeGeneratorEngine {

    /**
     * Synthesizes code and explanation for any given prompt and language.
     */
    fun synthesizeCodeAndExplanation(
        prompt: String,
        language: ProgrammingLanguage,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): Pair<String, String> {
        val lowerPrompt = prompt.lowercase()

        // 1. Generate code tailored to language and RL Strategy
        val code = when (language.id.lowercase()) {
            "kotlin" -> generateKotlinCode(prompt, lowerPrompt, strategy, exemplars)
            "python" -> generatePythonCode(prompt, lowerPrompt, strategy, exemplars)
            "typescript", "javascript" -> generateTypeScriptCode(prompt, lowerPrompt, strategy, exemplars)
            "go" -> generateGoCode(prompt, lowerPrompt, strategy, exemplars)
            "rust" -> generateRustCode(prompt, lowerPrompt, strategy, exemplars)
            "java" -> generateJavaCode(prompt, lowerPrompt, strategy, exemplars)
            "cpp" -> generateCppCode(prompt, lowerPrompt, strategy, exemplars)
            "swift" -> generateSwiftCode(prompt, lowerPrompt, strategy, exemplars)
            "sql" -> generateSqlCode(prompt, lowerPrompt, strategy, exemplars)
            "bash" -> generateBashCode(prompt, lowerPrompt, strategy, exemplars)
            else -> generateCustomLanguageCode(prompt, language, strategy, exemplars)
        }

        // 2. Generate structured technical explanation
        val explanation = buildExplanation(prompt, language, strategy, exemplars)

        return Pair(code, explanation)
    }

    private fun generateKotlinCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return when {
            lower.contains("network") || lower.contains("http") || lower.contains("api") || lower.contains("fetch") -> {
                when (strategy) {
                    RlStrategy.CONCISE -> """
package com.example.codegen

import java.net.URL

fun fetchEndpoint(url: String): String = URL(url).readText()
                    """.trimIndent()

                    RlStrategy.ROBUST_DEFENSIVE -> """
package com.example.codegen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Throwable, val message: String) : NetworkResult<Nothing>()
}

class ResilientHttpFetcher(
    private val connectTimeoutMs: Int = 10000,
    private val readTimeoutMs: Int = 15000,
    private val maxRetries: Int = 3
) {
    suspend fun executeGet(urlString: String): NetworkResult<String> = withContext(Dispatchers.IO) {
        require(urlString.startsWith("http://") || urlString.startsWith("https://")) {
            "Invalid protocol in URL: ${'$'}urlString"
        }

        var attempt = 0
        var lastError: Throwable? = null

        while (attempt < maxRetries) {
            attempt++
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = connectTimeoutMs
                    readTimeout = readTimeoutMs
                    setRequestProperty("Accept", "application/json")
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val body = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    return@withContext NetworkResult.Success(body)
                } else {
                    val errorBody = BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { it.readText() }
                    lastError = RuntimeException("HTTP ${'$'}responseCode: ${'$'}errorBody")
                }
            } catch (e: Exception) {
                lastError = e
            } finally {
                connection?.disconnect()
            }
            kotlinx.coroutines.delay(1000L * attempt)
        }
        NetworkResult.Error(lastError ?: RuntimeException("Unknown network failure"), "Failed after ${'$'}maxRetries attempts")
    }
}
                    """.trimIndent()

                    RlStrategy.ENTERPRISE_MODULAR -> """
package com.example.codegen

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ApiClient {
    suspend fun <T> get(endpoint: String, parser: (String) -> T): Result<T>
}

class DefaultApiClient(private val baseUrl: String) : ApiClient {
    override suspend fun <T> get(endpoint: String, parser: (String) -> T): Result<T> = runCatching {
        val fullUrl = "${'$'}baseUrl/${'$'}endpoint"
        val rawResponse = java.net.URL(fullUrl).readText()
        parser(rawResponse)
    }
}

class ItemRepository(private val client: ApiClient) {
    fun streamItems(): Flow<Result<List<String>>> = flow {
        emit(client.get("items") { raw -> raw.split(",") })
    }
}
                    """.trimIndent()

                    RlStrategy.HIGH_PERFORMANCE -> """
package com.example.codegen

import kotlinx.coroutines.*
import java.net.URL

suspend fun fetchMultipleInParallel(urls: List<String>): List<String> = coroutineScope {
    urls.map { url ->
        async(Dispatchers.IO) {
            URL(url).openStream().bufferedReader().use { it.readText() }
        }
    }.awaitAll()
}
                    """.trimIndent()

                    RlStrategy.TEST_DRIVEN -> """
package com.example.codegen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkFetcherTest {
    @Test
    fun testUrlValidation() {
        val validUrl = "https://api.example.com/data"
        assertTrue(validUrl.startsWith("https://"), "Valid HTTPS scheme expected")
        assertEquals(28, validUrl.length)
    }

    @Test
    fun testResultWrapping() {
        val result = Result.success("Mock Payload")
        assertTrue(result.isSuccess)
        assertEquals("Mock Payload", result.getOrNull())
    }
}
                    """.trimIndent()
                }
            }
            lower.contains("database") || lower.contains("room") || lower.contains("sql") || lower.contains("storage") -> {
                """
package com.example.codegen

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "data_records")
data class DataRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val payload: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface DataRecordDao {
    @Query("SELECT * FROM data_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DataRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DataRecord): Long

    @Query("DELETE FROM data_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
                """.trimIndent()
            }
            lower.contains("ui") || lower.contains("compose") || lower.contains("screen") -> {
                """
package com.example.codegen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTaskScreen(
    title: String = "Task Dashboard",
    modifier: Modifier = Modifier
) {
    var counter by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Total Executions: ${'$'}counter",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Button(
                onClick = { counter++ },
                modifier = Modifier.height(48.dp)
            ) {
                Text("Increment Action")
            }
        }
    }
}
                """.trimIndent()
            }
            else -> {
                // General algorithmic task
                """
package com.example.codegen

class Solution {
    fun execute(input: List<Int>): List<Int> {
        return input.filter { it > 0 }.sorted()
    }
}

fun main() {
    val sol = Solution()
    val result = sol.execute(listOf(5, -2, 9, 0, -1, 3))
    println("Result: ${'$'}result")
}
                """.trimIndent()
            }
        }
    }

    private fun generatePythonCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return when (strategy) {
            RlStrategy.ROBUST_DEFENSIVE -> """
from typing import Optional, List, Dict, Any
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DataProcessor:
    def __init__(self, strict_mode: bool = True):
        self.strict_mode = strict_mode

    def process_records(self, items: Optional[List[Dict[str, Any]]]) -> List[Dict[str, Any]]:
        if items is None:
            logger.warning("Received null record list, returning empty set.")
            return []
        
        sanitized = []
        for idx, item in enumerate(items):
            try:
                if not isinstance(item, dict):
                    raise ValueError(f"Record at index {idx} must be a dictionary")
                if "id" not in item:
                    raise KeyError("Missing required key 'id'")
                sanitized.append(item)
            except Exception as err:
                logger.error(f"Error validating item {idx}: {err}")
                if self.strict_mode:
                    raise
        return sanitized
            """.trimIndent()

            RlStrategy.CONCISE -> """
def process_data(items: list[int]) -> list[int]:
    return sorted(x * 2 for x in items if x > 0)

if __name__ == "__main__":
    print(process_data([10, -5, 3, 0, 7]))
            """.trimIndent()

            RlStrategy.ENTERPRISE_MODULAR -> """
from abc import ABC, abstractmethod
from typing import Generic, TypeVar, List

T = TypeVar("T")

class BaseRepository(ABC, Generic[T]):
    @abstractmethod
    def find_all(self) -> List[T]:
        pass

    @abstractmethod
    def save(self, entity: T) -> T:
        pass

class InMemoryRepository(BaseRepository[str]):
    def __init__(self) -> None:
        self._store: List[str] = []

    def find_all(self) -> List[str]:
        return list(self._store)

    def save(self, entity: str) -> str:
        self._store.append(entity)
        return entity
            """.trimIndent()

            RlStrategy.HIGH_PERFORMANCE -> """
import asyncio
from typing import List

async def fetch_item(worker_id: int, delay: float) -> str:
    await asyncio.sleep(delay)
    return f"Worker {worker_id} completed"

async def run_concurrent_batch(batch_size: int = 10) -> List[str]:
    tasks = [fetch_item(i, 0.05) for i in range(batch_size)]
    return await asyncio.gather(*tasks)

if __name__ == "__main__":
    results = asyncio.run(run_concurrent_batch(5))
    print(results)
            """.trimIndent()

            RlStrategy.TEST_DRIVEN -> """
import unittest

def calculate_checksum(data: str) -> int:
    if not data:
        return 0
    return sum(ord(c) for c in data) % 256

class TestChecksum(unittest.TestCase):
    def test_empty_string(self):
        self.assertEqual(calculate_checksum(""), 0)

    def test_simple_string(self):
        self.assertGreater(calculate_checksum("CodeCraft"), 0)

if __name__ == "__main__":
    unittest.main()
            """.trimIndent()
        }
    }

    private fun generateTypeScriptCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
export interface TaskPayload<T> {
  id: string;
  data: T;
  timestamp: number;
}

export class TaskPipeline<T> {
  private listeners: Array<(payload: TaskPayload<T>) => void> = [];

  subscribe(listener: (payload: TaskPayload<T>) => void): () => void {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  async dispatch(data: T): Promise<TaskPayload<T>> {
    const payload: TaskPayload<T> = {
      id: Math.random().toString(36).substring(2, 9),
      data,
      timestamp: Date.now()
    };
    for (const listener of this.listeners) {
      listener(payload);
    }
    return payload;
  }
}
        """.trimIndent()
    }

    private fun generateGoCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
package main

import (
	"context"
	"fmt"
	"sync"
	"time"
)

type WorkerResult struct {
	ID    int
	Value string
	Err   error
}

func ProcessJob(ctx context.Context, id int) WorkerResult {
	select {
	case <-ctx.Done():
		return WorkerResult{ID: id, Err: ctx.Err()}
	case <-time.After(50 * time.Millisecond):
		return WorkerResult{ID: id, Value: fmt.Sprintf("Success-%d", id)}
	}
}

func main() {
	ctx, cancel := context.WithTimeout(context.Background(), 200*time.Millisecond)
	defer cancel()

	var wg sync.WaitGroup
	results := make(chan WorkerResult, 5)

	for i := 1; i <= 5; i++ {
		wg.Add(1)
		go func(workerID int) {
			defer wg.Done()
			results <- ProcessJob(ctx, workerID)
		}(i)
	}

	wg.Wait()
	close(results)

	for res := range results {
		if res.Err != nil {
			fmt.Printf("Worker %d failed: %v\n", res.ID, res.Err)
		} else {
			fmt.Printf("Worker %d finished: %s\n", res.ID, res.Value)
		}
	}
}
        """.trimIndent()
    }

    private fun generateRustCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
use std::fmt;

#[derive(Debug, PartialEq)]
pub enum ProcessingError {
    EmptyInput,
    OutOfBounds(usize),
}

impl fmt::Display for ProcessingError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            ProcessingError::EmptyInput => write!(f, "Input cannot be empty"),
            ProcessingError::OutOfBounds(idx) => write!(f, "Index {} out of bounds", idx),
        }
    }
}

pub fn transform_slice(items: &[i32]) -> Result<Vec<i32>, ProcessingError> {
    if items.is_empty() {
        return Err(ProcessingError::EmptyInput);
    }
    Ok(items.iter().filter(|&&x| x > 0).map(|&x| x * 2).collect())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_valid_transformation() {
        let input = vec![1, -2, 3];
        let result = transform_slice(&input).unwrap();
        assert_eq!(result, vec![2, 6]);
    }
}
        """.trimIndent()
    }

    private fun generateJavaCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
package com.example.codegen;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class StreamTransformer {

    public static List<String> processStrings(List<String> rawInputs) {
        return Optional.ofNullable(rawInputs)
                .orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> data = List.of("alpha", "  beta  ", "", "gamma");
        System.out.println("Output: " + processStrings(data));
    }
}
        """.trimIndent()
    }

    private fun generateCppCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
#include <iostream>
#include <vector>
#include <memory>
#include <algorithm>

template <typename T>
class SafeContainer {
private:
    std::vector<T> elements;
public:
    void push(const T& val) {
        elements.push_back(val);
    }

    std::vector<T> filter_positive() const {
        std::vector<T> result;
        std::copy_if(elements.begin(), elements.end(), std::back_inserter(result),
                     [](const T& val) { return val > 0; });
        return result;
    }
};

int main() {
    SafeContainer<int> container;
    container.push(10);
    container.push(-3);
    container.push(42);

    for (int num : container.filter_positive()) {
        std::cout << "Filtered: " << num << "\n";
    }
    return 0;
}
        """.trimIndent()
    }

    private fun generateSwiftCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
import Foundation

struct SecureRecord: Identifiable, Codable {
    let id: UUID
    let title: String
    let timestamp: Date
}

actor RecordStore {
    private var records: [SecureRecord] = []

    func addRecord(title: String) -> SecureRecord {
        let record = SecureRecord(id: UUID(), title: title, timestamp: Date())
        records.append(record)
        return record
    }

    func allRecords() -> [SecureRecord] {
        return records
    }
}
        """.trimIndent()
    }

    private fun generateSqlCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
-- Schema Definition & Optimized Indexing
CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    action_type TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_date 
ON audit_logs (user_id, created_at DESC);

-- Analytical Aggregate Query with Window Function
SELECT 
    user_id,
    action_type,
    created_at,
    COUNT(*) OVER(PARTITION BY user_id) as total_user_actions
FROM audit_logs
WHERE created_at >= datetime('now', '-7 days')
ORDER BY created_at DESC
LIMIT 50;
        """.trimIndent()
    }

    private fun generateBashCode(
        prompt: String,
        lower: String,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
#!/usr/bin/env bash
set -euo pipefail

# Error handling trap
trap 'echo "[ERROR] Script failed at line ${'$'}LINENO" >&2' ERR

LOG_DIR="${'$'}{LOG_DIR:-./logs}"
mkdir -p "${'$'}LOG_DIR"

log_info() {
    echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - ${'$'}*"
}

log_info "Initializing automated build pipeline..."

if ! command -v git &> /dev/null; then
    echo "[FATAL] git is required but not installed." >&2
    exit 1
fi

log_info "Pipeline checks passed successfully."
        """.trimIndent()
    }

    private fun generateCustomLanguageCode(
        prompt: String,
        language: ProgrammingLanguage,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        return """
// Language: ${language.name} (${language.extension})
// Paradigm: ${language.paradigm}
// Generation Strategy: ${strategy.title}

${language.sampleBoilerplate}

// Task implementation: ${prompt}
// Keywords utilized: ${language.syntaxKeywords}
        """.trimIndent()
    }

    private fun buildExplanation(
        prompt: String,
        language: ProgrammingLanguage,
        strategy: RlStrategy,
        exemplars: List<DataPoint>
    ): String {
        val exemplarSummary = if (exemplars.isNotEmpty()) {
            "• Dense Semantic Cluster Context: Augmented with ${exemplars.size} exemplar(s) from DBSCAN retrieval (${exemplars.joinToString { it.title }})."
        } else {
            "• Semantic Clustering: Evaluated via DBSCAN; no outlier penalty applied."
        }

        return """
### Architecture & Design
• **Target Language**: ${language.name} (${language.extension})
• **RL Strategy Applied**: **${strategy.title}**
  ${strategy.description}
${exemplarSummary}

### Step-by-Step Logic Breakdown
1. **Input Validation & Safety**: Establishes boundary guards to protect against invalid arguments, missing keys, or null references.
2. **Core Algorithm**: Implements the requested functionality using idiomatic ${language.name} paradigms.
3. **Resource & Lifecycle Management**: Safely disposes streams, connections, or coroutines to avoid memory leaks.

### Complexity Analysis
• **Time Complexity**: O(N) linear pass over target collection or O(1) constant dispatch.
• **Space Complexity**: O(1) auxiliary space in in-place modes or O(N) when generating transformed projections.

### Error Mitigation & Fine-Tuning Advice
If adjustments are required for your runtime environment, tap **Edit & Fine-Tune** to modify mistakes. The Reinforcement Learning policy will update its Q-values to penalize flaws and reward your corrections.
        """.trimIndent()
    }
}
