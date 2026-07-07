package validation;

/** 画面とAPIで共有するつぶやき入力ルールです。 */
public final class MutterInputValidator {
    public static final int MAX_TEXT_LENGTH = 255;
    public static final int MAX_KEYWORD_LENGTH = 100;

    private MutterInputValidator() {
    }

    public static ValidationResult validateText(String text) {
        if (text == null || text.isBlank()) {
            return ValidationResult.invalid("TEXT_REQUIRED", "つぶやき本文を入力してください");
        }
        String normalized = text.trim();
        if (normalized.length() > MAX_TEXT_LENGTH) {
            return ValidationResult.invalid("TEXT_TOO_LONG", "つぶやき本文は255文字以内で入力してください");
        }
        return ValidationResult.valid(normalized);
    }

    /**
     * 検索キーワードとして適切かどうかを確認する。
     */
    public static ValidationResult validateKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return ValidationResult.valid(null);
        }
        String normalized = keyword.trim();
        if (normalized.length() > MAX_KEYWORD_LENGTH) {
            return ValidationResult.invalid("KEYWORD_TOO_LONG", "検索キーワードは100文字以内で入力してください");
        }
        return ValidationResult.valid(normalized);
    }
}
