package verbly.spring.domain.library.exception;

import verbly.spring.global.common.exception.BaseException;

public class LibraryHandler extends BaseException {
    public LibraryHandler(LibraryErrorStatus code) {
        super(code);
    }
}
