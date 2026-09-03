package com.example.solveflow.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.solveflow.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FlowchartEntity::class,
        DiagnosticRunEntity::class,
        ProgrammingLanguage::class,
        CodeSnippet::class,
        KnowledgeItem::class,
        GenerationRecord::class,
        RlPolicyEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun flowchartDao(): FlowchartDao
    abstract fun languageDao(): LanguageDao
    abstract fun snippetDao(): SnippetDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun generationRecordDao(): GenerationRecordDao
    abstract fun rlPolicyDao(): RlPolicyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solveflow_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context))
            }
        }
    }
}

suspend fun populateInitialData(db: AppDatabase) {
    // 1. Built-in Programming Languages
    val defaultLanguages = listOf(
        ProgrammingLanguage(
            id = "kotlin",
            name = "Kotlin (Android)",
            extension = ".kt",
            paradigm = "Multi-paradigm, Functional, Object-Oriented, Coroutines",
            sampleBoilerplate = "fun main() {\n    println(\"Hello, Kotlin!\")\n}",
            syntaxKeywords = "val,var,fun,class,data class,suspend,flow,coroutine,sealed,interface,override,when,let,apply",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "python",
            name = "Python",
            extension = ".py",
            paradigm = "Dynamic, Interpreted, Object-Oriented, Functional",
            sampleBoilerplate = "def main():\n    print(\"Hello, Python!\")\n\nif __name__ == '__main__':\n    main()",
            syntaxKeywords = "def,class,import,from,async,await,try,except,finally,with,lambda,yield,return,if,elif,else",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "typescript",
            name = "TypeScript",
            extension = ".ts",
            paradigm = "Typed JavaScript, Object-Oriented, Asynchronous",
            sampleBoilerplate = "function greet(name: string): string {\n    return `Hello, \${name}!`;\n}\nconsole.log(greet(\"TypeScript\"));",
            syntaxKeywords = "interface,type,const,let,async,await,Promise,export,import,class,extends,implements,enum",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "javascript",
            name = "JavaScript",
            extension = ".js",
            paradigm = "Dynamic, Prototype-based, Event-driven",
            sampleBoilerplate = "function main() {\n    console.log(\"Hello, JavaScript!\");\n}\nmain();",
            syntaxKeywords = "function,const,let,var,async,await,Promise,export,import,try,catch,return,class",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "go",
            name = "Go (Golang)",
            extension = ".go",
            paradigm = "Concurrent, Compiled, Statically Typed",
            sampleBoilerplate = "package main\n\nimport \"fmt\"\n\nfunc main() {\n    fmt.Println(\"Hello, Go!\")\n}",
            syntaxKeywords = "package,import,func,go,chan,struct,interface,defer,select,var,const,return,range",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "rust",
            name = "Rust",
            extension = ".rs",
            paradigm = "Memory-Safe, Zero-Cost Abstractions, Systems",
            sampleBoilerplate = "fn main() {\n    println!(\"Hello, Rust!\");\n}",
            syntaxKeywords = "fn,let,mut,struct,enum,impl,trait,match,Result,Option,pub,use,async,await,unsafe",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "java",
            name = "Java",
            extension = ".java",
            paradigm = "Class-based, Object-Oriented, Platform-Independent",
            sampleBoilerplate = "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Java!\");\n    }\n}",
            syntaxKeywords = "public,private,protected,class,interface,extends,implements,void,static,final,try,catch,throw",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "cpp",
            name = "C++",
            extension = ".cpp",
            paradigm = "Performance-oriented, Object-Oriented, Generic",
            sampleBoilerplate = "#include <iostream>\n\nint main() {\n    std::cout << \"Hello, C++!\" << std::endl;\n    return 0;\n}",
            syntaxKeywords = "int,void,auto,template,typename,class,struct,namespace,std,vector,unique_ptr,shared_ptr",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "swift",
            name = "Swift",
            extension = ".swift",
            paradigm = "Protocol-Oriented, Safe, Fast, Expressive",
            sampleBoilerplate = "import Foundation\n\nprint(\"Hello, Swift!\")",
            syntaxKeywords = "func,var,let,struct,class,enum,protocol,extension,guard,defer,async,await,throws",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "sql",
            name = "SQL",
            extension = ".sql",
            paradigm = "Declarative, Relational Database Querying",
            sampleBoilerplate = "SELECT id, name, created_at FROM users WHERE is_active = 1 ORDER BY created_at DESC;",
            syntaxKeywords = "SELECT,FROM,WHERE,JOIN,INNER JOIN,LEFT JOIN,GROUP BY,ORDER BY,INSERT,UPDATE,DELETE,CREATE TABLE",
            isCustom = false
        ),
        ProgrammingLanguage(
            id = "bash",
            name = "Bash / Shell",
            extension = ".sh",
            paradigm = "Scripting, Command Automation, Pipeline",
            sampleBoilerplate = "#!/usr/bin/env bash\nset -euo pipefail\n\necho \"Executing automation script...\"",
            syntaxKeywords = "set,if,then,else,fi,for,in,do,done,case,esac,function,export,trap,local",
            isCustom = false
        )
    )
    db.languageDao().insertAll(defaultLanguages)

    // 2. Initial Starter Code Snippets across domains (for DBSCAN clustering exemplars)
    val defaultSnippets = listOf(
        CodeSnippet(
            title = "Coroutine Network Fetcher with Retry & Result",
            languageId = "kotlin",
            code = """
suspend fun <T> safeApiCallWithRetry(
    maxAttempts: Int = 3,
    delayMillis: Long = 1000L,
    block: suspend () -> T
): Result<T> {
    var currentAttempt = 0
    while (true) {
        try {
            currentAttempt++
            return Result.success(block())
        } catch (e: Exception) {
            if (currentAttempt >= maxAttempts) {
                return Result.failure(e)
            }
            kotlinx.coroutines.delay(delayMillis * currentAttempt)
        }
    }
}
            """.trimIndent(),
            tags = "network,coroutine,retry,error-handling,async",
            category = "Network",
            explanation = "Defensive exponential backoff network wrapper returning Kotlin Result."
        ),
        CodeSnippet(
            title = "Room Database DAO with Reactive StateFlow",
            languageId = "kotlin",
            code = """
@Dao
interface UserRecordDao {
    @Query("SELECT * FROM user_records ORDER BY timestamp DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<UserRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: UserRecord): Long

    @Query("DELETE FROM user_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Long)
}
            """.trimIndent(),
            tags = "database,room,dao,sql,persistence,flow",
            category = "Database",
            explanation = "Reactive SQLite Room DAO providing non-blocking Flow queries."
        ),
        CodeSnippet(
            title = "FastAPI Asynchronous Endpoint with Pydantic Validation",
            languageId = "python",
            code = """
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel, Field

app = FastAPI(title="Data Engine API")

class PayloadSchema(BaseModel):
    query: str = Field(..., min_length=3, max_length=200)
    limit: int = Field(default=10, ge=1, le=100)

@app.post("/api/v1/search", status_code=status.HTTP_200_OK)
async def handle_search(payload: PayloadSchema):
    try:
        results = await perform_dense_retrieval(payload.query, payload.limit)
        return {"status": "success", "count": len(results), "data": results}
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))
            """.trimIndent(),
            tags = "api,python,fastapi,async,pydantic,http",
            category = "Network",
            explanation = "Asynchronous FastAPI handler with robust Pydantic schema validation."
        ),
        CodeSnippet(
            title = "Rust Thread-Safe Cache with RwLock & Arc",
            languageId = "rust",
            code = """
use std::collections::HashMap;
use std::sync::{Arc, RwLock};

#[derive(Clone)]
pub struct ThreadSafeStore<K, V> {
    inner: Arc<RwLock<HashMap<K, V>>>,
}

impl<K: std::hash::Hash + Eq + Clone, V: Clone> ThreadSafeStore<K, V> {
    pub fn new() -> Self {
        Self { inner: Arc::new(RwLock::new(HashMap::new())) }
    }

    pub fn get(&self, key: &K) -> Option<V> {
        let guard = self.inner.read().ok()?;
        guard.get(key).cloned()
    }

    pub fn insert(&self, key: K, val: V) {
        if let Ok(mut guard) = self.inner.write() {
            guard.insert(key, val);
        }
    }
}
            """.trimIndent(),
            tags = "rust,concurrency,arc,rwlock,cache,memory-safe",
            category = "Concurrency",
            explanation = "Zero-cost thread-safe concurrent in-memory key-value store using Rust Arc and RwLock."
        ),
        CodeSnippet(
            title = "TypeScript Resilient Fetch with Timeout & AbortController",
            languageId = "typescript",
            code = """
export async function fetchWithTimeout<T>(url: string, timeoutMs = 8000): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
        const res = await fetch(url, { signal: controller.signal });
        if (!res.ok) {
            throw new Error("HTTP error " + res.status + ": " + res.statusText);
        }
        return await res.json() as T;
    } finally {
        clearTimeout(timer);
    }
}
            """.trimIndent(),
            tags = "typescript,fetch,timeout,abort,http,async",
            category = "Network",
            explanation = "Production-grade HTTP client with automated signal timeout and memory leak prevention."
        ),
        CodeSnippet(
            title = "Go Worker Pool with Channels & Context Cancellation",
            languageId = "go",
            code = """
package worker

import (
	"context"
	"sync"
)

type Job func(ctx context.Context) error

func RunWorkerPool(ctx context.Context, workers int, jobs <-chan Job) {
	var wg sync.WaitGroup
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for {
				select {
				case <-ctx.Done():
					return
				case job, ok := <-jobs:
					if !ok {
						return
					}
					_ = job(ctx)
				}
			}
		}()
	}
	wg.Wait()
}
            """.trimIndent(),
            tags = "go,concurrency,channel,goroutine,worker-pool,sync",
            category = "Concurrency",
            explanation = "Idiomatic Go bounded worker pool with graceful context cancellation."
        )
    )
    db.snippetDao().insertAll(defaultSnippets)

    // 3. Initial Retrievable Knowledge Items (Architectures, error prevention, APIs)
    val defaultKnowledge = listOf(
        KnowledgeItem(
            title = "Clean Architecture & Single Responsibility Pattern",
            topic = "Architecture",
            languageScope = "All",
            content = "Keep UI decoupled from business logic and data access. UI observes ViewModel StateFlows, ViewModel invokes domain use cases or repositories, Repository coordinates remote and local sources. Always return Result or sealed UI state for predictable error propagation.",
            tags = "clean-architecture,repository,mvvm,error-handling"
        ),
        KnowledgeItem(
            title = "Defensive Programming & Null Safety Guardrails",
            topic = "Security & Error Prevention",
            languageScope = "All",
            content = "Validate arguments at function boundaries (require / check / assert). Never swallow exceptions without logging. Avoid force-unwrapping nullables (e.g. avoid !! in Kotlin, unwrap in Rust). Provide sensible defaults or structured Result types.",
            tags = "defensive,null-safety,validation,exceptions"
        ),
        KnowledgeItem(
            title = "Jetpack Compose Edge-to-Edge & State Hoisting",
            topic = "UI & Rendering",
            languageScope = "kotlin",
            content = "Use enableEdgeToEdge() with Scaffold contentWindowInsets or WindowInsets.safeDrawing. Hoist state to ViewModels using MutableStateFlow and collectAsStateWithLifecycle(). Ensure touch targets are at least 48dp.",
            tags = "compose,android,edge-to-edge,ui,state"
        ),
        KnowledgeItem(
            title = "GitHub Actions APK Build Pipeline Optimization",
            topic = "CI/CD & DevOps",
            languageScope = "kotlin",
            content = "For building Android APKs on GitHub Actions, use ubuntu-latest, setup-java with distribution 'temurin' and java-version '21'. Use Gradle assembleDebug, upload artifact with actions/upload-artifact@v4, and set gradlew execute permissions (+x).",
            tags = "github-actions,apk,ci-cd,gradle,android"
        )
    )
    db.knowledgeDao().insertAll(defaultKnowledge)
}
