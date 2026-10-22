package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.UserDTO;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.utils.CacheClient;
import com.example.utils.CurrentHolder;
import com.example.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.utils.RedisConstants.*;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private CacheClient cacheClient;

    @Override
    public UserDTO register(User user) {
        // 1. 用户名唯一性校验
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 补全注册默认字段
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(user.getUsername());
        }
        user.setRole(0);

        // 3. 入库
        userMapper.insert(user);

        // 4. 生成令牌，组装 UserDTO 返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserDTO userDTO = toUserDTO(user, token);
        return userDTO;
    }

    @Override
    public UserDTO login(User user) {
        // 1. 按用户名查用户（取数据库里的完整用户信息）
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));

        // 2. 校验（用户不存在 或 密码不匹配，统一返回“用户名或密码错误”）
        if (dbUser == null || !user.getPassword().equals(dbUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 生成令牌，组装 UserDTO 返回
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());
        UserDTO userDTO = toUserDTO(dbUser, token);
        return userDTO;
    }

    @Override
    public User getUserById(Long id) {
        User user = cacheClient.queryWithMutex(CACHE_USER_KEY, id, User.class,
                CACHE_USER_TTL, TimeUnit.MINUTES, this::queryUserById );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
        /*User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 返回 User 实体，密码置空不暴露
        user.setPassword(null);
        return user;*/
    }

    private User queryUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        // 密码不进缓存
        user.setPassword(null);
        return user;
    }


    @Override
    @Transactional
    public void updateProfile(User user) {
        Long userId = CurrentHolder.getCurrentId();
        // 只更新可编辑字段（昵称、头像、手机号、性别），其余字段不参与更新
        User update = new User();
        update.setId(userId);
        update.setNickname(user.getNickname());
        update.setAvatar(user.getAvatar());
        update.setPhone(user.getPhone());
        update.setGender(user.getGender());
        userMapper.updateById(update);
        //删除缓存
        cacheClient.delete(CACHE_USER_KEY + userId);
        //没必要给用户收藏加redis缓存，不过已经加了才想起来，就懒得删了
        // 商品缓存里存着卖家昵称/头像，改完资料要删掉这个用户所有商品的缓存
        List<Product> products = productMapper.selectList(Wrappers.<Product>lambdaQuery()
                .eq(Product::getUserId, userId));
        for (Product product : products) {
            cacheClient.delete(CACHE_PRODUCT_KEY + product.getId());
        }
    }

    /** 组装返回 DTO：token + 用户基础信息（不含密码、手机号、性别） */
    private UserDTO toUserDTO(User user, String token) {
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        userDTO.setToken(token);
        return userDTO;
    }
}