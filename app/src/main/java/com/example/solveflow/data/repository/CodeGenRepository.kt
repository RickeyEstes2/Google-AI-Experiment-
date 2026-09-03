package com.example.solveflow.data.repository

import com.example.solveflow.data.db.*
import com.example.solveflow.data.model.*
import com.example.solveflow.engine.dbscan.DBSCANEngine
import com.example.solveflow.engine.dbscan.DBSCANResult
import com.example.solveflow.engine.dbscan.DataPoint
import com.example.solveflow.engine.generator.CodeGeneratorEngine
import com.example.solveflow.engine.generator.GeminiCodeService
import com.example.solveflow.engine.rl.RLEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CodeGenRepository(
    private val languageDao: LanguageDao,
    private val snippetDao: SnippetDao,
    private val knowledgeDao: KnowledgeDao,
    private val recordDao: GenerationRecordDao,
    private val rlPolicyDao: RlPolicyDao
) {

    // Flow streams for UI observation
    val languages: Flow<List<ProgrammingLanguage>> = languageDao.getAllLanguages()
    val snippets: Flow<List<CodeSnippet>> = snippetDao.getAllSnippets()
    val knowledgeItems: Flow<List<KnowledgeItem>> = knowledgeDao.getAllKnowledge()
    val records: Flow<List<GenerationRecord>> = recordDao.getAllRecords()
    val policyEntries: Flow<List<RlPolicyEntry>> = rlPolicyDao.getAllPolicyEntries()

    // 1. Language Management
    suspend fun addLanguage(language: ProgrammingLanguage) {
        languageDao.insertLanguage(language)
    }

    suspend fun deleteLanguage(language: ProgrammingLanguage) {
        languageDao.deleteLanguage(language)
    }

    // 2. Snippet Management
    suspend fun addSnippet(snippet: CodeSnippet): Long {
        return snippetDao.insertSnippet(snippet)
    }

    suspend fun deleteSnippet(snippet: CodeSnippet) {
        snippetDao.deleteSnippet(snippet)
    }

    // 3. Knowledge Base Management
    suspend fun addKnowledge(item: KnowledgeItem): Long {
        return knowledgeDao.insertKnowledge(item)
    }

    suspend fun deleteKnowledge(item: KnowledgeItem) {
        knowledgeDao.deleteKnowledge(item)
    }

    // 4. DBSCAN Clustering Execution
    suspend fun runDBSCAN(eps: Double = 0.45, minPts: Int = 2): DBSCANResult {
        val snippetList = snippetDao.getAllSnippetsList()
        val knowledgeList = knowledgeDao.getAllKnowledgeList()

        val points = mutableListOf<DataPoint>()

        for (snip in snippetList) {
            val textToVectorize = "${snip.title} ${snip.tags} ${snip.category} ${snip.explanation} ${snip.code.take(300)}"
            points.add(
                DataPoint(
                    id = snip.id,
                    title = snip.title,
                    textContent = snip.code,
                    languageId = snip.languageId,
                    category = snip.category,
                    isSnippet = true,
                    vector = DBSCANEngine.vectorize(textToVectorize)
                )
            )
        }

        for (k in knowledgeList) {
            val textToVectorize = "${k.title} ${k.tags} ${k.topic} ${k.content}"
            points.add(
                DataPoint(
                    id = 100000L + k.id, // Offset ID to avoid collisions
                    title = k.title,
                    textContent = k.content,
                    languageId = k.languageScope,
                    category = k.topic,
                    isSnippet = false,
                    vector = DBSCANEngine.vectorize(textToVectorize)
                )
            )
        }

        return DBSCANEngine.cluster(points, eps = eps, minPts = minPts)
    }

    // 5. Code Generation with RL Strategy & DBSCAN Context
    suspend fun generateCode(
        prompt: String,
        language: ProgrammingLanguage,
        explicitStrategy: RlStrategy? = null,
        geminiApiKey: String = ""
    ): GenerationRecord {
        // Step A: Run DBSCAN on current repository points to find dense cluster context
        val dbscanResult = runDBSCAN()
        val (matchedClusterId, exemplars) = DBSCANEngine.findRelevantExemplars(prompt, dbscanResult)
        val clusterLabel = if (matchedClusterId != null) {
            val kws = dbscanResult.clusterKeywords[matchedClusterId] ?: emptyList()
            "Cluster $matchedClusterId: ${kws.joinToString(", ")}"
        } else {
            "Noise / Novel Domain"
        }

        // Step B: Determine RL Strategy via Q-learning policy or explicit override
        val stateKey = RLEngine.deriveStateKey(language.id, prompt)
        val existingEntries = rlPolicyDao.getEntriesForState(stateKey)

        val selectedStrategy = explicitStrategy ?: run {
            val (strategy, isExploration) = RLEngine.selectStrategy(stateKey, existingEntries)
            strategy
        }

        // Step C: Execute Generation (Try Gemini if key provided, fallback to Offline Engine)
        var codeAndExp: Pair<String, String>? = null
        if (geminiApiKey.isNotBlank()) {
            codeAndExp = GeminiCodeService.generateWithGemini(
                apiKey = geminiApiKey,
                prompt = prompt,
                language = language,
                strategy = selectedStrategy,
                exemplars = exemplars
            )
        }

        if (codeAndExp == null) {
            codeAndExp = CodeGeneratorEngine.synthesizeCodeAndExplanation(
                prompt = prompt,
                language = language,
                strategy = selectedStrategy,
                exemplars = exemplars
            )
        }

        // Step D: Record Generation in DB
        val record = GenerationRecord(
            prompt = prompt,
            languageId = language.id,
            generatedCode = codeAndExp.first,
            explanation = codeAndExp.second,
            strategy = selectedStrategy,
            clusterId = matchedClusterId ?: -1,
            clusterLabel = clusterLabel,
            userFeedback = 0,
            timestamp = System.currentTimeMillis()
        )

        val newId = recordDao.insertRecord(record)
        return record.copy(id = newId)
    }

    // 6. User Feedback (Reinforcement Learning Reward Update)
    suspend fun submitFeedback(record: GenerationRecord, feedbackScore: Int) {
        val updatedRecord = record.copy(userFeedback = feedbackScore)
        recordDao.updateRecord(updatedRecord)

        val stateKey = RLEngine.deriveStateKey(record.languageId, record.prompt)
        val currentEntry = rlPolicyDao.getEntry(stateKey, record.strategy.name)

        val reward = when {
            feedbackScore > 0 -> 1.0
            feedbackScore < 0 -> -0.8
            else -> 0.0
        }

        val updatedEntry = RLEngine.updateQValue(
            currentEntry = currentEntry,
            stateKey = stateKey,
            strategy = record.strategy,
            reward = reward
        )
        rlPolicyDao.insertOrUpdate(updatedEntry)
    }

    // 7. Error Mitigation & Fine-Tuning Edit
    suspend fun applyErrorMitigationAndFineTune(
        record: GenerationRecord,
        editedCode: String,
        mitigationType: String,
        mitigationNotes: String,
        saveAsGoldenExemplar: Boolean
    ): GenerationRecord {
        val reward = RLEngine.calculateFineTuningReward(
            originalCode = record.generatedCode,
            editedCode = editedCode,
            mitigationType = mitigationType
        )

        // 1. Update RL policy with fine-tuning reward
        val stateKey = RLEngine.deriveStateKey(record.languageId, record.prompt)
        val currentEntry = rlPolicyDao.getEntry(stateKey, record.strategy.name)
        val updatedEntry = RLEngine.updateQValue(
            currentEntry = currentEntry,
            stateKey = stateKey,
            strategy = record.strategy,
            reward = reward
        )
        rlPolicyDao.insertOrUpdate(updatedEntry)

        // 2. Optionally save corrected code as a prioritized golden exemplar snippet in DBSCAN database
        if (saveAsGoldenExemplar) {
            val goldenSnippet = CodeSnippet(
                title = "Fine-Tuned: ${record.prompt.take(40)}",
                languageId = record.languageId,
                code = editedCode,
                tags = "fine-tuned,error-mitigated,${mitigationType.lowercase()}",
                category = "Fine-Tuned",
                explanation = "Error mitigated: $mitigationType. Notes: $mitigationNotes",
                isFineTunedExemplar = true,
                errorMitigationNote = mitigationNotes
            )
            snippetDao.insertSnippet(goldenSnippet)
        }

        // 3. Update generation record with fine-tuned version
        val updatedRecord = record.copy(
            editedCode = editedCode,
            isFineTuned = true,
            errorMitigationType = mitigationType,
            errorMitigationNotes = mitigationNotes
        )
        recordDao.updateRecord(updatedRecord)
        return updatedRecord
    }
}
