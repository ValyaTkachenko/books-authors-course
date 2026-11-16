package com.booksauthors.testsupport;

import com.booksauthors.domain.model.Book;
import com.booksauthors.domain.model.SearchHit;
import com.booksauthors.domain.model.SearchResult;
import com.booksauthors.domain.port.out.SearchIndexPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** In-memory пошуковий індекс: документ знайдено, якщо в назві/анотації є всі слова запиту. */
public class FakeSearchIndex implements SearchIndexPort {

    public final List<Book> docs = new ArrayList<>();
    public int searchCalls = 0;
    public boolean failOnIndex = false;

    @Override
    public int indexAll(List<Book> books) {
        if (failOnIndex) throw new IllegalStateException("index down");
        docs.addAll(books);
        return 0;
    }

    @Override
    public SearchResult search(String normalizedQuery, int size) {
        searchCalls++;
        List<SearchHit> hits = new ArrayList<>();
        for (Book b : docs) {
            String text = ((b.getTitle() == null ? "" : b.getTitle()) + " "
                    + (b.getSynopsis() == null ? "" : b.getSynopsis())).toLowerCase(Locale.ROOT);
            boolean all = true;
            for (String token : normalizedQuery.split(" ")) {
                if (!text.contains(token)) { all = false; break; }
            }
            if (all) hits.add(new SearchHit(b, null, List.of()));
        }
        return new SearchResult(hits.size(), hits);
    }

    @Override
    public long count() { return docs.size(); }
}
