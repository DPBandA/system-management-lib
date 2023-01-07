package jm.com.dpbennett.jmts.manager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;

public class ZipFile {

    @Test
    public void createZip() {
        // source file
        String fileName = "costing.pdf";
        File file = new File(fileName);
        //Creating zipfile name from fileName by 
        // truncating .txt and appending .zip
        String zipFilename = fileName.substring(0, fileName.indexOf('.')) + ".zip";
        File zipFile = new File(zipFilename);
        zipFile(file, zipFile);
    }

    // Method to zip file
    private static void zipFile(File file, File zippedFile) {
        final int BUFFER = 1024;
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, BUFFER);
            // Creating ZipOutputStream for writing to zip file
            FileOutputStream fos = new FileOutputStream(zippedFile);
            zos = new ZipOutputStream(fos);
            // Each file in the zipped archive is represented by a ZipEntry 
            // Only source file name is needed 
            ZipEntry ze = new ZipEntry(file.getName());
            zos.putNextEntry(ze);
            byte data[] = new byte[BUFFER];
            int count;
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zos.write(data, 0, count);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                zos.close();
                bis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
