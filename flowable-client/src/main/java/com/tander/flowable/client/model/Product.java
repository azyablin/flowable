package com.tander.flowable.client.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "product")
@Accessors(chain = true)
public class Product implements Serializable {

    public static final String VAR_NAME= "product";

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    private Integer status;

    @Column(name = "update_thread_id")
    private Long updateThreadId;

}
