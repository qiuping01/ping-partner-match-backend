package com.ping.usercenter.service;

import com.ping.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * 算法的工具类测试
 */
public class AlgorithmUtilsTest {

    @Test
    void test() {
        List<String> tagList1 = Arrays.asList("a", "b", "c");
        List<String> tagList2 = Arrays.asList("a", "b", "d");
        List<String> tagList3 = Arrays.asList("java", "大一", "男");
        List<String> tagList4 = Arrays.asList("java", "大二", "男");
        List<String> tagList5 = Arrays.asList("java", "大一", "女");
        int distance = AlgorithmUtils.minDistance(tagList1, tagList2);
        System.out.println(distance);
        System.out.println("1:" + AlgorithmUtils.minDistance(tagList3, tagList4));
        System.out.println("1:" + AlgorithmUtils.minDistance(tagList3, tagList5));
        System.out.println("2:" + AlgorithmUtils.minDistance(tagList4, tagList5));
    }
}
