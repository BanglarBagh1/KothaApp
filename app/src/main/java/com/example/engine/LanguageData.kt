package com.example.engine

data class LanguageOption(
    val bcp47: String,
    val displayName: String,
    val nativeName: String
)

object LanguageData {

    val curatedLanguages = listOf(
        // Pinned Bangla variants
        LanguageOption("bn-BD", "Bangla (Bangladesh)", "বাংলা (বাংলাদেশ)"),
        LanguageOption("bn-IN", "Bangla (India)", "বাংলা (ভারত)"),

        // Major World & Regional Languages
        LanguageOption("en-US", "English (United States)", "English (US)"),
        LanguageOption("en-GB", "English (United Kingdom)", "English (UK)"),
        LanguageOption("en-IN", "English (India)", "English (India)"),
        LanguageOption("hi-IN", "Hindi (India)", "हिन्दी"),
        LanguageOption("ur-PK", "Urdu (Pakistan)", "اردو"),
        LanguageOption("ar-SA", "Arabic (Saudi Arabia)", "العربية"),
        LanguageOption("es-ES", "Spanish (Spain)", "Español"),
        LanguageOption("es-US", "Spanish (United States)", "Español (EE.UU.)"),
        LanguageOption("fr-FR", "French (France)", "Français"),
        LanguageOption("de-DE", "German (Germany)", "Deutsch"),
        LanguageOption("pt-BR", "Portuguese (Brazil)", "Português"),
        LanguageOption("ru-RU", "Russian (Russia)", "Русский"),
        LanguageOption("zh-CN", "Chinese (Simplified)", "中文 (简体)"),
        LanguageOption("zh-TW", "Chinese (Traditional)", "中文 (繁體)"),
        LanguageOption("ja-JP", "Japanese (Japan)", "日本語"),
        LanguageOption("ko-KR", "Korean (South Korea)", "한국어"),
        LanguageOption("it-IT", "Italian (Italy)", "Italiano"),
        LanguageOption("nl-NL", "Dutch (Netherlands)", "Nederlands"),
        LanguageOption("tr-TR", "Turkish (Turkey)", "Türkçe"),
        LanguageOption("fa-IR", "Persian (Iran)", "فارسی"),
        LanguageOption("id-ID", "Indonesian (Indonesia)", "Bahasa Indonesia"),
        LanguageOption("ms-MY", "Malay (Malaysia)", "Bahasa Melayu"),
        LanguageOption("th-TH", "Thai (Thailand)", "ไทย"),
        LanguageOption("vi-VN", "Vietnamese (Vietnam)", "Tiếng Việt"),
        LanguageOption("ta-IN", "Tamil (India)", "தமிழ்"),
        LanguageOption("te-IN", "Telugu (India)", "తెలుగు"),
        LanguageOption("mr-IN", "Marathi (India)", "मराठी"),
        LanguageOption("gu-IN", "Gujarati (India)", "ગુજરાતી"),
        LanguageOption("kn-IN", "Kannada (India)", "ಕನ್ನಡ"),
        LanguageOption("ml-IN", "Malayalam (India)", "മലയാളം"),
        LanguageOption("pa-IN", "Punjabi (India)", "ਪੰਜਾਬੀ"),
        LanguageOption("ne-NP", "Nepali (Nepal)", "नेपाली"),
        LanguageOption("si-LK", "Sinhala (Sri Lanka)", "සිංහල"),
        LanguageOption("my-MM", "Burmese (Myanmar)", "မြန်မာ"),
        LanguageOption("pl-PL", "Polish (Poland)", "Polski"),
        LanguageOption("uk-UA", "Ukrainian (Ukraine)", "Українська"),
        LanguageOption("el-GR", "Greek (Greece)", "Ελληνικά"),
        LanguageOption("sv-SE", "Swedish (Sweden)", "Svenska"),
        LanguageOption("da-DK", "Danish (Denmark)", "Dansk"),
        LanguageOption("no-NO", "Norwegian (Norway)", "Norsk"),
        LanguageOption("fi-FI", "Finnish (Finland)", "Suomi"),
        LanguageOption("cs-CZ", "Czech (Czechia)", "Čeština"),
        LanguageOption("ro-RO", "Romanian (Romania)", "Română"),
        LanguageOption("hu-HU", "Hungarian (Hungary)", "Magyar"),
        LanguageOption("he-IL", "Hebrew (Israel)", "עברית"),
        LanguageOption("sw-KE", "Swahili (Kenya)", "Kiswahili"),
        LanguageOption("am-ET", "Amharic (Ethiopia)", "አማርኛ"),
        LanguageOption("fil-PH", "Filipino (Philippines)", "Filipino")
    )

    fun getDisplayName(bcp47: String): String {
        return curatedLanguages.find { it.bcp47.equals(bcp47, ignoreCase = true) }?.let {
            "${it.displayName} • ${it.nativeName}"
        } ?: bcp47
    }
}
