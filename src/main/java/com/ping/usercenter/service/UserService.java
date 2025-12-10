package com.ping.usercenter.service;

import com.ping.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ping.usercenter.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.ping.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.ping.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 21877
 * @author ping
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-08-03 07:46:33
 * 业务接口，只定义方法不去实现
 * <p>
 * 用户服务
 */
public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword,
                      String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的信息
     */
    User userLogin(String userAccount, String userPassword,
                   HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTagsBySQL(List<String> tagNameList);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取脱敏用户
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏用户列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVoList(List<User> userList);
}
















