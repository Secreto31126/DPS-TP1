package edu.itba.exchange.exceptions;

public record ApiError(ApiErrorCategory category, String message) {
    private final static int CLIENT_ERROR_CLASS = 4;

    public static ApiError networkError(final String message) {
        return new ApiError(ApiErrorCategory.NETWORK_ERROR, message);
    }
    public static ApiError fromHttpStatus(final int status, final String message) {
        int statusClass = status / 100;
        final ApiErrorCategory category = statusClass == CLIENT_ERROR_CLASS ? ApiErrorCategory.CLIENT_ERROR : ApiErrorCategory.SERVER_ERROR;
        final String formattedMsg = String.format("(Status code %d) %s", status, message);
        return new ApiError(category, formattedMsg);
    }
}
