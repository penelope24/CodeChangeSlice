package fy.GW.utils;

import objectexplorer.ObjectGraphMeasurer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

class ObjectSizeFetcherTest {

    @Test
    void run () {
        Set<Integer> hashset = new HashSet<Integer>();
        Random random = new Random();
        int n = 10000;
        for (int i = 1; i <= n; i++) {
            hashset.add(random.nextInt());
        }
        System.out.println(ObjectGraphMeasurer.measure(hashset));
    }
}