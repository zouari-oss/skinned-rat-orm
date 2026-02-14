package org.zouarioss.skinnedratorm.util;

import java.util.List;

public class Page<T> {
  private final List<T> content;
  private final int pageNumber;
  private final int pageSize;
  private final long totalElements;
  private final int totalPages;

  public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
    this.content = content;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.totalElements = totalElements;
    this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
  }

  public List<T> getContent() { return content; }
  public int getPageNumber() { return pageNumber; }
  public int getPageSize() { return pageSize; }
  public long getTotalElements() { return totalElements; }
  public int getTotalPages() { return totalPages; }
  public boolean hasNext() { return pageNumber < totalPages - 1; }
  public boolean hasPrevious() { return pageNumber > 0; }
}
