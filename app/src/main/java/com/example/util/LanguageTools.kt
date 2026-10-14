package com.example.util

import java.util.Locale

object LanguageTools {

    data class ReplacementRule(
        val from: String,
        val to: String,
        val isRegex: Boolean = false
    )

    enum class DigitConversionMode {
        NONE,
        TO_BANGLA,
        TO_WESTERN
    }

    sealed class VoiceCommandResult {
        object None : VoiceCommandResult()
        object NewLine : VoiceCommandResult()
        object NewParagraph : VoiceCommandResult()
        object DeleteLastWord : VoiceCommandResult()
        object Undo : VoiceCommandResult()
        object StopListening : VoiceCommandResult()
    }

    val defaultBanglaReplacements = listOf(
        ReplacementRule(from = "দাঁড়ি", to = "।"),
        ReplacementRule(from = "দাড়ি", to = "।"),
        ReplacementRule(from = "কমা", to = ","),
        ReplacementRule(from = "প্রশ্নবোধক চিহ্ন", to = "?"),
        ReplacementRule(from = "প্রশ্ন চিহ্ন", to = "?"),
        ReplacementRule(from = "বিস্ময়বোধক চিহ্ন", to = "!"),
        ReplacementRule(from = "বিস্ময় চিহ্ন", to = "!"),
        ReplacementRule(from = "হাইফেন", to = "-"),
        ReplacementRule(from = "কলন", to = ":"),
        ReplacementRule(from = "সেমিকলন", to = ";")
    )

    fun rulesFromJson(json: String): List<ReplacementRule> {
        if (json.isBlank()) return defaultBanglaReplacements
        return try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<ReplacementRule>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val from = obj.optString("from", "")
                val to = obj.optString("to", "")
                val isRegex = obj.optBoolean("isRegex", false)
                if (from.isNotBlank()) {
                    list.add(ReplacementRule(from, to, isRegex))
                }
            }
            // A valid JSON array is authoritative. In particular, [] means the user
            // intentionally removed every replacement rule and must not be replaced
            // with the defaults on the next launch.
            list
        } catch (e: Exception) {
            defaultBanglaReplacements
        }
    }

    fun rulesToJson(rules: List<ReplacementRule>): String {
        val array = org.json.JSONArray()
        for (r in rules) {
            val obj = org.json.JSONObject()
            obj.put("from", r.from)
            obj.put("to", r.to)
            obj.put("isRegex", r.isRegex)
            array.put(obj)
        }
        return array.toString()
    }

    private val banglaToWesternDigits = mapOf(
        '০' to '0', '১' to '1', '২' to '2', '৩' to '3', '৪' to '4',
        '৫' to '5', '৬' to '6', '৭' to '7', '৮' to '8', '৯' to '9'
    )

    private val westernToBanglaDigits = mapOf(
        '0' to '০', '1' to '১', '2' to '২', '3' to '৩', '4' to '৪',
        '5' to '৫', '6' to '৬', '7' to '৭', '8' to '৮', '9' to '৯'
    )

    fun checkVoiceCommand(rawUtterance: String, isBangla: Boolean): VoiceCommandResult {
        val trimmed = rawUtterance.trim().lowercase(Locale.ROOT)
        return when {
            trimmed == "নতুন লাইন" || trimmed == "new line" -> VoiceCommandResult.NewLine
            trimmed == "নতুন প্যারা" || trimmed == "new paragraph" -> VoiceCommandResult.NewParagraph
            trimmed == "শব্দ মুছুন" || trimmed == "delete word" || trimmed == "delete last word" -> VoiceCommandResult.DeleteLastWord
            trimmed == "আনডু" || trimmed == "পূর্বাবস্থায় ফেরান" || trimmed == "undo" -> VoiceCommandResult.Undo
            trimmed == "শোনা বন্ধ" || trimmed == "শোনা বন্ধ করুন" || trimmed == "stop listening" -> VoiceCommandResult.StopListening
            else -> VoiceCommandResult.None
        }
    }

    fun applyReplacements(
        text: String,
        rules: List<ReplacementRule>
    ): String {
        var result = text
        for (rule in rules) {
            if (rule.from.isBlank()) continue
            try {
                result = if (rule.isRegex) {
                    result.replace(Regex(rule.from, RegexOption.IGNORE_CASE), rule.to)
                } else {
                    result.replace(rule.from, rule.to, ignoreCase = true)
                }
            } catch (e: Exception) {
                // If regex syntax was invalid, skip safely
            }
        }
        return result
    }

    fun convertDigits(text: String, mode: DigitConversionMode): String {
        return when (mode) {
            DigitConversionMode.NONE -> text
            DigitConversionMode.TO_BANGLA -> {
                val sb = java.lang.StringBuilder(text.length)
                for (ch in text) {
                    sb.append(westernToBanglaDigits[ch] ?: ch)
                }
                sb.toString()
            }
            DigitConversionMode.TO_WESTERN -> {
                val sb = java.lang.StringBuilder(text.length)
                for (ch in text) {
                    sb.append(banglaToWesternDigits[ch] ?: ch)
                }
                sb.toString()
            }
        }
    }

    fun applyAutoPunctuation(
        text: String,
        languageCode: String,
        autoPunctuate: Boolean
    ): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return text
        if (!autoPunctuate) return fixPunctuationSpacing(trimmed)

        val lastChar = trimmed.last()
        val isBangla = languageCode.startsWith("bn", ignoreCase = true)

        val hasTerminalPunctuation = when (lastChar) {
            '।', '.', '?', '!', ';', ':', '…' -> true
            else -> false
        }

        val punctText = if (!hasTerminalPunctuation) {
            if (isBangla) "$trimmed।" else "$trimmed."
        } else {
            trimmed
        }

        return fixPunctuationSpacing(punctText)
    }

    fun fixPunctuationSpacing(text: String): String {
        var formatted = text
        // Ensure no whitespace before punctuation marks
        formatted = formatted.replace(Regex("\\s+([।.,?!;:।])"), "$1")
        // Ensure single space after punctuation if followed by word character
        formatted = formatted.replace(Regex("([।.,?!;:।])(\\S)"), "$1 $2")
        return formatted
    }

    fun autoCapitalizeLatin(text: String, languageCode: String): String {
        if (languageCode.startsWith("bn", ignoreCase = true)) return text
        if (text.isEmpty()) return text

        val sb = StringBuilder()
        var capitalizeNext = true
        for (ch in text) {
            if (capitalizeNext && ch.isLetter()) {
                sb.append(ch.uppercaseChar())
                capitalizeNext = false
            } else {
                sb.append(ch)
                if (ch == '.' || ch == '?' || ch == '!') {
                    capitalizeNext = true
                }
            }
        }
        return sb.toString()
    }

    fun formatFinalUtterance(
        rawText: String,
        languageCode: String,
        autoPunctuate: Boolean,
        digitMode: DigitConversionMode,
        autoCapLatin: Boolean,
        replacements: List<ReplacementRule>
    ): String {
        var processed = rawText
        processed = applyReplacements(processed, rules = replacements)
        processed = convertDigits(processed, digitMode)
        if (autoCapLatin) {
            processed = autoCapitalizeLatin(processed, languageCode)
        }
        processed = applyAutoPunctuation(processed, languageCode, autoPunctuate)
        return processed
    }
}
