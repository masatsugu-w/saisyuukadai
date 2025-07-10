package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Report report,UserDetail userDetail) {
        List<Report> reportList=reportRepository.findByEmployee(userDetail.getEmployee());
        if(reportList!=null){
            for(Report dReport:reportList) {
                if(dReport.getReportDate().equals(report.getReportDate())) {
                    return ErrorKinds.DUPLICATE_ERROR;
                    
                }
            }
        }
        report.setUpdatedAt(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setEmployee(userDetail.getEmployee());
        report.setDeleteFlg(false);
        reportRepository.save(report);

        return ErrorKinds.SUCCESS;
    }
    
 // 従業員保存
    @Transactional
    public ErrorKinds update(Report report,UserDetail userDetail,Integer id) {
        Report bReport=findById(id);
        List<Report> reportList=reportRepository.findByEmployee(userDetail.getEmployee());
        if(reportList!=null &&
                bReport.getEmployee().getCode().equals(userDetail.getEmployee().getCode()) &&
                !bReport.getReportDate().equals(report.getReportDate())) {
            for(Report dReport:reportList) {
                if(dReport.getReportDate().equals(report.getReportDate())) {
                    return ErrorKinds.DUPLICATE_ERROR;
                    
                }
            }
        }
        report.setUpdatedAt(LocalDateTime.now());
        report.setCreatedAt(bReport.getCreatedAt());
        report.setEmployee(bReport.getEmployee());
        report.setDeleteFlg(false);
        reportRepository.save(report);
            
        
        return ErrorKinds.SUCCESS;
    }

    // 従業員削除
    @Transactional
    public ErrorKinds delete(Integer id) {

        Report report = findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findById(Integer id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }
}
