package com.gripe.megacells.misc;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Mutable non-negative amount that keeps its value in a primitive long and
 * switches to {@link BigInteger} only when an operation would exceed the long
 * range. Arithmetic mutates the instance in place and returns it, so long-lived
 * fields can be reused across operations without allocating.
 * <p>
 * Methods taking a {@link UnitAmount} argument only read it; they never mutate
 * the argument.
 */
public final class UnitAmount {
    private long small;
    @Nullable
    private BigInteger large;

    public UnitAmount() {
    }

    public UnitAmount init(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Unit amounts must be non-negative");
        }
        small = value;
        large = null;
        return this;
    }

    public UnitAmount init(UnitAmount other) {
        small = other.small;
        large = other.large;
        return this;
    }

    public UnitAmount init(BigInteger value) {
        Objects.requireNonNull(value, "value");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("Unit amounts must be non-negative");
        }
        if (value.bitLength() <= 63) {
            small = value.longValue();
            large = null;
        } else {
            small = 0;
            large = value;
        }
        return this;
    }

    public UnitAmount init(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Unit amount string cannot be null");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new NumberFormatException("For input string: \"\"");
        }

        try {
            return init(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            return init(new BigInteger(trimmed));
        }
    }

    public boolean isZero() {
        return large == null && small == 0;
    }

    public boolean isPositive() {
        return large != null || small > 0;
    }

    public UnitAmount add(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Unit amounts must be non-negative");
        }
        if (value == 0) {
            return this;
        }
        if (large != null) {
            large = large.add(BigInteger.valueOf(value));
            return this;
        }

        try {
            small = Math.addExact(small, value);
        } catch (ArithmeticException ignored) {
            promote().large = large.add(BigInteger.valueOf(value));
        }
        return this;
    }

    public UnitAmount add(UnitAmount other) {
        if (other.large != null) {
            if (large != null) {
                large = large.add(other.large);
            } else {
                large = BigInteger.valueOf(small).add(other.large);
                small = 0;
            }
            return this;
        }
        return add(other.small);
    }

    public UnitAmount subtractClamped(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Unit amounts must be non-negative");
        }
        if (value == 0 || isZero()) {
            return this;
        }
        if (large != null) {
            large = large.subtract(BigInteger.valueOf(value));
            normalize();
            return this;
        }
        small = small <= value ? 0 : small - value;
        return this;
    }

    public UnitAmount subtractClamped(UnitAmount other) {
        if (other.large != null) {
            if (large == null) {
                return init(0);
            }
            large = large.subtract(other.large);
            if (large.signum() < 0) {
                return init(0);
            }
            normalize();
            return this;
        }
        return subtractClamped(other.small);
    }

    public UnitAmount multiply(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Unit amounts must be non-negative");
        }
        if (value == 0 || isZero()) {
            return init(0);
        }
        if (value == 1) {
            return this;
        }
        if (large != null) {
            large = large.multiply(BigInteger.valueOf(value));
            return this;
        }

        try {
            small = Math.multiplyExact(small, value);
        } catch (ArithmeticException ignored) {
            promote().large = large.multiply(BigInteger.valueOf(value));
        }
        return this;
    }

    public UnitAmount multiply(UnitAmount other) {
        if (other.large != null) {
            if (isZero()) {
                return init(0);
            }
            if (large != null) {
                large = large.multiply(other.large);
            } else {
                large = BigInteger.valueOf(small).multiply(other.large);
                small = 0;
            }
            return this;
        }
        return multiply(other.small);
    }

    public UnitAmount divide(long divisor) {
        if (divisor <= 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (large != null) {
            large = large.divide(BigInteger.valueOf(divisor));
            normalize();
            return this;
        }
        small /= divisor;
        return this;
    }

    public UnitAmount divide(UnitAmount divisor) {
        if (!divisor.isPositive()) {
            throw new ArithmeticException("Division by zero");
        }
        if (divisor.large != null) {
            if (large == null) {
                return init(0);
            }
            large = large.divide(divisor.large);
            normalize();
            return this;
        }
        return divide(divisor.small);
    }

    public UnitAmount min(UnitAmount other) {
        if (compareTo(other) > 0) {
            return init(other);
        }
        return this;
    }

    public long remainderToLong(long divisor, long limit) {
        if (divisor <= 0) {
            throw new ArithmeticException("Division by zero");
        }
        long remainder = large != null
            ? large.remainder(BigInteger.valueOf(divisor)).longValue()
            : small % divisor;
        return Math.min(remainder, limit);
    }

    public long remainderToLong(UnitAmount divisor, long limit) {
        if (divisor.large != null) {
            if (large == null) {
                return Math.min(small, limit);
            }
            BigInteger remainder = large.remainder(divisor.large);
            return remainder.bitLength() <= 63
                ? Math.min(remainder.longValue(), limit)
                : limit;
        }
        return remainderToLong(divisor.small, limit);
    }

    public long toLongSaturated(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be non-negative");
        }
        if (large == null) {
            return Math.min(small, limit);
        }
        if (limit == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return large.compareTo(BigInteger.valueOf(limit)) > 0 ? limit : large.longValue();
    }

    public BigInteger asBigInteger() {
        return large != null ? large : BigInteger.valueOf(small);
    }

    public String toDecimalString() {
        return large != null ? large.toString() : Long.toString(small);
    }

    public int compareTo(UnitAmount other) {
        if (large == null && other.large == null) {
            return Long.compare(small, other.small);
        }
        if (large != null && other.large != null) {
            return large.compareTo(other.large);
        }
        return large != null ? 1 : -1;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UnitAmount amount)) {
            return false;
        }
        if (large == null && amount.large == null) {
            return small == amount.small;
        }
        return large != null && amount.large != null && large.equals(amount.large);
    }

    @Override
    public int hashCode() {
        return large != null ? large.hashCode() : Long.hashCode(small);
    }

    @Override
    public String toString() {
        return toDecimalString();
    }

    private UnitAmount promote() {
        large = BigInteger.valueOf(small);
        small = 0;
        return this;
    }

    private void normalize() {
        if (large != null && large.bitLength() <= 63) {
            small = large.longValue();
            large = null;
        }
    }
}
