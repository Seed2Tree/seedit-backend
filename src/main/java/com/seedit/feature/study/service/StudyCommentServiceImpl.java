package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.request.StudyCommentRequest;
import com.seedit.feature.study.dto.response.StudyCommentResponse;
import com.seedit.feature.study.repository.StudyCommentRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyCommentServiceImpl implements StudyCommentService {

    private final StudyCommentRepository commentRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    public List<StudyCommentResponse> getComments(Long isid, String email) {
        Long userId = getUserId(email);
        return commentRepository.findByIsid(isid, userId);
    }

    @Override
    public void addComment(Long isid, String email, StudyCommentRequest request) {
        Long userId = getUserId(email);
        commentRepository.insert(isid, userId, request.getContent());
    }

    @Override
    public void updateComment(Long scid, String email, StudyCommentRequest request) {
        Long userId = getUserId(email);
        checkOwnership(scid, userId);
        commentRepository.update(scid, request.getContent());
    }

    @Override
    public void deleteComment(Long scid, String email) {
        Long userId = getUserId(email);
        checkOwnership(scid, userId);
        commentRepository.delete(scid);
    }

    private Long getUserId(String email) {
        return userAccountRepository.findUserByEmail(email)
                .map(UserAccount::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private void checkOwnership(Long scid, Long userId) {
        Long ownerUserId = commentRepository.findOwnerUserId(scid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!ownerUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "본인 댓글만 수정/삭제할 수 있습니다.");
        }
    }
}
