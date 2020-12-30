# spring-boot-valid-token
自定义认证鉴权
#### 用到技术栈

 sdk 基于 aop和redis

依赖以及版本

```
       <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
            <version>2.1.8.RELEASE</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.1.8.RELEASE</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.1.8.RELEASE</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.11</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.62</version>
        </dependency>

```

#### sdk引入项目

1、引入响应的provided依赖和sdk

```
        <dependency>
            <groupId>liuxf.live</groupId>
            <artifactId>spring-boot-valid-token</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

2、配置sdk需要的redis

```
spring.redis.database=0
spring.redis.port=6379
spring.redis.password=
spring.redis.host=127.0.0.1
```



3、开启功能

启动类上加上启动注解

```
@EnableValidToken
```



#### 功能列表

##### 1、设置token

引入实现，模拟登陆接口，设置token和角色

设置token 有两个重载方法一个系统生成token，一个通过sdk生成，角色可以设定也可以不设定，自定义超时时间单位分钟

```
    @Resource
    UserTokenDetailService userTokenDetailService;

    @RequestMapping("login")
    public UserDto login(@RequestBody User user){


        //用户名密码正确


        String token = userTokenDetailService.putToken(user.getId()+"",50,"admin");

        UserDto userDto = new UserDto();

        userDto.setUserName(user.getUserName());

        userDto.setId(user.getId());

        userDto.setToken(token);

        return userDto;

    }
```



##### 2、移除token

模拟登出接口 调用移除方法会清除该用户的token和角色信息

```java
@RequestMapping("loginOut")
    public String loginOut(@RequestBody User user){


      
        userTokenDetailService.removeToken(user.getId()+"");

        return "移除成功";

    }
```

##### 3、权限验证

分为三种情况

1、不带注解@ValidToken 该模式模拟游客访问模式

2、带注解不设置角色验证 该模式只认证token

3、带注解设置角色验证  即校验token也校验登录时的角色设定

```java
@ValidToken(roles = {"admin"})
@RequestMapping("data")
public String data(){

    return "data接口";

}
```

##### 4、校验失败抛出异常

使用时需要全局异常捕获

```
public class ValidTokenException extends RuntimeException implements Serializable {

    private ValidTokenExceptionEnum validTokenExceptionEnum;

    public ValidTokenException() {
    }

    public ValidTokenException(ValidTokenExceptionEnum validTokenExceptionEnum) {
        super(validTokenExceptionEnum.getDescription());
        this.validTokenExceptionEnum = validTokenExceptionEnum;
    }

    public ValidTokenExceptionEnum getValidTokenExceptionEnum() {
        return validTokenExceptionEnum;
    }
}
```

全局异常捕获

```
    @ExceptionHandler(value = ValidTokenException.class)
    public JSONObject ExceptionHandler(ValidTokenException ex) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", ex.getValidTokenExceptionEnum().getCode());
        jsonObject.put("msg", ex.getValidTokenExceptionEnum().getDescription());
        return jsonObject;
    }
```



#### 实现思路

##### 利用aop获取token

利用aop的@Before功能来获取token进行验证，在利用token获取用户角色进行角色的验证

```
    @Before("validTokenCut()")
    public void before(JoinPoint point) throws ValidTokenException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String token = request.getHeader("token");

        validToken(token);

        validRoles(point, token);

    }
```

1、验证token

```
    private void validToken(String token) {

        /**
         * 无token
         */
        if (StringUtils.isBlank(token)) {
            throw new ValidTokenException(ValidTokenExceptionEnum.HTTP_401);
        }

    }
```

2、验证角色

```
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
```

##### 利用redis进行token和角色的存储查询

1、存储token和角色，使用者生成token

```
    @Override
    public void putToken(String userId, String token, long timeOut, String... roles) {


        //单点登录
        //是否已经登录
        String oldToken = stringRedisTemplate.opsForValue().get(userId);

        validToken(oldToken, token, userId, timeOut, roles);


    }
```

2、存储token和角色，sdk生成token

```
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
```

3、移除token

```
    @Override
    public void removeToken(String userId) {
        String token = stringRedisTemplate.opsForValue().get(userId);

        if (StringUtils.isNotBlank(token)) {

            stringRedisTemplate.delete(token);

        }
        stringRedisTemplate.delete(userId);
    }
```



#### 总体思路

通过登录获取权限信息存储到redis，通过aop进行获取校验，抛出异常，认证和鉴权的功能，每隔方法可以多角色赋值，登录也可以多角色录入。



#### 最后

[GitHub地址](https://github.com/CodeIsRunning/spring-boot-valid-token)

有问题欢迎指正，不用吝啬你的小星星哦
