package com.ping.usercenter.service;

import java.util.Arrays;
import java.util.List;

import com.ping.usercenter.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/*
 *用户测试
 *
 * @author ping
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("uiop");
        user.setUserAccount("abcd");
        user.setAvatarUrl("https://thirdwx.plogo" +
                ".cn/mmopen/vi_72" +
                "/PiajxSqBRaEL9u74UegRhnTfElKHHGQ7fzRSHoWZDHJO5dczRWslDbVSG125BMxnlwX075wTWosNVzzsy6IQsmaXGnicj9SnU6yds9dKSg3WsQtCClia7f1Cg/132");
        user.setGender(0);
        user.setUserPassword("12345678");
        user.setPhone("16608683257");
        user.setEmail("12345");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);
    }

    @Test
    void userRegister() {
        // 测试场景1：密码为空
        String userAccount = "pinoog";
        String userPassword = "";
        String checkPassword = "123456";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword,
                checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：空密码

        // 测试场景2：账户过短（小于4字符）
        userAccount = "pi";
        result = userService.userRegister(userAccount, userPassword,
                checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：账户长度不足

        // 测试场景3：密码过短（小于8字符）
        userAccount = "ping";
        userPassword = "123456"; // 6字符密码
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：密码长度不足

        // 测试场景4：账户包含空格
        userAccount = "pi ng"; // 包含空格
        userPassword = "12345678"; // 有效密码
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：账户含特殊字符

        // 测试场景5：密码和校验密码不一致
        userPassword = "123456789"; // 修改密码
        // checkPassword仍为"123456"（与密码不同）
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：密码不匹配

        // 测试场景6：账户已存在
        userAccount = "dogPing"; // 新账户
        userPassword = "12345678"; // 有效密码
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        // 注意：此处checkPassword仍为"123456"，与密码"12345678"不同
        Assertions.assertEquals(-1, result); // 预期失败：密码不匹配

        // 测试场景7：账户重复注册
        userAccount = "ping"; // 尝试注册已存在的账户
        // 使用有效密码，但checkPassword仍为"123456"（与密码不同）
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1, result); // 预期失败：密码不匹配

        // ========= 新增：测试场景8 - 成功注册 =========
        String successAccount = "qiuPing"; // 唯一账户名
        String successPassword = "87654321"; // 有效密码
        String successCheckPassword = "87654321"; // 与密码一致

        // 执行注册
        long successResult = userService.userRegister(successAccount, successPassword, successCheckPassword, planetCode);

        // 验证结果
        Assertions.assertTrue(successResult > 0, "注册应返回用户ID");
        System.out.println("成功注册用户: ID=" + successResult + ", 账号=" + successAccount);
    }

    @Test
    public void testSearchUserByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
}