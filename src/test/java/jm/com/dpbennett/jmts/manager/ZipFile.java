/*
Job Management & Tracking System (JMTS) 
Copyright (C) 2026  D P Bennett & Associates Limited

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Email: info@dpbennett.com.jm
 */
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

//    @Test
//    public void createZip() {
//        // source file
//        String fileName = "costing.pdf";
//        File file = new File(fileName);
//        //Creating zipfile name from fileName by 
//        // truncating .txt and appending .zip
//        String zipFilename = fileName.substring(0, fileName.indexOf('.')) + ".zip";
//        File zipFile = new File(zipFilename);
//        zipFile(file, zipFile);
//    }
//
//    // Method to zip file
//    private static void zipFile(File file, File zippedFile) {
//        final int BUFFER = 1024;
//        ZipOutputStream zos = null;
//        BufferedInputStream bis = null;
//        try {
//            FileInputStream fis = new FileInputStream(file);
//            bis = new BufferedInputStream(fis, BUFFER);
//            // Creating ZipOutputStream for writing to zip file
//            FileOutputStream fos = new FileOutputStream(zippedFile);
//            zos = new ZipOutputStream(fos);
//            // Each file in the zipped archive is represented by a ZipEntry 
//            // Only source file name is needed 
//            ZipEntry ze = new ZipEntry(file.getName());
//            zos.putNextEntry(ze);
//            byte data[] = new byte[BUFFER];
//            int count;
//            while ((count = bis.read(data, 0, BUFFER)) != -1) {
//                zos.write(data, 0, count);
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            try {
//                zos.close();
//                bis.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
}
