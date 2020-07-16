package com.batch.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class MakeExcelUtil {
    private static final String FILE_EXT1 = ".xls";
    private static final String FILE_EXT2 = ".xlsx";
    private static final String SHEET_NAME = "sheet";

    private static final String excelTemplatePath = "/Users/imc053/IdeaProjects/batch/src/main/resources/excelTemplate/";
    private static final String excelOutputPath = "/Users/imc053/Desktop/xmlFile/";

    public void statXlsDown(String type, List excelDataList) throws Exception {
        ModelMap model = new ModelMap();

        model.put("EXCEL_TEMPLATE_PATH", excelTemplatePath);
        model.put("excelTemplateName", type);
        model.put("excelOutputName", type);
        model.put("list",excelDataList);

        log.info("Make Excel : " + type);
        buildExcelDocument(model);
    }

    protected void buildExcelDocument(Map<String, Object> model) throws Exception {



        Workbook workbook;
        List resultList = (List)model.get("list");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat df1 = new SimpleDateFormat("yyyyMMdd");

        String excelTemplatePath = (String)model.get("EXCEL_TEMPLATE_PATH");
        String excelTemplateName = (String)model.get("excelTemplateName");
        String excelOutputName = (String)model.get("excelOutputName");

        if(!StringUtils.isEmpty(excelTemplatePath) && !StringUtils.isEmpty(excelTemplateName) && !StringUtils.isEmpty(excelOutputName)) {

            InputStream inputStream = null;
            String ext = FILE_EXT1;

            try {
                // 엑셀 템플릿을 스트림으로 읽어온다
                inputStream = new BufferedInputStream(new FileInputStream(excelTemplatePath + excelTemplateName + FILE_EXT1));
                ext = FILE_EXT1;
            } catch (Exception e) {
                inputStream = new BufferedInputStream(new FileInputStream(excelTemplatePath + excelTemplateName + FILE_EXT2));
                ext = FILE_EXT2;
            }

            List tempRowList = null;
            List<List> rowList = null;
            List<String> sheetNameList = null;

            if(resultList != null && resultList.size() > 0) {

                tempRowList = new ArrayList();
                rowList = new ArrayList<List>();
                sheetNameList = new ArrayList<String>();

                int sheetNumber = 1;
                int currentRow = 1;

                for(int index = 0; index < resultList.size(); index++) {
                    tempRowList.add(resultList.get(index));

                    if(currentRow == 1) { // 최초 시트
                        sheetNameList.add(SHEET_NAME + sheetNumber);
                        if(currentRow == resultList.size()) {
                            rowList.add(tempRowList);
                        }
                    } else if(currentRow % 65000 == 0) { // 한페이지 로우수를 65000개로 제한하고 나머지 로우를 다음시트에 add
                        rowList.add(tempRowList);
                        sheetNumber++;
                        sheetNameList.add(SHEET_NAME + sheetNumber);
                        tempRowList = new ArrayList();
                    } else if(currentRow == resultList.size()) { // 최종 시트
                        rowList.add(tempRowList);
                    }

                    currentRow++;
                }
            }

            XLSTransformer transformer = new XLSTransformer();
            Map<String, Object> resultMap = new HashMap<String, Object>();


            if(resultList != null && resultList.size() > 0) { // 데이터가 존재하는 경우 템플릿 출력
                workbook =  transformer.transformMultipleSheetsList(inputStream, rowList, sheetNameList, "resultList", resultMap, 0);
            } else { // 데이터가 없는경우 빈템플릿 출력
                resultMap.put("resultList", resultList);
                workbook =  transformer.transformXLS(inputStream, resultMap);
            }

            String fileName1 = excelOutputName +"_"+ df1.format(cal.getTime()) + ".xls";
            FileOutputStream fos = new FileOutputStream(excelOutputPath + fileName1);
            workbook.write(fos);
        }

    }



}
