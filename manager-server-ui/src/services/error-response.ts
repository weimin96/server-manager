class ErrorResponse {
  public readonly error: string;
  public readonly message: string;

  constructor({ error, message }) {
    this.error = error;
    this.message = message;
  }
}

export default ErrorResponse;
