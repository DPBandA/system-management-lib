/*
Human Resource Management (HRM) 
Copyright (C) 2023  D P Bennett & Associates Limited

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
package jm.com.dpbennett.hrm.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.persistence.EntityManager;
import jm.com.dpbennett.business.entity.hrm.Address;
import jm.com.dpbennett.business.entity.hrm.Business;
import jm.com.dpbennett.business.entity.hrm.BusinessOffice;
import jm.com.dpbennett.business.entity.hrm.Contact;
import jm.com.dpbennett.business.entity.hrm.Department;
import jm.com.dpbennett.business.entity.hrm.DepartmentUnit;
import jm.com.dpbennett.business.entity.hrm.Division;
import jm.com.dpbennett.business.entity.hrm.Employee;
import jm.com.dpbennett.business.entity.hrm.EmployeePosition;
import jm.com.dpbennett.business.entity.hrm.Internet;
import jm.com.dpbennett.business.entity.hrm.Laboratory;
import jm.com.dpbennett.business.entity.hrm.Manufacturer;
import jm.com.dpbennett.business.entity.sm.Preference;
import jm.com.dpbennett.business.entity.hrm.Subgroup;
import jm.com.dpbennett.business.entity.rm.DatePeriod;
import jm.com.dpbennett.business.entity.sm.Notification;
import jm.com.dpbennett.business.entity.sm.SystemOption;
import jm.com.dpbennett.business.entity.util.BusinessEntityUtils;
import jm.com.dpbennett.sm.manager.GeneralManager;
import jm.com.dpbennett.sm.validator.AddressValidator;
import jm.com.dpbennett.sm.validator.ContactValidator;
import jm.com.dpbennett.sm.manager.SystemManager;
import jm.com.dpbennett.sm.util.BeanUtils;
import jm.com.dpbennett.sm.util.MainTabView;
import jm.com.dpbennett.sm.util.PrimeFacesUtils;
import jm.com.dpbennett.sm.util.Utils;
import static jm.com.dpbennett.sm.manager.SystemManager.getStringListAsSelectItems;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Desmond Bennett
 */
public class HumanResourceManager extends GeneralManager implements Serializable {

    private Boolean isActiveEmployeesOnly;
    private Boolean isActiveEmployeePositionsOnly;
    private Boolean isActiveDepartmentsOnly;
    private Boolean isActiveBusinessesOnly;
    private Boolean isActiveSubgroupsOnly;
    private Boolean isActiveDivisionsOnly;
    private Boolean isActiveManufacturersOnly;
    private String employeeSearchText;
    private String employeePositionSearchText;
    private String departmentSearchText;
    private String businessSearchText;
    private String subgroupSearchText;
    private String divisionSearchText;
    private String manufacturerSearchText;
    private List<Employee> foundEmployees;
    private List<EmployeePosition> foundEmployeePositions;
    private List<Department> foundDepartments;
    private List<Business> foundBusinesses;
    private List<Subgroup> foundSubgroups;
    private List<Division> foundDivisions;
    private List<Manufacturer> foundManufacturers;
    private DualListModel<Employee> employeeDualList;
    private DualListModel<Department> departmentDualList;
    private DualListModel<Subgroup> subgroupDualList;
    private Employee selectedEmployee;
    private EmployeePosition selectedEmployeePosition;
    private Department selectedDepartment;
    private Subgroup selectedSubgroup;
    private Division selectedDivision;
    private Business selectedBusiness;
    private Contact selectedContact;
    private Address selectedAddress;
    private Manufacturer selectedManufacturer;
    private Boolean edit;

    public HumanResourceManager() {
        init();
    }

    @Override
    public final void init() {
        reset();
    }

    public List<SelectItem> getManufacturerStatuses() {
        ArrayList statuses = new ArrayList();

        statuses.addAll(getStringListAsSelectItems(getEntityManager1(), "factoryStatusList"));

        return statuses;
    }

    public List<SelectItem> getRegistrationStatuses() {
        ArrayList statuses = new ArrayList();

        statuses.addAll(getStringListAsSelectItems(getEntityManager1(), "registrationStatusList"));

        return statuses;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public List<SelectItem> getDepartmentLabelList() {

        return getStringListAsSelectItems(getEntityManager1(),
                "departmentLabels");
    }

    public void openManufacturerBrowser() {
        getMainTabView().openTab("Manufacturers");
    }

    public List<Manufacturer> completeManufacturer(String query) {
        return Manufacturer.findManufacturersBySearchPattern(getEntityManager1(), query);
    }

    public List<String> completePreferenceValue(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<String> preferenceValues = Preference.findAllPreferenceValues(em, query);

            return preferenceValues;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public SystemManager getSystemManager() {
        return BeanUtils.findBean("systemManager");
    }

    public static String getDepartmentFullCode(
            EntityManager em,
            Department department) {

        String code = department.getCode();

        Subgroup subgroup = Subgroup.findByDepartment(em, department);
        if (subgroup != null) {
            code = code + "-" + subgroup.getCode();
            Division division = Division.findBySubgroup(em, subgroup);
            if (division != null) {
                code = code + "-" + division.getCode();
            }
        }

        return code;
    }

    public List<Laboratory> completeLaboratory(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Laboratory> laboratories = Laboratory.findLaboratoriesByName(em, query);

            return laboratories;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<DepartmentUnit> completeDepartmentUnit(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<DepartmentUnit> departmentUnits = DepartmentUnit.findDepartmentUnitsByName(em, query);

            return departmentUnits;

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    public List<Employee> completeActiveEmployee(String query) {
        EntityManager em;

        try {

            em = getEntityManager1();
            List<Employee> employees = Employee.findActiveEmployeesByName(em, query);

            if (employees != null) {
                return employees;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<EmployeePosition> completeActiveEmployeePosition(String query) {
        EntityManager em;

        try {

            em = getEntityManager1();
            List<EmployeePosition> employeePositions
                    = EmployeePosition.findActiveEmployeePositionsByTitle(em, query);

            if (employeePositions != null) {
                return employeePositions;
            } else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.out.println(e);
            return new ArrayList<>();
        }
    }

    public List<Department> completeActiveDepartment(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<Department> departments = Department.findActiveDepartmentsByName(em, query);

            return departments;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<BusinessOffice> completeActiveBusinessOffice(String query) {
        EntityManager em;

        try {
            em = getEntityManager1();

            List<BusinessOffice> businessOffices = BusinessOffice.findActiveBusinessOfficesByName(em, query);

            return businessOffices;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Boolean getIsActiveEmployeePositionsOnly() {
        return isActiveEmployeePositionsOnly;
    }

    public void setIsActiveEmployeePositionsOnly(Boolean isActiveEmployeePositionsOnly) {
        this.isActiveEmployeePositionsOnly = isActiveEmployeePositionsOnly;
    }

    public List<EmployeePosition> getFoundEmployeePositions() {
        if (foundEmployeePositions == null) {
            foundEmployeePositions = EmployeePosition.findAllActiveEmployeePositions(getEntityManager1());
        }
        return foundEmployeePositions;
    }

    public void setFoundEmployeePositions(List<EmployeePosition> foundEmployeePositions) {
        this.foundEmployeePositions = foundEmployeePositions;
    }

    public String getEmployeePositionSearchText() {
        return employeePositionSearchText;
    }

    public void setEmployeePositionSearchText(String employeePositionSearchText) {
        this.employeePositionSearchText = employeePositionSearchText;
    }

    public EmployeePosition getSelectedEmployeePosition() {
        if (selectedEmployeePosition == null) {
            selectedEmployeePosition = new EmployeePosition();
        }
        return selectedEmployeePosition;
    }

    public void setSelectedEmployeePosition(EmployeePosition selectedEmployeePosition) {
        this.selectedEmployeePosition = selectedEmployeePosition;
    }

    @Override
    public String getApplicationHeader() {
        return "Human Resource Manager";
    }

    public List<Division> getFoundDivisions() {
        if (foundDivisions == null) {
            foundDivisions = Division.findAllActive(getEntityManager1());
        }
        return foundDivisions;
    }

    public String getDivisionSearchText() {
        return divisionSearchText;
    }

    public void setDivisionSearchText(String divisionSearchText) {
        this.divisionSearchText = divisionSearchText;
    }

    public Boolean getIsActiveDivisionsOnly() {
        return isActiveDivisionsOnly;
    }

    public void setIsActiveDivisionsOnly(Boolean isActiveDivisionsOnly) {
        this.isActiveDivisionsOnly = isActiveDivisionsOnly;
    }

    public DualListModel<Employee> getEmployeeDualList() {
        return employeeDualList;
    }

    public void setEmployeeDualList(DualListModel<Employee> employeeDualList) {
        this.employeeDualList = employeeDualList;
    }

    public DualListModel<Department> getDepartmentDualList() {
        return departmentDualList;
    }

    public void setDepartmentDualList(DualListModel<Department> departmentDualList) {
        this.departmentDualList = departmentDualList;
    }

    public DualListModel<Subgroup> getSubgroupDualList() {
        return subgroupDualList;
    }

    public void setSubgroupDualList(DualListModel<Subgroup> subgroupDualList) {
        this.subgroupDualList = subgroupDualList;
    }

    public String getSubgroupSearchText() {

        return subgroupSearchText;
    }

    public void setSubgroupSearchText(String subgroupSearchText) {
        this.subgroupSearchText = subgroupSearchText;
    }

    public String getBusinessSearchText() {
        if (businessSearchText == null) {
            businessSearchText = "";
        }

        return businessSearchText;
    }

    public void setBusinessSearchText(String businessSearchText) {
        this.businessSearchText = businessSearchText;
    }

    public Business getSelectedBusiness() {
        return selectedBusiness;
    }

    public void setSelectedBusiness(Business selectedBusiness) {
        this.selectedBusiness = selectedBusiness;
    }

    @Override
    public void reset() {
        super.reset();

        setSearchType("Employees");
        setSearchText("");
        setDefaultCommandTarget("@this");
        setModuleNames(new String[]{
            "systemManager",
            "humanResourceManager"});
        setDateSearchPeriod(new DatePeriod("This year", "year",
                "requisitionDate", null, null, null, false, false, false));
        getDateSearchPeriod().initDatePeriod();

        employeeSearchText = "";
        employeePositionSearchText = "";
        departmentSearchText = "";
        subgroupSearchText = "";
        divisionSearchText = "";
        businessSearchText = "";
        manufacturerSearchText = "";
        isActiveEmployeesOnly = true;
        isActiveEmployeePositionsOnly = true;
        isActiveDepartmentsOnly = true;
        isActiveBusinessesOnly = true;
        isActiveSubgroupsOnly = true;
        isActiveDivisionsOnly = true;
        isActiveManufacturersOnly = true;

    }

    public Boolean getIsActiveDepartmentsOnly() {

        return isActiveDepartmentsOnly;
    }

    public Boolean getIsActiveSubgroupsOnly() {

        return isActiveSubgroupsOnly;
    }

    public void setIsActiveSubgroupsOnly(Boolean isActiveSubgroupsOnly) {
        this.isActiveSubgroupsOnly = isActiveSubgroupsOnly;
    }

    public Boolean getIsActiveBusinessesOnly() {
        return isActiveBusinessesOnly;
    }

    public void setIsActiveBusinessesOnly(Boolean isActiveBusinessesOnly) {
        this.isActiveBusinessesOnly = isActiveBusinessesOnly;
    }

    public void setIsActiveDepartmentsOnly(Boolean isActiveDepartmentsOnly) {
        this.isActiveDepartmentsOnly = isActiveDepartmentsOnly;
    }

    public Boolean getIsActiveEmployeesOnly() {

        return isActiveEmployeesOnly;
    }

    public void setIsActiveEmployeesOnly(Boolean isActiveEmployeesOnly) {
        this.isActiveEmployeesOnly = isActiveEmployeesOnly;
    }

    public Department getSelectedDepartment() {
        if (selectedDepartment == null) {
            selectedDepartment = new Department();
        }
        return selectedDepartment;
    }

    public void setSelectedDepartment(Department selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }

    public Subgroup getSelectedSubgroup() {
        if (selectedSubgroup == null) {
            selectedSubgroup = new Subgroup();
        }

        return selectedSubgroup;
    }

    public Division getSelectedDivision() {
        return selectedDivision;
    }

    public void setSelectedDivision(Division selectedDivision) {
        this.selectedDivision = selectedDivision;
    }

    public void setSelectedSubgroup(Subgroup selectedSubgroup) {
        this.selectedSubgroup = selectedSubgroup;
    }

    public List<Department> getFoundDepartments() {
        if (foundDepartments == null) {
            foundDepartments = Department.findAllActiveDepartments(getEntityManager1());
        }
        return foundDepartments;
    }

    public List<Subgroup> getFoundSubgroups() {
        if (foundSubgroups == null) {
            foundSubgroups = Subgroup.findAllActive(getEntityManager1());
        }
        return foundSubgroups;
    }

    public List<Business> getFoundBusinesses() {
        if (foundBusinesses == null) {
            foundBusinesses = Business.findAllBusinesses(getEntityManager1());
        }

        return foundBusinesses;
    }

    public void okPickList() {
        closeDialog(null);
    }

    public void addSubgroupDepartmentsDialogReturn() {

        getSelectedSubgroup().setDepartments(departmentDualList.getTarget());

    }

    public void addDivisionDepartmentsDialogReturn() {

        getSelectedDivision().setDepartments(departmentDualList.getTarget());

    }

    public void addDivisionSubgroupsDialogReturn() {

        getSelectedDivision().setSubgroups(subgroupDualList.getTarget());

    }

    public void addBusinessDepartmentsDialogReturn() {

        getSelectedBusiness().setDepartments(departmentDualList.getTarget());

    }

    public void setFoundDepartments(List<Department> foundDepartments) {
        this.foundDepartments = foundDepartments;
    }

    public String getDepartmentSearchText() {
        return departmentSearchText;
    }

    public void setDepartmentSearchText(String departmentSearchText) {
        this.departmentSearchText = departmentSearchText;
    }

    public String getEmployeeSearchText() {
        return employeeSearchText;
    }

    public void setEmployeeSearchText(String employeeSearchText) {
        this.employeeSearchText = employeeSearchText;
    }

    public void doDepartmentSearch() {

        if (getIsActiveDepartmentsOnly()) {
            foundDepartments = Department.findActiveDepartmentsByName(getEntityManager1(), getDepartmentSearchText());
        } else {
            foundDepartments = Department.findDepartmentsByName(getEntityManager1(), getDepartmentSearchText());
        }

    }

    public void doEmployeePositionSearch() {

        if (getIsActiveEmployeePositionsOnly()) {
            foundEmployeePositions = EmployeePosition.findActiveEmployeePositionsByTitle(getEntityManager1(), getEmployeePositionSearchText());
        } else {
            foundEmployeePositions = EmployeePosition.findEmployeePositionsByTitle(getEntityManager1(), getEmployeePositionSearchText());
        }

    }

    public void doSubgroupSearch() {
        foundSubgroups = Subgroup.findAllByName(getEntityManager1(), getSubgroupSearchText());
    }

    public void doDivisionSearch() {
        foundDivisions = Division.findAllByName(getEntityManager1(), getDivisionSearchText());
    }

    public void doBusinessSearch() {
        foundBusinesses = Business.findBusinessesByName(getEntityManager1(), getBusinessSearchText());
    }

    public void doEmployeeSearch() {

        setDefaultCommandTarget("@this");

        doDefaultSearch(
                getMainTabView(),
                getDateSearchPeriod().getDateField(),
                "Employees",
                getEmployeeSearchText(),
                null,
                null);

    }

    public void openHumanResourceBrowser() {

        getMainTabView().openTab("Human Resource");
    }

    public void selectHumanResourceTab(
            MainTabView mainTabView,
            Boolean openTab,
            String innerTabViewVar,
            int innerTabIndex) {

        if (openTab) {
            mainTabView.openTab("Human Resource");
        }

        PrimeFaces.current().executeScript("PF('" + innerTabViewVar + "').select(" + innerTabIndex + ");");

    }

    public void editDepartment() {
        PrimeFacesUtils.openDialog(null, "departmentDialog", true, true, true, 600, 800);
    }

    public void editEmployeePosition() {
        PrimeFacesUtils.openDialog(null, "employeePositionDialog", true, true, true, 600, 725);
    }

    public void editSubgroup() {
        PrimeFacesUtils.openDialog(null, "subgroupDialog", true, true, true, 600, 700);
    }

    public void editDivision() {
        PrimeFacesUtils.openDialog(null, "divisionDialog", true, true, true, 600, 700);
    }

    public void editBusiness() {
        PrimeFacesUtils.openDialog(null, "businessDialog", true, true, true, 600, 700);
    }

    public void editEmployee() {

        PrimeFacesUtils.openDialog(null, "employeeDialog", true, true, true, 500, 700);
    }

    public Employee getSelectedEmployee() {

        if (selectedEmployee == null) {
            selectedEmployee = Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true);
        }

        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;

        this.selectedEmployee = Employee.findEmployeeById(getEntityManager1(), selectedEmployee.getId());

    }

    public void closeDialog(ActionEvent actionEvent) {
        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDepartment() {

        selectedDepartment.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedBusiness() {

        selectedBusiness.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedSubgroup() {

        selectedSubgroup.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedDivision() {

        selectedDivision.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void saveSelectedEmployee(ActionEvent actionEvent) {

        selectedEmployee.getName();

        selectedEmployee.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void saveSelectedEmployeePosition(ActionEvent actionEvent) {

        selectedEmployeePosition.save(getEntityManager1());

        PrimeFaces.current().dialog().closeDynamic(null);

    }

    public void updateSelectedEmployeeDepartment() {
        if (selectedEmployee.getDepartment() != null) {
            if (selectedEmployee.getDepartment().getId() != null) {
                selectedEmployee.setDepartment(Department.findDepartmentById(getEntityManager1(), selectedEmployee.getDepartment().getId()));
            } else {
                selectedEmployee.setDepartment(Department.findDefaultDepartment(getEntityManager1(), "--"));
            }
        }
    }

    public void updateSelectedDepartmentHead() {
        if (selectedDepartment.getHead().getId() != null) {
            selectedDepartment.setHead(Employee.findEmployeeById(getEntityManager1(), selectedDepartment.getHead().getId()));
        } else {
            selectedDepartment.setHead(Employee.findDefaultEmployee(getEntityManager1(), "--", "--", true));
        }
    }

    public List getSexes() {
        return Utils.getSexes();
    }

    public void createNewDepartment() {

        selectedDepartment = new Department();

        editDepartment();
    }

    public void createNewEmployeePosition() {

        selectedEmployeePosition = new EmployeePosition();

        editEmployeePosition();
    }

    public void openDepartmentPickListDialog() {
        PrimeFacesUtils.openDialog(null, "departmentPickListDialog", true, true, true, 500, 600);
    }

    public void addSubgroupDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager1());
        List<Department> target = selectedSubgroup.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void addDepartmentStaff() {
        List<Employee> source = Employee.findAllActiveEmployees(getEntityManager1());
        List<Employee> target = selectedDepartment.getStaff();

        source.removeAll(target);

        employeeDualList = new DualListModel<>(source, target);

        openEmployeePickListDialog();
    }

    public void addDepartmentStaffDialogReturn() {

        getSelectedDepartment().setStaff(employeeDualList.getTarget());

    }

    public void openEmployeePickListDialog() {
        PrimeFacesUtils.openDialog(null, "employeePickListDialog", true, true, true, 500, 600);
    }

    public void addDivisionDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager1());
        List<Department> target = selectedDivision.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void addDivisionSubgroups() {
        List<Subgroup> source = Subgroup.findAllActive(getEntityManager1());
        List<Subgroup> target = selectedDivision.getSubgroups();

        source.removeAll(target);

        subgroupDualList = new DualListModel<>(source, target);

        openSubgroupPickListDialog();
    }

    public void openSubgroupPickListDialog() {
        PrimeFacesUtils.openDialog(null, "subgroupPickListDialog", true, true, true, 320, 500);
    }

    public void addBusinessDepartments() {
        List<Department> source = Department.findAllActiveDepartments(getEntityManager1());
        List<Department> target = selectedBusiness.getDepartments();

        source.removeAll(target);

        departmentDualList = new DualListModel<>(source, target);

        openDepartmentPickListDialog();
    }

    public void createNewBusiness() {

        selectedBusiness = new Business();

        PrimeFacesUtils.openDialog(null, "businessDialog", true, true, true, 600, 700);
    }

    public void createNewSubgroup() {

        selectedSubgroup = new Subgroup();

        PrimeFacesUtils.openDialog(null, "subgroupDialog", true, true, true, 600, 700);
    }

    public void createNewEmployee() {

        selectedEmployee = new Employee();

        editEmployee();
    }

    public void createNewDivision() {

        selectedDivision = new Division();

        PrimeFacesUtils.openDialog(null, "divisionDialog", true, true, true, 600, 700);
    }

    public List<BusinessOffice> getBusinessOffices() {
        return BusinessOffice.findAllBusinessOffices(getEntityManager1());
    }

    public List<Department> getDepartments() {
        return Department.findAllDepartments(getEntityManager1());
    }

    public List<Employee> getEmployees() {
        return Employee.findAllEmployees(getEntityManager1());
    }

    public List<Employee> getFoundEmployees() {
        if (foundEmployees == null) {
            foundEmployees = Employee.findAllActiveEmployees(getEntityManager1());
        }

        return foundEmployees;
    }

    @Override
    public EntityManager getEntityManager1() {
        return getSystemManager().getEntityManager1();
    }

    // Manufacturer Management
    public Manufacturer getSelectedManufacturer() {
        if (selectedManufacturer == null) {
            return new Manufacturer();
        }
        return selectedManufacturer;
    }

    public void setSelectedManufacturer(Manufacturer selectedManufacturer) {
        this.selectedManufacturer = selectedManufacturer;
    }

    public List<Manufacturer> getFoundManufacturers() {
        if (foundManufacturers == null) {
            foundManufacturers = Manufacturer.findAllActiveManufacturers(getEntityManager1());
        }
        return foundManufacturers;
    }

    public void setFoundManufacturers(List<Manufacturer> foundManufacturers) {
        this.foundManufacturers = foundManufacturers;
    }

    public void createNewManufacturer() {
        createNewManufacturer(true);

        editSelectedManufacturer();
    }

    public void createNewManufacturer(Boolean active) {
        selectedManufacturer = new Manufacturer("");
    }

    public void editSelectedManufacturer() {

        PrimeFacesUtils.openDialog(null, "hr/manufacturer/manufacturerDialog", true, true, true, 500, 700);
    }

    public String getManufacturerSearchText() {
        return manufacturerSearchText;
    }

    public void setManufacturerSearchText(String manufacturerSearchText) {
        this.manufacturerSearchText = manufacturerSearchText;
    }

    public Boolean getIsActiveManufacturersOnly() {
        return isActiveManufacturersOnly;
    }

    public void setIsActiveManufacturersOnly(Boolean isActiveManufacturersOnly) {
        this.isActiveManufacturersOnly = isActiveManufacturersOnly;
    }

    public void doManufacturerSearch() {

        if (getIsActiveManufacturersOnly()) {
            foundManufacturers = Manufacturer.findActiveManufacturersByAnyPartOfName(getEntityManager1(), manufacturerSearchText);
        } else {
            foundManufacturers = Manufacturer.findManufacturersByAnyPartOfName(getEntityManager1(), manufacturerSearchText);
        }
    }

    public void onManufacturerCellEdit(CellEditEvent event) {
        BusinessEntityUtils.saveBusinessEntityInTransaction(getEntityManager1(),
                getFoundManufacturers().get(event.getRowIndex()));
    }

    public int getNumManufacturersFound() {
        return getFoundManufacturers().size();
    }

    public void okManufacturer() {
        Boolean hasValidAddress = false;
        Boolean hasValidContact = false;

        try {

            for (Address address : selectedManufacturer.getAddresses()) {
                hasValidAddress = hasValidAddress || AddressValidator.validate(address);
            }
            if (!hasValidAddress) {
                PrimeFacesUtils.addMessage("Address Required",
                        "A valid address was not entered for this manufacturer",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            for (Contact contact : getSelectedManufacturer().getContacts()) {
                hasValidContact = hasValidContact || ContactValidator.validate(contact);
            }
            if (!hasValidContact) {
                PrimeFacesUtils.addMessage("Contact Required",
                        "A valid contact was not entered for this manufacturer",
                        FacesMessage.SEVERITY_ERROR);

                return;
            }

            if (getSelectedManufacturer().getIsDirty()) {

                getSelectedManufacturer().save(getEntityManager1());
                getSelectedManufacturer().setIsDirty(false);
            }

            PrimeFaces.current().dialog().closeDynamic(null);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void cancelManufacturerEdit(ActionEvent actionEvent) {

        getSelectedManufacturer().setIsDirty(false);

        Iterator addressIterator = getSelectedManufacturer().getAddresses().iterator();
        Address address;
        while (addressIterator.hasNext()) {
            address = (Address) addressIterator.next();
            if (address.getId() == null) {
                addressIterator.remove();
            }
        }

        Iterator contactIterator = getSelectedManufacturer().getContacts().iterator();
        Contact contact;
        while (contactIterator.hasNext()) {
            contact = (Contact) contactIterator.next();
            if (contact.getId() == null) {
                contactIterator.remove();
            }
        }

        PrimeFaces.current().dialog().closeDynamic(null);
    }

    public void updateManufacturer() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void updateManufacturerName(AjaxBehaviorEvent event) {
        selectedManufacturer.setName(selectedManufacturer.getName().trim());

        updateManufacturer();
    }

    public void createNewAddress() {
        selectedAddress = null;

        for (Address address : getSelectedManufacturer().getAddresses()) {
            if (address.getAddressLine1().trim().isEmpty()) {
                selectedAddress = address;
                break;
            }
        }

        if (selectedAddress == null) {
            selectedAddress = new Address("", "Billing");
        }

        setEdit(false);

        getSelectedManufacturer().setIsDirty(false);
    }

    public void createNewContact() {
        selectedContact = null;

        for (Contact contact : getSelectedManufacturer().getContacts()) {
            if (contact.getFirstName().trim().isEmpty()) {
                selectedContact = contact;
                break;
            }
        }

        if (selectedContact == null) {
            selectedContact = new Contact("", "", "Main");
            selectedContact.setInternet(new Internet());
        }

        setEdit(false);

        getSelectedManufacturer().setIsDirty(false);
    }

    public Contact getSelectedContact() {
        return selectedContact;
    }

    public void setSelectedContact(Contact selectedContact) {
        this.selectedContact = selectedContact;

        setEdit(true);
    }

    public Address getSelectedAddress() {
        return selectedAddress;
    }

    public void setSelectedAddress(Address selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    public List<Address> getAddressesModel() {
        return getSelectedManufacturer().getAddresses();
    }

    public List<Contact> getContactsModel() {
        return getSelectedManufacturer().getContacts();
    }

    public void okAddress() {

        selectedAddress = selectedAddress.prepare();

        if (getIsNewAddress()) {
            getSelectedManufacturer().getAddresses().add(selectedAddress);
        }

        PrimeFaces.current().executeScript("PF('addressFormDialog').hide();");

    }

    public void okContact() {

        selectedContact = selectedContact.prepare();

        if (getIsNewContact()) {
            getSelectedManufacturer().getContacts().add(selectedContact);
        }

        PrimeFaces.current().executeScript("PF('contactFormDialog').hide();");

    }

    public Boolean getIsNewContact() {
        return getSelectedContact().getId() == null && !getEdit();
    }

    public Boolean getIsNewAddress() {
        return getSelectedAddress().getId() == null && !getEdit();
    }

    public void updateContact() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void updateAddress() {
        getSelectedManufacturer().setIsDirty(true);
    }

    public void removeContact() {
        getSelectedManufacturer().getContacts().remove(selectedContact);
        getSelectedManufacturer().setIsDirty(true);
        selectedContact = null;
    }

    public void removeAddress() {
        getSelectedManufacturer().getAddresses().remove(selectedAddress);
        getSelectedManufacturer().setIsDirty(true);
        selectedAddress = null;
    }

    public List<Manufacturer> completeActiveManufacturer(String query) {
        try {
            return Manufacturer.findActiveManufacturersByAnyPartOfName(getEntityManager1(), query);

        } catch (Exception e) {
            System.out.println(e);

            return new ArrayList<>();
        }
    }

    @Override
    public SelectItemGroup getSearchTypesGroup() {
        SelectItemGroup group = new SelectItemGroup("Human Resource");

        group.setSelectItems(getSearchTypes().toArray(new SelectItem[0]));

        return group;
    }

    @Override
    public ArrayList<SelectItem> getSearchTypes() {
        ArrayList searchTypes = new ArrayList();

        searchTypes.add(new SelectItem("Employees", "Employees"));
        searchTypes.add(new SelectItem("Employee Positions", "Employee Positions"));
        searchTypes.add(new SelectItem("Departments", "Departments"));
        searchTypes.add(new SelectItem("Subgroups", "Subgroups"));
        searchTypes.add(new SelectItem("Divisions", "Divisions"));
        searchTypes.add(new SelectItem("Organizations", "Organizations"));

        return searchTypes;
    }

    @Override
    public ArrayList<SelectItem> getDateSearchFields(String searchType) {
        ArrayList<SelectItem> dateSearchFields = new ArrayList<>();

        setSearchType(searchType);

        switch (searchType) {
            case "Employees":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Employee Positions":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Departments":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Subgroups":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Divisions":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            case "Organizations":
                dateSearchFields.add(new SelectItem("dateEntered", "Date entered"));
                dateSearchFields.add(new SelectItem("dateEdited", "Date edited"));
                return dateSearchFields;
            default:
                break;
        }

        return dateSearchFields;
    }

    @Override
    public void doDefaultSearch(
            MainTabView mainTabView,
            String dateSearchField,
            String searchType,
            String searchText,
            Date startDate,
            Date endDate) {
        switch (searchType) {
            case "Employees":
                if (getIsActiveEmployeesOnly()) {
                    foundEmployees = Employee.findActiveEmployees(getEntityManager1(),
                            searchText);
                } else {
                    foundEmployees = Employee.findEmployees(getEntityManager1(),
                            searchText);
                }

                if (startDate != null) {
                    setEmployeeSearchText(searchText);
                    selectHumanResourceTab(mainTabView, true, "humanResourceTabVar", 0);
                } 
                
                setDefaultCommandTarget("doSearch");

                break;
            case "Employee Positions":

                break;
            case "Departments":

                break;
            case "Subgroups":

                break;
            case "Divisions":

                break;
            case "Organizations":

                break;

            default:
                break;
        }
    }

    @Override
    public void handleKeepAlive() {
        getUser().setPollTime(new Date());

        if ((Boolean) SystemOption.getOptionValueObject(getEntityManager1(), "debugMode")) {
            System.out.println(getApplicationHeader()
                    + " keeping session alive: " + getUser().getPollTime());
        }
        if (getUser().getId() != null) {
            getUser().save(getEntityManager1());
        }

        PrimeFaces.current().ajax().update(":appForm:notificationBadge");
    }

    @Override
    public String getApplicationSubheader() {
        return "Human Resource Administration &amp; Management";
    }

    @Override
    public EntityManager getEntityManager2() {
        return getSystemManager().getEntityManager2();
    }

    @Override
    public String getAppShortcutIconURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "appShortcutIconURL");
    }

    @Override
    public String getLogoURL() {
        return (String) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURL");
    }

    @Override
    public Integer getLogoURLImageHeight() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageHeight");
    }

    @Override
    public Integer getLogoURLImageWidth() {
        return (Integer) SystemOption.getOptionValueObject(
                getEntityManager1(), "logoURLImageWidth");
    }

    @Override
    public void onNotificationSelect(SelectEvent event) {
        EntityManager em = getEntityManager1();

        Notification notification = Notification.findNotificationByNameAndOwnerId(
                em,
                (String) event.getObject(),
                getUser().getId(),
                false);

        if (notification != null) {

            handleSelectedNotification(notification);

            notification.setActive(false);
            notification.save(em);
        }
    }

    @Override
    public void handleSelectedNotification(Notification notification) {

        switch (notification.getType()) {
            case "EmployeeSearch":

                break;

            default:
                System.out.println("Unkown type");
        }

    }

}
