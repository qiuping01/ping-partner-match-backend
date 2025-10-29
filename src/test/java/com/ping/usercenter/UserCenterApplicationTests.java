package com.ping.usercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.sun.javafx.font.FontResource.SALT;

@SpringBootTest
class UserCenterApplicationTests {

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String newPassword =
                DigestUtils.md5DigestAsHex((SALT + "mypassword").getBytes());
        System.out.println(newPassword);
    }



    @Test
    void contextLoads() {

    }

}
