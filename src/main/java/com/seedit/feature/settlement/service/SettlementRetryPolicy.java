package com.seedit.feature.settlement.service;

import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.sql.SQLException;
import java.time.LocalDateTime;

public final class SettlementRetryPolicy {
    public static final int MAX_ATTEMPTS = 4;
    private static final int[] DELAYS_MINUTES = {1, 5, 15};

    private SettlementRetryPolicy() {}

    public static LocalDateTime nextRetryAt(int attempts, LocalDateTime now) {
        return attempts >= MAX_ATTEMPTS ? null : now.plusMinutes(DELAYS_MINUTES[Math.max(1, attempts) - 1]);
    }

    public static boolean isInfrastructureFailure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof CannotCreateTransactionException
                    || cause instanceof DataAccessResourceFailureException
                    || cause instanceof TransientDataAccessResourceException
                    || cause instanceof RecoverableDataAccessException) return true;
            if (cause instanceof SQLException sql && sql.getSQLState() != null
                    && sql.getSQLState().startsWith("08")) return true;
        }
        return false;
    }

    public static boolean isRetryable(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof PessimisticLockingFailureException) return true;
            if (cause instanceof SQLException sql && (sql.getErrorCode() == 1213
                    || sql.getErrorCode() == 1205 || "40001".equals(sql.getSQLState()))) return true;
        }
        return false;
    }

    public static String errorCode(Throwable error) {
        return isRetryable(error) ? "TRANSIENT_LOCK_FAILURE" : "NON_RETRYABLE_FAILURE";
    }
}
