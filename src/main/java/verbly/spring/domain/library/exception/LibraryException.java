package verbly.spring.domain.library.exception;

import verbly.spring.global.common.exception.BaseException;

public class LibraryException extends BaseException {
    public LibraryException(LibraryErrorStatus code) {
        super(code);
    }
}
