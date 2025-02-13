package com.example.springboot;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;

public class DateTest {

    @Test
    public void test() {
        String str = "2001-04-16";
        int age = DateUtil.ageOfNow(str);
        System.out.println(age);

    }
}
