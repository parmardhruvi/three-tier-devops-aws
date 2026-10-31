package backend.hobbiebackend.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@RestControllerAdvice
class GlobalAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalAdvice.class);

    @ExceptionHandler({FailToDeleteException.class, NotFoundException.class})
    public ModelAndView handleDBInconsistentException(RuntimeException ex) {
        ModelAndView modelAndView = new ModelAndView("handler");
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(RuntimeException ex) {
        LOGGER.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}

