package com.jeremy.service;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class OrdertimeService {

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

        int count = sqlSession.selectOne("ordertime.selectCount",paramMap);
        if(count > 0){
            throw  new Exception("此机构已设置过预约时段!");
        }

        int otimes=0;
        int otimee=0;

        int amtime1 = 0;
        int amtime2 = 0;
        int pmtime1 = 0;
        int pmtime2 = 0;
        String f_conflict = "0";

        List<Map> orderList = sqlSession.selectList("order.selectByPrimaryKey", paramMap);
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

        paramMap.put("fConflict",f_conflict);

        int ret = sqlSession.insert("ordertime.insertSelective",paramMap);
        if(ret == 1){
            return 1;
        }else{
            throw new Exception("保存记录失败");
        }


    }

    public Object judge(Map paramMap) throws Exception {

        int otimes=0;
        int otimee=0;

        int amtime1 = 0;
        int amtime2 = 0;
        int pmtime1 = 0;
        int pmtime2 = 0;
        String f_conflict = "0";

        int orderCount = sqlSession.selectOne("order.selectCount", paramMap);
        if(orderCount > 0) {
            List<Map> orderList = sqlSession.selectList("order.selectByPrimaryKey", paramMap);
            for (Map map : orderList) {
                String s = (String) map.get("fTime");
                System.out.println(s);
                otimes = Integer.parseInt(s.substring(0, 4));
                otimee = Integer.parseInt(s.substring(5, 9));
                System.out.println(otimes + "==" + otimee);
                if (!((amtime1 <= otimes && amtime2 >= otimes) || (pmtime1 <= otimes && pmtime2 >= otimee))) {
                    f_conflict = "1";
                    break;
                }
            }
        }

        if("1".equals(f_conflict)){
            throw new Exception("与已有预约冲突，确定要修改预约时段吗？");

        }else{
            return 1;
        }
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

        int ret = sqlSession.insert("ordertime.deleteByPrimaryKey",paramMap);
        if(ret == 1){
            return 1;
        }else{
            throw new Exception("删除记录失败");
        }
    }

    @Autowired
    private SqlSession sqlSession;
}
