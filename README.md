# Parallel Web Crawler

A multi-threaded web crawler written in Java. You give it a starting web page and it visits
that page, counts the words on it, follows the links it finds, and keeps going until it reaches
a depth or time limit you set. When it finishes it reports the most popular words it saw and how
many pages it visited.

The main goal of the project is to take a slow crawler that reads one page at a time and turn it
into a fast one that reads many pages at the same time using multiple CPU cores, without ever
counting the same page or the same URL twice.

## Features

- Reads its settings from a simple JSON configuration file.
- Crawls many pages in parallel using a `ForkJoinPool`.
- Tracks visited URLs and word counts with thread-safe data structures so nothing is double counted.
- Stops crawling once it hits the configured depth or time limit.
- Writes the crawl results to a JSON file (or prints them to the screen).
- Includes a small performance profiler that measures how long methods take and saves that to a
  text file.

## Built With

- Java 17
- Maven (build tool and dependency management)
- [Jackson](https://github.com/FasterXML/jackson) for reading and writing JSON
- [jsoup](https://jsoup.org/) for parsing HTML
- [Guice](https://github.com/google/guice) for dependency injection
- [JUnit 5](https://junit.org/junit5/) and [Truth](https://truth.dev/) for the tests

## Requirements

- Java JDK 17
- Maven 3.6.3 or higher

## Getting Started

Clone the repository and move into the project folder:

```
git clone <your-repo-url>
cd parallel-web-crawler
```

### Run the tests

```
mvn test
```

You should see all of the tests pass with a `BUILD SUCCESS` message at the end.

### Build the project

```
mvn package
```

This creates a runnable jar at `target/udacity-webcrawler-1.0.jar`.

### Run the crawler

```
java -cp target/udacity-webcrawler-1.0.jar com.udacity.webcrawler.main.WebCrawlerMain src/main/java/com/udacity/webcrawler/main/config/sample_config.json
```

The crawl result is printed as JSON, for example:

```
{"wordCounts":{"library":58,"books":34,"book":28,"open":28},"urlsVisited":5}
```

A file called `profileData.txt` is also created, showing how long the main steps took.

## Configuration

The crawler is controlled by a JSON file. Here is what each setting means:

- `startPages` - the URLs where the crawl begins.
- `ignoredUrls` - a list of patterns for URLs the crawler should skip.
- `ignoredWords` - a list of patterns for words that should not be counted.
- `parallelism` - how many threads to use. If set to 1 the sequential crawler is used.
- `implementationOverride` - force a specific crawler implementation by class name.
- `maxDepth` - how many links deep the crawler is allowed to go from the start pages.
- `timeoutSeconds` - the maximum time the crawler is allowed to run.
- `popularWordCount` - how many of the top words to include in the result.
- `profileOutputPath` - where to save the performance data. Empty means print to the screen.
- `resultPath` - where to save the crawl result. Empty means print to the screen.

You can edit `src/main/java/com/udacity/webcrawler/main/config/sample_config.json` to crawl a
different website or change the limits, then run the crawler again to see new results.

## Project Structure

- `json/` - loading the configuration file and writing the crawl result.
- `parser/` - downloading and parsing HTML pages.
- `profiler/` - the performance profiler built with a dynamic proxy.
- `ParallelWebCrawler.java` and `CrawlTask.java` - the parallel crawler and its recursive task.
- `SequentialWebCrawler.java` - the original one-page-at-a-time crawler.
- `WordCounts.java` - sorts the word counts and keeps only the most popular ones.
- `main/` - the entry point that ties everything together.
