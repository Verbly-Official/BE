package verbly.spring.domain.correction.ai;

public enum AiProvider {
    OPENAI,
    GEMINI;

    public static AiProvider from(String v) {
        if (v == null) return OPENAI;
        return switch (v.trim().toLowerCase()) {
            case "openai" -> OPENAI;
            case "gemini" -> GEMINI;
            default -> OPENAI;
        };
    }
}
