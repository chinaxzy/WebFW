package com.jeremy.service;

import com.jeremy.util.DateUtil;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class HolidayService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Object service(Map paramMap) throws Exception {
        String tradeCode = (String) paramMap.get("tradeCode");
        System.out.println(tradeCode);

        int count = sqlSession.selectOne("holiday.selectCount",paramMap);
        if(count > 0){
            throw new Exception("");
        }


        return 1;

    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object insert(Map paramMap) throws Exception {

        String fType = (String) paramMap.get("fType");

        if("FD".equals(fType)){
            int count = sqlSession.selectOne("holiday.selectCount",paramMap);
            if(count > 0){
                throw  new Exception("此日期已设置过节假日");
            }


            count = sqlSession.selectOne("order.selectCount",paramMap);
            logger.debug("11:"+String.valueOf(count));
            String  fOpen = (String) paramMap.get("fOpen");
            String f_conflict="0";
            if(count > 0 && "0".equals(fOpen)){
                f_conflict="1";
            }

            paramMap.put("fConflict",f_conflict);
            int ret = sqlSession.insert("holiday.insertSelective",paramMap);
            if(ret != 1){
                throw  new Exception("特殊节假日新增失败");
            }
            return 1;
        }

        if("WK".equals(fType)){
            int count = sqlSession.selectOne("holiday.selectCount",paramMap);
            if(count > 0){
                throw  new Exception("此星期已设置过节假日");
            }

            paramMap.put("fSection","PDYY");
            paramMap.put("fKey","PreDates");
            String value = sqlSession.selectOne("syscfg.selectKeyBySection",paramMap);

            if(value == null || value.length() == 0){
//                throw new Exception("请先设置提前预约天数!");
                value = "30";
                paramMap.put("fValue",value);
                int ret = sqlSession.update("syscfg.updateByPrimaryKeySelective",paramMap);
                if(ret == 0){
                    sqlSession.insert("syscfg.insertSelective",paramMap);
                }
            }

            String f_conflict="0";

            Map param = new HashMap();

            param.put("fDateStart",DateUtil.formatDate(DateUtil.getOffsiteDate(new Date(), Calendar.DAY_OF_MONTH, Integer.parseInt("-"+value))));
            param.put("fDateEnd",DateUtil.formatDate(new Date()));
            param.put("fDept",paramMap.get("fDept"));
            logger.debug(DateUtil.formatDate(DateUtil.getOffsiteDate(new Date(), Calendar.DAY_OF_MONTH, Integer.parseInt("-"+value))));
            logger.debug(DateUtil.formatDate(new Date()));
            List<Map> orderList = sqlSession.selectList("order.selectByPrimaryKey", param);
            logger.debug("orderList:" +orderList.size());
            if(orderList.size() > 0){
                for (Map map : orderList) {
                    String date = (String) map.get("fDate");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(DateUtil.parseDate(date));
                    logger.debug(cal.getTime().toString());
                    int weekcode = cal.get(Calendar.DAY_OF_WEEK)-1;

                    logger.debug("weekcode:"+weekcode);

                    if(Integer.toString(weekcode).equals(paramMap.get("fDate")) && "0".equals(paramMap.get("fOpen"))){
                        f_conflict = "1";
                    }
                }
            }

            paramMap.put("fConflict",f_conflict);
            int ret = sqlSession.insert("holiday.insertSelective",paramMap);
            if(ret != 1){
                throw  new Exception("星期新增失败");
            }
            return 1;
        }

        if("GD".equals(fType)){
            int count = sqlSession.selectOne("holiday.selectCount",paramMap);
            if(count > 0){
                throw  new Exception("此日期已设置过节假日");
            }
            count = sqlSession.selectOne("order.selectCountGD",paramMap);
            String  fOpen = (String) paramMap.get("fOpen");
            String f_conflict="0";
            if(count > 0 && "0".equals(fOpen)){
                f_conflict="1";
            }

            paramMap.put("fConflict",f_conflict);
            int ret = sqlSession.insert("holiday.insertSelective",paramMap);
            if(ret != 1){
                throw  new Exception("通用日期新增失败");
            }
            return 1;
        }

        return 1;
    }

    public Object judge(Map paramMap) throws Exception {

        String fType = (String) paramMap.get("fType");

        if("FD".equals(fType)){
            int count = sqlSession.selectOne("order.selectCount",paramMap);
            String  fOpen = (String) paramMap.get("fOpen");
            String f_conflict="0";
            if(count > 0 && "0".equals(fOpen)){
                f_conflict="1";
            }

            if("1".equals(f_conflict)){
                throw new Exception("节假日与已有预约冲突!");

            }else{
                return 1;
            }
        }

        if("WK".equals(fType)){

            paramMap.put("fSection","PDYY");
            paramMap.put("fKey","PreDates");
            String value = sqlSession.selectOne("syscfg.selectKeyBySection",paramMap);

            if(value == null || value.length() == 0){
                value = "30";
            }

            String f_conflict="0";

            paramMap.put("fDateStart", DateUtil.now());
            paramMap.put("fDateEnd",DateUtil.getOffsiteDate(new Date(),9,1));
            List<Map> orderList = sqlSession.selectList("order.selectByPriamryKey", paramMap);
            if(orderList.size() > 0){
                for (Map map : orderList) {
                    String date = (String) map.get("fDate");
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(DateUtil.parseDate(date));
                    int weekcode = cal.get(Calendar.DAY_OF_WEEK)-1;
                    if(Integer.toString(weekcode).equals(paramMap.get("fDate")) && "0".equals(paramMap.get("fOpen"))){
                        f_conflict = "1";
                    }
                }
            }

            if("1".equals(f_conflict)){
                throw new Exception("节假日与已有预约冲突!");

            }else{
                return 1;
            }
        }

        if("GD".equals(fType)){
            int count = sqlSession.selectOne("order.selectCountGD",paramMap);
            String  fOpen = (String) paramMap.get("fOpen");
            String f_conflict="0";
            if(count > 0 && "0".equals(fOpen)){
                f_conflict="1";
            }

            if("1".equals(f_conflict)){
                throw new Exception("节假日与已有预约冲突!");

            }else{
                return 1;
            }
        }

        return 1;
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object update(Map paramMap) throws Exception {
        int otimes=0;
        int otimee=0;

        int amtime1 = 0;
        int amtime2 = 0;
        int pmtime1 = 0;
        int pmtime2 = 0;
        String f_conflict = "0";

        int orderCount = sqlSession.selectOne("order.selectCount", paramMap);
        if(orderCount > 0){
            List<Map> orderList = sqlSession.selectList("order.selectByPrimaryKey",paramMap);
            for (Map map : orderList) {
                String s=(String)map.get("fTime");
                System.out.println(s);
                otimes = Integer.parseInt(s.substring(0, 4));
                otimee = Integer.parseInt(s.substring(5, 9));
                System.out.println(otimes+"=="+otimee);
                if(!((amtime1<=otimes&&amtime2>=otimes)||(pmtime1<=otimes&&pmtime2>=otimee))){
                    f_conflict="1";
                    break;
                }
            }
        }

        paramMap.put("fConflict",f_conflict);

        int ret = sqlSession.insert("ordertime.updateByPrimaryKeySelective",paramMap);
        if(ret == 1){
            return 1;
        }else{
            throw new Exception("更新记录失败");
        }
    }

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object delete(Map paramMap) throws Exception {

        int ret = sqlSession.insert("holiday.deleteByPrimaryKey",paramMap);
        if(ret == 1){
            return 1;
        }else{
            throw new Exception("删除记录失败");
        }
    }

    @Autowired
    private SqlSession sqlSession;
}
