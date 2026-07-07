package validation;

/** 正規化済み入力、またはエラーコードとメッセージを表します。 */
public record ValidationResult(boolean valid, String value, String code, String message) {
    public static ValidationResult valid(String value) {
        return new ValidationResult(true, value, null, null);
    }

    public static ValidationResult invalid(String code, String message) {
        return new ValidationResult(false, null, code, message);
    }
}
