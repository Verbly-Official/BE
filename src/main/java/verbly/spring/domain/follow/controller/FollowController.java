package verbly.spring.domain.follow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import verbly.spring.domain.follow.service.FollowService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/follows")
public class FollowController {

    private final FollowService followService;

}
