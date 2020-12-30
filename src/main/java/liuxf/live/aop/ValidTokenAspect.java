package liuxf.live.aop;

import com.alibaba.fastjson.JSONObject;
import jdk.nashorn.internal.ir.IfNode;
import liuxf.live.annotation.ValidToken;
import liuxf.live.enums.ValidTokenExceptionEnum;
import liuxf.live.exception.ValidTokenException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 14:59
 */
@Aspect
@Component
public class ValidTokenAspect {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Pointcut("@annotation(liuxf.live.annotation.ValidToken)")
    public void validTokenCut() {

    }


    @Before("validTokenCut()")
    public void before(JoinPoint point) throws ValidTokenException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("token");

        validToken(token);

        validRoles(point, token);

    }


    private void validToken(String token) {

        /**
         * 无token
         */
        if (StringUtils.isBlank(token)) {
            throw new ValidTokenException(ValidTokenExceptionEnum.HTTP_401);
        }

    }

    private void validRoles(JoinPoint point, String token) {

        MethodSignature signature = (MethodSignature) point.getSignature();

        Method method = signature.getMethod();

        String redisRolesStr = stringRedisTemplate.opsForValue().get(token);

        if (StringUtils.isBlank(redisRolesStr)) {
            validNotSetRoles(method);
        }

        List<String> redisRoles = JSONObject.parseObject(redisRolesStr, List.class);

        String[] roles = method.getAnnotation(ValidToken.class).roles();

        if (roles != null || roles.length > 0) {

            List<String> rolesList = new ArrayList<>();

            for (String role : roles) {
                rolesList.add(role);
            }

            List<String> intersection = redisRoles.stream().filter(item -> rolesList.contains(item)).collect(toList());

            if (CollectionUtils.isEmpty(intersection)) {
                throw new ValidTokenException(ValidTokenExceptionEnum.HTTP_403);
            }

        }


    }

    private void validNotSetRoles(Method method) {

        String[] roles = method.getAnnotation(ValidToken.class).roles();

        if (roles != null || roles.length > 0) {
            throw new ValidTokenException(ValidTokenExceptionEnum.HTTP_403);
        }
    }

}
