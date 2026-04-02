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
@Table(name = "bpm_entity")
public class BpmEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

}
