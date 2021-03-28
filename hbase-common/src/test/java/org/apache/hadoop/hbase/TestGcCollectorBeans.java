package org.apache.hadoop.hbase;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author leojie 2021/3/28 9:27 下午
 */
public class TestGcCollectorBeans {
    public static void main(String[] args) {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        System.out.println(gcBeans);

    }
}
