/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jm.com.dpbennett.sm.util;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author dbennett
 */
public class FileUtils {

    /**
     * Adds an entry to a Zip file.
     * @param filename
     * @param fileBytes
     * @param zos
     */
    public static void zipFile(String filename,
            byte[] fileBytes,
            ZipOutputStream zos) {

        try {
            ZipEntry ze = new ZipEntry(filename);
            zos.putNextEntry(ze);
            zos.write(fileBytes);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}
