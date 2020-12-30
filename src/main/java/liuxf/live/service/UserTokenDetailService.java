package liuxf.live.service;

/**
 * @author liuxf
 * @version 1.0
 * @date 2020/12/29 15:47
 */
public interface UserTokenDetailService {

    /**
     * 自定义token
     * 存储token
     *
     * @param userId
     * @param roles
     * @param token
     * @param timeOut 单位分钟
     */
    public void putToken(String userId,String token,long timeOut,String...roles);


    /**
     * 系统token
     * 采用加盐
     * 存储token
     *
     * @param userId
     * @param roles
     * @param timeOut 单位分钟
     *
     * @return string
     */
    public String putToken(String userId ,long timeOut,String...roles);


    /**
     * 移除token
     *
     * @param userId
     */
    public void removeToken(String userId);




}
