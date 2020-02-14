package csp;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.LOG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AcReport {
    public static void writeToXLS(String inputFile) throws IOException, InvalidFormatException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test");
        File dir = new File("./tests");
        File[] directoryListing = dir.listFiles();
        int startingRowIdx = 3;
        int id = 1;
        if (directoryListing != null) {
            Arrays.sort(directoryListing, (f1, f2) -> {
                return f1.getName().compareTo(f2.getName());
            });
            for (File child : directoryListing) {
                if (child.getName().equals("zebra-intension-nonbinary.xml")) { continue; }

                MyParser parserAc1 = new MyParser(child.getPath());
                ProblemInstance piAc1 = parserAc1.parse();
                CSPSolver cspSolverAc1 = new CSPSolver(piAc1);
                cspSolverAc1.arcConsistency1();

                Row row = sheet.createRow(startingRowIdx);
                row.createCell(0).setCellValue(id);
                row.createCell(1).setCellValue(child.getName());
                row.createCell(2).setCellValue(cspSolverAc1.getCc());
                row.createCell(3).setCellValue(cspSolverAc1.getCpuTime());
                row.createCell(4).setCellValue(cspSolverAc1.getFval());
                row.createCell(5).setCellValue(cspSolverAc1.getiSize());
                row.createCell(6).setCellValue(cspSolverAc1.getfSize() != null ? ""+cspSolverAc1.getfSize() : "FALSE");
                row.createCell(7).setCellValue(cspSolverAc1.getfSize() != null ? ""+cspSolverAc1.getfEffect() : "FALSE");


                //AC3
                MyParser parserAc3 = new MyParser(child.getPath());
                ProblemInstance piAc3 = parserAc3.parse();
                CSPSolver cspSolverAc3 = new CSPSolver(piAc3);
                cspSolverAc3.arcConsistency3();

                row.createCell(8).setCellValue(cspSolverAc3.getCc());
                row.createCell(9).setCellValue(cspSolverAc3.getCpuTime());
                row.createCell(10).setCellValue(cspSolverAc3.getFval());
                row.createCell(11).setCellValue(cspSolverAc3.getiSize());
                row.createCell(12).setCellValue(cspSolverAc3.getfSize() != null ? ""+cspSolverAc3.getfSize() : "FALSE");
                row.createCell(13).setCellValue(cspSolverAc3.getfSize() != null ? ""+cspSolverAc3.getfEffect() : "FALSE");

                startingRowIdx++; id++;
            }
            FileOutputStream fos = new FileOutputStream("./results/new.xlsx");
            wb.write(fos);
            fos.close();
            wb.close();
            return;
        }
        LOG.info("Nothing under tests/folder");
        FileOutputStream fos = new FileOutputStream("./results/new.xlsx");
        wb.write(fos);
        fos.close();
        wb.close();
    }
}
