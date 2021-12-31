package com.jeremy.controller;

import com.jeremy.service.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/3/2.
 */
@RestController
public class TableCtrl {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private WebService service;

  @RequestMapping(value = "/table/{domain}/{sqlid}", method = RequestMethod.GET)
  public Object controller(@PathVariable String domain, @PathVariable String sqlid,
      @RequestParam Map<String, Object> paramMap) throws Exception {

    logger.debug("deptid1:" + paramMap.get("deptid"));

    String columnString = String.valueOf(paramMap.get("columnString"));
    String[] columnArray = columnString.split("[,]");

    StringBuffer orderby = new StringBuffer();
    ArrayList arrayList = new ArrayList();
    for (String key : paramMap.keySet()) {
      logger.debug(key + ":" + paramMap.get(key));
      if (key.startsWith("order") && key.endsWith("[column]")) {
//                logger.debug("right key:"+key);
//                int index = Integer.parseInt(key.substring(key.indexOf("[")+1,key.indexOf("]")));
//                logger.debug("index:"+index);
//                orderby.append(columnArray[index]+" ");
//                logger.debug("columnArray[index]:"+columnArray[index]);
//                orderby.append(paramMap.get("order["+index+"][dir]")+",");

        int columnIndex = Integer.parseInt(String.valueOf(paramMap.get(key)));
        orderby.append(columnArray[columnIndex] + " ");
        orderby.append(paramMap.get(key.substring(0, key.indexOf("[column]")) + "[dir]") + ",");

      }
    }

    if (orderby != null && orderby.length() > 0) {
      logger.debug("orderby:" + orderby.toString());
      paramMap.put("orderby", orderby.substring(0, orderby.length() - 1));
    }
    Map retMap = new HashMap();
    retMap.put("draw", paramMap.get("draw"));

    paramMap.put("tradeCode", domain + "." + sqlid);

    logger.debug("tradeCode:" + domain + "." + sqlid);

    logger.debug("1111");

    for (String key : paramMap.keySet()) {
      logger.debug(key + ":" + paramMap.get(key));
    }

    logger.debug("deptid2:" + paramMap.get("deptid"));
    List retList = (List) service.dbinvoke(paramMap);
    logger.debug("retList:" + retList);

    logger.debug("paramMap." + paramMap.get("startdate"));
    logger.debug("paramMap." + paramMap.get("enddate"));
    logger.debug("paramMap." + paramMap.get("deptid"));

//        paramMap.put("tradeCode",domain+".selectCount");

    paramMap.remove("start");
    paramMap.remove("length");

    long startTime = System.currentTimeMillis();   //获取开始时间

    List totalList = (List) service.dbinvoke(paramMap);

    long endTime = System.currentTimeMillis(); //获取结束时间
    logger.debug("dbruntime程序运行时间： " + (endTime - startTime) + " ms");

    retMap.put("recordsTotal", totalList.size());
    retMap.put("recordsFiltered", totalList.size());
    retMap.put("data", retList);
    logger.debug("retList:" + retList.size());
    return retMap;
  }
}
