import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Ankur Luthra
 *
 */
public class OneLine_FolderStructure_Parser {

  static String filePath =
      "C:\\Users\\ankurl\\AppData\\Roaming\\Skype\\My Skype Received Files\\Folder Structure_SunSpec.xlsx";
  static int sheetNo = 1;


  public static void main(String args[]) throws IOException {
    List<String> rowData;
    List<List<String>> fileData = new ArrayList<List<String>>();
    try {
      FileInputStream excelFile = new FileInputStream(new File(filePath));
      Workbook workbook = new XSSFWorkbook(excelFile);
      Sheet datatypeSheet = workbook.getSheetAt(sheetNo);// sheet number in excel
      Iterator<Row> iterator = datatypeSheet.iterator();
      while (iterator.hasNext()) {
        Row currentRow = iterator.next();
        Iterator<Cell> cellIterator = currentRow.iterator();
        rowData = new ArrayList<String>();
        while (cellIterator.hasNext()) {
          Cell currentCell = cellIterator.next();
          // System.out.print(currentCell.getStringCellValue() + "\t");
          rowData.add(currentCell.getStringCellValue());
        }
        // System.out.println();
        fileData.add(rowData);
      }
    } catch (Exception e) {
      // TODO: handle exception
    }
    createFile(fileData);
  }

  public static void createFile(List<List<String>> fileData) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    XSSFSheet sheet = workbook.createSheet("Sheet 1");

    int rowCount = 0;
    Row row = sheet.createRow(rowCount++);
    for (List<String> rowData : fileData) {

      int columnCount = 0;

      for (Object field : rowData) {
        Cell cell = row.createCell(columnCount++);
        if (field instanceof String) {
          cell.setCellValue((String) field);
        } else if (field instanceof Integer) {
          cell.setCellValue((Integer) field);
        }
        row = sheet.createRow(rowCount++);
        if (columnCount > 2)
          columnCount = 0;
      }

    }

    try (FileOutputStream outputStream =
        new FileOutputStream("E://Formatted FolderStructure.xlsx")) {
      workbook.write(outputStream);
      workbook.close();
      System.out.println("File exported");
    }
  }
}

