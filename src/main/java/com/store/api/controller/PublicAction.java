package com.store.api.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.store.api.common.Constant;
import com.store.api.mongo.entity.Address;
import com.store.api.mongo.entity.User;
import com.store.api.mongo.entity.enumeration.UserType;
import com.store.api.mongo.service.AddressService;
import com.store.api.mongo.service.UserService;
import com.store.api.session.annotation.Authorization;
import com.store.api.utils.JsonUtils;
import com.store.api.utils.Utils;

/**
 * 公共方法控制器
 * 
 * @author vincent,2014年11月6日 created it
 */
@Controller()
@Scope("prototype")
@RequestMapping("/public")
public class PublicAction extends BaseAction {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    /**
     * 用户注册
     * 
     * @param name 用户名
     * @param nickname 昵称
     * @param pwd 密码(MD5)
     * @param phone 手机号
     * @param type 用户类型:1 商户 2 顾客
     * @param code 推广码
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/register")
    public String register(@RequestParam(value = "name", required = false, defaultValue = "")
    String userName, @RequestParam(value = "nickname", required = false, defaultValue = "")
    String nickName, @RequestParam(value = "pwd", required = false, defaultValue = "")
    String pwd, @RequestParam(value = "phone", required = false, defaultValue = "")
    String phone, @RequestParam(value = "type", required = false, defaultValue = "2")
    Long type, @RequestParam(value = "promocode", required = false, defaultValue = "")
    String code) throws Exception {
        if (Utils.isEmpty(userName) || Utils.isEmpty(pwd)) {
            return JsonUtils.resultJson(2, "用户名或密码不能为空", null);
        }
        if (userService.findByUserName(userName.trim()) != null)
            return JsonUtils.resultJson(3, "用户名已经被注册", null);
        if (Utils.isEmpty(nickName)) {
            nickName = userName;
        }
        User user = new User();
        user.setUserName(userName.trim());
        user.setNickName(nickName.trim());
        user.setPwd(pwd.trim());
        user.setPhone(phone.trim());
        user.setPromoCode(code.trim());
        user.setType(type == 1 ? UserType.merchants : UserType.customer);
        user.setImei(getImei());
        user.setRegisterVer(getVersionName());
        user.setCurrVer(getVersionName());
        userService.save(user);

        if (type == 1) {
            initSession(UserType.merchants, user, false);
        } else {
            initSession(UserType.customer, user, true);
        }

        Map<String, Object> veResult = new HashMap<String, Object>();
        veResult.put("user_id", user.getId() + "");
        veResult.put("user_name", userName);
        veResult.put("nick_name", nickName);
        veResult.put("phone", phone);
        veResult.put("user_type", "1");
        return JsonUtils.resultJson(0, "", veResult);
    }

    /**
     * 临时用户注册、登录
     * 
     * @param uuid
     * @return
     */
    @ResponseBody
    @RequestMapping("/visitorlogin")
    public String visitorLogin(@RequestParam(value = "uuid", required = false, defaultValue = "")
    String uuid) throws Exception {
        if (Utils.isEmpty(uuid))
            return JsonUtils.resultJson(2, "注册失败", null);
        User user = new User();
        user.setType(UserType.visitor);
        user.setImei(getImei());
        user.setRegisterVer(getVersionName());
        user.setCurrVer(getVersionName());
        userService.save(user);

        initSession(UserType.visitor, user, false);

        Map<String, Object> veResult = new HashMap<String, Object>();
        veResult.put("user_id", user.getId() + "");
        veResult.put("user_type", "0");
        return JsonUtils.resultJson(0, "", veResult);
    }

    /**
     * 用户登录
     * 
     * @param userName
     * @param pwd
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/login")
    public String login(@RequestParam(value = "name", required = false, defaultValue = "")
    String userName, @RequestParam(value = "pwd", required = false, defaultValue = "")
    String pwd) throws Exception {
        if (Utils.isEmpty(userName) || Utils.isEmpty(pwd))
            return JsonUtils.resultJson(2, "用户名或密码不能为空", null);
        User user = userService.findByUserName(userName.trim());
        if (null == user)
            return JsonUtils.resultJson(3, "用户尚未注册", null);
        if (Utils.isEmpty(user.getPwd()) || !user.getPwd().equalsIgnoreCase(pwd.trim()))
            return JsonUtils.resultJson(4, "密码错误", null);

        initSession(user.getType(), user, false);

        List<Address> addrs = addressService.findByUserId(user.getId());
        List<Map<String, String>> resAddr = new LinkedList<Map<String, String>>();
        if (null != addrs && !addrs.isEmpty()) {
            for (Address addr : addrs) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("addr_id", addr.getId() + "");
                map.put("address", addr.getAddress());
                map.put("def", addr.getId().equals(user.getAddressId()) ? "1" : "0");
                resAddr.add(map);
            }
        }

        Map<String, Object> veResult = new HashMap<String, Object>();
        veResult.put("user_id", user.getId() + "");
        veResult.put("user_name", user.getUserName());
        veResult.put("nick_name", user.getNickName());
        veResult.put("phone", user.getPhone());
        veResult.put("user_type", "1");
        veResult.put("addrs", resAddr);
        return JsonUtils.resultJson(0, "", veResult);
    }

    /**
     * 修改帐户信息
     * 
     * @param nickName
     * @param phone
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/modify")
    @Authorization(type = Constant.SESSION_USER)
    public String modify(@RequestParam(value = "nickname", required = false, defaultValue = "")
    String nickName, @RequestParam(value = "phone", required = false, defaultValue = "")
    String phone) throws Exception {
        if (Utils.isEmpty(nickName) && Utils.isEmpty(phone))
            return JsonUtils.resultJson(2, "修改字段不能为空", null);

        Object obj = session.getAttribute(Constant.SESSION_USER);

        User user = (User) obj;
        if (!Utils.isEmpty(nickName))
            user.setNickName(nickName.trim());
        if (!Utils.isEmpty(phone))
            user.setPhone(phone);
        userService.save(user);
        session.setAttribute(Constant.SESSION_USER, user);
        return JsonUtils.resultJson(0, "", null);
    }

    /**
     * 查询用户信息
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/userinfo")
    @Authorization(type = Constant.SESSION_USER)
    public String userInfo() throws Exception {
        Object obj = session.getAttribute(Constant.SESSION_USER);

        User user = (User) obj;
        userService.save(user);

        List<Address> addrs = addressService.findByUserId(user.getId());
        List<Map<String, String>> resAddr = new LinkedList<Map<String, String>>();
        if (null != addrs && !addrs.isEmpty()) {
            for (Address addr : addrs) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("addr_id", addr.getId() + "");
                map.put("address", addr.getAddress());
                map.put("def", addr.getId().equals(user.getAddressId()) ? "1" : "0");
                resAddr.add(map);
            }
        }

        Map<String, Object> veResult = new HashMap<String, Object>();
        veResult.put("user_id", user.getId() + "");
        veResult.put("user_name", user.getUserName());
        veResult.put("nick_name", user.getNickName());
        veResult.put("phone", user.getPhone());
        veResult.put("user_type", "1");
        veResult.put("addrs", resAddr);
        return JsonUtils.resultJson(0, "", veResult);
    }

    /**
     * 用户常用地址列表查询
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/queryaddress")
    @Authorization(type = Constant.SESSION_USER)
    public String queryAddress() throws Exception {
        Object obj = session.getAttribute(Constant.SESSION_USER);

        User user = (User) obj;
        userService.save(user);

        List<Address> addrs = addressService.findByUserId(user.getId());
        List<Map<String, String>> resAddr = new LinkedList<Map<String, String>>();
        if (null != addrs && !addrs.isEmpty()) {
            for (Address addr : addrs) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("addr_id", addr.getId() + "");
                map.put("address", addr.getAddress());
                map.put("def", addr.getId().equals(user.getAddressId()) ? "1" : "0");
                resAddr.add(map);
            }
        }
        return JsonUtils.resultJson(0, "", resAddr);
    }

    /**
     * 新增、编辑常用地址
     * 
     * @param addrId 地址ID
     * @param address 地址详细
     * @param lat 纬度
     * @param lng 经度
     * @param def 是否设置为默认（1默认 其它值不设为默认）
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/editaddress")
    @Authorization(type = Constant.SESSION_USER)
    public String editAddress(@RequestParam(value = "addressid", required = false, defaultValue = "0")
    Long addrId, @RequestParam(value = "address", required = false, defaultValue = "")
    String address, @RequestParam(value = "lat", required = false, defaultValue = "")
    Double lat, @RequestParam(value = "lng", required = false, defaultValue = "")
    Double lng, @RequestParam(value = "def", required = false, defaultValue = "")
    String def) throws Exception {
        if (lat <= 0 || lng <= 0)
            return JsonUtils.resultJson(2, "位置信息错误", null);
        if (Utils.isEmpty(address))
            return JsonUtils.resultJson(3, "请填写地址信息", null);
        boolean isadd = true;
        boolean isdef = false;
        if (addrId > 0)
            isadd = false;
        if (def.equals("1"))
            isdef = true;

        Object obj = session.getAttribute(Constant.SESSION_USER);
        User user = (User) obj;

        Address addObj = null;

        if (user.getType().equals(UserType.merchants)){//商户只有一个绑定地址。
            isdef = true;
            List<Address> oldAddress=addressService.findByUserId(user.getId());
            if(isadd && oldAddress.size()>0)
                return JsonUtils.resultJson(4, "设置地址失败", null);
        }
        
        if (isadd) {
            addObj = new Address();
            addObj.setUserId(user.getId());
        } else {
            addObj = addressService.findOne(addrId);
            if (null != addObj)
                isdef = addObj.getId().equals(user.getAddressId());
            else
                return JsonUtils.resultJson(5, "地址修改失败", null);
        }
        addObj.setAddress(address);
        addObj.setLocation(new Double[] { lat, lng });

        addressService.save(addObj);

        if (isdef) {
            user.setAddress(address);
            user.setAddressId(addObj.getId());
            user.setLocation(new Double[] { lat, lng });
            session.setAttribute(Constant.SESSION_USER, user);
        }
        userService.save(user);

        Map<String, String> reMap = new HashMap<String, String>();
        reMap.put("addr_id", addObj.getId() + "");

        return JsonUtils.resultJson(0, "", reMap);
    }

}
