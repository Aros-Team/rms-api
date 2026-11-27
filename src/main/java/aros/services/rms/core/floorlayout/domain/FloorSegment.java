/* (C) 2026 */

package aros.services.rms.core.floorlayout.domain;

/**
 * A horizontal run of painted cells inside one row of the floor grid.
 *
 * <p>Coordinates are zero-based. {@code row} grows downward, {@code from} and {@code to} are
 * inclusive column bounds and always satisfy {@code from <= to}.
 *
 * @param row grid row
 * @param from first column of the run (inclusive)
 * @param to last column of the run (inclusive)
 */
public record FloorSegment(int row, int from, int to) {}
