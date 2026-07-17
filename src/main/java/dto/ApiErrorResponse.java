package dto;

/** REST APIのエラーを一定のJSON形式で返します。 */
public record ApiErrorResponse(int status, String code, String message) {
}