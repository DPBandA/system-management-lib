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
     * @param costingFilename
     * @param costingFileBytes
     * @param zos
     */
    public static void zipFile(String costingFilename,
            byte[] costingFileBytes,
            ZipOutputStream zos) {

        try {
            ZipEntry ze = new ZipEntry(costingFilename);
            zos.putNextEntry(ze);
            zos.write(costingFileBytes);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

}
