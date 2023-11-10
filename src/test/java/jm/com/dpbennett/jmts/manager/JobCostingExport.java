/*
Business Entity Library (BEL) - A foundational library for JSF web applications 
Copyright (C) 2018  D P Bennett & Associates Limited

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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import jm.com.dpbennett.business.entity.jmts.Job;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Test;

/**
 *
 * @author Desmond Bennett NB: REMOVE PWD WHEN DONE AND SET SKIP TEST TRUE
 */
public class JobCostingExport {

    @Test
    public void exportJobCostings() {
        HashMap prop = new HashMap();
        HashMap parameters = new HashMap();
        Job[] selectedJobs = {null};

        prop.put("javax.persistence.jdbc.user",
                "root");
        prop.put("javax.persistence.jdbc.password",
                "bsj0001");
        prop.put("javax.persistence.jdbc.url",
                "jdbc:mysql://172.16.0.10:3306/jmtstest");
        prop.put("javax.persistence.jdbc.driver",
                "com.mysql.jdbc.Driver");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU", prop);
        EntityManager em = emf.createEntityManager();

        selectedJobs[0] = Job.findJobById(em, 345052L);

        try {

            em.getTransaction().begin();
            Connection con = BusinessEntityUtils.getConnection(em);

            for (Job selectedJob : selectedJobs) {
                parameters.put("jobId", selectedJob.getId());
                parameters.put("contactPersonName", BusinessEntityUtils.getContactFullName(selectedJob.getContact()));
                parameters.put("customerAddress", selectedJob.getBillingAddress().toString());
                parameters.put("contactNumbers", selectedJob.getContact().getMainPhoneNumber().getLocalNumber());
                parameters.put("jobDescription", selectedJob.getJobDescription());
                parameters.put("totalCost", selectedJob.getJobCostingAndPayment().getTotalJobCostingsAmount());
                parameters.put("depositReceiptNumbers", selectedJob.getJobCostingAndPayment().getReceiptNumbers());
                parameters.put("discount", selectedJob.getJobCostingAndPayment().getDiscount().getDiscountValue());
                parameters.put("discountType", selectedJob.getJobCostingAndPayment().getDiscount().getDiscountValueType());
                parameters.put("deposit", selectedJob.getJobCostingAndPayment().getTotalPayment());
                parameters.put("amountDue", selectedJob.getJobCostingAndPayment().getAmountDue());
                parameters.put("totalTax", selectedJob.getJobCostingAndPayment().getTotalTax());
                parameters.put("totalTaxLabel", selectedJob.getJobCostingAndPayment().getTotalTaxLabel());
                parameters.put("grandTotalCostLabel", selectedJob.getJobCostingAndPayment().getTotalCostWithTaxLabel().toUpperCase().trim());
                parameters.put("grandTotalCost", selectedJob.getJobCostingAndPayment().getTotalCost());
                if (selectedJob.getJobCostingAndPayment().getCostingPreparedBy() != null) {
                    parameters.put("preparedBy",
                            selectedJob.getJobCostingAndPayment().getCostingPreparedBy().getFirstName() + " "
                            + selectedJob.getJobCostingAndPayment().getCostingPreparedBy().getLastName());
                }
                if (selectedJob.getJobCostingAndPayment().getCostingApprovedBy() != null) {
                    parameters.put("approvedBy",
                            selectedJob.getJobCostingAndPayment().getCostingApprovedBy().getFirstName() + " "
                            + selectedJob.getJobCostingAndPayment().getCostingApprovedBy().getLastName());
                }
                parameters.put("approvalDate",
                        BusinessEntityUtils.getDateInMediumDateFormat(
                                selectedJob.getJobStatusAndTracking().getDateCostingApproved()));

                //if (con != null) {
                try {
                    //StreamedContent streamContent;
                    // Compile report
                    ZipOutputStream zos;
                    // Creating ZipOutputStream for writing to zip file
                    File zipFile = new File("costing.zip");
                    FileOutputStream fos = new FileOutputStream(zipFile);
                    zos = new ZipOutputStream(fos);

                    JasperReport jasperReport
                            = JasperCompileManager.
                                    compileReport((String) SystemOption.getOptionValueObject(em, "jobCosting"));

                    // Generate report
                    JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, con);

                    byte[] fileBytes = JasperExportManager.exportReportToPdf(print);

                    zipFile(fileBytes, zos);
                    zos.close();
                    
                    em.getTransaction().commit();
                    
                    ///streamContent = new DefaultStreamedContent(new ByteArrayInputStream(fileBytes), "application/pdf", "Job Costing - " + selectedJob.getJobNumber() + ".pdf");
                    //return streamContent;
                } catch (JRException ex) {
                    System.out.println(ex);

                }

                // }
            }

        } catch (Exception e) {
            System.out.println(e);

        }
    }

    private static void zipFile(byte[] fileBytes, ZipOutputStream zos) {

        try {

            // Each file in the zipped archive is represented by a ZipEntry 
            // Only source file name is needed 
            ZipEntry ze = new ZipEntry("costing.pdf");
            zos.putNextEntry(ze);

            zos.write(fileBytes);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
