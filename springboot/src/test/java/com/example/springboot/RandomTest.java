package com.example.springboot;

import cn.hutool.core.util.RandomUtil;
import org.junit.jupiter.api.Test;

public class RandomTest {

    @Test
    public void test() {
        int cnt = 0;
        for (int i = 1; i <= 100; i++) {
            Integer code = Integer.valueOf(RandomUtil.randomNumbers(6));

//            while (code < 100000) {
//                code = Integer.valueOf(RandomUtil.randomNumbers(6));
//            }
            if (code < 100000) {
                cnt++;
            }
            System.out.println(code);
        }
        System.out.println("cnt: " + cnt);
    }
}


