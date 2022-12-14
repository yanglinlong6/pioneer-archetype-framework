package com.glsx.plat.common.multithread;

import java.util.Iterator;
import java.util.List;

/**
 * 拆分集合
 *
 * @param <T> 泛型
 * @author yll
 */
public class ListSplitIterator<T> implements Iterator<List<T>> {

    /**
     * 原始数据集合
     */
    protected List<T> originData;

    /**
     * 分片大小
     */
    private int position;

    /**
     * 默认每页 1000 条
     */
    private int pageSize = 1000;

    public ListSplitIterator(List<T> originData) {
        if (originData == null) {
            return;
        }
        this.originData = originData;
        //初始化位置为 0
        position = 0;
    }

    public ListSplitIterator(List<T> originData, int pageSize) {
        this(originData);
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize 需要输入一个大于 0 的值");
        }
        this.pageSize = pageSize;
    }

    @Override
    public boolean hasNext() {
        return originData != null && !originData.isEmpty() && position < originData.size();
    }

    @Override
    public List<T> next() {
        if (hasNext()) {
            int start = position;
            int end = position + pageSize;
            if (end >= originData.size()) {
                end = originData.size();
            }
            List<T> current = originData.subList(start, end);
            position = end;
            return current;
        }
        return null;
    }

    @Override
    public void remove() {
        throw new RuntimeException("未实现 remove 方法");
    }

    public int size() {
        return originData == null ? 0 : (originData.size() - 1) / pageSize + 1;
    }

    public List<T> get(int index) {
        if (originData == null) {
            return null;
        }

        int start = index * pageSize;
        int end = (index + 1) * pageSize;
        if (end >= originData.size()) {
            end = originData.size();
        }

        return originData.subList(start, end);
    }
}
