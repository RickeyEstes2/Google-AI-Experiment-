package com.example.solveflow.engine.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * High-performance regex-based syntax highlighting engine for code editing and visualization.
 * Supports Kotlin, Python, TypeScript, JavaScript, Go, Rust, Java, C++, Swift, SQL, Bash, HTML/JSON,
 * as well as custom user-defined languages with dynamic keyword registries.
 */
object SyntaxHighlighterEngine {

    // Palette: Modern IDE Dark (Catppuccin Macchiato / VS Code Dark+ hybrid)
    val DarkKeywordColor = Color(0xFFC678DD)       // Soft vibrant purple / orchid
    val DarkControlFlowColor = Color(0xFFFF7B72)   // Rose red for control flow (if, else, return)
    val DarkTypeColor = Color(0xFFE5C07B)          // Golden amber for types & classes
    val DarkStringColor = Color(0xFF98C379)        // Muted emerald green for strings
    val DarkNumberColor = Color(0xFFD19A66)        // Warm peach / orange for numbers
    val DarkCommentColor = Color(0xFF7F849C)       // Slate gray, italic for comments
    val DarkAnnotationColor = Color(0xFF61AFEF)    // Cerulean cyan for @annotations & decorators
    val DarkFunctionColor = Color(0xFF89DCEB)      // Sky blue for function declarations & calls
    val DarkOperatorColor = Color(0xFFF38BA8)      // Pinkish coral for symbols & operators
    val DarkBooleanNullColor = Color(0xFFFAB387)   // Apricot for true, false, null, nil
    val DarkDefaultTextColor = Color(0xFFCDD6F4)   // Clean off-white for identifiers / text

    // Palette: Modern IDE Light (One Light / GitHub Light)
    val LightKeywordColor = Color(0xFFA626A4)
    val LightControlFlowColor = Color(0xFFE45649)
    val LightTypeColor = Color(0xFFC18401)
    val LightStringColor = Color(0xFF50A14F)
    val LightNumberColor = Color(0xFF986801)
    val LightCommentColor = Color(0xFFA0A1A7)
    val LightAnnotationColor = Color(0xFF4078F2)
    val LightFunctionColor = Color(0xFF0184BC)
    val LightOperatorColor = Color(0xFFCA1243)
    val LightBooleanNullColor = Color(0xFFD19A66)
    val LightDefaultTextColor = Color(0xFF24292F)

    data class SyntaxTheme(
        val keywordColor: Color,
        val controlFlowColor: Color,
        val typeColor: Color,
        val stringColor: Color,
        val numberColor: Color,
        val commentColor: Color,
        val annotationColor: Color,
        val functionColor: Color,
        val operatorColor: Color,
        val booleanNullColor: Color,
        val defaultTextColor: Color
    )

    val DarkTheme = SyntaxTheme(
        keywordColor = DarkKeywordColor,
        controlFlowColor = DarkControlFlowColor,
        typeColor = DarkTypeColor,
        stringColor = DarkStringColor,
        numberColor = DarkNumberColor,
        commentColor = DarkCommentColor,
        annotationColor = DarkAnnotationColor,
        functionColor = DarkFunctionColor,
        operatorColor = DarkOperatorColor,
        booleanNullColor = DarkBooleanNullColor,
        defaultTextColor = DarkDefaultTextColor
    )

    val LightTheme = SyntaxTheme(
        keywordColor = LightKeywordColor,
        controlFlowColor = LightControlFlowColor,
        typeColor = LightTypeColor,
        stringColor = LightStringColor,
        numberColor = LightNumberColor,
        commentColor = LightCommentColor,
        annotationColor = LightAnnotationColor,
        functionColor = LightFunctionColor,
        operatorColor = LightOperatorColor,
        booleanNullColor = LightBooleanNullColor,
        defaultTextColor = LightDefaultTextColor
    )

    enum class TokenType {
        COMMENT,
        STRING,
        ANNOTATION,
        CONTROL_FLOW,
        KEYWORD,
        TYPE,
        BOOLEAN_NULL,
        NUMBER,
        FUNCTION,
        OPERATOR
    }

    private data class TokenSpan(
        val start: Int,
        val end: Int,
        val type: TokenType
    )

    // Language definitions with keyword and type sets
    private val kotlinControlFlow = setOf(
        "if", "else", "when", "for", "while", "do", "return", "break", "continue", "throw", "try", "catch", "finally"
    )
    private val kotlinKeywords = setOf(
        "package", "import", "class", "interface", "object", "fun", "val", "var", "constructor", "init",
        "this", "super", "typeof", "as", "is", "in", "!in", "!is", "by", "out", "reified", "enum",
        "open", "final", "abstract", "sealed", "data", "inline", "noinline", "crossinline", "companion",
        "override", "private", "protected", "public", "internal", "suspend", "tailrec", "operator", "infix",
        "const", "lateinit", "vararg", "external", "annotation", "actual", "expect", "value"
    )
    private val kotlinTypes = setOf(
        "Int", "Long", "Short", "Byte", "Float", "Double", "Boolean", "Char", "String", "Array",
        "List", "MutableList", "Set", "MutableSet", "Map", "MutableMap", "Any", "Unit", "Nothing",
        "Flow", "StateFlow", "SharedFlow", "CoroutineScope", "Job", "Deferred", "Result", "Sequence"
    )

    private val pythonControlFlow = setOf(
        "if", "elif", "else", "for", "while", "break", "continue", "return", "yield", "try", "except", "finally", "raise", "assert"
    )
    private val pythonKeywords = setOf(
        "def", "class", "import", "from", "as", "with", "pass", "lambda", "global", "nonlocal", "del",
        "async", "await", "self", "cls", "match", "case"
    )
    private val pythonTypes = setOf(
        "int", "float", "str", "bool", "list", "dict", "set", "tuple", "bytes", "object", "type",
        "Any", "Union", "Optional", "List", "Dict", "Set", "Tuple", "Callable", "Iterable", "Generator"
    )

    private val tsControlFlow = setOf(
        "if", "else", "switch", "case", "default", "for", "while", "do", "break", "continue", "return",
        "throw", "try", "catch", "finally"
    )
    private val tsKeywords = setOf(
        "function", "const", "let", "var", "class", "interface", "type", "enum", "namespace", "module",
        "import", "export", "from", "as", "default", "extends", "implements", "new", "this", "super",
        "typeof", "instanceof", "in", "keyof", "readonly", "async", "await", "yield", "public",
        "private", "protected", "static", "abstract", "declare", "override", "never", "unknown"
    )
    private val tsTypes = setOf(
        "string", "number", "boolean", "symbol", "bigint", "any", "void", "object", "Record", "Array",
        "Promise", "Observable", "Set", "Map", "Partial", "Required", "Pick", "Omit", "Readonly"
    )

    private val rustControlFlow = setOf(
        "if", "else", "match", "loop", "while", "for", "in", "break", "continue", "return"
    )
    private val rustKeywords = setOf(
        "fn", "let", "mut", "const", "static", "struct", "enum", "trait", "impl", "type", "where",
        "pub", "mod", "use", "crate", "super", "self", "Self", "unsafe", "async", "await", "dyn",
        "move", "ref", "extern", "as"
    )
    private val rustTypes = setOf(
        "i8", "i16", "i32", "i64", "i128", "isize", "u8", "u16", "u32", "u64", "u128", "usize",
        "f32", "f64", "bool", "char", "str", "String", "Vec", "Option", "Result", "Box", "Rc", "Arc", "Cell", "RefCell"
    )

    private val goControlFlow = setOf(
        "if", "else", "switch", "case", "default", "for", "range", "break", "continue", "return", "goto", "fallthrough"
    )
    private val goKeywords = setOf(
        "package", "import", "func", "var", "const", "type", "struct", "interface", "map", "chan",
        "select", "go", "defer"
    )
    private val goTypes = setOf(
        "int", "int8", "int16", "int32", "int64", "uint", "uint8", "uint16", "uint32", "uint64", "uintptr",
        "float32", "float64", "complex64", "complex128", "byte", "rune", "string", "bool", "error", "any"
    )

    private val javaControlFlow = setOf(
        "if", "else", "switch", "case", "default", "for", "while", "do", "break", "continue", "return",
        "throw", "try", "catch", "finally"
    )
    private val javaKeywords = setOf(
        "public", "protected", "private", "static", "final", "abstract", "synchronized", "volatile",
        "transient", "native", "strictfp", "class", "interface", "enum", "record", "extends", "implements",
        "new", "this", "super", "instanceof", "package", "import", "throws", "assert", "void"
    )
    private val javaTypes = setOf(
        "byte", "short", "int", "long", "float", "double", "boolean", "char", "String", "Object",
        "List", "ArrayList", "Map", "HashMap", "Set", "HashSet", "Optional", "CompletableFuture", "Stream"
    )

    private val cppControlFlow = setOf(
        "if", "else", "switch", "case", "default", "for", "while", "do", "break", "continue", "return",
        "throw", "try", "catch"
    )
    private val cppKeywords = setOf(
        "auto", "class", "struct", "union", "enum", "namespace", "using", "template", "typename",
        "public", "protected", "private", "virtual", "override", "final", "explicit", "friend", "inline",
        "constexpr", "const", "static", "volatile", "mutable", "extern", "new", "delete", "this", "operator",
        "sizeof", "decltype", "noexcept"
    )
    private val cppTypes = setOf(
        "void", "bool", "char", "wchar_t", "char16_t", "char32_t", "int", "short", "long", "signed",
        "unsigned", "float", "double", "size_t", "std::string", "std::vector", "std::map", "std::shared_ptr", "std::unique_ptr"
    )

    private val swiftControlFlow = setOf(
        "if", "else", "guard", "switch", "case", "default", "for", "in", "while", "repeat", "break",
        "continue", "return", "throw", "try", "catch", "defer"
    )
    private val swiftKeywords = setOf(
        "func", "let", "var", "class", "struct", "enum", "protocol", "extension", "typealias", "associatedtype",
        "import", "public", "private", "fileprivate", "internal", "open", "mutating", "nonmutating",
        "static", "final", "override", "init", "deinit", "self", "Self", "super", "as", "is", "async", "await", "actor"
    )
    private val swiftTypes = setOf(
        "Int", "Double", "Float", "Bool", "String", "Character", "Array", "Dictionary", "Set", "Optional", "Result", "Any", "AnyObject"
    )

    private val sqlKeywords = setOf(
        "select", "from", "where", "insert", "into", "update", "delete", "join", "left", "right", "inner",
        "outer", "full", "cross", "on", "group", "by", "having", "order", "asc", "desc", "limit", "offset",
        "create", "alter", "drop", "table", "index", "view", "database", "schema", "primary", "foreign",
        "key", "references", "not", "and", "or", "in", "between", "like", "is", "null", "as", "distinct",
        "union", "all", "case", "when", "then", "else", "end", "values", "set", "default", "check", "unique"
    )
    private val sqlTypes = setOf(
        "int", "integer", "bigint", "smallint", "tinyint", "varchar", "char", "text", "boolean", "date",
        "datetime", "timestamp", "decimal", "numeric", "float", "double", "blob", "json"
    )

    private val bashControlFlow = setOf(
        "if", "then", "else", "elif", "fi", "case", "esac", "for", "while", "until", "do", "done", "return", "exit"
    )
    private val bashKeywords = setOf(
        "function", "select", "time", "in", "echo", "export", "local", "readonly", "set", "unset", "source", "alias"
    )

    private val booleanNullLiterals = setOf(
        "true", "false", "null", "nil", "None", "True", "False", "undefined", "NaN"
    )

    // Regex patterns
    private val multiLineCommentRegex = Regex("/\\*[\\s\\S]*?\\*/")
    private val singleLineSlashCommentRegex = Regex("//[^\r\n]*")
    private val hashCommentRegex = Regex("#[^\r\n]*")
    private val dashDashCommentRegex = Regex("--[^\r\n]*")

    private val tripleDoubleQuoteStringRegex = Regex("\"\"\"[\\s\\S]*?\"\"\"")
    private val tripleSingleQuoteStringRegex = Regex("'''[\\s\\S]*?'''")
    private val doubleQuoteStringRegex = Regex("\"([^\"\\\\\\r\\n]|\\\\.)*\"")
    private val singleQuoteStringRegex = Regex("'([^'\\\\\\r\\n]|\\\\.)*'")
    private val backtickStringRegex = Regex("`([^`\\\\]|\\\\.)*`")

    private val annotationRegex = Regex("@[a-zA-Z_][a-zA-Z0-9_]*")
    private val numberRegex = Regex("\\b(0x[0-9a-fA-F]+|0b[01]+|\\d+(\\.\\d+)?([fFdDlL]?))\\b")
    private val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
    private val functionCallRegex = Regex("\\b([a-zA-Z_][a-zA-Z0-9_]*)(?=\\s*\\()")
    private val operatorRegex = Regex("(==|!=|<=|>=|=>|->|::|\\|\\||&&|\\+\\+|--|[+\\-*/%=&|!<>?~^:])")

    /**
     * Highlights code text returning an AnnotatedString with colorized tokens.
     */
    fun highlight(
        code: String,
        languageId: String,
        isDark: Boolean = true,
        customKeywords: Set<String> = emptySet()
    ): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        val theme = if (isDark) DarkTheme else LightTheme
        val normLang = languageId.lowercase().trim()

        // 1. Gather all non-overlapping token spans
        val spans = mutableListOf<TokenSpan>()
        val occupied = BooleanArray(code.length)

        fun markOccupied(start: Int, end: Int) {
            for (i in start until end.coerceAtMost(code.length)) {
                occupied[i] = true
            }
        }

        fun isFree(start: Int, end: Int): Boolean {
            for (i in start until end.coerceAtMost(code.length)) {
                if (occupied[i]) return false
            }
            return true
        }

        // --- Comments have top priority ---
        // Slash comments (Kotlin, Java, TS, JS, Go, Rust, C++, Swift)
        if (normLang !in setOf("python", "bash", "shell", "sh", "sql")) {
            multiLineCommentRegex.findAll(code).forEach { m ->
                val range = m.range
                if (isFree(range.first, range.last + 1)) {
                    spans.add(TokenSpan(range.first, range.last + 1, TokenType.COMMENT))
                    markOccupied(range.first, range.last + 1)
                }
            }
            singleLineSlashCommentRegex.findAll(code).forEach { m ->
                val range = m.range
                if (isFree(range.first, range.last + 1)) {
                    spans.add(TokenSpan(range.first, range.last + 1, TokenType.COMMENT))
                    markOccupied(range.first, range.last + 1)
                }
            }
        }

        // Hash comments (Python, Bash)
        if (normLang in setOf("python", "bash", "shell", "sh")) {
            hashCommentRegex.findAll(code).forEach { m ->
                val range = m.range
                if (isFree(range.first, range.last + 1)) {
                    spans.add(TokenSpan(range.first, range.last + 1, TokenType.COMMENT))
                    markOccupied(range.first, range.last + 1)
                }
            }
        }

        // SQL Dash-Dash comments
        if (normLang == "sql") {
            dashDashCommentRegex.findAll(code).forEach { m ->
                val range = m.range
                if (isFree(range.first, range.last + 1)) {
                    spans.add(TokenSpan(range.first, range.last + 1, TokenType.COMMENT))
                    markOccupied(range.first, range.last + 1)
                }
            }
            multiLineCommentRegex.findAll(code).forEach { m ->
                val range = m.range
                if (isFree(range.first, range.last + 1)) {
                    spans.add(TokenSpan(range.first, range.last + 1, TokenType.COMMENT))
                    markOccupied(range.first, range.last + 1)
                }
            }
        }

        // --- Strings have second priority ---
        // Triple quotes (Python, Kotlin)
        tripleDoubleQuoteStringRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.STRING))
                markOccupied(range.first, range.last + 1)
            }
        }
        tripleSingleQuoteStringRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.STRING))
                markOccupied(range.first, range.last + 1)
            }
        }

        // Double & Single quote strings
        doubleQuoteStringRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.STRING))
                markOccupied(range.first, range.last + 1)
            }
        }
        singleQuoteStringRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.STRING))
                markOccupied(range.first, range.last + 1)
            }
        }
        backtickStringRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.STRING))
                markOccupied(range.first, range.last + 1)
            }
        }

        // --- Annotations / Decorators ---
        annotationRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.ANNOTATION))
                markOccupied(range.first, range.last + 1)
            }
        }

        // --- Numbers ---
        numberRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.NUMBER))
                markOccupied(range.first, range.last + 1)
            }
        }

        // Prepare language keyword & type registries
        val (controlFlows, keywords, types) = when (normLang) {
            "kotlin", "kt" -> Triple(kotlinControlFlow, kotlinKeywords, kotlinTypes)
            "python", "py" -> Triple(pythonControlFlow, pythonKeywords, pythonTypes)
            "typescript", "ts", "javascript", "js" -> Triple(tsControlFlow, tsKeywords, tsTypes)
            "rust", "rs" -> Triple(rustControlFlow, rustKeywords, rustTypes)
            "go", "golang" -> Triple(goControlFlow, goKeywords, goTypes)
            "java" -> Triple(javaControlFlow, javaKeywords, javaTypes)
            "cpp", "c++", "c" -> Triple(cppControlFlow, cppKeywords, cppTypes)
            "swift" -> Triple(swiftControlFlow, swiftKeywords, swiftTypes)
            "sql" -> Triple(emptySet(), sqlKeywords, sqlTypes)
            "bash", "shell", "sh" -> Triple(bashControlFlow, bashKeywords, emptySet())
            else -> Triple(
                kotlinControlFlow + pythonControlFlow + tsControlFlow,
                kotlinKeywords + pythonKeywords + tsKeywords + customKeywords,
                kotlinTypes + pythonTypes + tsTypes
            )
        }

        val allCustom = customKeywords.map { it.lowercase() }.toSet()

        // --- Words (Keywords, Types, Booleans, Functions) ---
        wordRegex.findAll(code).forEach { m ->
            val range = m.range
            val word = m.value
            val lowerWord = word.lowercase()

            if (isFree(range.first, range.last + 1)) {
                when {
                    booleanNullLiterals.contains(word) || (normLang == "sql" && lowerWord == "null") -> {
                        spans.add(TokenSpan(range.first, range.last + 1, TokenType.BOOLEAN_NULL))
                        markOccupied(range.first, range.last + 1)
                    }
                    controlFlows.contains(if (normLang == "sql") lowerWord else word) -> {
                        spans.add(TokenSpan(range.first, range.last + 1, TokenType.CONTROL_FLOW))
                        markOccupied(range.first, range.last + 1)
                    }
                    keywords.contains(if (normLang == "sql") lowerWord else word) || allCustom.contains(lowerWord) -> {
                        spans.add(TokenSpan(range.first, range.last + 1, TokenType.KEYWORD))
                        markOccupied(range.first, range.last + 1)
                    }
                    types.contains(if (normLang == "sql") lowerWord else word) || (word.firstOrNull()?.isUpperCase() == true && normLang != "python") -> {
                        spans.add(TokenSpan(range.first, range.last + 1, TokenType.TYPE))
                        markOccupied(range.first, range.last + 1)
                    }
                }
            }
        }

        // --- Function Calls ---
        functionCallRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.FUNCTION))
                markOccupied(range.first, range.last + 1)
            }
        }

        // --- Operators ---
        operatorRegex.findAll(code).forEach { m ->
            val range = m.range
            if (isFree(range.first, range.last + 1)) {
                spans.add(TokenSpan(range.first, range.last + 1, TokenType.OPERATOR))
                markOccupied(range.first, range.last + 1)
            }
        }

        // 2. Build the final AnnotatedString
        return buildAnnotatedString {
            append(code)

            // Base text style
            addStyle(
                SpanStyle(
                    color = theme.defaultTextColor,
                    fontFamily = FontFamily.Monospace
                ),
                0,
                code.length
            )

            // Apply token styles
            spans.forEach { span ->
                val spanStyle = when (span.type) {
                    TokenType.KEYWORD -> SpanStyle(
                        color = theme.keywordColor,
                        fontWeight = FontWeight.Bold
                    )
                    TokenType.CONTROL_FLOW -> SpanStyle(
                        color = theme.controlFlowColor,
                        fontWeight = FontWeight.Bold
                    )
                    TokenType.TYPE -> SpanStyle(
                        color = theme.typeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    TokenType.STRING -> SpanStyle(
                        color = theme.stringColor
                    )
                    TokenType.NUMBER -> SpanStyle(
                        color = theme.numberColor
                    )
                    TokenType.COMMENT -> SpanStyle(
                        color = theme.commentColor,
                        fontStyle = FontStyle.Italic
                    )
                    TokenType.ANNOTATION -> SpanStyle(
                        color = theme.annotationColor,
                        fontWeight = FontWeight.Medium
                    )
                    TokenType.FUNCTION -> SpanStyle(
                        color = theme.functionColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    TokenType.OPERATOR -> SpanStyle(
                        color = theme.operatorColor
                    )
                    TokenType.BOOLEAN_NULL -> SpanStyle(
                        color = theme.booleanNullColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                addStyle(spanStyle, span.start, span.end)
            }
        }
    }

    /**
     * Creates a VisualTransformation for Jetpack Compose TextFields so that
     * user input is colorized in real time without interfering with cursor offset or editing.
     */
    fun createVisualTransformation(
        languageId: String,
        isDark: Boolean = true,
        customKeywords: Set<String> = emptySet()
    ): VisualTransformation {
        return VisualTransformation { text ->
            val highlighted = highlight(
                code = text.text,
                languageId = languageId,
                isDark = isDark,
                customKeywords = customKeywords
            )
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }
}
