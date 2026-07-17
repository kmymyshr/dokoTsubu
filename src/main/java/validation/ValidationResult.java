package validation;

/**
 * 入力チェックの結果を表すクラスです。
 * 正常なら値を、失敗ならエラーコードとメッセージを持ちます。
 */
public record ValidationResult(boolean valid, String value, String code, String message) {
    public static ValidationResult valid(String value) {
        return new ValidationResult(true, value, null, null);
    }

    public static ValidationResult invalid(String code, String message) {
        return new ValidationResult(false, null, code, message);
    }
}
