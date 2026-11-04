package com.aprendia.backend.feature.student.service;

import com.aprendia.backend.feature.student.dto.PersonLookupDTO;
import com.aprendia.backend.feature.student.dto.StudentRelativeDTO;

import java.util.List;

public interface StudentRelativeService {
    PersonLookupDTO findPersonByCurp(String curp);
    List<StudentRelativeDTO> getRelatives(Long studentId);
    StudentRelativeDTO addRelative(Long studentId, StudentRelativeDTO request);
    StudentRelativeDTO updateRelative(Long studentId, Long relativeId, StudentRelativeDTO request);
    void removeRelative(Long studentId, Long relativeId);
}
