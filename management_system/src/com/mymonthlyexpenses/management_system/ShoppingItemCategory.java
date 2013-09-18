package com.mymonthlyexpenses.management_system;

public class ShoppingItemCategory {
	private String id;
	private String name;
	private String imageLocation;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the imageLocation
	 */
	public String getImageLocation() {
		return imageLocation;
	}

	/**
	 * @param imageLocation
	 *            the imageLocation to set
	 */
	public void setImageLocation(String imageLocation) {
		this.imageLocation = imageLocation;
	}
	
	public String toString(){
		return this.name;
	}
}
