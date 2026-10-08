package com.aprendia.backend.feature.user.service;


import com.aprendia.backend.feature.user.dto.UserResponse;
import com.aprendia.backend.feature.user.dto.RegisterRequest;

import com.aprendia.backend.feature.user.dto.StudentDto;
import com.aprendia.backend.feature.user.dto.StudentRequest;
import com.aprendia.backend.feature.user.dto.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByUsername(String username);

    Page<StudentDto> getAllStudents(Pageable pageable);
    StudentDto getStudentById(Long id);

    UserResponse register(RegisterRequest registerRequest);

    StudentResponse registerStudent(StudentRequest request);


    StudentDto updateStudent(Long id, StudentRequest request);

    void deleteUser(Long id);
}
