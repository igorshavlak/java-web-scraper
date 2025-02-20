package com.webscraper.exceptions;

/**
 * Exception thrown when a non-retryable error occurs.
 * This exception indicates that the error is critical and retrying the operation is not advisable.
 */
public class NonRetryableException extends RuntimeException {

  /**
   * Constructs a new NonRetryableException with the specified detail message and cause.
   *
   * @param message detailed message about the error
   * @param cause   the underlying cause of the exception
   */
  public NonRetryableException(String message, Throwable cause) {
    super(message, cause);
  }
}