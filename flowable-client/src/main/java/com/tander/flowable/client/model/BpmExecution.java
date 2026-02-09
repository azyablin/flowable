package com.tander.flowable.client.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bpm_execution")
public class BpmExecution {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

}