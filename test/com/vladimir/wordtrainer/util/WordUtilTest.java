package com.vladimir.wordtrainer.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class WordUtilTest {

    @ParameterizedTest(name = "''{0}'' equals ''{1}'' → {2}")
    @CsvSource({
            "cat, cat, true",
            "Cat, cat, true",
            "CAT, CAT, true",
            "the cat, cat, true",
            "a cat, cat, true",
            "an apple, apple, true",
            "to go, go, true",
            "give up, give up, true",
            "cat, dog, false",
            "the cat, the dog, false",
    })
    void equalsIgnorePrepositions_variousCases(String s1, String s2, boolean expected) {
        assertEquals(expected, WordUtil.equalsIgnorePrepositions(s1, s2));
    }

    @Test
    void equalsIgnorePrepositions_bothNull_returnsTrue() {
        assertTrue(WordUtil.equalsIgnorePrepositions(null, null));
    }

    @Test
    void equalsIgnorePrepositions_nullAndEmpty_returnsTrue() {
        assertTrue(WordUtil.equalsIgnorePrepositions(null, ""));
        assertTrue(WordUtil.equalsIgnorePrepositions("", null));
    }

    @Test
    void equalsIgnorePrepositions_emptyVsWord_returnsFalse() {
        assertFalse(WordUtil.equalsIgnorePrepositions("", "cat"));
        assertFalse(WordUtil.equalsIgnorePrepositions("cat", ""));
    }

    @Test
    void equalsIgnorePrepositions_parenthesesRemoved() {
        // "(a) cat" → "a  cat" → split → ["a","cat"] → strip article → "cat"
        assertTrue(WordUtil.equalsIgnorePrepositions("(a) cat", "cat"));
    }

    @Test
    void equalsIgnorePrepositions_articleInMiddle_notStripped() {
        // only leading article is stripped
        assertTrue(WordUtil.equalsIgnorePrepositions("run the risk", "run the risk"));
    }

    @Test
    void equalsIgnorePrepositions_blankString_treatedAsEmpty() {
        assertTrue(WordUtil.equalsIgnorePrepositions("   ", null));
        assertFalse(WordUtil.equalsIgnorePrepositions("   ", "cat"));
    }
}