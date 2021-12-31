package com.jeremy.controller;

import com.jeremy.service.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by Jeremy on 2017/2/14.
 */

@RestController
public class WebCtrl {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private WebService service;

  @Autowired
  private MenuService menuService;

  @Autowired
  private DevService devService;

  @Autowired
  private BranchService branchService;

  @Autowired
  private PMenuService pMenuService;

  @Autowired
  private UserService userService;

  @Autowired
  private UploadService uploadService;

  @Autowired
  private DeptflagService deptflagService;

  @Autowired
  private HolidayService holidayService;

  @Autowired
  private OrdertimeService ordertimeService;

  @Autowired
  private AdvertiseService advertiseService;

  @Value("${pdj:false}")
  private String pdj;

  @Value("${auth:false}")
  private String auth;

  @RequestMapping(value = "/web", method = RequestMethod.POST)
  public Object controller(@RequestParam Map<String, Object> paramMap,HttpServletRequest request) throws Exception {

    logger.debug("收到请求...开始处理");
    Map retMap = new HashMap();
    UUID uuid = UUID.randomUUID();
    logger.debug("serial:" + uuid);
    retMap.put("serial", uuid);

    String tradeCode = getMapParamValue(paramMap, "tradeCode", "");
    logger.info("tradeCode:" + tradeCode);

    String tradeCodeList = getMapParamValue(paramMap, "tradeCodeList", "");
    logger.info("tradeCodeList:" + tradeCodeList);

    if ("".equals(tradeCode) && "".equals(tradeCodeList)) {
      retMap.put("rspCode", "90");
      retMap.put("rspMsg", "Please Submit tradeCode!");
    }

    try {

      logger.debug("auth:"+auth);

      if (!"users.selectByPrimaryKey".equals(tradeCode) && "true".equals(auth)) {
        String authHeader = request.getParameter("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
          throw new Exception("Missing or invalid Authorization header.");
        }

        logger.debug("authHeader:"+authHeader);
        String token = authHeader.substring(7); // The part after "Bearer "

        Map param = new HashMap();
        param.put("tradeCode","users.selectByPrimaryKey");
        logger.debug("request.getParameter(\"authUserId\"):"+request.getParameter("authUserId"));
        param.put("fId",request.getParameter("authUserId"));
        List<Map> userList = (List<Map>) service.dbinvoke(param);
        if(userList.size() != 1){
          throw new Exception("Illegal token.");
        }else {
          String token_save = (String) userList.get(0).get("fToken");

          logger.debug("token:"+token);
          logger.debug("token_save:"+token_save);
          logger.debug(String.valueOf(token.equals(token_save)));

          if(!token.equals(token_save)){
            throw new Exception("登录认证失效，请重新登录.");
          }
        }
      }

      String toList = (String) paramMap.get("toList");
      if (toList != null) {
        String[] array = toList.split(",");
        for (String key : array) {
          String value = (String) paramMap.get(key);
          if (value != null) {
            paramMap.put(key + "List", Arrays.asList(value.split(",")));
          } else {
            throw new Exception(key + "参数有误，请检查");
          }
        }
      }

      String listKey = getMapParamValue(paramMap, "listKey", "listInfo");
      logger.debug("listKey:" + listKey);

      if (!"".equals(tradeCodeList)) {

        service.service(paramMap);

      } else if("dev.insertSelective".equals(tradeCode) || "dev.updateByPrimaryKeySelective".equals(tradeCode)) {

        devService.insert(paramMap);


      } else if ("dev.selectDevStateWithDetail".equals(tradeCode)) {

        retMap.put("listInfo", devService.selectDevStateWithDetail(paramMap));

      } else if ("users.selectByPrimaryKey".equals(tradeCode)) {

        retMap.put("userInfo", userService.service(paramMap));

      } else if ("pmenu.selectAllPMenus".equals(tradeCode)) {

        retMap.put("pmenuInfo", pMenuService.service(paramMap));

      } else if ("dev.selectDevCountWithType".equals(tradeCode)) {

        retMap.put("result", devService.selectDevCountWithType(paramMap));

      } else if ("branch.selectAllBranches".equals(tradeCode)) {

        retMap.put("branchInfo", branchService.service(paramMap));

      } else if ("dev.deleteDev".equals(tradeCode)) {

        devService.deleteDev(paramMap);

      } else if ("dev.stopDev".equals(tradeCode)) {

        devService.stopDev(paramMap);

      } else if ("dev.startDev".equals(tradeCode)) {

        devService.startDev(paramMap);

      } else if ("menu.selectByDevJson".equals(tradeCode)) {

        List retList = (List) menuService.selectByDevJson(paramMap);
        retMap.put(listKey, retList);

      } else if ("menu.selectByDevtypeJson".equals(tradeCode)) {

        List retList = (List) menuService.selectByDevtypeJson(paramMap);
        retMap.put(listKey, retList);

      } else if ("devtx.insertBatch".equals(tradeCode)) {

        menuService.devtxInsertBatch(paramMap);
      } else if ("devtypetx.insertBatch".equals(tradeCode)) {

        menuService.devtypetxInsertBatch(paramMap);

      } else if ("menuauth.getMenu".equals(tradeCode)) {
        retMap.put("menuInfo", (List) menuService.service(paramMap));

      } else if ("advertise.insertSelective".equals(tradeCode)) {

        int ret = (int) advertiseService.service(paramMap);
        System.out.println("ret:" + ret);
        if (0 >= ret) {
          throw new Exception("操作失败");
        }
      } else if ("advertise.updateByPrimaryKeySelective".equals(tradeCode)) {

        int ret = (int) advertiseService.update(paramMap);
        System.out.println("ret:" + ret);
        if (0 >= ret) {
          throw new Exception("操作失败");
        }

      } else if ("advertise.deleteByPrimaryKey".equals(tradeCode)) {

        int ret = (int) advertiseService.delete(paramMap);
        System.out.println("ret:" + ret);
        if (0 >= ret) {
          throw new Exception("操作失败");
        }


      } else if ("soft.insertSelective".equals(tradeCode)) {

        int ret = (int) uploadService.service(paramMap);
        System.out.println("ret:" + ret);
        if (0 >= ret) {
          throw new Exception("操作失败");
        }
      } else if ("todo.insertSelective".equals(tradeCode)) {

        int ret = (int) uploadService.todo(paramMap);
        System.out.println("ret:" + ret);
        if (0 >= ret) {
          throw new Exception("操作失败");
        }

      } else if ("ordertime.insert".equals(tradeCode)) {

        ordertimeService.insert(paramMap);
      } else if ("ordertime.update".equals(tradeCode)) {

        ordertimeService.update(paramMap);
      } else if ("ordertime.delete".equals(tradeCode)) {

        ordertimeService.delete(paramMap);
      } else if ("ordertime.judge".equals(tradeCode)) {

        ordertimeService.judge(paramMap);

      } else if ("holiday.insert".equals(tradeCode)) {

        holidayService.insert(paramMap);
      } else if ("holiday.delete".equals(tradeCode)) {

        holidayService.delete(paramMap);
      } else if ("holiday.judge".equals(tradeCode)) {

        holidayService.judge(paramMap);

      } else {

        if (SqlCommandType.SELECT == service.getSqlCommandType(tradeCode)) {
          List retList = (List) service.dbinvoke(paramMap);
          retMap.put(listKey, retList);
        } else {
          int ret = (int) service.dbinvoke(paramMap);
          System.out.println("ret:" + ret);
          if (0 >= ret) {
            throw new Exception("操作失败");
          }
        }

      }

      if ("true".equals(pdj)) {
        deptflagService.service(paramMap);
      }

      retMap.put("rspCode", "00");
      retMap.put("rspMsg", "交易成功");
    } catch (Exception e) {
      e.printStackTrace();
      logger.debug(e.toString());
      if (e.getMessage() != null) {
        logger.debug("message:" + e.getMessage());
      }
      if (e.getCause() != null) {
        logger.debug("cause:" + e.getCause());
      }
      retMap.put("rspCode", "99");
      retMap.put("rspMsg", getError(e));
    } finally {
      return retMap;
    }

  }

  private String getError(Exception e) {
    if (e.getMessage().contains("Duplicate entry")) {
      return "主键重复";
    } else if (e.getMessage().contains("Data too long")) {
      return "参数长度过长";
    } else if (e.getMessage().contains("unique constraint")) {
      return "id字段重复，请检查";
    } else if (e.getMessage().contains("ORA-00904")) {
      return "无效列名";
    } else if (e.getMessage().contains("ORA-00942")) {
      return "表或者视图不存在";
    } else if (e.getMessage().contains("ORA-01400")) {
      return "不能将空值插入";
    } else if (e.getMessage().contains("ORA-00936")) {
      return "缺少表达式";
    } else if (e.getMessage().contains("ORA-00932")) {
      return "数据类型不一致";
    } else if (e.getMessage().contains("ORA-00933")) {
      return "SQL 命令未正确结束";
    } else if (e.getMessage().contains("ORA-01722")) {
      return "无效的数字赋值";
    } else if (e.getMessage().contains("ORA-06592")) {
      return "case语句格式有误";
    } else if (e.getMessage().contains("ORA-00001")) {
      return "主键唯一值冲突";
    } else if (e.getMessage().contains("ORA-00001")) {
      return "主键唯一值冲突，请检查id值";
    } else if (e.getMessage().contains("ORA-01422")) {
      return "返回超过一行";
    } else if (e.getMessage().contains("NullPointerException")) {
      return "空指针异常";
    } else if (e.getMessage().contains("ClassNotFoundException")) {
      return "类不存在";
    } else if (e.getMessage().contains("NumberFormatException")) {
      return "字符串转换为数字异常";
    } else if (e.getMessage().contains("IndexOutOfBoundsException")) {
      return "数组越界";
    } else if (e.getMessage().contains("IllegalArgumentException")) {
      return "方法的参数错误";
    } else if (e.getMessage().contains("NullPointerException")) {
      return "空指针异常";
    } else if (e.getMessage().contains("NullPointerException")) {
      return "空指针异常";
    }
    return e.getMessage();
  }


  private String getMapParamValue(Map paramMap, String listKey, String defVal) {
    if (paramMap.get(listKey) != null) {
      return (String) paramMap.get(listKey);
    } else {
      return defVal;
    }
  }

}
