package com.jeremy.service;

import com.jeremy.exception.DBException;
import commonj.sdo.DataObject;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Jeremy on 2017/2/15.
 */

@Service
public class WebService {

    Logger logger = LoggerFactory.getLogger("com.jeremy.WebService");

    @Autowired
    private SqlSession sqlSession;

    public static String KEY_RET_CODE = "retCode";
    public static String KEY_RET_PARA = "retParas";
    public static String RET_CODE_SUCCESS = "0";

    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public Object service(Map paramMap) throws Exception {
        String tradeCodeList = (String) paramMap.get("tradeCodeList");
        String[] tradeCodeArray = tradeCodeList.split(",");
        String rollbackList = (String) paramMap.get("rollback");
        String[] rollbackArray = new String[0];
        if(rollbackList != null){
            rollbackArray = rollbackList.split(",");
        }

        int i = 0;
        for (String tradeCode : tradeCodeArray) {

            paramMap.put("tradeCode",tradeCode);
            int retCode = (int) dbinvoke(paramMap);
            if(rollbackArray.length > 0 && "1".equals(rollbackArray[i++])){

                if(SqlCommandType.INSERT == getSqlCommandType(tradeCode) ||
                        SqlCommandType.UPDATE == getSqlCommandType(tradeCode) ||
                        SqlCommandType.DELETE == getSqlCommandType(tradeCode)){
                    if(retCode <= 0){
                        throw new Exception("操作失败,数据已回滚");
                    }
                }
            }
        }
        return 1;
    }

    public Object dbinvoke(Map paramMap){

        String stmtName = (String) paramMap.get("tradeCode");
        logger.debug("stmtName:" + stmtName);

        MappedStatement ms = sqlSession.getConfiguration() .getMappedStatement(stmtName);
        SqlCommandType sqlct = ms.getSqlCommandType();
        String sql = ms.getBoundSql(paramMap).getSql();
        //存储过程
        if (startsWith(sql, "^\\s*\\{\\s*call")) {
            return invokeProcedure(stmtName, paramMap);
        }
        //插入语句
        if (SqlCommandType.INSERT == sqlct) {
            return Integer.valueOf(sqlSession.insert(stmtName, paramMap));
        }
        //更新语句
        if (SqlCommandType.UPDATE == sqlct) {
            return Integer.valueOf(sqlSession.update(stmtName, paramMap));
        }
        //删除
        if (SqlCommandType.DELETE == sqlct) {
            return Integer.valueOf(sqlSession.delete(stmtName, paramMap));
        }
        //选择
        if (SqlCommandType.SELECT == sqlct) {
            String offset = (String) paramMap.get("start");
            String limit = (String) paramMap.get("length");

            if(((String) paramMap.get("tradeCode")).indexOf("selectCount") >= 0){
                return sqlSession.selectOne(String.valueOf(paramMap.get("tradeCode")),paramMap);
            }

            if(isEmpty(offset) || isEmpty(limit)){
                logger.debug("2222");
                return sqlSession.selectList(stmtName, paramMap);
            }else{
                logger.debug("3333");
                logger.debug("start:"+offset);
                logger.debug("limit:"+limit);
                return sqlSession.selectList(stmtName, paramMap,new RowBounds(Integer.parseInt(offset),Integer.parseInt(limit)));
            }

        }

        throw new DBException(
                "exception.lsy.runtime.db.unsupportedMappedStmt",
                new Object[] { sqlct.toString(), stmtName });
    }

    public SqlCommandType getSqlCommandType(String tradeCode){
        MappedStatement ms = sqlSession.getConfiguration() .getMappedStatement(tradeCode);
        SqlCommandType sqlct = ms.getSqlCommandType();

        return sqlct;
    }


    protected Object invokeProcedure(String stmtName, Object parameter) {
        DefaultResultHandler rstHandler = new DefaultResultHandler();
        sqlSession.select(stmtName, parameter, rstHandler);
        if (containsKey(parameter, KEY_RET_CODE)) {
            String retCode = getValue(parameter, KEY_RET_CODE);

            if (isEmpty(retCode)) {
                throw new DBException(
                        "exception.lsy.runtime.db.nullRetCode",
                        new Object[] { stmtName }
                );
            }

            if (!RET_CODE_SUCCESS.equals(retCode)) {
                String retPara = getValue(parameter, KEY_RET_PARA);
                String[] retParas = (retPara == null ? "" : retPara).split("[\\s,]+");
                throw new DBException(retCode, retParas);
            }
        }
        return rstHandler.getResultList();
    }

    protected String getValue(Object container, String key) {
        if ((container instanceof Map)) {
            return (String) ((Map) container).get(key);
        }
        if ((container instanceof DataObject)) {
            return ((DataObject) container).getString(key);
        }
        throw new DBException(
                "exception.lsy.runtime.db.unsupportedParameterType",
                new Object[] { container.getClass().getName(),container.toString()}
        );
    }

    protected boolean containsKey(Object container, String key) {
        if ((container instanceof Map)) {
            return ((Map) container).containsKey(key);
        }
        if ((container instanceof DataObject)) {
            return ((DataObject) container).isSet(key);
        }
        throw new DBException(
                "exception.lsy.runtime.db.unsupportedParameterType",
                new Object[] { container.getClass().getName(),container.toString()}
        );
    }

    public static boolean isEmpty(String str) {
        return (str == null) || ("".equals(str.trim()));
    }

    public static boolean startsWith(String str, String regexp) {
        return Pattern.compile(regexp).matcher(str).find();
    }
}
