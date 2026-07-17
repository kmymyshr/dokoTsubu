package validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MutterInputValidatorTest {
    @Test
    void trimsValidText() {
        ValidationResult result = MutterInputValidator.validateText("  hello  ");
        assertTrue(result.valid());
        assertEquals("hello", result.value());
    }

    @Test
    void rejectsBlankText() {
        ValidationResult result = MutterInputValidator.validateText("   ");
        assertFalse(result.valid());
        assertEquals("TEXT_REQUIRED", result.code());
    }

    @Test
    void rejectsTextLongerThanDatabaseColumn() {
        ValidationResult result = MutterInputValidator.validateText("a".repeat(256));
        assertFalse(result.valid());
        assertEquals("TEXT_TOO_LONG", result.code());
    }

    @Test
    void treatsBlankKeywordAsNoFilter() {
        ValidationResult result = MutterInputValidator.validateKeyword("  ");
        assertTrue(result.valid());
        assertNull(result.value());
    }

    @Test
    void rejectsTooLongKeyword() {
        ValidationResult result = MutterInputValidator.validateKeyword("a".repeat(101));
        assertFalse(result.valid());
        assertEquals("KEYWORD_TOO_LONG", result.code());
    }
}
