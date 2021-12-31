package com.jeremy.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
/**
 * Created by Jeremy on 2017/2/20.
 */
public class Test {
    public static void main(String[] args) {
        Test obj=new Test();
        File file = new File("D:/history.xls");

        List excelList = obj.readExcel(file);
        System.out.println(excelList);
        JSONArray jsonarr = new JSONArray();
        for(int index = 0; index < excelList.size(); index++){
                List inner= (List) excelList.get(index);
                System.out.println("inner=========>"+inner);



//                System.out.println("ss===>"+inner.get(i));

                JSONObject jo = new JSONObject();
                jo.put("fDeptno",inner.get(0));
                jo.put("sj_kksum",inner.get(2));
                jo.put("sj_keksum",inner.get(3));
                jo.put("sj_dxqysum",inner.get(4));
                jo.put("sj_dsqysum",inner.get(5));
                jo.put("sj_sjqysum",inner.get(6));
                jo.put("sj_wyqysum",inner.get(7));
                jsonarr.add(jo);



        }

        for(int i=0;i<jsonarr.size();i++){

        }
        System.out.println(jsonarr.toJSONString());

    }

    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
    public List readExcel(File file) {
        try {
            System.out.println(file.getAbsolutePath());
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                List<List> outerList=new ArrayList<List>();
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 0; i < sheet.getRows(); i++) {
                    List innerList=new ArrayList();
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if(cellinfo.isEmpty()){
                            innerList.add("0");
                            continue;
                        }
                        innerList.add(cellinfo);

                    }
                    outerList.add(i, innerList);

                }
                return outerList;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}