/*
Job Management & Tracking System (JMTS) 
Copyright (C) 2024  D P Bennett & Associates Limited

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.jmts.JobSample;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.jmts.Job;
import jm.com.dpbennett.business.entity.gm.BusinessEntityManagement;
import jm.com.dpbennett.business.entity.sm.User;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.hrm.manager.HumanResourceManager;
import jm.com.dpbennett.jmts.JobSampleDataModel;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DialogFrameworkOptions;

/**
 *
 * @author Desmond Bennett
 */
public class JobSampleManager implements Serializable, BusinessEntityManagement {

    private JobSample selectedJobSample;
    private Integer jobSampleDialogTabViewActiveIndex;
    private JobManager jobManager;
    private Boolean cancelEdit;

    public JobSampleManager() {
        init();
    }

    private void init() {
        selectedJobSample = new JobSample();
        jobSampleDialogTabViewActiveIndex = 0;
    }

    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public HumanResourceManager getHumanResourceManager() {
        return BeanUtils.findBean("humanResourceManager");
    }

    public void jobSamplesDialogReturn() {

        if (getCurrentJob().getId() != null) {
            if (getCurrentJob().getIsDirty()) {
                if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {

                    getJobManager().processJobActions();
                    getCurrentJob().getJobStatusAndTracking().setEditStatus("");
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Job Samples"
                            + " Saved", "This job"
                            + " and the job samples were saved", FacesMessage.SEVERITY_INFO);

                } else {
                    PrimeFacesUtils.addMessage(getCurrentJob().getType()
                            + " Job Samples"
                            + " NOT Saved", "This job"
                            + " and the job sampples were NOT saved",
                            FacesMessage.SEVERITY_ERROR);
                }
            }

        }
    }

    public void okJobSamples(ActionEvent actionEvent) {

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void cancelJobSamplesEdit(ActionEvent actionEvent) {

        getCurrentJob().setIsDirty(false);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void openJobSamplesDialog() {
        if (getCurrentJob().getId() != null && !getCurrentJob().getIsDirty()) {

            editJobSamples();

        } else {

            if (getJobManager().getCurrentJob().getIsDirty()) {
                getJobManager().saveCurrentJob();
            }

            if (getCurrentJob().getId() != null) {
                editJobSamples();
            } else {
                PrimeFacesUtils.addMessage(getCurrentJob().getType() + " NOT Saved",
                        "This " + getCurrentJob().getType()
                        + " must be saved before job samples can be viewed or edited",
                        FacesMessage.SEVERITY_WARN);
            }
        }
    }

    public void editJobSamples() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 300) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/sample/jobSamplesDialog", options, null);

    }

    public Integer getDialogHeight() {
        return 400;
    }

    public Integer getDialogWidth() {
        return 500;
    }

    public Boolean getCancelEdit() {
        return cancelEdit;
    }

    public void setCancelEdit(Boolean cancelEdit) {
        this.cancelEdit = cancelEdit;
    }

    /*
     * NB: Methods may be put in system options and not hard coded.
     * The user's organization could be used in the code.
     * The number of days for sample collection could be put as a system option 
     * instead of being hard coded.
     */
    public List getMethodsOfDisposal() {
        ArrayList methods = new ArrayList();

        Integer days
                = (Integer) SystemOption.getOptionValueObject(
                        getSystemManager().getEntityManager1(),
                        "sampleCollectionDays");

        String org
                = (String) SystemOption.getOptionValueObject(
                        getSystemManager().getEntityManager1(),
                        "organizationName");

        methods.add(new SelectItem("1", "Collected by the client within " + days + " days"));
        if (org != null) {
            methods.add(new SelectItem("2", "Disposed of by " + org));
        } else {
            methods.add(new SelectItem("2", "Disposed of by us"));
        }
        methods.add(new SelectItem("3", "To be determined"));

        return methods;
    }

    public JobManager getJobManager() {
        if (jobManager == null) {
            jobManager = BeanUtils.findBean("jobManager");
        }
        return jobManager;
    }

    public void reset() {
        init();
    }

    public Boolean isSamplesDirty() {
        Boolean dirty = false;

        for (JobSample jobSample : getCurrentJob().getJobSamples()) {
            dirty = dirty || jobSample.getIsDirty();
        }

        return dirty;
    }

    public void createNewJobSample(ActionEvent event) {

        if (getCurrentJob().hasOnlyDefaultJobSample()) {
            selectedJobSample = getCurrentJob().getJobSamples().get(0);
            selectedJobSample.setDescription("");
            selectedJobSample.setIsToBeAdded(false);
        } else {
            selectedJobSample = new JobSample();
            selectedJobSample.setIsToBeAdded(true);
            // Init sample
            selectedJobSample.setJobId(getCurrentJob().getId());
            selectedJobSample.setSampleQuantity(1L);
            selectedJobSample.setQuantity(1L);

            selectedJobSample.setReferenceIndex(getCurrentNumberOfJobSamples());

            if (selectedJobSample.getSampleQuantity() == 1L) {
                selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()));
            } else {
                selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()) + "-"
                        + BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()
                                + selectedJobSample.getSampleQuantity() - 1));
            }
        }

        selectedJobSample.setDateSampled(new Date());
        jobSampleDialogTabViewActiveIndex = 0;

        editJobSample(event);
    }

    public User getUser() {
        return getJobManager().getUser();
    }

    @Override
    public void setIsDirty(Boolean dirty) {
        selectedJobSample.setIsDirty(dirty);
    }

    @Override
    public Boolean getIsDirty() {
        return selectedJobSample.getIsDirty();
    }

    public Boolean isCurrentJobDirty() {
        return getCurrentJob().getIsDirty();
    }

    public void setCurrentJobDirty() {
        getCurrentJob().setIsDirty(true);
    }

    public void jobSampleDialogReturn() {

        if (!getCancelEdit()) {
            if (getCurrentJob().getId() != null) {
                if (!isCurrentJobDirty() && getSelectedJobSample().getIsDirty()) {
                    if (getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {
                        getSelectedJobSample().setIsDirty(false);
                        PrimeFacesUtils.addMessage("Sample(s) and Job Saved", "This job and the edited/added sample(s) were saved", FacesMessage.SEVERITY_INFO);
                    }
                } else if (isCurrentJobDirty() && getSelectedJobSample().getIsDirty()) {
                    PrimeFacesUtils.addMessage("Sample(s) Added/Edited", "Save this job if you wish to keep the changes", FacesMessage.SEVERITY_WARN);
                } else if (isCurrentJobDirty() && !getSelectedJobSample().getIsDirty()) {
                    PrimeFacesUtils.addMessage("Job to be Saved", "Sample(s) not edited but this job was previously edited but not saved", FacesMessage.SEVERITY_WARN);
                } else if (!isCurrentJobDirty() && !getSelectedJobSample().getIsDirty()) {
                    // Nothing to do yet
                }
            }
        }
    }

    /**
     * To be applied when sample if saved
     *
     * @param event
     */
    public void updateSampleQuantity(AjaxBehaviorEvent event) {
        getSelectedJobSample().setIsDirty(true);
        updateSampleReference();
    }

    private void updateSampleReference() {
        // update reference while ensuring number of samples is not less than 1
        // or greater than 700 (for now but to be made system option)        
        if (selectedJobSample.getSampleQuantity() != null) {
            if (selectedJobSample.getSampleQuantity() == 1) {
                selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()));
            } else {
                selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()) + "-"
                        + BusinessEntityUtils.getAlphaCode(selectedJobSample.getReferenceIndex()
                                + selectedJobSample.getSampleQuantity() - 1));
            }
        }
    }

    public void closeJobSampleDeleteConfirmDialog() {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public Integer getJobSampleDialogTabViewActiveIndex() {
        return jobSampleDialogTabViewActiveIndex;
    }

    public void setJobSampleDialogTabViewActiveIndex(Integer jobSampleDialogTabViewActiveIndex) {
        this.jobSampleDialogTabViewActiveIndex = jobSampleDialogTabViewActiveIndex;
    }

    public void updateSample(AjaxBehaviorEvent event) {
        if (event.getComponent() != null) {
            getSelectedJobSample().setIsDirty(true);
        }
    }

    public void okJobSample() {
        EntityManager em = getHumanResourceManager().getEntityManager1();
        updateSampleReference();
        if (selectedJobSample.getIsToBeAdded()) {
            getCurrentJob().getJobSamples().add(selectedJobSample);
        }

        selectedJobSample.setIsToBeAdded(false);

        setNumberOfSamples();

        updateSampleReferences();

        // Update department
        if (!getCurrentJob().getDepartment().getName().equals("")) {
            Department department = Department.findByName(
                    em,
                    getCurrentJob().getDepartment().getName());

            if (department != null) {
                getCurrentJob().setDepartment(department);
            }
        }
        // Update subcontracted department
        if (!getCurrentJob().getSubContractedDepartment().getName().equals("")) {
            Department subContractedDepartment
                    = Department.findByName(
                            em,
                            getCurrentJob().getSubContractedDepartment().getName());
            getCurrentJob().setSubContractedDepartment(subContractedDepartment);
        }

        if (getCurrentJob().getAutoGenerateJobNumber()) {
            getCurrentJob().setJobNumber(getCurrentJobNumber());
        }
        jobSampleDialogTabViewActiveIndex = 0;

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void deleteJobSample() {

        // update number of samples
        if ((getCurrentJob().getNumberOfSamples() - selectedJobSample.getSampleQuantity()) > 0) {
            getCurrentJob().setNumberOfSamples(getCurrentJob().getNumberOfSamples() - selectedJobSample.getSampleQuantity());
        } else {
            getCurrentJob().setNumberOfSamples(0L);
        }

        List<JobSample> samples = getCurrentJob().getJobSamples();
        int index = 0;
        for (JobSample sample : samples) {
            if (sample.getReference().equals(selectedJobSample.getReference())) {
                // removed sample record
                samples.remove(index);
                break;
            }
            ++index;
        }

        updateSampleReferences();

        if (getCurrentJob().getAutoGenerateJobNumber()) {
            getCurrentJob().setJobNumber(getCurrentJobNumber());
        }

        // Do job save if possible...
        if (getCurrentJob().getId() != null
                && getCurrentJob().prepareAndSave(getEntityManager1(), getUser()).isSuccess()) {
            PrimeFacesUtils.addMessage("Job Saved",
                    "Sample(s) deleted and the job was saved", FacesMessage.SEVERITY_INFO);
        } else {
            setCurrentJobDirty();
            PrimeFacesUtils.addMessage("Job NOT Saved",
                    "Sample(s) deleted but the job was not saved", FacesMessage.SEVERITY_WARN);
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public JobSampleDataModel getSamplesModel() {
        return new JobSampleDataModel(getCurrentJob().getFilteredJobSamples());
    }

    public void editJobSample(ActionEvent event) {
        jobSampleDialogTabViewActiveIndex = 0;
        setCancelEdit(false);

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/sample/jobSampleDialog", options, null);

    }

    public void openJobSampleDeleteConfirmDialog(ActionEvent event) {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() - 125) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/sample/jobSampleDeleteConfirmDialog", options, null);

    }

    public void doCopyJobSample() {

        selectedJobSample = new JobSample(selectedJobSample);
        selectedJobSample.setReferenceIndex(getCurrentNumberOfJobSamples());
        // Init sample    
        if (selectedJobSample.getSampleQuantity() == 1L) {
            selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(getCurrentNumberOfJobSamples()));
        } else {
            selectedJobSample.setReference(BusinessEntityUtils.getAlphaCode(getCurrentNumberOfJobSamples()) + "-"
                    + BusinessEntityUtils.getAlphaCode(getCurrentNumberOfJobSamples()
                            + selectedJobSample.getSampleQuantity() - 1));
        }

        jobSampleDialogTabViewActiveIndex = 0;

    }

    public void copyJobSample() {

        DialogFrameworkOptions options = DialogFrameworkOptions.builder()
                .modal(true)
                .fitViewport(true)
                .responsive(true)
                .width((getDialogWidth() + 200) + "px")
                .contentWidth("100%")
                .resizeObserver(true)
                .resizeObserverCenter(true)
                .resizable(false)
                .styleClass("max-w-screen")
                .iframeStyleClass("max-w-screen")
                .build();

        PrimeFaces.current().dialog().openDynamic("/job/sample/jobSampleDialog", options, null);

    }

    public void setCopySelectedJobSample(JobSample selectedJobSample) {
        this.selectedJobSample = selectedJobSample;
        if (selectedJobSample != null) {
            doCopyJobSample();
            this.selectedJobSample.setIsToBeAdded(true);
            this.selectedJobSample.setIsDirty(true);
        }
    }

    public void cancelJobSampleDialogEdits() {

        setCancelEdit(true);

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    private Long getCurrentNumberOfJobSamples() {
        if (getCurrentJob().getNumberOfSamples() == null) {
            getCurrentJob().setNumberOfSamples(0L);
            return getCurrentJob().getNumberOfSamples();
        } else {
            return getCurrentJob().getNumberOfSamples();
        }
    }

    /**
     * Checks maximum allowed samples and groups. Currently not used
     */
    public void checkNumberOfJobSamplesAndGroups() {
        EntityManager em = getSystemManager().getEntityManager1();

        // check for max sample
        int maxSamples = (Integer) SystemOption.getOptionValueObject(em,
                "maximumJobSamples");
        if (getCurrentNumberOfJobSamples() == maxSamples) {
            PrimeFaces.current().ajax().addCallbackParam("maxJobSamplesReached", true);
        }
        // check for max sample groups
        int maxGropus = (Integer) SystemOption.getOptionValueObject(em,
                "maximumJobSampleGroups");
        if (getCurrentJob().getJobSamples().size() == maxGropus) {
            PrimeFaces.current().ajax().addCallbackParam("maxJobSampleGroupsReached", true);
        }
    }

    private void setNumberOfSamples() {
        getCurrentJob().setNumberOfSamples(0L);
        for (int i = 0; i < getCurrentJob().getJobSamples().size(); i++) {
            if (getCurrentJob().getJobSamples().get(i).getSampleQuantity() == null) {
                getCurrentJob().getJobSamples().get(i).setSampleQuantity(1L);
            }
            getCurrentJob().setNumberOfSamples(getCurrentJob().getNumberOfSamples()
                    + getCurrentJob().getJobSamples().get(i).getSampleQuantity());
        }
    }

    private void updateSampleReferences() {
        Long refIndex = 0L;

        ArrayList<JobSample> samplesCopy = new ArrayList<>(getCurrentJob().getJobSamples());
        getCurrentJob().getJobSamples().clear();

        for (JobSample jobSample : samplesCopy) {

            jobSample.setReferenceIndex(refIndex);
            if (jobSample.getSampleQuantity() == 1) {
                jobSample.setReference(BusinessEntityUtils.getAlphaCode(refIndex));
            } else {
                jobSample.setReference(BusinessEntityUtils.getAlphaCode(refIndex) + "-"
                        + BusinessEntityUtils.getAlphaCode(refIndex + jobSample.getSampleQuantity() - 1));
            }

            getCurrentJob().getJobSamples().add(jobSample);

            refIndex = refIndex + jobSample.getSampleQuantity();

        }
    }

    public Job getCurrentJob() {
        return getJobManager().getCurrentJob();
    }

    public JobSample getSelectedJobSample() {
        return selectedJobSample;
    }

    public void setSelectedJobSample(JobSample selectedJobSample) {
        this.selectedJobSample = selectedJobSample;
    }

    public String getCurrentJobNumber() {
        return Job.generateJobNumber(getCurrentJob(), getEntityManager1());
    }

    private EntityManager getEntityManager1() {
        return getJobManager().getEntityManager1();
    }

}
