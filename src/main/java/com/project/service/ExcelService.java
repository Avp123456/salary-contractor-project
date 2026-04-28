package com.project.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelService {

    public List<List<String>> parseExcel(MultipartFile file) {
        try {
            return parseExcel(file.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<List<String>> parseExcel(byte[] fileBytes) {
        List<List<String>> data = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(fileBytes))) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> rowData = new ArrayList<>();

                int lastCell = row.getLastCellNum();

                for (int j = 0; j < lastCell; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.add(getCellValue(cell));
                }

                data.add(rowData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        String value = "";
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    value = new java.math.BigDecimal(String.valueOf(val)).toPlainString();
                }
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    org.apache.poi.ss.usermodel.CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING: value = cellValue.getStringValue(); break;
                        case NUMERIC: 
                            value = new java.math.BigDecimal(String.valueOf(cellValue.getNumberValue())).toPlainString();
                            break;
                        case BOOLEAN: value = String.valueOf(cellValue.getBooleanValue()); break;
                        default: value = ""; break;
                    }
                } catch (Exception e) {
                    value = cell.getCellFormula();
                }
                break;
            default:
                value = "";
                break;
        }
        
        // Final cleanup: remove .0 from integers and remove any lingering commas
        if (value.endsWith(".0")) {
            value = value.substring(0, value.length() - 2);
        }
        return value.replace(",", "");
    }
}