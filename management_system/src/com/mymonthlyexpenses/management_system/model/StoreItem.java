package com.mymonthlyexpenses.management_system.model;

import java.util.Comparator;

public class StoreItem implements Comparable<StoreItem> {

	private String id;
	private String storeId;
	private String shoppingItemName;
	private String shoppingItemDescription;
	private String shoppingItemImageLocation;
	private String shoppingItemId;
	private String shoppingItemCategoryId;
	private String shoppingItemUnit;
	private String price;
	private String quantity;
	private String updated;

	private boolean filtered = false;

	/**
	 * @return the shoppingItemDescription
	 */
	public String getShoppingItemDescription() {
		return shoppingItemDescription;
	}

	/**
	 * @param shoppingItemDescription
	 *            the shoppingItemDescription to set
	 */
	public void setShoppingItemDescription(String shoppingItemDescription) {
		this.shoppingItemDescription = shoppingItemDescription;
	}

	public StoreItem(String id, String storeId, String shoppingItemId,
			String shoppingItemCategoryId, String shoppingItemUnit,
			String price, String quantity, String updated,
			String shoppingItemName, String shoppingItemDescription,
			String shoppingItemImageLocation) {
		this.id = id;
		this.storeId = storeId;
		this.shoppingItemId = shoppingItemId;
		this.shoppingItemCategoryId = shoppingItemCategoryId;
		this.shoppingItemUnit = shoppingItemUnit;
		this.price = price;
		this.quantity = quantity;
		this.updated = updated;
		this.shoppingItemName = shoppingItemName;
		this.shoppingItemDescription = shoppingItemDescription;
		this.shoppingItemImageLocation = shoppingItemImageLocation;
	}

	public StoreItem() {
		this.id = "";
		this.storeId = "";
		this.shoppingItemId = "";
		this.shoppingItemCategoryId = "";
		this.shoppingItemUnit = "";
		this.price = "";
		this.quantity = "";
		this.updated = "";
		this.shoppingItemName = "";
		this.shoppingItemImageLocation = "";
	}

	/**
	 * Copy constructor.
	 */
	public StoreItem(StoreItem storeItem) {
		this(storeItem.getId(), storeItem.getStoreId(), storeItem
				.getShoppingItemId(), storeItem.getShoppingItemCategoryId(),
				storeItem.getShoppingItemUnit(), storeItem.getPrice(),
				storeItem.getQuantity(), storeItem.getUpdated(), storeItem
						.getShoppingItemName(), storeItem
						.getShoppingItemDescription(), storeItem
						.getShoppingItemImageLocation());
	}

	/**
	 * @return the shoppingItemUnitId
	 */
	public String getShoppingItemUnit() {
		return shoppingItemUnit;
	}

	/**
	 * @param shoppingItemUnitId
	 *            the shoppingItemUnitId to set
	 */
	public void setShoppingItemUnit(String shoppingItemUnit) {
		this.shoppingItemUnit = shoppingItemUnit;
	}

	/**
	 * @return the shoppingItemName
	 */
	public String getShoppingItemName() {
		return shoppingItemName;
	}

	/**
	 * @param shoppingItemName
	 *            the shoppingItemName to set
	 */
	public void setShoppingItemName(String shoppingItemName) {
		this.shoppingItemName = shoppingItemName;
	}

	/**
	 * @return the shoppingItemImageLocation
	 */
	public String getShoppingItemImageLocation() {
		return shoppingItemImageLocation;
	}

	/**
	 * @param shoppingItemImageLocation
	 *            the shoppingItemImageLocation to set
	 */
	public void setShoppingItemImageLocation(String shoppingItemImageLocation) {
		this.shoppingItemImageLocation = shoppingItemImageLocation;
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
	 * @return the storeId
	 */
	public String getStoreId() {
		return storeId;
	}

	/**
	 * @param storeId
	 *            the storeId to set
	 */
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	/**
	 * @return the shoppingItemId
	 */
	public String getShoppingItemId() {
		return shoppingItemId;
	}

	/**
	 * @param shoppingItemId
	 *            the shoppingItemId to set
	 */
	public void setShoppingItemId(String shoppingItemId) {
		this.shoppingItemId = shoppingItemId;
	}

	/**
	 * @return the shoppingItemCategoryId
	 */
	public String getShoppingItemCategoryId() {
		return shoppingItemCategoryId;
	}

	/**
	 * @param shoppingItemCategoryId
	 *            the shoppingItemCategoryId to set
	 */
	public void setShoppingItemCategoryId(String shoppingItemCategoryId) {
		this.shoppingItemCategoryId = shoppingItemCategoryId;
	}

	/**
	 * @return the price
	 */
	public String getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(String price) {
		this.price = price;
	}

	/**
	 * @return the quantity
	 */
	public String getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity
	 *            the quantity to set
	 */
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}

	/**
	 * @param updated
	 *            the updated to set
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public String toString() {
		return this.shoppingItemName;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StoreItem another) {
		return shoppingItemName.compareToIgnoreCase(another
				.getShoppingItemName());
	}

	public static Comparator<StoreItem> StoreItemComparator = new Comparator<StoreItem>() {

		@Override
		public int compare(StoreItem lhs, StoreItem rhs) {

			return lhs.compareTo(rhs);
		}
	};
}
