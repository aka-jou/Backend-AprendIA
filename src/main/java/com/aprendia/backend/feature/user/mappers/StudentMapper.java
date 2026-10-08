package com.aprendia.backend.feature.user.mappers;

import com.aprendia.backend.feature.user.dto.AddressRequest;
import com.aprendia.backend.feature.user.dto.PersonRequest;
import com.aprendia.backend.feature.user.entities.Address;
import com.aprendia.backend.feature.user.entities.Person;
import com.aprendia.backend.feature.user.entities.Student;
import com.aprendia.backend.feature.user.dto.StudentRequest;

public class StudentMapper {

    private StudentMapper() {
    }

    public static Person buildPersonFromRequest(PersonRequest request) {
        if (request == null) return null;
        
        return Person.builder()
                .firstName(request.firstName())
                .middleName(request.middleName())
                .lastName(request.lastName())
                .secondLastName(request.secondLastName())
                .curp(request.curp())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .phone(request.phone())
                .imageUrl(request.imageUrl())
                .build();
    }

    public static void updatePersonFromRequest(Person person, PersonRequest request) {
        if (person == null || request == null) return;
        
        person.setFirstName(request.firstName());
        person.setMiddleName(request.middleName());
        person.setLastName(request.lastName());
        person.setSecondLastName(request.secondLastName());
        person.setCurp(request.curp());
        person.setBirthDate(request.birthDate());
        person.setGender(request.gender());
        person.setPhone(request.phone());
        person.setImageUrl(request.imageUrl());
    }

    public static Address buildAddressFromRequest(AddressRequest request) {
        if (request == null) return null;

        return Address.builder()
                .street(request.street())
                .exteriorNumber(request.exteriorNumber())
                .settlementType(request.settlementType())
                .settlement(request.settlement())
                .municipalityId(request.municipalityId())
                .stateId(request.stateId())
                .zipCode(request.zipCode())
                .build();
    }

    public static Student buildStudentFromRequest(StudentRequest request, Person person, String qrUrl) {
        if (request == null) return null;

        Student student = Student.builder()
                .person(person)
                .profileId(request.profile())
                .qrUrl(qrUrl)
                .build();

        Address address = buildAddressFromRequest(request.address());
        if (address != null) {
            address.setStudent(student);
            student.setAddress(address);
        }

        return student;
    }

    public static void updateAddressFromRequest(Address address, AddressRequest request) {
        if (address == null || request == null) return;

        address.setStreet(request.street());
        address.setExteriorNumber(request.exteriorNumber());
        address.setSettlementType(request.settlementType());
        address.setSettlement(request.settlement());
        address.setMunicipalityId(request.municipalityId());
        address.setStateId(request.stateId());
        address.setZipCode(request.zipCode());
    }
}
