package com.booksauthors.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QueryNormalizerTest {

    private final QueryNormalizer n = new QueryNormalizer();

    @Test
    void reorderedWordsGiveSameKey() {
        assertEquals(n.normalize("green energy"), n.normalize("energy green"));
    }

    @Test
    void caseAndPunctuationAreIgnored() {
        assertEquals("energy green", n.normalize("  Green,  ENERGY! "));
    }

    @Test
    void duplicatesAreCollapsed() {
        assertEquals("book harry", n.normalize("harry book harry"));
    }

    @Test
    void nullAndBlankGiveEmptyKey() {
        assertEquals("", n.normalize(null));
        assertEquals("", n.normalize("  "));
    }

    @Test
    void cyrillicIsSupported() {
        assertEquals(n.normalize("зелена енергетика"), n.normalize("Енергетика зелена"));
    }
}
