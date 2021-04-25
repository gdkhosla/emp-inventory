package com.vmware.empinv.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.vmware.empinv.enums.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task extends SuperEntity{
	@Column(name = "description")
	private String description;
	
	@Column(name = "status")
	@Enumerated
	private TaskStatus status;
	
	@Column(name = "query_urls")
	private String queryUrl;
	

}
