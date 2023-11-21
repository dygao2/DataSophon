package com.datasophon.api.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author rwj
 * @date 2022/11/20
 */
public class HostsValidatorTest {

    @Test
    public void testIsValid() {
        HostsValidator hostsValidator = new HostsValidator();

        assertEquals(true, hostsValidator.isValid("a1,ae,45", null));
        assertEquals(false, hostsValidator.isValid("a1,ae,45,@##$@#", null));
        assertEquals(false, hostsValidator.isValid("##$@#", null));
        assertEquals(true, hostsValidator.isValid(",,,", null));
        assertEquals(false, hostsValidator.isValid("，，，", null));
        assertEquals(true, hostsValidator.isValid("a.82.66.61", null));   //这个是主机名，不按ip校验
        assertEquals(true, hostsValidator.isValid("a.a.82.66.61", null)); //主机名
        assertEquals(true, hostsValidator.isValid("255.82.66.61", null));
        assertEquals(false, hostsValidator.isValid("256.82.66.61", null));
        assertEquals(false, hostsValidator.isValid("255.832.66.61", null));
        assertEquals(true, hostsValidator.isValid("0.82.66.61", null));
        assertEquals(true, hostsValidator.isValid("255.82.66.0", null));
        assertEquals(false, hostsValidator.isValid("255.82.66.01", null));
        assertEquals(false, hostsValidator.isValid("255.004.66.61", null));
        assertEquals(false, hostsValidator.isValid("255.004.66.61,", null));
        assertEquals(true, hostsValidator.isValid(",,255.4.66.61,,", null));
        assertEquals(false, hostsValidator.isValid(",,255.004.66.61,,", null));
        assertEquals(false, hostsValidator.isValid(",[-,255.004.66.61,,", null));
        assertEquals(true, hostsValidator.isValid(",,255.4.66.61[1-5],,", null));  // 带 [ - ] 任意符号的跳过，不处理
        assertEquals(false, hostsValidator.isValid(",,255.4-5.66.6,,", null));  // 带 [ - ] 任意符号的跳过，不处理
        assertEquals(false, hostsValidator.isValid(",,a.4-5.66.6,,", null));  // 带 [ - ] 任意符号的跳过，不处理
        assertEquals(false, hostsValidator.isValid(",,a.4-$%5.66.6,,", null));  // 带 [ - ] 任意符号的跳过，不处理
    }

}
