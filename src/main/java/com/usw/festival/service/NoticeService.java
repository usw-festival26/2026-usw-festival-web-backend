package com.usw.festival.service;

import com.usw.festival.dto.notice.NoticeDetailResponse;
import com.usw.festival.dto.notice.NoticeResponse;
import com.usw.festival.dto.notice.NoticeSaveRequest;
import com.usw.festival.entity.Notice;
import com.usw.festival.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<NoticeResponse> getNotices() {
        return noticeRepository.findAllByOrderByPinnedDescCreatedAtDesc()
                .stream()
                .map(NoticeResponse::from)
                .toList();
    }

    @Transactional
    public NoticeDetailResponse createNotice(NoticeSaveRequest request) {
        Notice notice = noticeRepository.save(new Notice(request.title(), request.content(), request.pinned()));
        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public NoticeDetailResponse updateNotice(Long id, NoticeSaveRequest request) {
        Notice notice = findNotice(id);
        notice.update(request.title(), request.content(), request.pinned());
        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = findNotice(id);
        noticeRepository.delete(notice);
    }

    private Notice findNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 공지사항입니다. id=" + id));
    }
}
