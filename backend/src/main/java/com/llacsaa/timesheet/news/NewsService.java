package com.llacsaa.timesheet.news;

import com.llacsaa.timesheet.common.NameResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.llacsaa.timesheet.common.PilotContext.CODECOMPANY;
import static com.llacsaa.timesheet.common.PilotContext.CODEINSTANCE;
import com.llacsaa.timesheet.auth.AuthContext;

@Service
public class NewsService {

    public static final String NEWSTYPEIMPACTCAT = "PRJ_NEWTYPEIMPACTCAT";
    public static final String NEWSTATUSCAT = "PRJ_NEWSTATUSCAT";
    private static final String DEFAULT_STATUS = "ABI";

    private final NewsRepository newsRepository;
    private final NewsHisRepository newsHisRepository;
    private final NameResolver nameResolver;

    public NewsService(NewsRepository newsRepository, NewsHisRepository newsHisRepository, NameResolver nameResolver) {
        this.newsRepository = newsRepository;
        this.newsHisRepository = newsHisRepository;
        this.nameResolver = nameResolver;
    }

    public List<NewsView> listByProject(Long seqproject) {
        return newsRepository.findBySeqprojectOrderByDatenewarrivalDesc(seqproject)
                .stream().map(this::toView).collect(Collectors.toList());
    }

    @Transactional
    public NewsView create(Long seqproject, NewsRequest request) {
        TprjProjectNews n = new TprjProjectNews();
        n.setCodeinstance(CODEINSTANCE);
        n.setCodecompany(CODECOMPANY);
        n.setSeqproject(seqproject);
        applyRequest(n, request);
        if (n.getNewstatus() == null) {
            n.setNewstatus(DEFAULT_STATUS);
        }
        n.setUsercreate(AuthContext.currentUserCode());
        n.setUserlastmodify(AuthContext.currentUserCode());
        n.setDatecreate(LocalDateTime.now());
        n.setDatemodify(LocalDateTime.now());

        TprjProjectNews saved = newsRepository.save(n);
        newsHisRepository.save(TprjProjectNewsHis.snapshotOf(saved, "NEW", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public NewsView update(Long seqproject, Long seqNews, NewsRequest request) {
        TprjProjectNews n = findOrThrow(seqproject, seqNews);
        applyRequest(n, request);
        n.setUserlastmodify(AuthContext.currentUserCode());
        n.setDatemodify(LocalDateTime.now());

        TprjProjectNews saved = newsRepository.save(n);
        newsHisRepository.save(TprjProjectNewsHis.snapshotOf(saved, "UPDATE", AuthContext.currentUserCode()));
        return toView(saved);
    }

    @Transactional
    public void delete(Long seqproject, Long seqNews) {
        TprjProjectNews n = findOrThrow(seqproject, seqNews);
        newsHisRepository.save(TprjProjectNewsHis.snapshotOf(n, "DELETE", AuthContext.currentUserCode()));
        newsRepository.delete(n);
    }

    private TprjProjectNews findOrThrow(Long seqproject, Long seqNews) {
        return newsRepository.findById(seqNews)
                .filter(n -> n.getSeqproject().equals(seqproject))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Novedad no encontrada"));
    }

    private void applyRequest(TprjProjectNews n, NewsRequest request) {
        n.setDatenewarrival(request.getDatenewarrival());
        n.setNewstypeimpactcat(NEWSTYPEIMPACTCAT);
        n.setNewstypeimpact(request.getNewstypeimpact());
        n.setDescriptionnews(request.getDescriptionnews());
        n.setPersonreporting(request.getPersonreporting());
        n.setAffectation(request.getAffectation());
        n.setPersonincharge(request.getPersonincharge());
        n.setCompany(request.getCompany());
        n.setSolution(request.getSolution());
        n.setDatesolution(request.getDatesolution());
        n.setDaterealsolution(request.getDaterealsolution());
        n.setNewstatuscat(NEWSTATUSCAT);
        if (request.getNewstatus() != null) {
            n.setNewstatus(request.getNewstatus());
        }
    }

    private NewsView toView(TprjProjectNews n) {
        return new NewsView(
                n.getSeqNews(),
                n.getDatenewarrival(),
                n.getNewstypeimpactcat(),
                n.getNewstypeimpact(),
                nameResolver.catalogItemName(n.getNewstypeimpactcat(), n.getNewstypeimpact()),
                n.getDescriptionnews(),
                n.getPersonreporting(),
                n.getAffectation(),
                n.getPersonincharge(),
                n.getCompany(),
                n.getSolution(),
                n.getDatesolution(),
                n.getDaterealsolution(),
                n.getNewstatuscat(),
                n.getNewstatus(),
                nameResolver.catalogItemName(n.getNewstatuscat(), n.getNewstatus())
        );
    }
}
