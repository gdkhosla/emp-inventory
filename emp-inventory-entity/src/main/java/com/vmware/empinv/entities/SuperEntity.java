package com.vmware.empinv.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class SuperEntity {
	@Id
	@GeneratedValue
	private long id;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "created_on")
	private long createdOn;
	
	@Column(name = "last_modified_by")
	private String lastModifiedBy;
	
	@Column(name = "last_modified_on")
	private long lastModifiedOn;

}
