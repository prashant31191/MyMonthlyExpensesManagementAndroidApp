package com.mymonthlyexpenses.management_system.model;

/*
 * This is a class representation of a shopping item
 */
public class ShoppingItem {
    private String name;
    private String id;
    private String description;
    private String categoryId;
    private String shoppingItemUnitId;
    private String imageLocation;

    /*
     * Constructor
     */
    ShoppingItem(String name, String id, String description, String categoryId,
	    String shoppingItemUnitId, String imageLocation) {
	this.name = name;
	this.id = id;
	this.description = description;
	this.categoryId = categoryId;
	this.shoppingItemUnitId = shoppingItemUnitId;
	this.imageLocation = imageLocation;
    }

    public ShoppingItem() {
	this.name = "";
	this.id = "";
	this.description = "";
	this.categoryId = "";
	this.shoppingItemUnitId = "";
	this.imageLocation = "";
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
     * @return the description
     */
    public String getDescription() {
	return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * @return the categoryId
     */
    public String getCategoryId() {
	return categoryId;
    }

    /**
     * @param categoryId
     *            the categoryId to set
     */
    public void setCategoryId(String categoryId) {
	this.categoryId = categoryId;
    }

    /**
     * @return the shoppingItemUnitId
     */
    public String getShoppingItemUnitId() {
	return shoppingItemUnitId;
    }

    /**
     * @param shoppingItemUnitId
     *            the shoppingItemUnitId to set
     */
    public void setShoppingItemUnitId(String shoppingItemUnitId) {
	this.shoppingItemUnitId = shoppingItemUnitId;
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

    public String toString() {
	return name;
    }
}
