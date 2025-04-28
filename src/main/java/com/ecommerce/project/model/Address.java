package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adressId;

    @NotBlank
    @Size(min = 5 , message = "Street name must be atleast 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5 , message = "Building name must be atleast 5 characters")
    private String BuildingName;

    @NotBlank
    @Size(min = 5 , message = "City name must be atleast 5 characters")
    private String city;

    @NotBlank
    @Size(min = 5 , message = "State name must be atleast 5 characters")
    private String state;

    @NotBlank
    @Size(min = 5 , message = "Country name must be atleast 5 characters")
    private String country;

    @NotBlank
    @Size(min = 6 , message = "Pincode must be atleast 5 characters")
    private String pincode;

    @ManyToMany(mappedBy = "addresses")
    @ToString.Exclude
    private List<User> users =  new ArrayList<>();

    public Address(String street, String buildingName, String city, String state, String country, String pincode) {
        this.street = street;
        BuildingName = buildingName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
    }
}
