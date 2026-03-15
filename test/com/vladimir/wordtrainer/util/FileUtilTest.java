package com.vladimir.wordtrainer.util;

import com.vladimir.wordtrainer.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    // ── wrapText ──────────────────────────────────────────────────────────────

    @Test
    void wrapText_null_returnsEmpty() {
        assertEquals("", FileUtil.wrapText(null, 80));
    }

    @Test
    void wrapText_shortText_unchanged() {
        assertEquals("hello world", FileUtil.wrapText("hello world", 80));
    }

    @Test
    void wrapText_textExceedsWidth_insertsNewline() {
        // "aaaaa bbbbb" with width=5: "aaaaa" fits, "bbbbb" triggers wrap
        String result = FileUtil.wrapText("aaaaa bbbbb", 5);
        assertTrue(result.contains("\n"), "Expected newline in wrapped text");
        assertTrue(result.contains("aaaaa"), "First word must be present");
        assertTrue(result.contains("bbbbb"), "Second word must be present");
    }

    @Test
    void wrapText_singleLongWord_noNewline() {
        // A single word longer than width cannot be wrapped — stays on one line
        String result = FileUtil.wrapText("averylongword", 5);
        assertFalse(result.contains("\n"));
    }

    // ── loadDictionaries ──────────────────────────────────────────────────────

    @Test
    void loadDictionaries_parsesValidEntries() {
        Map<String, String> result = FileUtil.loadDictionaries("test_dictionaries.txt");

        assertEquals(2, result.size());
        assertEquals("dictionary/one.txt", result.get("Dict One"));
        assertEquals("dictionary/two.txt", result.get("Dict Two"));
    }

    @Test
    void loadDictionaries_skipsCommentLines() {
        Map<String, String> result = FileUtil.loadDictionaries("test_dictionaries.txt");
        result.forEach((k, v) -> assertFalse(k.startsWith("#")));
    }

    @Test
    void loadDictionaries_skipsInvalidLines() {
        Map<String, String> result = FileUtil.loadDictionaries("test_dictionaries.txt");
        // "invalid_line_without_equals" has no '=' → must be skipped
        assertFalse(result.containsKey("invalid_line_without_equals"));
    }

    @Test
    void loadDictionaries_preservesInsertionOrder() {
        Map<String, String> result = FileUtil.loadDictionaries("test_dictionaries.txt");
        List<String> keys = List.copyOf(result.keySet());
        assertEquals("Dict One", keys.get(0));
        assertEquals("Dict Two", keys.get(1));
    }

    @Test
    void loadDictionaries_missingFile_returnsEmptyMap() {
        Map<String, String> result = FileUtil.loadDictionaries("nonexistent.txt");
        assertTrue(result.isEmpty());
    }

    // ── loadWords ─────────────────────────────────────────────────────────────

    @Test
    void loadWords_parsesValidEntries() {
        List<Word> words = FileUtil.loadWords("test_words.txt");

        assertEquals(2, words.size());
        assertEquals("run", words.get(0).getEnglish());
        assertEquals("бежать", words.get(0).getRussian());
        assertNotNull(words.get(0).getEnglishDescription());

        assertEquals("jump", words.get(1).getEnglish());
        assertEquals("прыгать", words.get(1).getRussian());
    }

    @Test
    void loadWords_skipsCommentAndInvalidLines() {
        List<Word> words = FileUtil.loadWords("test_words.txt");
        // file has 1 comment + 2 valid + 1 invalid = only 2 words expected
        assertEquals(2, words.size());
    }

    @Test
    void loadWords_missingFile_returnsEmptyList() {
        List<Word> words = FileUtil.loadWords("nonexistent.txt");
        assertTrue(words.isEmpty());
    }
}