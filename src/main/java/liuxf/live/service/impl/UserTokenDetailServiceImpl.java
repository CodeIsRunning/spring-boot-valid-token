package liuxf.live.service.impl;

import com.alibaba.fastjson.JSONObject;
import liuxf.live.service.UserTokenDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 15:56
 */
@Service(value = "userTokenDetailService")
public class UserTokenDetailServiceImpl implements UserTokenDetailService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public void putToken(String userId, String token, long timeOut, String... roles) {


        //单点登录
        //是否已经登录
        String oldToken = stringRedisTemplate.opsForValue().get(userId);

        validToken(oldToken, token, userId, timeOut, roles);


    }

    @Override
    public String putToken(String userId, long timeOut, String... roles) {


        //单点登录
        //是否已经登录
        String oldToken = stringRedisTemplate.opsForValue().get(userId);


        String createTokenStr = userId + "liuxf" + System.currentTimeMillis();

        String token = DigestUtils.md5DigestAsHex(createTokenStr.getBytes());

        validToken(oldToken, token, userId, timeOut, roles);

        return token;

    }

    @Override
    public void removeToken(String userId) {
        String token = stringRedisTemplate.opsForValue().get(userId);

        if (StringUtils.isNotBlank(token)) {

            stringRedisTemplate.delete(token);

        }
        stringRedisTemplate.delete(userId);
    }

    private void validToken(String oldToken, String token, String userId, long timeOut, String... roles) {

        if (StringUtils.isBlank(oldToken)) {

            stringRedisTemplate.opsForValue().set(userId, token, timeOut, TimeUnit.MINUTES);

            stringRedisTemplate.opsForValue().set(token, JSONObject.toJSONString(roles), timeOut, TimeUnit.MINUTES);

        } else {

            stringRedisTemplate.delete(oldToken);

            stringRedisTemplate.delete(userId);

            stringRedisTemplate.opsForValue().set(userId, token, timeOut, TimeUnit.MINUTES);

            stringRedisTemplate.opsForValue().set(token, JSONObject.toJSONString(roles), timeOut, TimeUnit.MINUTES);

        }
    }
}
