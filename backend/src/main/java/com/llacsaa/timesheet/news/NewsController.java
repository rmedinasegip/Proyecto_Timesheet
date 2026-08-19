package com.llacsaa.timesheet.news;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{seqproject}/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsView> list(@PathVariable Long seqproject) {
        return newsService.listByProject(seqproject);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewsView create(@PathVariable Long seqproject, @RequestBody NewsRequest request) {
        return newsService.create(seqproject, request);
    }

    @PutMapping("/{seqNews}")
    public NewsView update(@PathVariable Long seqproject, @PathVariable Long seqNews, @RequestBody NewsRequest request) {
        return newsService.update(seqproject, seqNews, request);
    }

    @DeleteMapping("/{seqNews}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long seqproject, @PathVariable Long seqNews) {
        newsService.delete(seqproject, seqNews);
    }
}
