package com.tander.flowable.client.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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