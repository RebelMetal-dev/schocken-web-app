package de.rebelmetal.schockenwebapp.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Hybrid exception handler for both REST and HTMX clients.
 *
 * Routing logic:
 *   HTMX request (HX-Request: true header present)
 *     → returns a Thymeleaf ModelAndView (error-alert fragment)
 *       so HTMX can swap it into the page without a full reload.
 *   REST / plain browser request (no HX-Request header)
 *     → returns a ResponseEntity with a JSON body and the appropriate HTTP status.
 *
 * Why @ControllerAdvice instead of @RestControllerAdvice:
 *   @RestControllerAdvice adds @ResponseBody to every handler method, which forces
 *   JSON serialization on all return values. That makes it impossible to return a
 *   ModelAndView for Thymeleaf rendering. @ControllerAdvice leaves the return type
 *   open: Spring MVC picks the right handler based on the actual runtime type
 *   (ModelAndView → Thymeleaf, ResponseEntity → Jackson).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // Fragment path returned to HTMX clients on any error.
    private static final String ERROR_FRAGMENT = "fragments/error-alert :: error-alert";

    @ExceptionHandler(IllegalStateException.class)
    public Object handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        if (isHtmxRequest(request)) {
            return errorFragment(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    public Object handlePlayerNotFound(PlayerNotFoundException ex, HttpServletRequest request) {
        if (isHtmxRequest(request)) {
            return errorFragment(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Object handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        if (isHtmxRequest(request)) {
            return errorFragment(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // --- Private helpers ---

    /**
     * Returns true when the incoming request carries the HTMX sentinel header.
     * HTMX sets "HX-Request: true" on every request it issues — this is reliable
     * and does not depend on Accept headers or URL patterns.
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }

    /**
     * Builds a ModelAndView for the error-alert Thymeleaf fragment.
     * The errorMessage attribute is rendered inside the fragment via th:text.
     */
    private ModelAndView errorFragment(String message) {
        ModelAndView mav = new ModelAndView(ERROR_FRAGMENT);
        mav.addObject("errorMessage", message);
        return mav;
    }
}
