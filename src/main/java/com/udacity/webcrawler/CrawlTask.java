package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

/**
 * A task that downloads one URL, counts its words, and then starts a new task for every link on
 * that page. This is the parallel version of the crawlInternal() method in SequentialWebCrawler.
 *
 * <p>We build these tasks with the nested {@link Builder} class so we don't have to pass a long
 * list of arguments to the constructor every time.
 */
final class CrawlTask extends RecursiveAction {
  private final Clock clock;
  private final PageParserFactory parserFactory;
  private final List<Pattern> ignoredUrls;
  private final Instant deadline;
  private final int maxDepth;
  private final String url;
  private final ConcurrentMap<String, Integer> counts;
  private final Set<String> visitedUrls;

  private CrawlTask(
      Clock clock,
      PageParserFactory parserFactory,
      List<Pattern> ignoredUrls,
      Instant deadline,
      int maxDepth,
      String url,
      ConcurrentMap<String, Integer> counts,
      Set<String> visitedUrls) {
    this.clock = clock;
    this.parserFactory = parserFactory;
    this.ignoredUrls = ignoredUrls;
    this.deadline = deadline;
    this.maxDepth = maxDepth;
    this.url = url;
    this.counts = counts;
    this.visitedUrls = visitedUrls;
  }

  @Override
  protected void compute() {
    // Stop if we have gone too deep or if we are out of time.
    if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
      return;
    }
    for (Pattern pattern : ignoredUrls) {
      if (pattern.matcher(url).matches()) {
        return;
      }
    }
    // add() returns false if some other thread already added this URL. That check is safe to do
    // from many threads at once, so each page is only visited and counted one time.
    if (!visitedUrls.add(url)) {
      return;
    }

    PageParser.Result result = parserFactory.get(url).parse();

    // Add this page's word counts into the shared map. merge() is safe to call from many threads.
    for (Map.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
      counts.merge(e.getKey(), e.getValue(), Integer::sum);
    }

    // Make a new task for each link and run them all, then wait for them to finish.
    List<CrawlTask> subtasks = new ArrayList<>();
    for (String link : result.getLinks()) {
      subtasks.add(
          new Builder()
              .setClock(clock)
              .setParserFactory(parserFactory)
              .setIgnoredUrls(ignoredUrls)
              .setDeadline(deadline)
              .setMaxDepth(maxDepth - 1)
              .setUrl(link)
              .setCounts(counts)
              .setVisitedUrls(visitedUrls)
              .build());
    }
    invokeAll(subtasks);
  }

  /**
   * Builder for CrawlTask. This lets us set each value by name instead of remembering the order of
   * a long list of constructor arguments.
   */
  static final class Builder {
    private Clock clock;
    private PageParserFactory parserFactory;
    private List<Pattern> ignoredUrls;
    private Instant deadline;
    private int maxDepth;
    private String url;
    private ConcurrentMap<String, Integer> counts;
    private Set<String> visitedUrls;

    Builder setClock(Clock clock) {
      this.clock = clock;
      return this;
    }

    Builder setParserFactory(PageParserFactory parserFactory) {
      this.parserFactory = parserFactory;
      return this;
    }

    Builder setIgnoredUrls(List<Pattern> ignoredUrls) {
      this.ignoredUrls = ignoredUrls;
      return this;
    }

    Builder setDeadline(Instant deadline) {
      this.deadline = deadline;
      return this;
    }

    Builder setMaxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    Builder setUrl(String url) {
      this.url = url;
      return this;
    }

    Builder setCounts(ConcurrentMap<String, Integer> counts) {
      this.counts = counts;
      return this;
    }

    Builder setVisitedUrls(Set<String> visitedUrls) {
      this.visitedUrls = visitedUrls;
      return this;
    }

    CrawlTask build() {
      return new CrawlTask(
          Objects.requireNonNull(clock),
          Objects.requireNonNull(parserFactory),
          Objects.requireNonNull(ignoredUrls),
          Objects.requireNonNull(deadline),
          maxDepth,
          Objects.requireNonNull(url),
          Objects.requireNonNull(counts),
          Objects.requireNonNull(visitedUrls));
    }
  }
}
