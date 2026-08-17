package com.example.nlp

import com.example.data.model.*
import java.util.Locale

/**
 * High-performance, comprehensive Natural Language Processing (NLP) Analyzer.
 * Computes Term Frequency, Named Entities, Verb-to-Verb collocations, Noun Phrases,
 * Bigrams, Trigrams, and Part-of-Speech (POS) tags for all terms.
 */
object NLPAnalyzer {

    private val STOP_WORDS = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by",
        "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't",
        "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have",
        "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him",
        "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is",
        "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself",
        "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours",
        "ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should",
        "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
        "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've",
        "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we",
        "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where",
        "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would",
        "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
    )

    private val VERB_STEMS = setOf(
        "want", "need", "like", "love", "hate", "plan", "decide", "hope", "try", "attempt", "manage",
        "seem", "appear", "tend", "aim", "strive", "learn", "teach", "ask", "tell", "allow", "force",
        "seek", "begin", "start", "continue", "choose", "expect", "refuse", "agree", "fail", "intend",
        "promise", "prepare", "wish", "claim", "hesitate", "struggle", "prove", "demand", "lead", "help"
    )

    private val COMMON_VERBS = setOf(
        "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did",
        "say", "says", "said", "go", "goes", "went", "gone", "make", "makes", "made", "know", "knows",
        "knew", "known", "think", "thinks", "thought", "take", "takes", "took", "taken", "see", "sees",
        "saw", "seen", "come", "comes", "came", "look", "looks", "looked", "use", "uses", "used", "find",
        "finds", "found", "give", "gives", "gave", "given", "tell", "tells", "told", "work", "works",
        "worked", "call", "calls", "called", "try", "tries", "tried", "ask", "asks", "asked", "need",
        "needs", "needed", "feel", "feels", "felt", "become", "becomes", "became", "leave", "leaves",
        "left", "put", "puts", "mean", "means", "meant", "keep", "keeps", "kept", "let", "lets", "begin",
        "begins", "began", "seem", "seems", "seemed", "help", "helps", "helped", "talk", "talks", "talked",
        "turn", "turns", "turned", "start", "starts", "started", "show", "shows", "showed", "hear", "hears",
        "heard", "play", "plays", "played", "run", "runs", "ran", "move", "moves", "moved", "like", "likes",
        "liked", "live", "lives", "lived", "believe", "believes", "believed", "hold", "holds", "held",
        "bring", "brings", "brought", "happen", "happens", "happened", "write", "writes", "wrote", "written",
        "provide", "provides", "provided", "sit", "sits", "sat", "stand", "stands", "stood", "lose", "loses",
        "lost", "pay", "pays", "paid", "meet", "meets", "met", "include", "includes", "included", "continue",
        "continues", "continued", "set", "sets", "learn", "learns", "learned", "change", "changes", "changed",
        "lead", "leads", "led", "understand", "understands", "understood", "watch", "watches", "watched",
        "follow", "follows", "followed", "stop", "stops", "stopped", "create", "creates", "created", "speak",
        "speaks", "spoke", "spoken", "read", "reads", "allow", "allows", "allowed", "add", "adds", "added",
        "spend", "spends", "spent", "grow", "grows", "grew", "grown", "open", "opens", "opened", "walk",
        "walks", "walked", "win", "wins", "won", "offer", "offers", "offered", "remember", "remembers",
        "remembered", "love", "loves", "loved", "consider", "considers", "considered", "appear", "appears",
        "appeared", "buy", "buys", "bought", "wait", "waits", "waited", "serve", "serves", "served", "die",
        "dies", "died", "send", "sends", "sent", "expect", "expects", "expected", "build", "builds", "built",
        "stay", "stays", "stayed", "fall", "falls", "fell", "fallen", "cut", "cuts", "reach", "reaches",
        "reached", "kill", "kills", "killed", "remain", "remains", "remained", "suggest", "suggests",
        "suggested", "raise", "raises", "raised", "pass", "passes", "passed", "sell", "sells", "sold",
        "require", "requires", "required", "report", "reports", "reported", "decide", "decides", "decided",
        "pull", "pulls", "pulled"
    )

    private val COMMON_ADJECTIVES = setOf(
        "quantum", "strong", "weak", "electromagnetic", "nuclear", "gravitational", "general", "special",
        "unified", "elementary", "relativistic", "atomic", "subatomic", "fundamental", "theoretical",
        "experimental", "mathematical", "physical", "neural", "deep", "computational", "linguistic",
        "syntactic", "semantic", "semantic", "cryptographic", "encrypted", "offline", "digital",
        "good", "new", "first", "last", "long", "great", "little", "own", "other", "old", "right",
        "big", "high", "different", "small", "large", "next", "early", "young", "important", "few",
        "public", "bad", "same", "able", "major", "better", "best", "simple", "complex", "massive",
        "tiny", "infinite", "continuous", "discrete", "symmetric", "invariant", "covariant", "conserved"
    )

    /**
     * Performs complete NLP analysis on text.
     */
    fun analyze(text: String): NLPAnalysisResult {
        if (text.isBlank()) {
            return NLPAnalysisResult(
                totalWords = 0,
                uniqueWords = 0,
                readabilityScore = 0.0,
                termFrequencies = emptyList(),
                namedEntities = emptyList(),
                verbToVerbs = emptyList(),
                nounPhrases = emptyList(),
                bigrams = emptyList(),
                trigrams = emptyList(),
                posTaggedTerms = emptyList()
            )
        }

        // Tokenize words
        val rawTokens = text.split(Regex("[^\\w'-]+")).filter { it.isNotBlank() }
        val cleanTokens = rawTokens.map { it.lowercase(Locale.ROOT).trim('\'', '-') }.filter { it.length > 1 }
        val totalWords = cleanTokens.size

        // 1. Term Frequency
        val tfMap = mutableMapOf<String, Int>()
        for (token in cleanTokens) {
            if (!STOP_WORDS.contains(token)) {
                tfMap[token] = (tfMap[token] ?: 0) + 1
            }
        }
        val termFrequencies = tfMap.entries
            .sortedByDescending { it.value }
            .take(50)
            .map {
                TermFreqItem(
                    term = it.key,
                    count = it.value,
                    percentage = if (totalWords > 0) (it.value.toFloat() / totalWords.toFloat()) * 100f else 0f
                )
            }

        val uniqueWords = cleanTokens.toSet().size

        // 2. Named Entity Frequency (NER)
        val namedEntities = extractNamedEntities(text)

        // 3. Verb 'to' Verb Frequency
        val verbToVerbs = extractVerbToVerbs(text)

        // 4. Noun Phrase Frequency
        val nounPhrases = extractNounPhrases(text)

        // 5. Bigrams
        val bigramMap = mutableMapOf<String, Int>()
        for (i in 0 until cleanTokens.size - 1) {
            val w1 = cleanTokens[i]
            val w2 = cleanTokens[i + 1]
            if (!STOP_WORDS.contains(w1) || !STOP_WORDS.contains(w2)) {
                val bg = "$w1 $w2"
                bigramMap[bg] = (bigramMap[bg] ?: 0) + 1
            }
        }
        val bigrams = bigramMap.entries
            .filter { it.value >= 1 }
            .sortedByDescending { it.value }
            .take(35)
            .map { BigramItem(it.key, it.value) }

        // 6. Trigrams
        val trigramMap = mutableMapOf<String, Int>()
        for (i in 0 until cleanTokens.size - 2) {
            val w1 = cleanTokens[i]
            val w2 = cleanTokens[i + 1]
            val w3 = cleanTokens[i + 2]
            val tg = "$w1 $w2 $w3"
            trigramMap[tg] = (trigramMap[tg] ?: 0) + 1
        }
        val trigrams = trigramMap.entries
            .sortedByDescending { it.value }
            .take(30)
            .map { TrigramItem(it.key, it.value) }

        // 7. POS Tag All Terms
        val posTaggedTerms = extractPosTags(cleanTokens)

        // Readability (Flesch-Kincaid estimate)
        val sentencesCount = maxOf(1, text.split(Regex("[.!?]+")).filter { it.isNotBlank() }.size)
        val avgWordsPerSentence = totalWords.toDouble() / sentencesCount.toDouble()
        val readabilityScore = maxOf(20.0, minOf(100.0, 206.835 - (1.015 * avgWordsPerSentence) - (84.6 * 1.5)))

        return NLPAnalysisResult(
            totalWords = totalWords,
            uniqueWords = uniqueWords,
            readabilityScore = readabilityScore,
            termFrequencies = termFrequencies,
            namedEntities = namedEntities,
            verbToVerbs = verbToVerbs,
            nounPhrases = nounPhrases,
            bigrams = bigrams,
            trigrams = trigrams,
            posTaggedTerms = posTaggedTerms
        )
    }

    private fun extractNamedEntities(text: String): List<NamedEntityItem> {
        val entityMap = mutableMapOf<String, Pair<String, Int>>() // entity -> (Type, count)
        val capitalizedWordRegex = Regex("\\b[A-Z][a-zA-Z0-9'-]+(?:\\s+[A-Z][a-zA-Z0-9'-]+)*\\b")
        val matches = capitalizedWordRegex.findAll(text)

        val knownConcepts = setOf(
            "Quantum Chromodynamics", "Standard Model", "General Relativity", "Electroweak Theory",
            "Higgs Boson", "Dirac Equation", "Maxwell Equations", "Schrödinger Equation", "Newtonian Gravity",
            "Strong Force", "Weak Force", "Electromagnetism", "Gravitation", "Big Bang", "Cosmology",
            "Natural Language Processing", "Machine Learning", "Neural Network", "Database Mastermind",
            "Google Chrome", "Google Drive", "Android", "Room Database", "End-to-End Encryption"
        )
        val knownPersons = setOf(
            "Einstein", "Albert Einstein", "Newton", "Isaac Newton", "Dirac", "Paul Dirac",
            "Feynman", "Richard Feynman", "Maxwell", "James Clerk Maxwell", "Fermi", "Enrico Fermi",
            "Higgs", "Peter Higgs", "Schrödinger", "Erwin Schrödinger", "Heisenberg", "Werner Heisenberg",
            "Noam Chomsky", "Chomsky", "Turing", "Alan Turing", "Shannon", "Claude Shannon", "Euler", "Leonhard Euler"
        )
        val knownOrgs = setOf(
            "CERN", "NASA", "MIT", "Stanford", "Google", "DeepMind", "OpenAI", "Harvard", "Princeton", "Caltech"
        )
        val knownLocations = setOf(
            "Geneva", "Switzerland", "United States", "Cambridge", "Princeton", "California", "Earth", "Milky Way", "Sun"
        )

        for (match in matches) {
            val entity = match.value.trim()
            if (entity.length < 2 || entity.equals("The", ignoreCase = true) || entity.equals("A", ignoreCase = true)) continue

            val type = when {
                knownPersons.any { entity.contains(it, ignoreCase = true) } -> "PERSON"
                knownOrgs.any { entity.contains(it, ignoreCase = true) } -> "ORGANIZATION"
                knownLocations.any { entity.contains(it, ignoreCase = true) } -> "LOCATION / GPE"
                knownConcepts.any { entity.contains(it, ignoreCase = true) } -> "SCIENTIFIC CONCEPT"
                entity.split(" ").size > 1 -> "NAMED ENTITY"
                else -> "PROPER NOUN"
            }

            val current = entityMap[entity]
            val count = (current?.second ?: 0) + 1
            entityMap[entity] = Pair(type, count)
        }

        return entityMap.entries
            .sortedByDescending { it.value.second }
            .take(30)
            .map { NamedEntityItem(entity = it.key, type = it.value.first, count = it.value.second) }
    }

    private fun extractVerbToVerbs(text: String): List<VerbToVerbItem> {
        val verbToVerbRegex = Regex("\\b([a-zA-Z]+)\\s+to\\s+([a-zA-Z]+)\\b", RegexOption.IGNORE_CASE)
        val matches = verbToVerbRegex.findAll(text)
        val counts = mutableMapOf<String, Triple<String, String, Int>>()

        for (match in matches) {
            val v1 = match.groupValues[1].lowercase(Locale.ROOT)
            val v2 = match.groupValues[2].lowercase(Locale.ROOT)

            // Check if v1 and v2 are likely verbs or verb-like stems
            val isV1Verb = VERB_STEMS.contains(v1) || COMMON_VERBS.contains(v1) || v1.endsWith("ed") || v1.endsWith("ing") || v1.endsWith("s")
            val isV2Verb = COMMON_VERBS.contains(v2) || VERB_STEMS.contains(v2) || v2.endsWith("e") || v2.endsWith("fy") || v2.endsWith("ize") || v2.endsWith("ise")

            if (isV1Verb || isV2Verb || VERB_STEMS.contains(v1)) {
                val phrase = "$v1 to $v2"
                val current = counts[phrase]
                val newCount = (current?.third ?: 0) + 1
                counts[phrase] = Triple(v1, v2, newCount)
            }
        }

        return counts.values
            .sortedByDescending { it.third }
            .take(25)
            .map { VerbToVerbItem(phrase = "${it.first} to ${it.second}", verb1 = it.first, verb2 = it.second, count = it.third) }
    }

    private fun extractNounPhrases(text: String): List<NounPhraseItem> {
        // Pattern: [Adjective/Noun]* [Noun]+
        val tokens = text.split(Regex("[^\\w'-]+")).filter { it.isNotBlank() }
        val counts = mutableMapOf<String, Int>()

        for (i in 0 until tokens.size - 1) {
            val w1 = tokens[i].lowercase(Locale.ROOT)
            val w2 = tokens[i + 1].lowercase(Locale.ROOT)

            if (!STOP_WORDS.contains(w1) && !STOP_WORDS.contains(w2)) {
                if (COMMON_ADJECTIVES.contains(w1) || isNoun(w1) || isNoun(w2)) {
                    val phrase = "$w1 $w2"
                    counts[phrase] = (counts[phrase] ?: 0) + 1
                }
            }

            if (i < tokens.size - 2) {
                val w3 = tokens[i + 2].lowercase(Locale.ROOT)
                if (!STOP_WORDS.contains(w1) && !STOP_WORDS.contains(w3)) {
                    val phrase3 = "$w1 $w2 $w3"
                    counts[phrase3] = (counts[phrase3] ?: 0) + 1
                }
            }
        }

        return counts.entries
            .filter { it.value >= 1 }
            .sortedByDescending { it.value }
            .take(30)
            .map { NounPhraseItem(it.key, it.value) }
    }

    private fun extractPosTags(tokens: List<String>): List<PosTagItem> {
        val tagMap = mutableMapOf<String, Pair<String, Int>>() // term -> (tag, count)

        for (token in tokens) {
            val (tag, desc) = determinePos(token)
            val existing = tagMap[token]
            val count = (existing?.second ?: 0) + 1
            tagMap[token] = Pair(tag, count)
        }

        return tagMap.entries
            .sortedByDescending { it.value.second }
            .take(45)
            .map {
                val tag = it.value.first
                val desc = getTagDescription(tag)
                PosTagItem(term = it.key, tag = tag, tagDescription = desc, count = it.value.second)
            }
    }

    private fun determinePos(word: String): Pair<String, String> {
        val w = word.lowercase(Locale.ROOT)
        return when {
            STOP_WORDS.contains(w) && (w in listOf("in", "on", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from")) ->
                Pair("IN", "Preposition / Conjunction")
            STOP_WORDS.contains(w) && (w in listOf("and", "but", "or", "nor", "for", "yet", "so")) ->
                Pair("CC", "Coordinating Conjunction")
            STOP_WORDS.contains(w) && (w in listOf("he", "she", "it", "they", "we", "i", "you", "them", "him", "her", "us", "me")) ->
                Pair("PRP", "Personal Pronoun")
            STOP_WORDS.contains(w) && (w in listOf("the", "a", "an", "this", "that", "these", "those")) ->
                Pair("DT", "Determiner")
            COMMON_VERBS.contains(w) || w.endsWith("ed") || w.endsWith("ing") || VERB_STEMS.contains(w) ->
                Pair(if (w.endsWith("ing")) "VBG" else if (w.endsWith("ed")) "VBD" else "VB", "Verb Action")
            COMMON_ADJECTIVES.contains(w) || w.endsWith("al") || w.endsWith("ic") || w.endsWith("ive") || w.endsWith("ous") || w.endsWith("ful") || w.endsWith("able") ->
                Pair("JJ", "Adjective Descriptor")
            w.endsWith("ly") ->
                Pair("RB", "Adverb Modifier")
            word.first().isUpperCase() ->
                Pair("NNP", "Proper Noun")
            w.endsWith("tion") || w.endsWith("sion") || w.endsWith("ment") || w.endsWith("ness") || w.endsWith("ity") || w.endsWith("ism") || w.endsWith("er") || w.endsWith("or") ->
                Pair("NN", "Abstract/Core Noun")
            w.endsWith("s") && !w.endsWith("ss") ->
                Pair("NNS", "Plural Noun")
            else ->
                Pair("NN", "Noun Entity")
        }
    }

    private fun getTagDescription(tag: String): String {
        return when (tag) {
            "NN" -> "Noun, Singular or Mass"
            "NNS" -> "Noun, Plural"
            "NNP" -> "Proper Noun, Singular"
            "VB" -> "Verb, Base Form"
            "VBD" -> "Verb, Past Tense"
            "VBG" -> "Verb, Gerund / Present Participle"
            "JJ" -> "Adjective, Qualitative"
            "RB" -> "Adverb, Modifier"
            "IN" -> "Preposition or Subordinating Conjunction"
            "CC" -> "Coordinating Conjunction"
            "DT" -> "Determiner / Article"
            "PRP" -> "Personal Pronoun"
            else -> "Lexical Category"
        }
    }

    private fun isNoun(w: String): Boolean {
        return !STOP_WORDS.contains(w) && !COMMON_VERBS.contains(w) && (w.endsWith("tion") || w.endsWith("ity") || w.endsWith("ism") || w.endsWith("ment") || w.length > 3)
    }
}
