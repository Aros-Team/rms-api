/* (C) 2026 */

package aros.services.rms.core.analytics.domain;

import java.util.List;

/** A page of variance alerts. */
public record AlertsPage(List<SystemAlert> items, PageInfo page) {

  /** Paging metadata. */
  public record PageInfo(int limit, int offset, long total) {}
}
