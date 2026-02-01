package verbly.spring.domain.correction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import verbly.spring.domain.correction.dto.response.CorrectionResponseDTO;
import verbly.spring.domain.correction.exception.CorrectionHandler;
import verbly.spring.domain.correction.repository.CorrectionQueryRepository;
import verbly.spring.domain.user.entity.User;
import verbly.spring.global.common.code.ErrorStatus;
import verbly.spring.global.security.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class CorrectionNativeService {
    private static final int MAX_SIZE = 50;

    private final CorrectionQueryRepository correctionQueryRepository;

    public Page<CorrectionResponseDTO.MyCorrectionDto> getNativeCorrectionRequests(Pageable pageable) {
        validateNativeAccess();

        Pageable safePageable = normalize(pageable);
        return correctionQueryRepository.findNativeCorrectionRequests(safePageable);
    }

    private Pageable normalize(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), MAX_SIZE);

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "id");

        return PageRequest.of(page, size, sort);
    }

    private void validateNativeAccess() {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser == null || !"en".equalsIgnoreCase(currentUser.getNativeLang())) {
            throw new CorrectionHandler(ErrorStatus.CORRECTION_NATIVE_ACCESS_DENIED);
        }
    }
}
