package dto;

/** つぶやきの作成・更新で受け取るJSON。更新時はversionが必須です。 */
public record MutterWriteRequest(String text, Integer version) {
}