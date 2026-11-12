package com.cemetery.common;
import lombok.*; import java.util.List;
@Data @AllArgsConstructor public class PageResult<T>{ private List<T> records; private long total; private int page; private int size; }
