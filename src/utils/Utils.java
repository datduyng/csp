package utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    public static <G> List<G> arr2List(G[] vars) {
        return Arrays.stream(vars).collect(Collectors.toList());
    }

    public static long getCpuTimeInNano() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ?
                bean.getCurrentThreadCpuTime() : 0L;
    }
    public static <L> List<L> listUnion(List<L> l1, List<L> l2) {
        return Stream.concat(l1.stream(), l2.stream())
                .distinct()
                .collect(Collectors.toList());
    }
}
