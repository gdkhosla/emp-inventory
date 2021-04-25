package com.vmware.empinv.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends SuperEntity{

	@Column(name = "name")
	private String name;
	
	@Column(name = "age")
	private int age;
	
	@Column(name="task_id")
	private long taskId;
}
