package org.zouarioss.skinnedratorm.util;

public class PageRequest {
  private final int page;
  private final int size;

  private PageRequest(int page, int size) {
    this.page = page;
    this.size = size;
  }

  public static PageRequest of(int page, int size) {
    if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
    if (size < 1) throw new IllegalArgumentException("Size must be >= 1");
    return new PageRequest(page, size);
  }

  public int getPage() { return page; }
  public int getSize() { return size; }
  public int getOffset() { return page * size; }
}
