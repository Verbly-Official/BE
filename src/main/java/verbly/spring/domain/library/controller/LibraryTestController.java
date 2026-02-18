package verbly.spring.domain.library.controller;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.library.dto.request.LibraryAiRequest;
import verbly.spring.domain.library.dto.response.LibraryAiDTO;
import verbly.spring.domain.library.service.LibraryAiService;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryTestController {

    private final LibraryAiService libraryAiService;

    @PostMapping("/ai-test")
    public ResponseEntity<LibraryAiDTO> getLearningPoint(@RequestBody LibraryAiRequest request) {

        LibraryAiDTO result = libraryAiService.createLearningPoint(
                request.getOriginal(),
                request.getRevised()
        );

        return ResponseEntity.ok(result);
    }
}
