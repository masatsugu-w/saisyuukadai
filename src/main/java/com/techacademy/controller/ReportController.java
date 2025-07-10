package com.techacademy.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @Autowired
    public ReportController(ReportService reportService,ReportRepository reportRepository) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model,@AuthenticationPrincipal UserDetail userDetail) {

        List<Report> reportList=new ArrayList<Report>();
        Employee emp=userDetail.getEmployee();
        
        if(emp.getRole()==Employee.Role.GENERAL) {
            reportList=reportRepository.findByEmployee(emp);
        }else {
            reportList=reportRepository.findAll();
        }
        model.addAttribute("reportList", reportList);
        model.addAttribute("listSize", reportList.size());
        return "reports/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{id}/")
    public String detail(@PathVariable("id") int id, Model model) {

        model.addAttribute("report", reportService.findById(id));
        return "reports/detail";
    }
    
    // 従業員更新画面
    @GetMapping(value = "/{id}/update")
    public String edit(@PathVariable("id") Integer id,Model model,@ModelAttribute Report report) {

        if(id!=null) {
            report=reportService.findById(id);
        }
        model.addAttribute("report",report);
        return "reports/update";
    }
    

    /** User更新処理 */
    @PostMapping(value = "/{id}/update")
    public String update(@PathVariable("id") Integer id,Model model,@Validated Report report, BindingResult res,@AuthenticationPrincipal UserDetail userDetail) {

        if(res.hasErrors()) {
            return edit(null,model,report);
        }
        
        ErrorKinds result = reportService.update(report,userDetail,id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            return edit(null,model,report);
        }
        return "redirect:/reports";
    }
    
    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(Report report,@AuthenticationPrincipal UserDetail userDetail,Model model) {

        if(userDetail!=null) {
            report=new Report();
            report.setEmployee(userDetail.getEmployee());
        }
        model.addAttribute("report", report);
        return "reports/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Report report, BindingResult res,@AuthenticationPrincipal UserDetail userDetail,Model model) {


        // 入力チェック
        if (res.hasErrors()) {
            return create(report,userDetail,null);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = reportService.save(report,userDetail);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(report,null,model);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(report,userDetail,null);
        }

        return "redirect:/reports";
    }

    // 従業員削除処理
    @PostMapping(value = "/{id}/delete")
    public String delete(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = reportService.delete(id);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("report", reportService.findById(id));
            return detail(id, model);
        }

        return "redirect:/reports";
    }

}
